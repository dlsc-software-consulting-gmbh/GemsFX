package com.dlsc.gemsfx.paging;

import com.dlsc.gemsfx.skins.PagingControlsSkin;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.MapChangeListener;
import javafx.scene.control.Skin;
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

        getProperties().addListener((MapChangeListener<? super Object, ? super Object>) change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("controls.needed")) {
                    needed.set((Boolean) change.getValueAdded());
                }
            }
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PagingControlsSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(PagingControls.class.getResource("paging-controls.css")).toExternalForm();
    }

    private final ReadOnlyBooleanWrapper needed = new ReadOnlyBooleanWrapper(this, "needed", true);

    public final boolean isNeeded() {
        return needed.get();
    }

    /**
     * This property can be used to determine if the paging controls should be shown in the UI or not.
     * The controls are only needed if the user needs to be able to navigate to different pages. This might not
     * be the case if the view only shows a limited number of items AND the "page size" selector does not contain
     * any values that would create more than one page to show.
     *
     * @return true if the controls should be shown to the user
     */
    public final ReadOnlyBooleanProperty neededProperty() {
        return needed.getReadOnlyProperty();
    }
}
