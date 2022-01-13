//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.dlsc.gemsfx.skins.autocomplete;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.util.UUID;

public class AutoCompletePopup<T> extends PopupControl {
    private final ObservableList<T> suggestions = FXCollections.observableArrayList();
    private StringConverter<T> converter;
    private IntegerProperty visibleRowCount = new SimpleIntegerProperty(this, "visibleRowCount", 10);
    private ObjectProperty<EventHandler<SuggestionEvent<T>>> onSuggestion = new ObjectPropertyBase<EventHandler<SuggestionEvent<T>>>() {
        protected void invalidated() {
            setEventHandler(SuggestionEvent.SUGGESTION, (EventHandler) get());
        }

        public Object getBean() {
            return AutoCompletePopup.this;
        }

        public String getName() {
            return "onSuggestion";
        }
    };

    public static final String DEFAULT_STYLE_CLASS = "auto-complete-popup";

    public AutoCompletePopup() {
        setAutoFix(true);
        setAutoHide(true);
        setHideOnEscape(true);
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    public ObservableList<T> getSuggestions() {
        return suggestions;
    }

    public void show(Node node) {
        if (node.getScene() != null && node.getScene().getWindow() != null) {
            if (!isShowing()) {
                Window parent = node.getScene().getWindow();
                getScene().setNodeOrientation(node.getEffectiveNodeOrientation());
                if (node.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
                    setAnchorLocation(AnchorLocation.CONTENT_TOP_RIGHT);
                } else {
                    setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);
                }

                show(parent, parent.getX() + node.localToScene(0.0D, 0.0D).getX() + node.getScene().getX(), parent.getY() + node.localToScene(0.0D, 0.0D).getY() + node.getScene().getY() + node.getBoundsInParent().getHeight());
            }
        } else {
            throw new IllegalStateException("Can not show popup. The node must be attached to a scene/window.");
        }
    }

    public void setConverter(StringConverter<T> converter) {
        this.converter = converter;
    }

    public StringConverter<T> getConverter() {
        return converter;
    }

    public final void setVisibleRowCount(int value) {
        visibleRowCount.set(value);
    }

    public final int getVisibleRowCount() {
        return visibleRowCount.get();
    }

    public final IntegerProperty visibleRowCountProperty() {
        return visibleRowCount;
    }

    public final ObjectProperty<EventHandler<SuggestionEvent<T>>> onSuggestionProperty() {
        return onSuggestion;
    }

    public final void setOnSuggestion(EventHandler<SuggestionEvent<T>> value) {
        onSuggestionProperty().set(value);
    }

    public final EventHandler<SuggestionEvent<T>> getOnSuggestion() {
        return onSuggestionProperty().get();
    }

    protected Skin<?> createDefaultSkin() {
        return new AutoCompletePopupSkin(this);
    }

    public static class SuggestionEvent<TE> extends Event {
        public static final EventType<SuggestionEvent<?>> SUGGESTION = new EventType("SUGGESTION" + UUID.randomUUID());
        private final TE suggestion;

        public SuggestionEvent(TE suggestion) {
            super(SUGGESTION);
            this.suggestion = suggestion;
        }

        public TE getSuggestion() {
            return suggestion;
        }
    }
}
