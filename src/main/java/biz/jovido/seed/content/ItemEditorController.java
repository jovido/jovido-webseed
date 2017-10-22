package biz.jovido.seed.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Stephan Grundner
 */
@Controller
@RequestMapping(path = "/admin/item")
@SessionAttributes(types = {ItemEditor.class})
public class ItemEditorController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private HierarchyService hierarchyService;

    @ModelAttribute
    protected ItemEditor editor() {
        ItemEditor editor = new ItemEditor();

        return editor;
    }

    @RequestMapping
    protected String index(@ModelAttribute ItemEditor editor,
                           BindingResult bindingResult,
                           Model model) {

        return "admin/item/editor";
    }

    @RequestMapping(path = "/create")
    protected String create(@ModelAttribute ItemEditor editor,
                            BindingResult bindingResult,
                            @RequestParam(name = "structure") String structureName) {

        Item item = itemService.createItem(structureName);
        editor.setItem(item);

        return "redirect:";
    }

//    @ExceptionHandler(Exception.class)
//    protected ModelAndView handleError(Exception e, ModelAndView modelAndView) {
//
//        modelAndView.addObject("e", e);
//        modelAndView.setViewName("admin/exception");
//        return modelAndView;
//    }


    @RequestMapping(path = "edit")
    protected String edit(@ModelAttribute ItemEditor editor,
                          @RequestParam(name = "item") Long itemId,
                          BindingResult bindingResult) {

        Item item = itemService.getItem(itemId);
        editor.setItem(item);

        return "redirect:";
    }

    @RequestMapping(path = "save")
    protected String save(@ModelAttribute ItemEditor editor,
                          BindingResult bindingResult) {

        Item item = editor.getItem();
        item = itemService.saveItem(item);
        editor.setItem(item);

        return "redirect:";
    }

    @RequestMapping(path = "close")
    protected String close(@ModelAttribute ItemEditor editor,
                           BindingResult bindingResult) {

        editor.setItem(null);

        return "redirect:/admin/items";
    }

