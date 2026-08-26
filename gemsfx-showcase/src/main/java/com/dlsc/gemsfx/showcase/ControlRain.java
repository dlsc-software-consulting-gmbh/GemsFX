package com.dlsc.gemsfx.showcase;

import com.dlsc.gemsfx.util.SVGUtil;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.concurrent.Task;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableDoubleProperty;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A decorative background that lets stylized user interface graphics rain down the window,
 * the same effect that is used on the hero section of the DLSC website.
 * <p>
 * The graphics are SVG files (see the {@code rain} resource folder), each of them a small
 * mockup of a typical user interface element: a calendar, a table, a set of buttons, a color
 * palette, and so on. The SVG files are rendered to images exactly once, in the background,
 * and the resulting images are shared by all items of the rain. Rendering to images up front
 * keeps the animation itself cheap, no matter how many items are falling at the same time.
 * <p>
 * The animation loops forever: an item that has left the bottom of the pane is not removed but
 * re-spawned above the top edge with a new graphic, a new horizontal position, a new size, a
 * new tilt, and a new speed. Items fade in shortly after they appear and fade out again before
 * they leave, hence they never pop into or out of existence.
 * <p>
 * How strongly the graphics stand out from the background is controlled via CSS, see the
 * {@code -fx-min-item-opacity} and {@code -fx-max-item-opacity} properties. Dark and light
 * themes need quite different values here: the graphics themselves are light, hence they need
 * to be a lot more opaque on a light background than on a dark one to be visible at all.
 */
public class ControlRain extends Pane {

    private static final Logger LOG = Logger.getLogger(ControlRain.class.getName());

    /** The SVG files shown by the rain, relative to the {@code rain} resource folder. */
    private static final List<String> GRAPHICS = List.of(
            "calendar-picker.svg",
            "rating-view.svg",
            "paging-table.svg",
            "buttons.svg",
            "search-field.svg",
            "filter-view.svg",
            "progress.svg",
            "circle-progress.svg",
            "tree-view.svg",
            "color-picker.svg",
            "toggles.svg",
            "notification.svg",
            "time-picker.svg");

    /** Number of graphics falling at the same time. */
    private static final int ITEM_COUNT = 18;

    /**
     * Factor by which the SVG files are over-sampled when they are rendered into images. The
     * items are never scaled above 1.0, hence the images stay crisp on high resolution displays.
     */
    private static final double RENDER_SCALE = 2;

    /** Time an item needs to travel from the top to the bottom, in seconds. */
    private static final double MIN_DURATION = 10;
    private static final double MAX_DURATION = 24;

    /** Size of an item relative to the size of its SVG graphic. */
    private static final double MIN_SCALE = .55;
    private static final double MAX_SCALE = 1.1;

    /** Default opacity of an item while it is fully visible. */
    private static final double DEFAULT_MIN_ITEM_OPACITY = .5;
    private static final double DEFAULT_MAX_ITEM_OPACITY = .85;

    /** Maximum tilt of an item, in degrees. */
    private static final double MAX_TILT = 9;

    /** Distance an item keeps above the top and below the bottom edge of the pane. */
    private static final double MARGIN = 60;

    /** Fraction of the travel used to fade an item in and out. */
    private static final double FADE_IN = .07;
    private static final double FADE_OUT = .12;

    /** The rendered graphics, shared by all instances and by all items. */
    private static final Map<String, Image> IMAGES = new ConcurrentHashMap<>();

    private final List<Item> items = new ArrayList<>();
    private final Random random = new Random();
    private final AnimationTimer timer;

    private boolean loading;
    private boolean playing;

    /**
     * Creates a new, empty rain. The animation has to be started with {@link #play()}.
     */
    public ControlRain() {
        getStyleClass().add("control-rain");
        setMouseTransparent(true);

        timer = new AnimationTimer() {

            private long lastTime;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                // cap the time step so that a long pause (breakpoint, sleeping laptop, ...) does
                // not make the items jump across the whole window in a single frame
                double dt = Math.min((now - lastTime) / 1_000_000_000.0, .05);
                lastTime = now;
                update(dt);
            }

            @Override
            public void stop() {
                super.stop();
                lastTime = 0;
            }
        };

