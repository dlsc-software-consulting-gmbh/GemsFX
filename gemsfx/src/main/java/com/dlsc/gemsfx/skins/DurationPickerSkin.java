package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.DurationPicker;

import org.kordamp.ikonli.javafx.FontIcon;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import javafx.util.Pair;

public class DurationPickerSkin extends SkinBase<DurationPicker> {

    private final HBox innerBox = new HBox();
    private final DurationPickerPopup popup;
    private final List<DurationUnitField> durationUnitFields = new ArrayList<>();

    public DurationPickerSkin(DurationPicker picker) {
        super(picker);

        Button editButton = new Button();
        editButton.getStyleClass().add("edit-button");
        editButton.setOnAction(evt -> picker.getOnShowPopup().accept(picker));
        editButton.setMaxHeight(Double.MAX_VALUE);
        editButton.setGraphic(new FontIcon());
        editButton.setFocusTraversable(false);
        editButton.visibleProperty().bind(picker.showPopupTriggerButtonProperty());
        editButton.managedProperty().bind(picker.showPopupTriggerButtonProperty());

        popup = new DurationPickerPopup();
        popup.durationProperty().bindBidirectional(picker.durationProperty());
        popup.minimumDurationProperty().bind(picker.minimumDurationProperty());
        popup.maximumDurationProperty().bind(picker.maximumDurationProperty());
        popup.fieldsProperty().bind(picker.fieldsProperty());

        picker.showingProperty().addListener(it -> {
            if (picker.isShowing()) {
                showPopup();
            } else {
                popup.hide();
            }
        });

        popup.setOnHidden(evt -> picker.getProperties().put("TIME_PICKER_POPUP", "TIME_PICKER_POPUP"));
        popup.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode().equals(KeyCode.ESCAPE)) {
                popup.hide();
            }
        });
        InvalidationListener updateListener = it -> buildView();
        picker.fieldsProperty().addListener(updateListener);

        innerBox.getStyleClass().add("fields-box");
        innerBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        spacer.getStyleClass().add("spacer");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox box = new HBox(innerBox, spacer, editButton);
        box.getStyleClass().add("box");

        getChildren().add(box);

        picker.separatorFactoryProperty().addListener(it -> buildView());
        buildView();
    }

    private void showPopup() {
        DurationPicker picker = getSkinnable();
        Bounds bounds = picker.getBoundsInLocal();
        Bounds screenBounds = picker.localToScreen(bounds);

        int x = (int) screenBounds.getMinX();
        int y = (int) screenBounds.getMinY();
        int height = (int) screenBounds.getHeight();

        popup.show(picker, x, y + height);
    }

    private void buildView() {
        innerBox.getChildren().clear();

        durationUnitFields.clear();

        ObservableList<ChronoUnit> fields = getSkinnable().getFields();
        for (int i = 0; i < fields.size(); i++) {

            ChronoUnit chronoUnit = fields.get(i);
            DurationUnitField unitField = createField(chronoUnit);
            durationUnitFields.add(unitField);
            innerBox.getChildren().add(unitField);

            if (i < fields.size() - 1) {
                Callback<Pair<ChronoUnit, ChronoUnit>, Node> separatorFactory = getSkinnable().getSeparatorFactory();
                Node separatorNode = separatorFactory.call(new Pair<>(chronoUnit, fields.get(i + 1)));
                innerBox.getChildren().add(separatorNode);
            }
        }

        // linking the fields
        for (int i = 0; i < durationUnitFields.size(); i++) {
            DurationUnitField field = durationUnitFields.get(i);
            DurationUnitField previousField = null;
            DurationUnitField nextField = null;

            if (i > 0) {
                previousField = durationUnitFields.get(i - 1);
            }
            if (i < durationUnitFields.size() - 1) {
                nextField = durationUnitFields.get(i + 1);
            }

            if (previousField != null) {
                field.setPreviousField(previousField);
            }
            if (nextField != null) {
                field.setNextField(nextField);
            }
        }
    }

    private DurationUnitField createField(ChronoUnit unit) {
        DurationUnitField field = new DurationUnitField(getSkinnable(), unit);
        switch (unit) {
            default:
            case WEEKS:
            case DAYS:
                break;
            case HOURS:
                field.setMaximumValue(23L);
                break;
            case MINUTES:
            case SECONDS:
                field.setMaximumValue(59L);
                break;
            case MILLIS:
                field.setMaximumValue(999L);
                break;
        }

        field.setValue(0L);
        field.durationProperty().bind(getSkinnable().durationProperty());
        field.labelTypeProperty().bind(getSkinnable().labelTypeProperty());
        field.fillDigitsProperty().bind(getSkinnable().fillDigitsProperty());

        field.valueProperty().addListener(it -> {
            if (!field.isUpdating()) {
                getSkinnable().getProperties().put("NEW_DURATION", createDuration());
            }
        });

        return field;
    }

    private Duration createDuration() {
        Duration duration = Duration.ZERO;

        for (DurationUnitField field : durationUnitFields) {
            ChronoUnit chronoUnit = field.getChronoUnit();
            Long value = field.getValue();
            if (value != null) {
                duration = duration.plus(value, chronoUnit);
            }
        }

        return duration;
    }
}
