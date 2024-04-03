package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CustomComboBox;
import com.dlsc.gemsfx.DurationPicker;
import com.dlsc.gemsfx.Spacer;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import javafx.util.Pair;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DurationPickerSkin extends ToggleVisibilityComboBoxSkin<DurationPicker> {

    private final HBox box;
    private final Spacer spacer;
    private final Button editButton;
    private final HBox innerBox = new HBox();
    private final List<DurationUnitField> durationUnitFields = new ArrayList<>();

    private Node popupContent;

    public DurationPickerSkin(DurationPicker picker) {
        super(picker);

        editButton = new Button();
        editButton.getStyleClass().add("edit-button");
        editButton.addEventHandler(MouseEvent.MOUSE_ENTERED, this::mouseEntered);
        editButton.addEventHandler(MouseEvent.MOUSE_EXITED, this::mouseExited);
        editButton.addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);

        editButton.setMaxHeight(Double.MAX_VALUE);
        editButton.setMaxWidth(Double.MAX_VALUE);
        editButton.setGraphic(new FontIcon());
        editButton.setFocusTraversable(false);
        editButton.visibleProperty().bind(picker.showPopupTriggerButtonProperty());
        editButton.managedProperty().bind(picker.showPopupTriggerButtonProperty());

        InvalidationListener updateListener = it -> buildView();
        picker.fieldsProperty().addListener(updateListener);

        innerBox.getStyleClass().add("fields-box");
        innerBox.setAlignment(Pos.CENTER_LEFT);

        spacer = new Spacer();

        box = new HBox();
        box.getStyleClass().add("box");
        updateBox();
        getChildren().add(box);

        registerChangeListener(picker.buttonDisplayProperty(), it -> updateBox());
        picker.separatorFactoryProperty().addListener(it -> buildView());
        buildView();
    }

    private void updateBox() {
        CustomComboBox.ButtonDisplay value = getSkinnable().getButtonDisplay();
        switch (value) {
            case LEFT:
                box.getChildren().setAll(editButton, spacer, innerBox);
                HBox.setHgrow(editButton, Priority.NEVER);
                break;
            case RIGHT:
                box.getChildren().setAll(innerBox, spacer, editButton);
                HBox.setHgrow(editButton, Priority.NEVER);
                break;
            case BUTTON_ONLY:
                box.getChildren().setAll(editButton);
                HBox.setHgrow(editButton, Priority.ALWAYS);
                break;
            case FIELD_ONLY:
                box.getChildren().setAll(innerBox);
                HBox.setHgrow(editButton, Priority.NEVER);
                break;
        }
    }

    @Override
    protected Node getPopupContent() {
        if (popupContent == null) {
            DurationPicker skinnable = getSkinnable();
            com.dlsc.pickerfx.DurationPicker durationPicker = new com.dlsc.pickerfx.DurationPicker() {
                @Override
                public String getUserAgentStylesheet() {
                    return Objects.requireNonNull(DurationPicker.class.getResource("duration-picker.css")).toExternalForm();
                }
            };
            durationPicker.valueProperty().bindBidirectional(skinnable.durationProperty());
            durationPicker.maximumDurationProperty().bind(skinnable.maximumDurationProperty());
            durationPicker.minimumDurationProperty().bind(skinnable.minimumDurationProperty());
            durationPicker.fieldsProperty().bind(skinnable.fieldsProperty());
            popupContent = new HBox(durationPicker);
            popupContent.getStyleClass().add("popup");
        }

        return popupContent;
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
