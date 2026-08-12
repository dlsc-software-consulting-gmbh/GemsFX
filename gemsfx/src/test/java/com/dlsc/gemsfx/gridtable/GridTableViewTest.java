package com.dlsc.gemsfx.gridtable;

import com.dlsc.gemsfx.FxTestBase;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link GridTableView}.
 */
public class GridTableViewTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        GridTableView<String> view = invoke(GridTableView::new);
        assertNotNull(view);
    }

    @Test
    public void testStyleClass() {
        GridTableView<String> view = invoke(GridTableView::new);
        assertTrue(view.getStyleClass().contains("grid-table-view"));
    }

    @Test
    public void testUserAgentStylesheetNotNull() {
        GridTableView<String> view = invoke(GridTableView::new);
        assertNotNull(view.getUserAgentStylesheet());
    }

    @Test
    public void testSkinCreation() {
        GridTableView<String> view = layout(invoke(GridTableView::new));
        assertNotNull(view.getSkin());
    }

    @Test
    public void testDefaultItemsIsEmpty() {
        GridTableView<String> view = invoke(GridTableView::new);
        assertTrue(view.getItems().isEmpty());
    }

    @Test
    public void testItemsCanBeAdded() {
        GridTableView<String> view = invoke(GridTableView::new);
        runFx(() -> view.getItems().addAll("alpha", "beta", "gamma"));
        assertEquals(3, view.getItems().size());
    }

    @Test
    public void testDefaultColumnsIsEmpty() {
        GridTableView<String> view = invoke(GridTableView::new);
        assertTrue(view.getColumns().isEmpty());
    }

    @Test
    public void testColumnsCanBeAdded() {
        GridTableView<String> view = invoke(GridTableView::new);
        runFx(() -> view.getColumns().add(new GridTableColumn<>("Name")));
        assertEquals(1, view.getColumns().size());
    }

    @Test
    public void testSetItemsViaObservableList() {
        GridTableView<String> view = invoke(GridTableView::new);
        runFx(() -> view.setItems(FXCollections.observableArrayList("x")));
        assertEquals(1, view.getItems().size());
    }

    @Test
    public void testRowHeaderFactoryRoundTrip() {
        GridTableView<String> view = invoke(GridTableView::new);
        runFx(() -> view.setRowHeaderFactory(s -> new Label(s)));
        assertNotNull(view.getRowHeaderFactory());
    }

    @Test
    public void testRowFooterFactoryRoundTrip() {
        GridTableView<String> view = invoke(GridTableView::new);
        runFx(() -> view.setRowFooterFactory(s -> new Label(s)));
        assertNotNull(view.getRowFooterFactory());
    }

    @Test
    public void testProgressIndicatorDefaultNotNull() {
        GridTableView<String> view = invoke(GridTableView::new);
        assertNotNull(view.getProgressIndicator());
    }

    @Test
    public void testMinNumberOfRowsDefault() {
        GridTableView<String> view = invoke(GridTableView::new);
        // just confirm it has the getter, value is defined in source
        view.getMinNumberOfRows(); // no exception
    }
}
