package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.CircleProgressIndicatorSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Skin;

/**
 * CircleProgressIndicator is a visual control used to indicate the progress of a task.
 * It represents progress in a circular form, with options to show determinate or indeterminate states.
 * <p>
 * In a determinate state, the circle fills up based on the progress value, which ranges from 0.0 to 1.0,
 * where 0.0 indicates no progress and 1.0 indicates completion.
 * <p>
 * In an indeterminate state, the circle shows a cyclic animation, indicating that progress is ongoing
 * but the exact status is unknown. This state is useful for tasks where the progress cannot be determined.
 * <p>
 * The control also supports displaying a text or graphic inside the circle to provide additional
 * information or visual feedback to the user.
 * <p>
 * Usage examples include file downloads, file transfers, or any long-running tasks where
 * visual feedback on progress is beneficial.
 * <p>
 * The CircleProgressIndicator extends ArcProgressIndicator and adds a start angle property
 * that defines the starting angle of the arc used to display the progress. The default start angle is 90 degrees,
 * which corresponds to the top of the circle.
 *
 * <p>
 * <b>Pseudo class:</b> Beyond the <b>inherited</b> , <b>indeterminate</b> and determinate pseudo-classes
 * from ProgressIndicator, CircleProgressIndicator introduces a <b>completed</b> pseudo-class.
 * This pseudo-class can be used in CSS to apply custom styles when the progress reaches 1.0 (100%).
 *
 * <p>
 * <b>Tips:</b> If you prefer not to instantiate the animation object during initialization,
 * pass a 0.0 as the initial progress. This setup indicates no progress but avoids entering
 * the indeterminate state, which would otherwise instantiate and start the animation.
 *
 * <p>Usage examples:
 * <pre>
 *     // Initializes with no progress and no animation.
 *     CircleProgressIndicator progressIndicator = new CircleProgressIndicator(0.0);
 * </pre>
 */
public class CircleProgressIndicator extends ArcProgressIndicator {

    private static final String DEFAULT_STYLE_CLASS = "circle-progress-indicator";
    private static final double DEFAULT_START_ANGLE = 90.0;

    public CircleProgressIndicator() {
        this(INDETERMINATE_PROGRESS);
    }

    public CircleProgressIndicator(double progress) {
        super(progress);
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setMinSize(26, 26);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CircleProgressIndicatorSkin(this);
    }

    private DoubleProperty startAngle;

    /**
     * The start angle property defines the starting angle of the arc that is used to display the progress.
     * The default value is 90 degrees, which corresponds to the top of the circle.
     *
     * @return the start angle property
     */
    public final DoubleProperty startAngleProperty() {
        if (startAngle == null) {
            startAngle = new SimpleDoubleProperty(this, "startAngle", DEFAULT_START_ANGLE);
        }
        return startAngle;
    }

    public final double getStartAngle() {
        return startAngle == null ? DEFAULT_START_ANGLE : startAngle.get();
    }

    public final void setStartAngle(double startAngle) {
        startAngleProperty().set(startAngle);
    }

}
