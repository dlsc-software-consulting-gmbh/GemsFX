package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.StripViewSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.dlsc.gemsfx.util.DurationConverter;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A view which can be used to lay out a fixed number of items horizontally. If the
 * available width is not sufficient then scrolling buttons will appear on either side
 * of the view so that the user can make hidden items visible. The nice thing about
 * this control is that it uses a {@link MaskedView} to fade out the elements on the
 * side. This ensures that the scroll buttons will be fully visible.
 *
 * @param <T> the type of the items shown by the view
 */
public class StripView<T> extends Control {

    private static final int DEFAULT_FADING_SIZE = 120;

    /**
     * Constructs a new strip view.
     */
    public StripView() {
        getStyleClass().add("strip-view");

        setPrefWidth(400);
        setPrefHeight(50);
        setFocusTraversable(false);

        setCellFactory(strip -> new StripCell<>());

        selectedItemProperty().addListener(it -> {
            if (getSelectedItem() != null && isAutoScrolling()) {
                scrollTo(getSelectedItem());
            }

            // important to remove the property after the scroll operation has been performed,
            // otherwise we can cause a memory leak.
            getProperties().remove("scroll.to");
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new StripViewSkin<>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(StripView.class.getResource("strip-view.css")).toExternalForm();
    }

    private final StyleableBooleanProperty alwaysCenter = new SimpleStyleableBooleanProperty(StyleableProperties.ALWAYS_CENTER, this, "alwaysCenter", true);

    public final boolean isAlwaysCenter() {
        return alwaysCenter.get();
    }

    /**
     * A flag used to signal whether the currently selected item should always end up in
     * the center location of the view (if possible).
     * <p>
     * Can be set via CSS using the {@code -fx-always-center} property.
     * Valid values are: {@code true} or {@code false}.
     * The default value is {@code true}.
     * </p>
     *
     * @return the property
     */
    public final BooleanProperty alwaysCenterProperty() {
        return alwaysCenter;
    }

    public final void setAlwaysCenter(boolean alwaysCenter) {
        this.alwaysCenter.set(alwaysCenter);
    }

    private DoubleProperty fadingSize;

    public final double getFadingSize() {
        return fadingSize == null ? DEFAULT_FADING_SIZE : fadingSize.get();
    }

    /**
     * Specifies the size of the fade in / out areas on the left- and right-hand side.
     * <p>
     * Can be set via CSS using the {@code -fx-fading-size} property.
     * Valid values are: positive numbers.
     * The default value is {@code 120}.
     * </p>
     *
     * @return the property
     */
    public final DoubleProperty fadingSizeProperty() {
        if (fadingSize == null) {
            fadingSize = new StyleableDoubleProperty(DEFAULT_FADING_SIZE) {
                @Override
                public Object getBean() {
                    return StripView.this;
                }

                @Override
                public String getName() {
                    return "fadingSize";
                }

                @Override
                public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return StyleableProperties.FADING_SIZE;
                }
            };
        }
        return fadingSize;
    }

    public final void setFadingSize(double fadingSize) {
        fadingSizeProperty().set(fadingSize);
    }

    // Autoscrolling.

    private final BooleanProperty autoScrolling = new SimpleBooleanProperty(this, "autoScrolling", true);

    /**
     * Determines whether the view will automatically scroll to a newly selected item.
     *
     * @return true if the control uses automatic scrolling when an item becomes selected
     */
    public final BooleanProperty autoScrollingProperty() {
        return autoScrolling;
    }

    public final boolean isAutoScrolling() {
        return autoScrolling.get();
    }

    public final void setAutoScrolling(boolean autoScrolling) {
        this.autoScrolling.set(autoScrolling);
    }

    // Animation support.

    private final StyleableBooleanProperty animateScrolling = new SimpleStyleableBooleanProperty(StyleableProperties.ANIMATE_SCROLLING, this, "animateScrolling", true);

    /**
     * Enables or disables whether animation is being used when scrolling to the left or right.
     * <p>
     * Can be set via CSS using the {@code -fx-animate-scrolling} property.
     * Valid values are: {@code true} or {@code false}.
     * The default value is {@code true}.
     * </p>
     *
     * @return the property
     */
    public final BooleanProperty animateScrollingProperty() {
        return animateScrolling;
    }

    public final boolean isAnimateScrolling() {
        return animateScrolling.get();
    }

    public final void setAnimateScrolling(boolean animateScrolling) {
        this.animateScrolling.set(animateScrolling);
    }

    // Animation duration support.

    private final StyleableObjectProperty<Duration> animationDuration = new StyleableObjectProperty<>(Duration.millis(200)) {
        @Override
        public Object getBean() {
            return StripView.this;
        }

        @Override
        public String getName() {
            return "animationDuration";
        }

        @Override
        public CssMetaData<? extends Styleable, Duration> getCssMetaData() {
            return StyleableProperties.ANIMATION_DURATION;
        }
    };

    /**
     * Determines the duration of the scroll animation.
     * <p>
     * Can be set via CSS using the {@code -fx-animation-duration} property.
     * Valid values are: a numeric millisecond value (e.g. {@code 200}).
     * The default value is {@code 200} ms.
     * </p>
     *
     * @return the property
     */
    public final ObjectProperty<Duration> animationDurationProperty() {
        return animationDuration;
    }

    public final Duration getAnimationDuration() {
        return animationDuration.get();
    }

    public final void setAnimationDuration(Duration animationDuration) {
        this.animationDuration.set(animationDuration);
    }

    // Selection model support.

    public final ObjectProperty<T> selectedItem = new SimpleObjectProperty<>(this, "selectedItem");

    /**
     * Stores the currently selected item.
     *
     * @return the selected item
     */
    public final ObjectProperty<T> selectedItemProperty () {
        return selectedItem;
    }

    public final T getSelectedItem () {
        return selectedItemProperty().get();
    }

    public final void setSelectedItem (T selectedItem) {
        selectedItemProperty().set(selectedItem);
    }

    // items support

    private final ListProperty<T> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

    /**
     * The model used by the strip view control.
     *
     * @return the list of items shown in the view
     */
    public final ListProperty<T> itemsProperty() {
        return items;
    }

    public final ObservableList<T> getItems() {
        return items.get();
    }

    public final void setItems(ObservableList<T> items) {
        this.items.set(items);
    }

    // cell factory support

    private final ObjectProperty<Callback<StripView<T>, StripCell<T>>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory");

    public final Callback<StripView<T>, StripCell<T>> getCellFactory() {
        return cellFactory.get();
    }

    /**
     * A factory used for creating cells that will display the items added to the strip view.
     *
     * @return the cell factory
     */
    public final ObjectProperty<Callback<StripView<T>, StripCell<T>>> cellFactoryProperty() {
        return cellFactory;
    }

    public final void setCellFactory(Callback<StripView<T>, StripCell<T>> cellFactory) {
        this.cellFactory.set(cellFactory);
    }

    public void scrollTo(T item) {
        getProperties().put("scroll.to", item);
    }

    private final StyleableBooleanProperty loopSelection = new SimpleStyleableBooleanProperty(StyleableProperties.LOOP_SELECTION, this, "loopSelection", true);

    public final boolean isLoopSelection() {
        return loopSelection.get();
    }

    public final void setLoopSelection(boolean value) {
        loopSelection.set(value);
    }

    /**
     * Property to determine whether the selection should loop from the end to the start and vice versa.
     * <p>
     * Can be set via CSS using the {@code -fx-loop-selection} property.
     * Valid values are: {@code true} or {@code false}.
     * The default value is {@code true}.
     * </p>
     *
     * @return the property
     */
    public final BooleanProperty loopSelectionProperty() {
        return loopSelection;
    }

    /**
     * A strip cell is being used by cell factories of the {@link StripView} control.
     *
     * @param <T> the type of the model object shown by the cell
     */
    public static class StripCell<T> extends Label {

        private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");

        private final InvalidationListener selectionListener = it -> updateSelection();

        private final WeakInvalidationListener weakSelectionListener = new WeakInvalidationListener(selectionListener);

        /**
         * Constructs a new cell.
         */
        public StripCell() {
            getStyleClass().add("strip-cell");

            setMaxWidth(Double.MAX_VALUE);
            setMaxHeight(Double.MAX_VALUE);

            stripViewProperty().addListener((it, oldStrip, newStrip) -> {
                if (oldStrip != null) {
                    oldStrip.selectedItemProperty().removeListener(weakSelectionListener);
                }

                if (newStrip != null) {
                    newStrip.selectedItemProperty().addListener(weakSelectionListener);
                    updateSelection();
                }
            });

            itemProperty().addListener(it -> {
                setText(getItem() == null ? "" : getItem().toString());
                updateSelection();
            });
        }

        private void updateSelection() {
            T selectedItem = getStripView().getSelectedItem();
            setSelected(selectedItem == getItem());
        }

        private final ObjectProperty<StripView<T>> stripView = new SimpleObjectProperty<>(this, "stripView");

        /**
         * Stores a reference back to the strip view where the cell is
         * being used.
         *
         * @return the parent control
         */
        public final ObjectProperty<StripView<T>> stripViewProperty() {
            return stripView;
        }

        public final StripView<T> getStripView() {
            return stripView.get();
        }

        public final void setStripView(StripView<T> stripView) {
            this.stripView.set(stripView);
        }

        private BooleanProperty selected;

        public final void setSelected(boolean value) {
            selectedProperty().set(value);
        }

        public final boolean isSelected() {
            return selected != null && selected.get();
        }

        /**
         * Defines whether a cell is currently selected or not.
         *
         * @return true if the cell is selected
         */
        public final BooleanProperty selectedProperty() {
            if (selected == null) {
                selected = new BooleanPropertyBase() {
                    @Override protected void invalidated() {
                        boolean selected = get();
                        pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, selected);
                        notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
                    }

                    @Override
                    public Object getBean() {
                        return StripCell.this;
                    }

                    @Override
                    public String getName() {
                        return "selected";
                    }
                };
            }
            return selected;
        }

        private final ObjectProperty<T> item = new SimpleObjectProperty<>(this, "item");

        public final T getItem() {
            return item.get();
        }

        /**
         * The model item shown in the cell.
         *
         * @return the model item
         */
        public final ObjectProperty<T> itemProperty() {
            return item;
        }

        public final void setItem(T item) {
            this.item.set(item);
        }
    }

