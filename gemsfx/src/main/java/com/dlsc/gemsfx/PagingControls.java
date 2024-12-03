package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PagingControlsSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.Objects;

/**
 * A control for navigating paged information, for example, a paged database table view.
 */
public class PagingControls extends PagingControlBase {

    private static final String DEFAULT_STYLE_CLASS = "paging-controls";

    /**
     * Constructs a new instance.
     */
    public PagingControls() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> requestFocus());
        setMessageLabelProvider(view -> {
            if (getPageCount() == 0) {
                return "No items";
            }

            if (getPageCount() == 1) {
                return "Showing all items";
            }

            int startIndex = (view.getPage() * getPageSize()) + 1;
            int endIndex = startIndex + getPageSize() - 1;

            endIndex = Math.min(endIndex, getTotalItemCount());
            return "Showing items " + startIndex + " to " + endIndex + " of " + getTotalItemCount();
        });

        addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
            if (Objects.equals(evt.getCode(), KeyCode.RIGHT)) {
                nextPage();
            } else if (Objects.equals(evt.getCode(), KeyCode.LEFT)) {
                previousPage();
            } else if (Objects.equals(evt.getCode(), KeyCode.HOME)) {
                firstPage();
            } else if (Objects.equals(evt.getCode(), KeyCode.END)) {
                lastPage();
            }
        });

        pageCount.bind(Bindings.createIntegerBinding(() -> {
            int count = getTotalItemCount() / getPageSize();
            if (getTotalItemCount() % getPageSize() > 0) {
                count++;
            }
            return count;
        }, totalItemCountProperty(), pageSizeProperty()));

        Label firstPageDivider = new Label("...");
        firstPageDivider.getStyleClass().addAll("page-divider", "first-page-divider");
        setFirstPageDivider(firstPageDivider);

        Label lastPageDivider = new Label("...");
        lastPageDivider.getStyleClass().addAll("page-divider", "first-page-divider");
        setLastPageDivider(lastPageDivider);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PagingControlsSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(PagingControls.class.getResource("paging-controls.css")).toExternalForm();
    }

    private final ObjectProperty<HPos> alignment = new SimpleObjectProperty<>(this, "alignment", HPos.RIGHT);

    public final HPos getAlignment() {
        return alignment.get();
    }

    /**
     * The alignment property controls where in the view the paging buttons will appear: left,
     * center, middle.
     *
     * @return the alignment / the position of the paging buttons
     */
    public final ObjectProperty<HPos> alignmentProperty() {
        return alignment;
    }

    public final void setAlignment(HPos alignment) {
        this.alignment.set(alignment);
    }
}
