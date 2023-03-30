package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.daterange.DateRange;
import com.dlsc.gemsfx.daterange.DateRangePicker;
import com.dlsc.gemsfx.daterange.DateRangePreset;
import com.dlsc.gemsfx.daterange.DateRangeView;
import com.dlsc.gemsfx.util.Utils;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.css.Styleable;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Skinnable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

public class DateRangePickerSkin extends SkinBase<DateRangePicker> {

    private Label titleLabel;
    private Label rangeLabel;

    private DateRangeView view;
    private boolean popupNeedsReconfiguring = true;

    public DateRangePickerSkin(DateRangePicker picker) {
        super(picker);

        picker.selectedDateRangeProperty().addListener(it -> updateLabels());
        picker.setOnMouseClicked(evt -> positionAndShowPopup());
        picker.smallVersionProperty().addListener(it -> updateView());

        updateView();
    }

    private void updateView() {
        titleLabel = new Label();
        titleLabel.getStyleClass().add("title-label");

        rangeLabel = new Label();
        rangeLabel.getStyleClass().add("range-label");

        FontIcon calendarIcon = new FontIcon("fa-calendar");
        rangeLabel.setGraphic(calendarIcon);

        Pane pane;

        if (!getSkinnable().isSmallVersion()) {
            pane = new VBox(titleLabel, rangeLabel);
            pane.getStyleClass().remove("small");
        } else {
            Region divider = new Region();
            divider.getStyleClass().add("divider");
            pane = new HBox(titleLabel, divider, rangeLabel);
            pane.getStyleClass().add("small");
        }

        pane.getStyleClass().add("inner-range-container");
        pane.setMinWidth(Region.USE_PREF_SIZE);

        FontIcon expandIcon = new FontIcon(MaterialDesign.MDI_CHEVRON_DOWN);

        HBox.setHgrow(pane, Priority.ALWAYS);
        HBox hBox = new HBox(pane, expandIcon);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getStyleClass().add("outer-range-container");

        updateLabels();

        getChildren().setAll(hBox);
    }

    private PopupControl popup;

    private void updateLabels() {
        DateRange dateRange = getSkinnable().getSelectedDateRange();
        if (dateRange != null) {
            if (dateRange instanceof DateRangePreset) {
                DateRangePreset preset = (DateRangePreset) dateRange;
                titleLabel.setText(preset.getTitle());
            } else {
                titleLabel.setText("Custom range");
            }
            rangeLabel.setText(dateRange.toString());
        }
    }

    PopupControl getPopup() {
        if (popup == null) {
            createPopup();
        }
        return popup;
    }

    private Node getPopupContent() {
        if (view == null) {
            view = new DateRangeView();
            Bindings.bindContent(view.getPresets(), getSkinnable().getPresets());
            view.selectedDateRangeProperty().bindBidirectional(getSkinnable().selectedDateRangeProperty());
            view.defaultPresetProperty().bind(getSkinnable().defaultPresetProperty());
            view.setOnClose(() -> popup.hide());
        }

        return view;
    }

    private void positionAndShowPopup() {
        DateRangePicker picker = getSkinnable();
        if (picker.getScene() == null) {
            return;
        }

        getPopup();

        popup.getScene().setNodeOrientation(getSkinnable().getEffectiveNodeOrientation());

        Node popupContent = getPopupContent();
        sizePopup();

        Point2D p = getPrefPopupPosition();

        popupNeedsReconfiguring = true;
        reconfigurePopup();

        popup.show(picker.getScene().getWindow(),
                snapPositionX(p.getX()),
                snapPositionY(p.getY()));

        popupContent.requestFocus();

        // second call to sizePopup here to enable proper sizing _after_ the popup
        // has been displayed. See RT-37622 for more detail.
        sizePopup();
    }

    private Point2D getPrefPopupPosition() {
        return Utils.pointRelativeTo(getSkinnable(), getPopupContent(), HPos.CENTER, VPos.BOTTOM, 0, 0, true);
    }

