package biz.jovido.seed.content;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.Constructor;

/**
 * @author Stephan Grundner
 */
public class StructureConfigurer implements StructureConfiguration {

    private final Configurer parentConfigurer;
    private final Structure structure;

    private int numberOfAttributes = 0;

    @Override
    public HierarchyConfigurer createHierarchy(String hierarchyName) {
        return parentConfigurer.createHierarchy(hierarchyName);
    }

    @Override
    public StructureConfigurer createStructure(String typeName) {
        return parentConfigurer.createStructure(typeName);
    }

    @Override
    public void apply() {
        parentConfigurer.apply();
    }

    public Structure getStructure() {
        return structure;
    }

    @SuppressWarnings("unchecked")
    private <A extends Attribute, C extends AttributeConfigurer<A, C>> C addAttribute(Class<A> attributeType, Class<C> configurerType, String name) {
        A attribute = (A) structure.getAttribute(name);
        if (attribute == null) {
            attribute = BeanUtils.instantiate(attributeType);
            structure.setAttribute(name, attribute);
        }
        attribute.setOrdinal(numberOfAttributes++);
        try {
            Constructor<C> constructor = configurerType.getDeclaredConstructor(StructureConfigurer.class, attributeType);
            C configurer = BeanUtils.instantiateClass(constructor, this, attribute);

            return configurer;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TextAttributeConfigurer addTextAttribute(String name) {
        return addAttribute(TextAttribute.class, TextAttributeConfigurer.class, name);
    }

    @Override
    public YesNoAttributeConfigurer addYesNoAttribute(String name) {
        return addAttribute(YesNoAttribute.class, YesNoAttributeConfigurer.class, name);
    }

    @Override
    public IconAttributeConfigurer addIconAttribute(String name) {
        return addAttribute(IconAttribute.class, IconAttributeConfigurer.class, name);
    }

    @Override
    public ImageAttributeConfigurer addImageAttribute(String name) {
        return addAttribute(ImageAttribute.class, ImageAttributeConfigurer.class, name);
    }

    @Override
    public ItemAttributeConfigurer addItemAttribute(String name) {
        return addAttribute(ItemAttribute.class, ItemAttributeConfigurer.class, name);
    }

    @Override
    public LinkAttributeConfigurer addLinkAttribute(String name) {
        return addAttribute(LinkAttribute.class, LinkAttributeConfigurer.class, name);
    }

    @Override
    public SelectionAttributeConfigurer addSelectionAttribute(String name) {
        return addAttribute(SelectionAttribute.class, SelectionAttributeConfigurer.class, name);
    }

    public StructureConfigurer setNestedOnly(boolean nestedOnly) {
        structure.setNestedOnly(nestedOnly);

        return this;
    }

    public StructureConfigurer setPublishable(boolean publishable) {
        structure.setPublishable(publishable);

        return this;
    }

    @Override
    public StructureConfigurer setLabelAttribute(String name) {
        structure.setLabelAttributeName(name);

        return this;
    }

    @Override
    public StructureConfigurer setTemplate(String template) {
        structure.setTemplate(template);

        return this;
    }

    public StructureConfigurer(Configurer parentConfigurer, Structure structure) {
        this.parentConfigurer = parentConfigurer;
        this.structure = structure;
    }
}
