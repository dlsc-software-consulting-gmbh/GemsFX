package com.dlsc.gemsfx.docking;

import com.dlsc.gemsfx.util.AccessibilityUtil;
import com.dlsc.gemsfx.util.ResourceBundleManager;
import com.dlsc.gemsfx.util.ResourceBundleManager.BundleType;
import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * Default docking tab header, displaying a dockable's title, graphic, optional toolbar, actions menu and help button,
 * and close button.
 * Selection, keyboard navigation, and docking drag-and-drop are installed by the workspace. Selected headers
 * have the {@code selected} pseudo-class.
 * Applications can create or subclass this header in a {@link DockingContext#tabFactoryProperty() tab factory}.
 * Double-clicking edits the title when {@link Dockable#titleEditableProperty()} permits it.
 * Enter or focus loss commits a nonblank title; Escape cancels the edit.
 */
public class DockingTab extends HBox {

    private final Dockable dockable;
    private final Label label;
    private final HBox actions = new HBox();
    private TextField titleEditor;
    private final BooleanBinding dockableHovered;

    @SuppressWarnings("FieldCanBeLocal")
    private final ChangeListener<Boolean> titleEditableListener; // this is a field to prevent garbage collection of the weak listener
    @SuppressWarnings("FieldCanBeLocal")
    private final ChangeListener<Parent> contentParentListener; // this is a field to prevent garbage collection of the weak listener
    @SuppressWarnings("FieldCanBeLocal")
    private final ChangeListener<Node> toolbarListener; // this is a field to prevent garbage collection of the weak listener

    /**
     * Creates a header whose close button requests closure through the given context.
     *
     * @param context the non-null context managing the dockable
     * @param dockable the non-null dockable represented by this header
     */
    public DockingTab(DockingContext context, Dockable dockable) {
        Objects.requireNonNull(context, "context must not be null");
        this.dockable = Objects.requireNonNull(dockable, "dockable must not be null");
        getStyleClass().add("docking-tab");
        setAlignment(Pos.CENTER_LEFT);
        AccessibilityUtil.setRole(this, AccessibleRole.TAB_ITEM);

        label = createTitleLabel();
        StackPane toolbar = createToolbar();
        toolbarListener = (obs, oldToolbar, newToolbar) -> updateToolbar(toolbar, newToolbar);
        dockable.toolbarProperty().addListener(new WeakChangeListener<>(toolbarListener));
        updateToolbar(toolbar, dockable.getToolbar());

        MenuButton menu = createMenuButton();
        configureActions(toolbar, menu, createHelpButton(), createCloseButton(context));
        getChildren().addAll(label, actions);

        BooleanProperty contentHovered = new SimpleBooleanProperty(false);
        contentParentListener = trackContentHover(contentHovered);
        dockableHovered = hoverProperty().or(contentHovered).or(menu.showingProperty());
        installActionsFade();
        titleEditableListener = installTitleEditing();
    }

    private Label createTitleLabel() {
        Tooltip titleTooltip = new Tooltip();
        titleTooltip.textProperty().bind(dockable.titleProperty());
        Label label = new Label() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                // Text measurements are current only after the skin has laid out the label.
                setTooltip(getWidth() < prefWidth(-1) ? titleTooltip : null);
            }
        };
        label.textProperty().bind(dockable.titleProperty());
        label.graphicProperty().bind(dockable.graphicProperty());
        label.getStyleClass().add("docking-tab-title");
        label.setMnemonicParsing(false);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        fillAvailableHeight(label);
        HBox.setHgrow(label, Priority.ALWAYS);
        return label;
    }

    private void configureActions(Node... controls) {
        actions.getStyleClass().add("docking-tab-actions");
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setMinWidth(USE_PREF_SIZE);
        fillAvailableHeight(actions);
        actions.setOpacity(0);
        actions.managedProperty().bind(actions.opacityProperty().greaterThan(0));
        actions.getChildren().addAll(controls);
    }

    private Button createCloseButton(DockingContext context) {
        Button close = new Button();
        close.getStyleClass().add("docking-tab-close-button");
        close.setFocusTraversable(false);
        close.visibleProperty().bind(dockable.closeableProperty());
        close.managedProperty().bind(close.visibleProperty());
        fillAvailableHeight(close);
        Region icon = new Region();
        icon.getStyleClass().add("icon");
        close.setGraphic(icon);
        close.setTooltip(new Tooltip(ResourceBundleManager.getString(BundleType.DOCKING_PANE, "tooltip.close", "Close")));
        String closePattern = ResourceBundleManager.getString(BundleType.DOCKING_PANE, "accessible.text.close", "Close {0}");
        close.accessibleTextProperty().bind(Bindings.createStringBinding(
                () -> MessageFormat.format(closePattern, dockable.getTitle()), dockable.titleProperty()));
        close.setOnAction(event -> context.requestCloseDockable(dockable));
        close.addEventHandler(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
        return close;
    }

    private Button createHelpButton() {
        Button help = new Button();
        help.getStyleClass().add("docking-tab-help-button");
        help.setFocusTraversable(false);
        fillAvailableHeight(help);
        help.setGraphic(new FontIcon("bi-question-circle"));
        help.visibleProperty().bind(dockable.onHelpProperty().isNotNull());
        help.managedProperty().bind(help.visibleProperty());
        help.setTooltip(new Tooltip(ResourceBundleManager.getString(BundleType.DOCKING_PANE, "tooltip.help", "Help")));
        String helpPattern = ResourceBundleManager.getString(BundleType.DOCKING_PANE, "accessible.text.help", "Help for {0}");
        help.accessibleTextProperty().bind(Bindings.createStringBinding(
                () -> MessageFormat.format(helpPattern, dockable.getTitle()), dockable.titleProperty()));
        help.setOnAction(event -> {
            Runnable onHelp = dockable.getOnHelp();
            if (onHelp != null) {
                onHelp.run();
            }
        });
        help.addEventHandler(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
        return help;
    }

    private MenuButton createMenuButton() {
        MenuButton menu = new MenuButton();
        menu.getStyleClass().add("docking-tab-menu-button");
        menu.setFocusTraversable(false);
        fillAvailableHeight(menu);
        menu.setGraphic(new FontIcon("bi-three-dots-vertical"));
        menu.visibleProperty().bind(Bindings.isNotEmpty(dockable.getMenuItems()));
        menu.managedProperty().bind(menu.visibleProperty());
        menu.setTooltip(new Tooltip(ResourceBundleManager.getString(BundleType.DOCKING_PANE, "tooltip.more", "More actions")));
        String menuPattern = ResourceBundleManager.getString(BundleType.DOCKING_PANE, "accessible.text.more", "Actions for {0}");
        menu.accessibleTextProperty().bind(Bindings.createStringBinding(
                () -> MessageFormat.format(menuPattern, dockable.getTitle()), dockable.titleProperty()));
        menu.addEventHandler(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
        menu.visibleProperty().addListener((obs, wasVisible, visible) -> {
            if (!visible) {
                menu.hide();
            }
        });
        Bindings.bindContent(menu.getItems(), dockable.getMenuItems());
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            menu.hide();
            Bindings.unbindContent(menu.getItems(), dockable.getMenuItems());
            if (newScene == null) {
                menu.getItems().clear();
            } else {
                Bindings.bindContent(menu.getItems(), dockable.getMenuItems());
            }
        });
        return menu;
    }

    private StackPane createToolbar() {
        StackPane toolbar = new StackPane() {
            @Override
            protected void layoutChildren() {
                if (!getChildren().isEmpty()) {
                    Node content = getChildren().get(0);
                    double width = Math.max(0, getWidth() - snappedLeftInset() - snappedRightInset());
                    double height = Math.max(0, getHeight() - snappedTopInset() - snappedBottomInset());
                    // The tab, not the toolbar's size constraints, determines the available height.
                    content.resize(width, height);
                    positionInArea(content, snappedLeftInset(), snappedTopInset(), width, height,
                            -1, HPos.CENTER, VPos.CENTER);
                }
            }
        };
        toolbar.getStyleClass().add("docking-tab-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        fillAvailableHeight(toolbar);
        toolbar.visibleProperty().bind(dockable.toolbarProperty().isNotNull());
        toolbar.managedProperty().bind(toolbar.visibleProperty());
        toolbar.addEventHandler(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
        return toolbar;
    }

    private ChangeListener<Parent> trackContentHover(BooleanProperty contentHovered) {
        ChangeListener<Parent> listener = (obs, oldParent, parent) -> {
            contentHovered.unbind();
            if (parent == null) {
                contentHovered.set(false);
            } else {
                contentHovered.bind(parent.hoverProperty());
            }
        };
        dockable.getContent().parentProperty().addListener(new WeakChangeListener<>(listener));
        listener.changed(dockable.getContent().parentProperty(), null, dockable.getContent().getParent());
        return listener;
    }

    private void installActionsFade() {
        FadeTransition fade = new FadeTransition(Duration.millis(150), actions);
        fade.setFromValue(0);
        fade.setToValue(1);
        dockableHovered.addListener((obs, wasHovered, isHovered) -> {
            double opacity = actions.getOpacity();
            fade.stop();
            fade.setFromValue(opacity);
            fade.setToValue(isHovered ? 1 : 0);
            fade.playFromStart();
        });
        if (dockableHovered.get()) {
            fade.playFromStart();
        }
    }

    private ChangeListener<Boolean> installTitleEditing() {
        ChangeListener<Boolean> listener = (obs, wasEditable, editable) -> {
            if (!editable) {
                finishTitleEditing(false);
            }
        };
        dockable.titleEditableProperty().addListener(new WeakChangeListener<>(listener));
        addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2
                    && event.isStillSincePress() && dockable.isTitleEditable()) {
                startTitleEditing();
                event.consume();
            }
        });
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (titleEditor != null) {
                event.consume();
            }
        });
        return listener;
    }

    private void fillAvailableHeight(Region child) {
        child.setMinHeight(0);
        child.setMaxHeight(Double.MAX_VALUE);
    }

    private void startTitleEditing() {
        if (titleEditor != null) {
            return;
        }
        TextField editor = new TextField(dockable.getTitle());
        editor.getStyleClass().add("docking-tab-title-editor");
        editor.accessibleTextProperty().bind(dockable.titleProperty());
        editor.setMinWidth(0);
        editor.setMaxWidth(Double.MAX_VALUE);
        fillAvailableHeight(editor);
        HBox.setHgrow(editor, Priority.ALWAYS);
        editor.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE) {
                finishTitleEditing(event.getCode() == KeyCode.ENTER);
                requestFocus();
                event.consume();
            }
        });
        editor.addEventHandler(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
        editor.addEventHandler(MouseEvent.DRAG_DETECTED, MouseEvent::consume);
        editor.focusedProperty().addListener((obs, wasFocused, focused) -> {
            if (!focused && titleEditor == editor) {
                finishTitleEditing(true);
            }
        });
        titleEditor = editor;
        getChildren().set(getChildren().indexOf(label), editor);
        editor.requestFocus();
        editor.selectAll();
    }

    private void finishTitleEditing(boolean commit) {
        TextField editor = titleEditor;
        if (editor == null) {
            return;
        }
        titleEditor = null;
        getChildren().set(getChildren().indexOf(editor), label);
        if (commit && dockable.isTitleEditable() && !editor.getText().isBlank()) {
            dockable.setTitle(editor.getText());
        }
    }

    private void updateToolbar(StackPane container, Node toolbar) {
        if (toolbar == null) {
            container.getChildren().clear();
        } else {
            container.getChildren().setAll(toolbar);
        }
    }
}