    private static class StyleableProperties {

        private static final CssMetaData<StripView, Boolean> ALWAYS_CENTER = new CssMetaData<>(
                "-fx-always-center", BooleanConverter.getInstance(), true) {

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(StripView control) {
                return (StyleableProperty<Boolean>) control.alwaysCenterProperty();
            }

            @Override
            public boolean isSettable(StripView control) {
                return !control.alwaysCenter.isBound();
            }
        };

        private static final CssMetaData<StripView, Boolean> ANIMATE_SCROLLING = new CssMetaData<>(
                "-fx-animate-scrolling", BooleanConverter.getInstance(), true) {

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(StripView control) {
                return (StyleableProperty<Boolean>) control.animateScrollingProperty();
            }

            @Override
            public boolean isSettable(StripView control) {
                return !control.animateScrolling.isBound();
            }
        };

        private static final CssMetaData<StripView, Duration> ANIMATION_DURATION = new CssMetaData<>(
                "-fx-animation-duration", DurationConverter.getInstance(), Duration.millis(200)) {

            @Override
            public StyleableProperty<Duration> getStyleableProperty(StripView control) {
                return (StyleableProperty<Duration>) control.animationDurationProperty();
            }

            @Override
            public boolean isSettable(StripView control) {
                return !control.animationDuration.isBound();
            }
        };

        private static final CssMetaData<StripView, Boolean> LOOP_SELECTION = new CssMetaData<>(
                "-fx-loop-selection", BooleanConverter.getInstance(), true) {

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(StripView control) {
                return (StyleableProperty<Boolean>) control.loopSelectionProperty();
            }

            @Override
            public boolean isSettable(StripView control) {
                return !control.loopSelection.isBound();
            }
        };

        private static final CssMetaData<StripView, Number> FADING_SIZE = new CssMetaData<>(
                "-fx-fading-size", SizeConverter.getInstance(), DEFAULT_FADING_SIZE) {

            @Override
            public StyleableProperty<Number> getStyleableProperty(StripView control) {
                return (StyleableProperty<Number>) control.fadingSizeProperty();
            }

            @Override
            public boolean isSettable(StripView control) {
                return control.fadingSize == null || !control.fadingSize.isBound();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(FADING_SIZE);
            styleables.add(ALWAYS_CENTER);
            styleables.add(ANIMATE_SCROLLING);
            styleables.add(ANIMATION_DURATION);
            styleables.add(LOOP_SELECTION);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StripView.StyleableProperties.STYLEABLES;
    }

}