        minItemOpacity.addListener(it -> layoutItems());
        maxItemOpacity.addListener(it -> layoutItems());

        // the animation must never keep running while the pane is not part of a scene
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                stop();
            }
        });
    }

    /**
     * Starts the endless animation. The graphics are rendered upon the first call, in the
     * background, hence the rain may start with a short delay.
     */
    public void play() {
        playing = true;

        if (items.isEmpty()) {
            loadGraphics();
            return;
        }

        timer.start();
    }

    /**
     * Stops the animation. The items keep their current position, a subsequent call to
     * {@link #play()} continues where the animation left off.
     */
    public void stop() {
        playing = false;
        timer.stop();
    }

    private final StyleableDoubleProperty minItemOpacity = new SimpleStyleableDoubleProperty(StyleableProperties.MIN_ITEM_OPACITY, this, "minItemOpacity", DEFAULT_MIN_ITEM_OPACITY);

    /**
     * The opacity of the most transparent item of the rain. Every item picks a fixed value
     * between this opacity and {@link #maxItemOpacityProperty()}, which gives the rain some
     * depth. Settable via the CSS property {@code -fx-min-item-opacity}.
     *
     * @return the lower bound of the opacity of the items
     */
    public final DoubleProperty minItemOpacityProperty() {
        return minItemOpacity;
    }

    public final double getMinItemOpacity() {
        return minItemOpacity.get();
    }

    public final void setMinItemOpacity(double minItemOpacity) {
        this.minItemOpacity.set(minItemOpacity);
    }

    private final StyleableDoubleProperty maxItemOpacity = new SimpleStyleableDoubleProperty(StyleableProperties.MAX_ITEM_OPACITY, this, "maxItemOpacity", DEFAULT_MAX_ITEM_OPACITY);

    /**
     * The opacity of the most opaque item of the rain. Settable via the CSS property
     * {@code -fx-max-item-opacity}.
     *
     * @return the upper bound of the opacity of the items
     */
    public final DoubleProperty maxItemOpacityProperty() {
        return maxItemOpacity;
    }

    public final double getMaxItemOpacity() {
        return maxItemOpacity.get();
    }

    public final void setMaxItemOpacity(double maxItemOpacity) {
        this.maxItemOpacity.set(maxItemOpacity);
    }

    private static class StyleableProperties {

        private static final CssMetaData<ControlRain, Number> MIN_ITEM_OPACITY = new CssMetaData<>("-fx-min-item-opacity", SizeConverter.getInstance(), DEFAULT_MIN_ITEM_OPACITY) {
            @Override
            public boolean isSettable(ControlRain rain) {
                return !rain.minItemOpacity.isBound();
            }

            @Override
            public StyleableDoubleProperty getStyleableProperty(ControlRain rain) {
                return rain.minItemOpacity;
            }
        };

        private static final CssMetaData<ControlRain, Number> MAX_ITEM_OPACITY = new CssMetaData<>("-fx-max-item-opacity", SizeConverter.getInstance(), DEFAULT_MAX_ITEM_OPACITY) {
            @Override
            public boolean isSettable(ControlRain rain) {
                return !rain.maxItemOpacity.isBound();
            }

            @Override
            public StyleableDoubleProperty getStyleableProperty(ControlRain rain) {
                return rain.maxItemOpacity;
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Pane.getClassCssMetaData());
            styleables.add(MIN_ITEM_OPACITY);
            styleables.add(MAX_ITEM_OPACITY);
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

    /**
     * Renders the SVG graphics into images on a background thread. Rendering is based on Java2D
     * and takes long enough to be noticeable, hence it must not happen on the UI thread.
     */
    private void loadGraphics() {
        if (loading) {
            return;
        }
        loading = true;

        Task<Map<String, Image>> task = new Task<>() {
            @Override
            protected Map<String, Image> call() {
                for (String graphic : GRAPHICS) {
                    IMAGES.computeIfAbsent(graphic, ControlRain::render);
                }
                return IMAGES;
            }
        };

        task.setOnSucceeded(evt -> {
            loading = false;
            createItems();
            if (playing) {
                timer.start();
            }
        });

        task.setOnFailed(evt -> {
            loading = false;
            LOG.log(Level.WARNING, "unable to render the graphics of the welcome page", task.getException());
        });

        Thread thread = new Thread(task, "Control Rain Renderer");
        thread.setDaemon(true);
        thread.start();
    }

    private static Image render(String graphic) {
        URL url = Objects.requireNonNull(ControlRain.class.getResource("rain/" + graphic), () -> "missing graphic: " + graphic);
        return SVGUtil.toImage(url, 0, 0, RENDER_SCALE, RENDER_SCALE);
    }

    private void createItems() {
        getChildren().clear();
        items.clear();

        List<Image> images = GRAPHICS.stream().map(IMAGES::get).filter(Objects::nonNull).toList();
        if (images.isEmpty()) {
            return;
        }

        for (int i = 0; i < ITEM_COUNT; i++) {
            Item item = new Item(images);
            // seed the rain with items that are already somewhere on their way down, otherwise
            // the first couple of seconds would show an empty window
            item.respawn(random.nextDouble());
            items.add(item);
            getChildren().add(item.view);
        }

        Platform.runLater(this::layoutItems);
    }

    private void update(double dt) {
        for (Item item : items) {
            item.progress += dt / item.duration;
            if (item.progress >= 1) {
                item.respawn(0);
            }
        }
        layoutItems();
    }

    private void layoutItems() {
        double width = getWidth();
        double height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        for (Item item : items) {
            item.layout(width, height);
        }
    }

    /**
     * A single falling graphic. The item keeps its own randomized appearance and a progress
     * value between 0 (just above the top edge) and 1 (just below the bottom edge).
     */
    private class Item {

        private final ImageView view = new ImageView();
        private final List<Image> images;

        private double progress;
        private double duration;
        private double positionX;
        private double opacityFactor;

        Item(List<Image> images) {
            this.images = images;
            view.getStyleClass().add("control-rain-item");
            view.setPreserveRatio(true);
            view.setSmooth(true);
            view.setManaged(false);
            view.setMouseTransparent(true);
            // stays invisible until the first frame has positioned it
            view.setOpacity(0);
        }

        /**
         * Gives the item a new appearance and places it at the given point of its travel.
         *
         * @param progress the initial progress, 0 for an item entering at the top
         */
        void respawn(double progress) {
            Image image = images.get(random.nextInt(images.size()));

            this.progress = progress;
            this.duration = MIN_DURATION + random.nextDouble() * (MAX_DURATION - MIN_DURATION);
            this.positionX = random.nextDouble();
            this.opacityFactor = random.nextDouble();

            double scale = MIN_SCALE + random.nextDouble() * (MAX_SCALE - MIN_SCALE);

            view.setImage(image);
            view.setFitWidth(image.getWidth() / RENDER_SCALE * scale);
            view.setRotate((random.nextDouble() - .5) * 2 * MAX_TILT);
        }

        void layout(double width, double height) {
            double itemWidth = view.getFitWidth();
            double itemHeight = itemWidth / view.getImage().getWidth() * view.getImage().getHeight();

            double from = -itemHeight - MARGIN;
            double to = height + MARGIN;

            double min = getMinItemOpacity();
            double max = getMaxItemOpacity();

            view.relocate(positionX * Math.max(1, width - itemWidth), from + progress * (to - from));
            view.setOpacity((min + opacityFactor * (max - min)) * fade());
        }

        /**
         * Ramps the opacity up right after the item has appeared and back down before it leaves.
         */
        private double fade() {
            if (progress < FADE_IN) {
                return progress / FADE_IN;
            }
            if (progress > 1 - FADE_OUT) {
                return (1 - progress) / FADE_OUT;
            }
            return 1;
        }
    }
}
