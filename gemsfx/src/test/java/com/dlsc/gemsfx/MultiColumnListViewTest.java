package com.dlsc.gemsfx;

import com.dlsc.gemsfx.MultiColumnListView.ColumnItem;
import com.dlsc.gemsfx.MultiColumnListView.ListViewColumn;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MultiColumnListViewTest {

    @BeforeClass
    public static void initToolkit() {
        // Creating a column creates its default header label, which requires a real JavaFX
        // toolkit. The toolkit is not available on the CI runners.
        Assume.assumeTrue("Skipping test inside GitHub Actions", System.getenv("GITHUB_ACTIONS") == null);

        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await(10, TimeUnit.SECONDS);
        } catch (IllegalStateException alreadyStarted) {
            // toolkit was started by a previous test - that is fine
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

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
}
