package biz.jovido.seed.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author Stephan Grundner
 */
@Controller
@RequestMapping(path = "/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    private String respond(Item item, Model model) {
        model.addAttribute("this", item);
        model.addAttribute("mode", itemService.getMode(item));

        Map<String, Object> map = itemService.toMap(item);
        model.addAllAttributes(map);

        Structure structure = itemService.getStructure(item);
        return structure.getName();
    }

    @RequestMapping(params = {"id"})
    protected String itemById(@RequestParam(name = "id") Long id, Model model) {
        Item item = itemService.getItem(id);
        return respond(item, model);
    }

    @RequestMapping(params = {"leaf"})
    protected String itemByLeaf(@RequestParam(name = "leaf") Long leafId, Model model) {
        Item item = itemService.findPublished(leafId);
        if (item == null) {
            throw new ItemNotPublishedException();
        }

        return respond(item, model);
    }
}
