package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import java.util.Objects;
import java.util.Optional;

/**
 * Provides a concrete implementation of a skin for {@link HistoryPopup}, defining the visual representation
 * and interaction handling of the popup. This skin layout includes a {@link ListView} that displays the history
 * items, which can be interacted with via mouse or keyboard.
 *
 * <p>The skin binds various properties from the {@link HistoryPopup} to configure and customize the layout
 * and behavior of the popup elements, including the arrangement of nodes around the central list view (top,
 * bottom, left, right).</p>
 *
 * <p>Interactions such as mouse clicks and keyboard inputs are handled to select and confirm history items,
 * allowing for a seamless user experience. The history items are displayed using a configurable cell factory,
 * and the skin reacts to changes in the popup's properties to update the UI accordingly.</p>
 *
 * <p>This skin ensures that the popup's visual structure is maintained in alignment with the popup's configuration,
 * supporting dynamic changes to the content and layout.</p>
 *
 * @param <T> the type of the items displayed in the history popup
 */
public class HistoryPopupSkin<T> implements Skin<HistoryPopup<T>> {

    private final HistoryPopup<T> control;
    private final BorderPane root;
    private final ListView<T> listView;

    public HistoryPopupSkin(HistoryPopup<T> popup) {
        this.control = popup;

        root = new BorderPane() {
            @Override
            public String getUserAgentStylesheet() {
                return Objects.requireNonNull(SearchField.class.getResource("history-popup.css")).toExternalForm();
            }
        };

        root.getStyleClass().add("content-pane");

        listView = createHistoryListView();
        root.setCenter(listView);

        root.leftProperty().bind(control.leftProperty());
        root.rightProperty().bind(control.rightProperty());
        root.topProperty().bind(control.topProperty());
        root.bottomProperty().bind(control.bottomProperty());
    }

    private ListView<T> createHistoryListView() {
        ListView<T> listView = new ListView<>();
        listView.getStyleClass().add("history-list-view");

        Bindings.bindContent(listView.getItems(), control.getHistoryManager().getAll());

        listView.cellFactoryProperty().bind(control.historyCellFactoryProperty());
        listView.placeholderProperty().bind(control.historyPlaceholderProperty());

        // handle mouse clicks on the listView item
        listView.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (isPrimarySingleClick(mouseEvent) && !mouseEvent.isConsumed()) {
                handlerHistoryItemConfirmed(listView);
                mouseEvent.consume();
            }
        });

        // handle keyboard events on the listView
        listView.addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                handlerHistoryItemConfirmed(listView);
                keyEvent.consume();
            }
        });

        // handle selection changes
        listView.getSelectionModel().selectedItemProperty().addListener(it ->
                Optional.ofNullable(control.getOnHistoryItemSelected())
                        .ifPresent(onItemSelected -> onItemSelected.accept(listView.getSelectionModel().getSelectedItem()))
        );

        return listView;
    }

    private void handlerHistoryItemConfirmed(ListView<T> listView) {
        T historyItem = listView.getSelectionModel().getSelectedItem();
        Optional.ofNullable(control.getOnHistoryItemConfirmed())
                .ifPresent(onClickHistoryItem -> onClickHistoryItem.accept(historyItem));
    }

    private boolean isPrimarySingleClick(MouseEvent mouseEvent) {
        return mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 1;
    }

    public final ListView<T> getListView() {
        return listView;
    }

    public Node getNode() {
        return root;
    }

    public HistoryPopup getSkinnable() {
        return control;
    }

    public void dispose() {
        Bindings.unbindContent(listView.getItems(), control.getHistoryManager().getAll());

        listView.prefWidthProperty().unbind();
        listView.maxWidthProperty().unbind();
        listView.minWidthProperty().unbind();

        listView.cellFactoryProperty().unbind();
        listView.placeholderProperty().unbind();
    }

}
