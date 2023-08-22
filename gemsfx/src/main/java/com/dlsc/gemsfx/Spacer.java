package com.dlsc.gemsfx;

import javafx.beans.property.BooleanProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Spacer class extends the Region class and provides functionality
 * to create flexible spaces in layouts such as HBox and VBox. It is primarily
 * used to push adjacent nodes apart or together by filling up available space. <br/>
 *
 * The Spacer can be toggled between active and inactive states. When active,
 * it tries to grow as much as possible within its parent container. When
 * inactive, it collapses and doesn't take up any space. <br/>
 *
 * The growth direction of the Spacer (horizontal or vertical) is determined
 * based on its parent container. For instance, when placed inside an HBox, the
 * Spacer will grow horizontally. Conversely, inside a VBox, it will grow vertically. <br/>
 *
 * The active state of the Spacer can also be controlled through CSS with the
 * "-fx-active" property.
 */
public class Spacer extends Region {

    public Spacer() {
        getStyleClass().add("spacer");

        managedProperty().bind(visibleProperty());
        visibleProperty().bind(activeProperty());

        VBox.setVgrow(this, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
    }

    private final BooleanProperty active = new StyleableBooleanProperty(true) {
        @Override
        public Object getBean() {
            return Spacer.this;
        }

        @Override
        public String getName() {
            return "active";
        }

        @Override
        public CssMetaData<Spacer, Boolean> getCssMetaData() {
            return StyleableProperties.ACTIVE;
        }
    };

    public final boolean isActive() {
        return active.get();
    }

    public final void setActive(boolean value) {
        active.set(value);
    }

    public final BooleanProperty activeProperty() {
        return active;
    }

    private static class StyleableProperties {
        private static final CssMetaData<Spacer, Boolean> ACTIVE =
                new CssMetaData<>("-fx-active", BooleanConverter.getInstance(), false) {

                    @Override
                    public boolean isSettable(Spacer n) {
                        return !n.active.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(Spacer n) {
                        return (StyleableProperty<Boolean>) n.activeProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Region.getClassCssMetaData());
            styleables.add(ACTIVE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }
}