    private void sizePopup() {
        Node popupContent = getPopupContent();

        if (popupContent instanceof Region) {
            Region r = (Region) popupContent;

            // 0 is used here for the width due to RT-46097
            double prefHeight = snapSizeY(r.prefHeight(0));
            double minHeight = snapSizeY(r.minHeight(0));
            double maxHeight = snapSizeY(r.maxHeight(0));
            double h = snapSizeY(Math.min(Math.max(prefHeight, minHeight), Math.max(minHeight, maxHeight)));

            double prefWidth = snapSizeX(r.prefWidth(h));
            double minWidth = snapSizeX(r.minWidth(h));
            double maxWidth = snapSizeX(r.maxWidth(h));
            double w = snapSizeX(Math.min(Math.max(prefWidth, minWidth), Math.max(minWidth, maxWidth)));

            popupContent.resize(w, h);
        } else {
            popupContent.autosize();
        }
    }


    private void createPopup() {
        popup = new PopupControl() {
            @Override
            public Styleable getStyleableParent() {
                return getSkinnable();
            }

            {
                setSkin(new Skin<>() {
                    @Override
                    public Skinnable getSkinnable() {
                        return getSkinnable();
                    }

                    @Override
                    public Node getNode() {
                        return getPopupContent();
                    }

                    @Override
                    public void dispose() {
                    }
                });
            }
        };

        popup.setConsumeAutoHidingEvents(false);
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.setHideOnEscape(true);
        popup.addEventHandler(WindowEvent.WINDOW_HIDDEN, t -> {
            // Make sure the accessibility focus returns to the combo box
            // after the window closes.
            getSkinnable().notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        });

        // Fix for RT-21207
        InvalidationListener layoutPosListener = o -> {
            popupNeedsReconfiguring = true;
            reconfigurePopup();
        };
        getSkinnable().layoutXProperty().addListener(layoutPosListener);
        getSkinnable().layoutYProperty().addListener(layoutPosListener);
        getSkinnable().widthProperty().addListener(layoutPosListener);
        getSkinnable().heightProperty().addListener(layoutPosListener);

        // RT-36966 - if skinnable's scene becomes null, ensure popup is closed
        getSkinnable().sceneProperty().addListener(o -> {
            if (((ObservableValue) o).getValue() == null) {
                popup.hide();
//            } else if (getSkinnable().isShowing()) {
//                positionAndShowPopup();
            }
        });

    }

    void reconfigurePopup() {
        // RT-26861. Don't call getPopup() here because it may cause the popup
        // to be created too early, which leads to memory leaks like those noted
        // in RT-32827.
        if (popup == null) {
            return;
        }

        boolean isShowing = popup.isShowing();
        if (!isShowing) {
            return;
        }

        if (!popupNeedsReconfiguring) {
            return;
        }

        popupNeedsReconfiguring = false;

        Point2D p = getPrefPopupPosition();

        Node popupContent = getPopupContent();
        double minWidth = popupContent.prefWidth(Region.USE_COMPUTED_SIZE);
        double minHeight = popupContent.prefHeight(Region.USE_COMPUTED_SIZE);

        if (p.getX() > -1) popup.setAnchorX(p.getX());
        if (p.getY() > -1) popup.setAnchorY(p.getY());
        if (minWidth > -1) popup.setMinWidth(minWidth);
        if (minHeight > -1) popup.setMinHeight(minHeight);

        Bounds b = popupContent.getLayoutBounds();
        double currentWidth = b.getWidth();
        double currentHeight = b.getHeight();
        double newWidth = currentWidth < minWidth ? minWidth : currentWidth;
        double newHeight = currentHeight < minHeight ? minHeight : currentHeight;

        if (newWidth != currentWidth || newHeight != currentHeight) {
            // Resizing content to resolve issues such as RT-32582 and RT-33700
            // (where RT-33700 was introduced due to a previous fix for RT-32582)
            popupContent.resize(newWidth, newHeight);
            if (popupContent instanceof Region) {
                ((Region) popupContent).setMinSize(newWidth, newHeight);
                ((Region) popupContent).setPrefSize(newWidth, newHeight);
            }
        }
    }
}
