package biz.jovido.seed.content;

import biz.jovido.seed.UsedInTemplates;
import biz.jovido.seed.content.frontend.ItemValues;
import biz.jovido.seed.content.frontend.ValueMap;
import biz.jovido.seed.content.frontend.ValuesList;
import biz.jovido.seed.net.HostService;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Stephan Grundner
 */
@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private StructureService structureService;

    @Autowired
    private HierarchyService hierarchyService;

    @Autowired
    private HostService hostService;

    @Autowired
    private HtmlService htmlService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AuditingHandler auditingHandler;

    @Autowired
    private MessageSource messageSource;

    public Item getItem(Long id) {
        return itemRepository.findOne(id);
    }

    public Item findPublished(Long leafId) {
        return itemRepository.findPublished(leafId);
    }

    public List<Item> findAllItems() {
        return itemRepository.findAllCurrent();
    }

    public List<Item> findAllPublishedItemsByPath(String path) {
        return itemRepository.findAllPublishedByPath(path);
    }

    public Structure getStructure(Item item) {
        if (item != null) {
            String structureName = item.getStructureName();
            return structureService.getStructure(structureName);
        }

        return null;
    }

    @Deprecated
    public String getLabelText(Item item) {
        Structure structure = getStructure(item);
        if (structure != null) {
            String attributeName = structure.getLabelAttributeName();
            if (StringUtils.hasText(attributeName)) {
                TextPayload label = (TextPayload) getPayload(item, attributeName, 0);
                if (label != null) {
                    return label.getText();
                }
            }

            return null;
        }

        return null;
    }

    @Deprecated
    public List<Payload> getPayloads(Item item, String attributeName) {
        PayloadGroup payloadGroup = item.getPayloadGroup(attributeName);
        return payloadGroup.getPayloads();
    }

    @Deprecated
    public Payload getPayload(Item item, String attributeName, int index) {
        return getPayloads(item, attributeName).get(index);
    }

    private void applyPayloads(Item item) {
        Structure structure = getStructure(item);
        for (String attributeName : structure.getAttributeNames()) {
            Attribute attribute = structure.getAttribute(attributeName);
            PayloadGroup payloadGroup = item.getPayloadGroup(attributeName);
            if (payloadGroup == null) {
                payloadGroup = new PayloadGroup();
                item.setPayloadGroup(attributeName, payloadGroup);
            }

            List<Payload> payloads = payloadGroup.getPayloads();
            if (!(attribute instanceof ItemAttribute)) {
                int remaining = attribute.getRequired() - payloads.size();
                while (remaining-- > 0) {
                    Payload payload = attribute.createPayload();
                    payloadGroup.addPayload(payload);
                }
            }
        }
    }

    private Item createItem(Structure structure) {
        Leaf leaf = new Leaf();
        return createItem(structure, leaf);
    }

    public Item createItem(String structureName) {
        Structure structure = structureService.getStructure(structureName);
        return createItem(structure);
    }

    private Item createItem(Structure structure, Leaf leaf) {
        Item item = new Item();
        item.setLeaf(leaf);
        item.setStructureName(structure.getName());
        item.setLocale(LocaleContextHolder.getLocale());
        if (leaf != null) {
            leaf.setCurrent(item);
        }

        applyPayloads(item);

        return item;
    }

    public Item createEmbeddedItem(String structureName) {
        Structure structure = structureService.getStructure(structureName);
        return createItem(structure, null);
    }

    public boolean isPublished(Item item) {
        if (item != null) {
            Leaf leaf = item.getLeaf();
            if (leaf != null) {
                return new EqualsBuilder()
                        .append(leaf.getPublished(), item)
                        .isEquals();
            }
        }

        return false;
    }

    public Mode getMode(Item item) {
        return isPublished(item) ? Mode.LIVE : Mode.PREVIEW;
    }

    @UsedInTemplates
    @Deprecated
    public String getPath(Object object) {
        Item item = (Item) object;
        if (item != null) {
            String path = item.getPath();
            if (StringUtils.isEmpty(path)) {
                Leaf leaf = item.getLeaf();
                return leaf != null
                        ? String.format("/item?leaf=%s", leaf.getId())
                        : null;
            }

            if (!path.startsWith("/")) {
                return String.format("/%s", path);
            }

            return path;
        }

        return null;
    }

    public String getUrl(Item item) {
        return getPath(item);
    }

    @Transactional
    public Item saveItem(Item item) {
        if (item.getId() == null) {
            auditingHandler.markCreated(item);
        }
        auditingHandler.markModified(item);
        return entityManager.merge(item);
    }

    @Transactional
    public Item publishItem(final Item item) {
        Item current = item.copy();
        current = entityManager.merge(current);
        Leaf history = current.getLeaf();
        history.setPublished(item);
        history.setCurrent(current);
        entityManager.merge(history);

        return current;
    }

    public Attribute getAttribute(Item item, String attributeName) {
        Structure structure = getStructure(item);
        return structure.getAttribute(attributeName);
    }

    public Attribute getAttribute(Payload payload) {
        PayloadGroup payloadGroup = payload.getGroup();
        Item item = payloadGroup.getItem();
        String attributeName = payloadGroup.getAttributeName();
        return getAttribute(item, attributeName);
    }

    public String getDescription(Attribute attribute, Object[] messageArgs, Locale locale) {
        Structure structure = attribute.getStructure();
        String messageCode = String.format("seed.structure.%s.%s.description",
                structure.getName(),
                attribute.getName());
        try {
            return messageSource.getMessage(messageCode, messageArgs, locale);
        } catch (NoSuchMessageException e) {
            return null;
        }
    }

    public String getDescription(Payload payload, Object... messageArgs) {
        Assert.notNull(payload, "[payload] must not be null");
        Attribute attribute = getAttribute(payload);
        PayloadGroup payloadGroup = payload.getGroup();
        Item item = payloadGroup.getItem();

        return getDescription(attribute, messageArgs, item.getLocale());
    }

    public ItemVisitResult walkPayload(Payload payload, ItemVisitor visitor) {
        ItemVisitResult result = visitor.visitPayload(payload);
        if (result == ItemVisitResult.CONTINUE) {
            Attribute attribute = getAttribute(payload);
            if (attribute instanceof ItemAttribute) {
                Item item = ((ItemPayload) payload).getItem();
                result = walkItem(item, visitor);
            }
        }

        return result;
    }

    public ItemVisitResult walkItem(Item item, ItemVisitor visitor) {
        ItemVisitResult result = visitor.visitItem(item);
        if (result == ItemVisitResult.CONTINUE) {
            for (String attributeName : item.getAttributeNames()) {
                PayloadGroup payloadGroup = item.getPayloadGroup(attributeName);
                for (Payload payload : payloadGroup.getPayloads()) {
                    result = walkPayload(payload, visitor);
                    if (result != ItemVisitResult.CONTINUE) {
                        return result;
                    }
                }
            }
        }

        return ItemVisitResult.CONTINUE;
    }

    public Payload findPayload(Item item, UUID payloadUuid) {
        final AtomicReference<Payload> found = new AtomicReference<>();
        walkItem(item, new SimpleItemVisitor() {
            @Override
            public ItemVisitResult visitPayload(Payload payload) {
                if (payload.getUuid().equals(payloadUuid)) {
                    found.set(payload);
                    return ItemVisitResult.TERMINATE;
                }

                return ItemVisitResult.CONTINUE;
            }
        });

        return found.get();
    }

    public Item findItem(Item item, UUID itemUuid) {
        final AtomicReference<Item> found = new AtomicReference<>();
        walkItem(item, new SimpleItemVisitor() {
            @Override
            public ItemVisitResult visitItem(Item item) {
                if (item.getUuid().equals(itemUuid)) {
                    found.set(item);
                    return ItemVisitResult.TERMINATE;
                }

                return ItemVisitResult.CONTINUE;
            }
        });

        return found.get();
    }

    private ValuesList toList(ItemValues values, String attributeName) {
        Item item = values.getItem();
        ValuesList list = new ValuesList(attributeName);
        for (Payload payload : getPayloads(item, attributeName)) {
            Attribute attribute = getAttribute(payload);
            if (attribute instanceof ItemAttribute) {
                Item relatedItem = ((ItemPayload) payload).getItem();
                list.add(toValues(relatedItem, values));
            } else {

                ValueMap map = new ValueMap();

                if (attribute instanceof ImageAttribute) {
                    ImagePayload imagePayload = (ImagePayload) payload;
                    Image image = imagePayload.getImage();
                    map.put("fileName", Optional.ofNullable(image).map(Image::getFileName));
                    map.put("alt", Optional.ofNullable(image).map(Image::getAlt));
                    map.put("id", Optional.ofNullable(image).map(Image::getId));
                    String url = image != null ? String.format("/asset/files/%s/%s",
                            image.getUuid(),
                            image.getFileName()) : null;
                    map.put("url", url);
                } else if (attribute instanceof IconAttribute) {
                    IconPayload iconPayload = ((IconPayload) payload);
                    map.put("code", iconPayload.getCode());
                } else if (attribute instanceof LinkAttribute) {
                    LinkPayload linkPayload = ((LinkPayload) payload);
                    map.put("url", linkPayload.getUrl());
                    String value = linkPayload.getText();
                    map.put("value", value);
                } else if (attribute instanceof TextAttribute) {
                    TextPayload textPayload = ((TextPayload) payload);
                    String value = textPayload.getText();
                    value = value.trim();
                    if (StringUtils.startsWithIgnoreCase(value, "<p>")
                            && StringUtils.endsWithIgnoreCase(value, "</p>")) {
                        value = org.apache.commons.lang3.StringUtils.removeStart(value, "<p>");
                        value = org.apache.commons.lang3.StringUtils.removeEnd(value, "</p>");
                    }
                    map.put("value", value);
                } else if (attribute instanceof YesNoAttribute) {
                    YesNoPayload yesNo = ((YesNoPayload) payload);
                    map.put("value", yesNo.isYes());
                    map.put("yes", yesNo.isYes());
                    map.put("no", !yesNo.isYes());
                } else if (attribute instanceof SelectionAttribute) {
                    SelectionPayload selection = ((SelectionPayload) payload);
                    SelectionAttribute selectionAttribute = (SelectionAttribute) attribute;
                    List<String> selectionValues = selection.getValues();
                    for (String option : selectionAttribute.getOptions()) {
                        boolean selected = selectionValues.contains(option);
                        map.put(option, selected);
                    }
//                    map.put("selection", selection.getValues());
                    map.put("value", selectionValues.get(0));
                } else {
//                    throw new UnsupportedOperationException();
                }

                list.add(map);
            }
        }
        return list;
    }

    public ItemValues toValues(Item item, ItemValues parent) {
        ItemValues values = new ItemValues(item, parent);
        for (String attributeName : item.getAttributeNames()) {
            values.put(attributeName, toList(values, attributeName));
        }

        return values;
    }

    public ItemValues toValues(Item item) {
        return toValues(item, null);
    }

    public Map<String, Object> toMap(Item item) {
        Map<String, Object> map = new HashMap<>();
        ItemValues values = toValues(item);
        map.putAll(values);
        map.put("self", values);
        map.put("label", getLabelText(item));
        map.put("url", getUrl(item));
        map.put("date", item.getCreatedAt());

        return map;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public List<Locale> getAllSupportedLocales() {
        return Stream.of("de", "en", "it", "es")
                .map(Locale::forLanguageTag)
                .collect(Collectors.toList());
    }
}
