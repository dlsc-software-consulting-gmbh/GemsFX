package com.dlsc.gemsfx.gridtable;

import com.dlsc.gemsfx.FxTestBase;
import javafx.scene.control.ContentDisplay;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link GridTableColumn}.
 */
public class GridTableColumnTest extends FxTestBase {

    @Test
    public void testConstructionWithText() {
        GridTableColumn<String, String> col = invoke(() -> new GridTableColumn<>("Name"));
        assertEquals("Name", col.getText());
    }

    @Test
    public void testConstructionDefault() {
        GridTableColumn<String, String> col = invoke(GridTableColumn::new);
        assertNotNull(col);
    }

    @Test
    public void testTextRoundTrip() {
        GridTableColumn<String, String> col = invoke(() -> new GridTableColumn<>("Old"));
        runFx(() -> col.setText("New"));
        assertEquals("New", col.getText());
    }

    @Test
    public void testDefaultContentDisplay() {
        GridTableColumn<String, String> col = invoke(() -> new GridTableColumn<>("X"));
        assertEquals(ContentDisplay.TEXT_ONLY, col.getContentDisplay());
    }

    @Test
    public void testContentDisplayRoundTrip() {
        GridTableColumn<String, String> col = invoke(GridTableColumn::new);
        runFx(() -> col.setContentDisplay(ContentDisplay.LEFT));
        assertEquals(ContentDisplay.LEFT, col.getContentDisplay());
    }

    @Test
    public void testHeaderIsNotNull() {
        GridTableColumn<String, String> col = invoke(() -> new GridTableColumn<>("H"));
        assertNotNull(col.getHeader());
    }

    @Test
    public void testCellFactoryCanBeSet() {
        GridTableColumn<String, String> col = invoke(() -> new GridTableColumn<>("C"));
        runFx(() -> col.setCellFactory(tableView -> new GridTableCell<>()));
        assertNotNull(col.getCellFactory());
    }

    @Test
    public void testConverterCanBeSet() {
        GridTableColumn<String, String> col = invoke(() -> new GridTableColumn<>("C"));
        runFx(() -> col.setConverter(new javafx.util.StringConverter<String>() {
            @Override public String toString(String s) { return s; }
            @Override public String fromString(String s) { return s; }
        }));
        assertNotNull(col.getConverter());
    }

    @Test
    public void testStyleClassList() {
        GridTableColumn<String, String> col = invoke(GridTableColumn::new);
        assertNotNull(col.getStyleClass());
    }
}
