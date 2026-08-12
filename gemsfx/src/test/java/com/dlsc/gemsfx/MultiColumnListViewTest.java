package com.dlsc.gemsfx;

import com.dlsc.gemsfx.MultiColumnListView.ColumnItem;
import com.dlsc.gemsfx.MultiColumnListView.ListViewColumn;
import com.dlsc.gemsfx.MultiColumnListView.ColumnListCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MultiColumnListViewTest extends FxTestBase {

    private List<String> userObjects(ListViewColumn<String> column) {
        return column.getItemWrappers().stream().map(ColumnItem::getUserObject).collect(Collectors.toList());
    }

    @Test
    public void shouldWrapItemsAddedToTheColumn() {
        ListViewColumn<String> column = new ListViewColumn<>();
        column.getItems().setAll("A", "B", "C");

        assertEquals(3, column.getItemWrappers().size());
        assertEquals(List.of("A", "B", "C"), userObjects(column));
        assertFalse(column.getItemWrappers().get(0).isPlaceholder());
    }

    @Test
    public void shouldWrapItemsWhenTheListIsBeingReplaced() {
        ListViewColumn<String> column = new ListViewColumn<>();
        column.getItems().setAll("A", "B");

        ObservableList<String> newItems = FXCollections.observableArrayList("X", "Y", "Z");
        column.setItems(newItems);

        assertEquals(List.of("X", "Y", "Z"), userObjects(column));

        newItems.add("W");
        assertEquals(List.of("X", "Y", "Z", "W"), userObjects(column));
    }

    @Test
    public void shouldReuseWrappersWhenTheUserListChanges() {
        ListViewColumn<String> column = new ListViewColumn<>();
        column.getItems().setAll("A", "B");

        ColumnItem<String> wrapperOfA = column.getItemWrappers().get(0);
        ColumnItem<String> wrapperOfB = column.getItemWrappers().get(1);

        column.getItems().add(0, "C");

        assertEquals(List.of("C", "A", "B"), userObjects(column));
        assertSame(wrapperOfA, column.getItemWrappers().get(1));
        assertSame(wrapperOfB, column.getItemWrappers().get(2));
    }

    @Test
    public void shouldSyncWrapperChangesBackToTheUserList() {
        ListViewColumn<String> column = new ListViewColumn<>();
        column.getItems().setAll("A", "B", "C");

        // simulates a re-arrangement performed via drag and drop
        ColumnItem<String> wrapperOfA = column.getItemWrappers().remove(0);
        column.getItemWrappers().add(wrapperOfA);

        assertEquals(List.of("B", "C", "A"), column.getItems());
    }

    @Test
    public void shouldNotExposePlaceholdersToTheUserList() {
        MultiColumnListView<String> view = new MultiColumnListView<>();

        ListViewColumn<String> column = new ListViewColumn<>();
        column.getItems().setAll("A", "B");

        column.getItemWrappers().add(1, view.getToPlaceholder());

        assertTrue(view.getToPlaceholder().isPlaceholder());
        assertTrue(view.getToPlaceholder().isToPlaceholder());
        assertFalse(view.getToPlaceholder().isFromPlaceholder());

        assertEquals(3, column.getItemWrappers().size());
        assertEquals(List.of("A", "B"), column.getItems());

        column.getItemWrappers().remove(view.getToPlaceholder());

        assertEquals(List.of("A", "B"), column.getItems());
    }

    @Test
    public void shouldKeepPlaceholdersWhenTheUserListChangesDuringADrag() {
        MultiColumnListView<String> view = new MultiColumnListView<>();

        ListViewColumn<String> column = new ListViewColumn<>();
        column.getItems().setAll("A", "B", "C");

        // simulates the beginning of a drag operation on "B"
        ColumnItem<String> wrapperOfB = column.getItemWrappers().get(1);
        column.getItemWrappers().set(1, view.getFromPlaceholder());

        assertEquals(List.of("A", "C"), column.getItems());

        // the application modifies its own list while the drag is in progress
        column.getItems().add("X");

        assertTrue(column.getItemWrappers().contains(view.getFromPlaceholder()));
        assertEquals(1, column.getItemWrappers().indexOf(view.getFromPlaceholder()));

        // simulates a cancelled drag operation
        column.getItemWrappers().set(column.getItemWrappers().indexOf(view.getFromPlaceholder()), wrapperOfB);

        assertEquals(List.of("A", "B", "C", "X"), column.getItems());
    }

    @Test
    public void shouldTolerateANullItemsList() {
        ListViewColumn<String> column = new ListViewColumn<>();
        column.getItems().setAll("A");

        column.setItems(null);

        assertTrue(column.getItemWrappers().isEmpty());

        // must not throw
        column.getItemWrappers().add(new ColumnItem<>("B"));
    }

    @Test
    public void shouldSupportDuplicateUserObjects() {        ListViewColumn<String> column = new ListViewColumn<>();
        String item = "A";
        column.getItems().setAll(item, item);

        assertEquals(2, column.getItemWrappers().size());
        assertFalse(column.getItemWrappers().get(0) == column.getItemWrappers().get(1));

        column.getItems().remove(0);

        assertEquals(1, column.getItemWrappers().size());
        assertSame(item, column.getItemWrappers().get(0).getUserObject());
    }

    private static ColumnListCell<String> createCell(MultiColumnListView<String> view, ColumnItem<String> item, boolean empty) {
        return invoke(() -> {
            ColumnListCell<String> cell = view.getCellFactory().call(view);
            cell.updateItem(item, empty);
            return cell;
        });
    }

    @Test
    public void defaultCellShouldUseARichGraphicInsteadOfPlainText() {
        MultiColumnListView<String> view = invoke(MultiColumnListView::new);
        ColumnListCell<String> cell = createCell(view, new ColumnItem<>("Hello"), false);

        assertNull("the default cell must not use the text of the cell", cell.getText());

        Node graphic = cell.getGraphic();
        assertNotNull("the default cell must show a graphic", graphic);
        assertTrue(graphic instanceof StackPane);
        assertTrue(graphic.getStyleClass().contains("content-pane"));
    }

    @Test
    public void defaultCellShouldCenterTheTextOfTheItem() {
        MultiColumnListView<String> view = invoke(MultiColumnListView::new);
        ColumnListCell<String> cell = createCell(view, new ColumnItem<>("Hello"), false);

        StackPane contentPane = (StackPane) cell.getGraphic();
        assertEquals(Pos.CENTER, contentPane.getAlignment());
        assertEquals(1, contentPane.getChildren().size());

        Label label = (Label) contentPane.getChildren().get(0);
        assertTrue(label.getStyleClass().contains("content-label"));
        assertEquals("Hello", label.getText());
        assertEquals(Pos.CENTER, label.getAlignment());
        assertEquals(TextAlignment.CENTER, label.getTextAlignment());
    }

    @Test
    public void defaultCellShouldBeEmptyWhenTheCellIsEmpty() {
        MultiColumnListView<String> view = invoke(MultiColumnListView::new);
        ColumnListCell<String> cell = createCell(view, null, true);

        assertNull(cell.getText());
        assertNull(cell.getGraphic());
    }

    @Test
    public void defaultCellShouldShowTheDragAndDropPlaceholders() {
        MultiColumnListView<String> view = invoke(MultiColumnListView::new);

        ColumnListCell<String> fromCell = createCell(view, view.getFromPlaceholder(), false);
        assertTrue(fromCell.isFromPlaceholder());
        Label fromLabel = (Label) ((StackPane) fromCell.getGraphic()).getChildren().get(0);
        assertNotNull(fromLabel.getText());
        assertFalse(fromLabel.getText().isEmpty());

        ColumnListCell<String> toCell = createCell(view, view.getToPlaceholder(), false);
        assertTrue(toCell.isToPlaceholder());
        Label toLabel = (Label) ((StackPane) toCell.getGraphic()).getChildren().get(0);
        assertNotNull(toLabel.getText());
        assertFalse(toLabel.getText().isEmpty());
    }

    @Test
    public void defaultCellShouldReuseItsGraphic() {
        MultiColumnListView<String> view = invoke(MultiColumnListView::new);
        ColumnListCell<String> cell = createCell(view, new ColumnItem<>("A"), false);

        Node firstGraphic = cell.getGraphic();
        runFx(() -> cell.updateItem(new ColumnItem<>("B"), false));

        assertSame(firstGraphic, cell.getGraphic());
        assertEquals("B", ((Label) ((StackPane) cell.getGraphic()).getChildren().get(0)).getText());
    }

    @Test
    public void defaultCellShouldHaveADashedBorder() {
        MultiColumnListView<String> view = invoke(MultiColumnListView::new);

        ListViewColumn<String> column = invoke(ListViewColumn::new);
        runFx(() -> {
            column.getItems().setAll("A", "B", "C");
            view.getColumns().setAll(column);
        });

        layout(view);
        waitForFxEvents();

        StackPane contentPane = invoke(() -> {
            for (Node node : view.lookupAll(".content-pane")) {
                if (node instanceof StackPane) {
                    return (StackPane) node;
                }
            }
            return null;
        });

        assertNotNull("no cell content pane was created", contentPane);

        Border border = contentPane.getBorder();
        assertNotNull("the default cell content must have a border", border);
        assertFalse(border.getStrokes().isEmpty());

        BorderStroke stroke = border.getStrokes().get(0);
        assertTrue("the border of the default cell content must be dashed",
                stroke.getTopStyle().getDashArray() != null && !stroke.getTopStyle().getDashArray().isEmpty());

        // the content pane must span the entire width of its cell, otherwise the border would
        // only be as wide as the text inside of it
        ColumnListCell<String> cell = (ColumnListCell<String>) contentPane.getParent();
        assertTrue("the content pane must fill the width of the cell",
                contentPane.getWidth() > 0 && contentPane.getWidth() >= cell.getWidth() - cell.getInsets().getLeft() - cell.getInsets().getRight() - 1);
    }
}
