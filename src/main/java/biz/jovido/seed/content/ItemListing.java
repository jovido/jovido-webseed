package biz.jovido.seed.content;

import biz.jovido.seed.admin.Actions;
import biz.jovido.seed.admin.Listing;
import biz.jovido.seed.ui.Grid;
import biz.jovido.seed.ui.source.BeanSource;
import biz.jovido.seed.ui.source.BeanSourcesContainer;
import biz.jovido.seed.ui.source.Source;

import java.util.List;

/**
 * @author Stephan Grundner
 */
public class ItemListing extends Listing {

    public static class ActionsCell extends Grid.Cell {

        private Actions actions = new Actions();

        public Actions getActions() {
            return actions;
        }

        public ActionsCell(Grid.Row row, Grid.Column column) {
            super(row, column);
        }
    }

    private Grid grid = new Grid();

    private List<Item> items;

    public Grid getGrid() {
        return grid;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;

        grid.removeAllColumns();
        grid.addColumn("id");
        grid.addColumn("structureName");
        grid.addColumn("createdAt");
        grid.addColumn("lastModifiedAt");

        Grid.Column actionsColumn = grid.addColumn("actions");
        actionsColumn.setCellGenerator(new Grid.CellGenerator() {
            @Override
            public Grid.Cell generateCell(Grid.Row row, Grid.Column column) {
                Source source = row.getSource();
                Item item = ((BeanSource<Item>) source).getBean();
                ActionsCell cell = new ActionsCell(row, column);
                cell.setTemplate("admin/item/grid/actions-cell");
                Actions actions = cell.getActions();
//                actions.add(new Action());
                return cell;
            }
        });

        BeanSourcesContainer<Item> container = new BeanSourcesContainer<>();
        container.addBeans(items);
        grid.setContainer(container);
    }
}