//    @RequestMapping(path = "append", params = {"structure"})
//    protected String append(@ModelAttribute ItemEditor editor,
//                            @RequestParam(name = "nested-path") String nestedPath,
//                            @RequestParam(name = "attribute") String attributeName,
//                            @RequestParam(name = "structure") String typeName,
//                            BindingResult bindingResult) {
//
//        String propertyPath = String.format("%s.sequences[%s]", nestedPath, attributeName);
//        Sequence sequence = (Sequence) editor.getPropertyValue(propertyPath);
//        Attribute attribute = itemService.getAttribute(sequence);
//        RelationPayload payload = (RelationPayload) attribute.createPayload();
//        Item target = itemService.createEmbeddedItem(typeName, 1);
////        target.setLeaf(null);
//        Relation relation = payload.getRelation();
//        relation.setTarget(target);
//        sequence.addPayload(payload);
//
//        return "redirect:";
//    }

    @RequestMapping(path = "append", params = {"structure"})
    protected String append(@ModelAttribute ItemEditor editor,
                            @RequestParam(name = "nested-path") String nestedPath,
                            @RequestParam(name = "attribute") String attributeName,
                            @RequestParam(name = "structure") String structureName,
                            BindingResult bindingResult) {

        String propertyPath = String.format("%s.sequences[%s]", nestedPath, attributeName);
        Sequence sequence = (Sequence) editor.getPropertyValue(propertyPath);

        Item item = itemService.createEmbeddedItem(structureName, 1);
        Attribute attribute = sequence.getAttribute();
        ItemPayload payload = (ItemPayload) attribute.createPayload();
        payload.setItem(item);
        sequence.addPayload(payload);

        return "redirect:";
    }

    @RequestMapping(path = "append", params = {"!structure"})
    protected String append(@ModelAttribute ItemEditor editor,
                            @RequestParam(name = "nested-path") String nestedPath,
                            @RequestParam(name = "attribute") String attributeName,
                            BindingResult bindingResult) {

        String propertyPath = String.format("%s.sequences[%s]", nestedPath, attributeName);
        Sequence sequence = (Sequence) editor.getPropertyValue(propertyPath);
        Attribute attribute = sequence.getAttribute();
        Payload payload = attribute.createPayload();
        sequence.addPayload(payload);

        return "redirect:";
    }

    @RequestMapping(path = "move-payload-up")
    protected String movePayloadUp(@ModelAttribute ItemEditor editor,
                                   @RequestParam(name = "nested-path") String nestedPath,
                                   @RequestParam(name = "attribute") String attributeName,
                                   @RequestParam(name = "index") int index,
                                   BindingResult bindingResult) {

        String propertyPath = String.format("%s.sequences[%s]", nestedPath, attributeName);
        Sequence sequence = (Sequence) editor.getPropertyValue(propertyPath);
        sequence.movePayload(index, index - 1);

        return "redirect:";
    }

    @RequestMapping(path = "move-payload-down")
    protected String movePayloadDown(@ModelAttribute ItemEditor editor,
                                     @RequestParam(name = "nested-path") String nestedPath,
                                     @RequestParam(name = "attribute") String attributeName,
                                     @RequestParam(name = "index") int index,
                                     BindingResult bindingResult) {

        String propertyPath = String.format("%s.sequences[%s]", nestedPath, attributeName);
        Sequence sequence = (Sequence) editor.getPropertyValue(propertyPath);
        sequence.movePayload(index, index + 1);

        return "redirect:";
    }

    @RequestMapping(path = "remove-payload")
    protected String removePayload(@ModelAttribute ItemEditor editor,
                                   @RequestParam(name = "nested-path") String nestedPath,
                                   @RequestParam(name = "attribute") String attributeName,
                                   @RequestParam(name = "index") int index,
                                   BindingResult bindingResult) {

        String propertyPath = String.format("%s.sequences[%s]", nestedPath, attributeName);
        Sequence sequence = (Sequence) editor.getPropertyValue(propertyPath);
        sequence.removePayload(index);

        return "redirect:";
    }


    @Autowired
    private AssetService assetService;

    @Transactional
    @PostMapping(path = "upload", params = {"type=image"})
    protected String uploadImage(@ModelAttribute ItemEditor editor,
                                 @RequestParam(name = "nested-path") String nestedPath,
                                 @RequestParam(name = "attribute") String attributeName,
                                 @RequestParam(name = "index") int index,
                                 @RequestParam("file") MultipartFile file) throws IOException {


        String propertyPath = String.format("%s.sequences[%s]", nestedPath, attributeName);
        Sequence sequence = (Sequence) editor.getPropertyValue(propertyPath);
        ImagePayload payload = (ImagePayload) sequence.getPayload(index);

        Image image = payload.getImage();
        if (image == null) {
            image = new Image();
            payload.setImage(image);
        }

        File fileOnDisk = new File("assets/" + image.getUuid() + ".asset");

        try (InputStream inputStream = file.getInputStream()) {
            try (OutputStream outputStream = new FileOutputStream(fileOnDisk)) {
                FileCopyUtils.copy(inputStream, outputStream);
            }
        }

        image.setFileName(file.getOriginalFilename());
//        image = (Image) assetService.saveAsset(image);
//        payload.setImage(image);


        return "redirect:";
    }


    @RequestMapping(path = "add-node", params = {})
    protected String addNode(@ModelAttribute ItemEditor editor,
                             BindingResult bindingResult,
                             @RequestParam(name = "branch") Long branchId,
                             @RequestParam(name = "parent-uuid", required = false) UUID parentUuid) {

        Item item = editor.getItem();
        History history = item.getHistory();
        Locale locale = item.getLocale();
        Assert.notNull(locale);

        Node node = new Node();
        node.setUuid(UUID.randomUUID());
        node.setHistory(item.getHistory());


        Branch branch = hierarchyService.getBranch(branchId);


        Node parent = null;

        if (parentUuid != null) {
            parent = branch.getNodes().stream()
                    .filter(Objects::nonNull)
                    .filter(it -> it.getUuid() != null)
                    .filter(it -> it.getUuid().equals(parentUuid))
                    .findFirst()
                    .orElse(null);
        }
        node.setParent(parent);

        node.setBranch(branch);
        node = hierarchyService.saveNode(node);

        history.addNode(node);
        branch.addNode(node);

        hierarchyService.saveBranch(branch);

        return "redirect:";
    }


    @RequestMapping(path = "publish")
    protected String publish(@ModelAttribute ItemEditor editor,
                             BindingResult bindingResult) {

        Item item = editor.getItem();
        Item current = itemService.publishItem(item);

        editor.setItem(current);

        return "redirect:";
    }
}
