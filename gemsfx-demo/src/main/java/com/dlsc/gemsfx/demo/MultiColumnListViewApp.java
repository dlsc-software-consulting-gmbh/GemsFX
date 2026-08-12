package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.AvatarView;
import com.dlsc.gemsfx.MultiColumnListView;
import com.dlsc.gemsfx.MultiColumnListView.ColumnListCell;
import com.dlsc.gemsfx.MultiColumnListView.ListViewColumn;
import com.dlsc.gemsfx.MultiColumnListView.MultiColumnListViewEvent;
import com.dlsc.gemsfx.Skeleton;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.StatusBar;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.random.RandomGenerator;

public class MultiColumnListViewApp extends GemApplication {
    private final ListViewColumn<Issue> col1 = new ListViewColumn<>();
    private final ListViewColumn<Issue> col2 = new ListViewColumn<>();
    private final ListViewColumn<Issue> col3 = new ListViewColumn<>();
    private final ListViewColumn<Issue> col4 = new ListViewColumn<>();
    private final ListViewColumn<Issue> col5 = new ListViewColumn<>();

    @Override
    public void start(Stage stage) { super.start(stage);
        MultiColumnListView<Issue> multiColumnListView = new MultiColumnListView<>();
        multiColumnListView.setCellFactory(listView -> new IssueListCell(multiColumnListView));

        Node placeholder = createSkeletonPlaceholder();
        multiColumnListView.setPlaceholder(placeholder);
        multiColumnListView.getColumns().setAll(createColumns());
        multiColumnListView.addEventHandler(MultiColumnListViewEvent.ANY, System.out::println);
        VBox.setVgrow(multiColumnListView, Priority.ALWAYS);

        CheckBox showHeaders = new CheckBox("Show Headers");
        showHeaders.selectedProperty().bindBidirectional(multiColumnListView.showHeadersProperty());

        CheckBox disableDragAndDrop = new CheckBox("Disable Editing");
        disableDragAndDrop.selectedProperty().bindBidirectional(multiColumnListView.disableDragAndDropProperty());

        Callback<Integer, Node> separatorFactory = multiColumnListView.getSeparatorFactory();

        CheckBox separators = new CheckBox("Use Separators");
        separators.setSelected(true);
        separators.selectedProperty().addListener(it -> {
            if (separators.isSelected()) {
                multiColumnListView.setSeparatorFactory(separatorFactory);
            } else {
                multiColumnListView.setSeparatorFactory(null);
            }
        });

        Button clearColumns = new Button("Clear Columns");
        clearColumns.setOnAction(evt -> multiColumnListView.getColumns().clear());
        clearColumns.disableProperty().bind(multiColumnListView.columnsProperty().emptyProperty());

        Button restoreColumns = new Button("Restore Columns");
        restoreColumns.setOnAction(evt -> multiColumnListView.getColumns().setAll(col1, col2, col3, col4, col5));
        restoreColumns.disableProperty().bind(multiColumnListView.columnsProperty().emptyProperty().not());

        HBox optionsBox = new HBox(10, clearColumns, restoreColumns, createShimmerToggle(placeholder), separators, showHeaders, disableDragAndDrop);
        optionsBox.setAlignment(Pos.CENTER_RIGHT);
        createThemeSwitcher().ifPresent(switcher -> optionsBox.getChildren().add(0, switcher));

        StatusBar statusBar = new StatusBar();
        multiColumnListView.addEventHandler(MultiColumnListViewEvent.DRAG_NOT_POSSIBLE, e-> statusBar.setText("Drag not possible"));
        multiColumnListView.addEventHandler(MultiColumnListViewEvent.DROP_NOT_POSSIBLE, e-> statusBar.setText("Drop here not possible at index " + e.getIndex() + " in column: " + e.getColumn().getUserObject()));
        multiColumnListView.addEventHandler(MultiColumnListViewEvent.ITEM_MOVED, e-> statusBar.setText("Item was moved to column: " + e.getColumn().getUserObject() + " at index: " + e.getIndex()));
        multiColumnListView.addEventHandler(MultiColumnListViewEvent.DRAG_OVER, e-> statusBar.setText("Item dragged over column: " + e.getColumn().getUserObject() + " at index: " + e.getIndex()));

        VBox vbox = new VBox(10, multiColumnListView, optionsBox);
        vbox.setAlignment(Pos.TOP_RIGHT);
        vbox.setPadding(new Insets(20));

        VBox outerBox = new VBox(vbox, statusBar);
        VBox.setVgrow(vbox, Priority.ALWAYS);

        Scene scene = new Scene(outerBox);
        scene.getStylesheets().add(Objects.requireNonNull(MultiColumnListViewApp.class.getResource("multi-column-app.css")).toExternalForm());
        if (Boolean.getBoolean("atlantafx")) {
            scene.getStylesheets().add(Objects.requireNonNull(MultiColumnListViewApp.class.getResource("multi-column-app-atlantafx.css")).toExternalForm());
        }

        CSSFX.start();

        stage.setTitle("MultiColumnListView");
        stage.setScene(scene);
        stage.setWidth(1400);
        stage.setHeight(950);

        stage.show();
    }

    /**
     * Creates the placeholder node that will be shown by the {@link MultiColumnListView} when
     * no columns have been added to it. The placeholder uses the {@link Skeleton} control of
     * GemsFX to mimic four columns of issue cards that are still being loaded.
     *
     * @return the placeholder node
     */
    private Node createSkeletonPlaceholder() {
        HBox placeholder = new HBox();
        placeholder.getStyleClass().add("skeleton-placeholder");
        placeholder.setFillHeight(true);

        for (int i = 0; i < 4; i++) {
            VBox column = createSkeletonColumn(2 + i % 3);
            HBox.setHgrow(column, Priority.ALWAYS);
            placeholder.getChildren().add(column);
        }

        return placeholder;
    }

    /**
     * Creates a single skeleton column consisting of a column header and the given number of
     * skeleton cards.
     *
     * @param cardCount the number of skeleton cards shown inside the column
     * @return the skeleton column
     */
    private VBox createSkeletonColumn(int cardCount) {
        Skeleton header = new Skeleton(Skeleton.Variant.ROUNDED_RECTANGLE);
        header.getStyleClass().add("skeleton-header");
        header.setPrefSize(90, 14);
        header.setMinSize(90, 14);
        header.setMaxWidth(90);

        VBox column = new VBox(header);
        column.getStyleClass().add("skeleton-column");

        for (int i = 0; i < cardCount; i++) {
            column.getChildren().add(createSkeletonCard());
        }

        Region filler = new Region();
        VBox.setVgrow(filler, Priority.ALWAYS);
        column.getChildren().add(filler);

        return column;
    }

    /**
     * Creates a single skeleton card that mimics the layout of the {@link IssueListCell}.
     *
     * @return the skeleton card
     */
    private VBox createSkeletonCard() {
        Skeleton typeIcon = new Skeleton(Skeleton.Variant.CIRCULAR);
        typeIcon.setPrefSize(14, 14);
        typeIcon.setMinSize(14, 14);
        typeIcon.setMaxSize(14, 14);

        Skeleton id = new Skeleton(Skeleton.Variant.ROUNDED_RECTANGLE);
        id.setPrefSize(55, 10);
        id.setMaxWidth(55);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Skeleton priority = new Skeleton(Skeleton.Variant.ROUNDED_RECTANGLE);
        priority.setPrefSize(45, 12);
        priority.setMaxWidth(45);

        HBox header = new HBox(typeIcon, id, headerSpacer, priority);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);

        Skeleton title = new Skeleton(Skeleton.Variant.TEXT);
        title.setLineCount(2);
        title.setLineHeight(12);
        title.setLineSpacing(6);
        title.setLastLineFillPercent(60);

        Skeleton progress = new Skeleton(Skeleton.Variant.ROUNDED_RECTANGLE);
        progress.setPrefHeight(6);
        progress.setMaxHeight(6);

        Skeleton avatar = new Skeleton(Skeleton.Variant.CIRCULAR);
        avatar.setPrefSize(24, 24);
        avatar.setMinSize(24, 24);
        avatar.setMaxSize(24, 24);

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        Skeleton counters = new Skeleton(Skeleton.Variant.ROUNDED_RECTANGLE);
        counters.setPrefSize(50, 10);
        counters.setMaxWidth(50);

        HBox footer = new HBox(avatar, footerSpacer, counters);
        footer.getStyleClass().add("footer");
        footer.setAlignment(Pos.CENTER_LEFT);

        Region verticalSpacer = new Region();
        VBox.setVgrow(verticalSpacer, Priority.ALWAYS);

        VBox card = new VBox(header, title, verticalSpacer, progress, footer);
        card.getStyleClass().add("skeleton-card");

        return card;
    }

    private List<ListViewColumn<Issue>> createColumns() {
        col1.setHeader(new Label("Backlog"));
        col2.setHeader(new Label("To Do"));
        col3.setHeader(new Label("In Progress"));
        col4.setHeader(new Label("In Review"));
        col5.setHeader(new Label("Done"));

        col1.setUserObject("col1");
        col2.setUserObject("col2");
        col3.setUserObject("col3");
        col4.setUserObject("col4");
        col5.setUserObject("col5");

        col1.getItems().setAll(
                new Issue("Search field ignores diacritics", Issue.Type.BUG),
                new Issue("Add dark mode support to the calendar view", Issue.Type.FEATURE),
                new Issue("Document the drag and drop callbacks", Issue.Type.TASK));

        col2.getItems().setAll(
                new Issue("Memory leak when disposing skins", Issue.Type.BUG),
                new Issue("Introduce a paging tree table view", Issue.Type.FEATURE),
                new Issue("Speed up the initial layout pass", Issue.Type.IMPROVEMENT),
                new Issue("Migrate demos to the new launcher", Issue.Type.TASK),
                new Issue("Time picker rolls over at midnight", Issue.Type.BUG),
                new Issue("Support keyboard navigation in the chip view", Issue.Type.FEATURE));

        col3.getItems().setAll(
                new Issue("Rework the default kanban card", Issue.Type.IMPROVEMENT),
                new Issue("Avatar view flickers on image change", Issue.Type.BUG));

        col4.getItems().setAll(
                new Issue("Headless unit tests for all controls", Issue.Type.TASK));

        col5.getItems().setAll(
                new Issue("Publish 2.x release notes", Issue.Type.TASK),
                new Issue("Fix scrollbar artefacts on Windows", Issue.Type.BUG),
                new Issue("Localize the date range presets", Issue.Type.IMPROVEMENT));

        col1.getItems().forEach(issue -> issue.setStatus(Issue.Status.BACKLOG));
        col2.getItems().forEach(issue -> issue.setStatus(Issue.Status.TODO));
        col3.getItems().forEach(issue -> issue.setStatus(Issue.Status.IN_PROGRESS));
        col4.getItems().forEach(issue -> issue.setStatus(Issue.Status.IN_REVIEW));
        col5.getItems().forEach(issue -> issue.setStatus(Issue.Status.DONE));

        return List.of(col1, col2, col3, col4, col5);
    }

    /**
     * A model object representing an issue / ticket as it can be found in modern issue
     * management systems. The various fields are used by the {@link IssueListCell} to
     * visualize a rich kanban board card.
     */
    public static class Issue {

        private static final RandomGenerator RANDOM = RandomGenerator.getDefault();

        private static final List<String> ASSIGNEES = List.of("Dirk Lemmermann", "Katja Meier",
                "Philip Jordan", "Jule Winter", "Armin Fischer", "Paula Sousa");

        private static final List<String> LABELS = List.of("ui", "core", "css", "a11y", "docs",
                "performance", "regression", "api");

        private static int counter = 100;

        private final String id = "GEM-" + (++counter);
        private final String title;
        private final Type type;
        private final Priority priority;
        private final String assignee;
        private final List<String> labels;
        private final int storyPoints;
        private final int comments;
        private final int attachments;
        private final int subtasks;
        private final int completedSubtasks;
        private final LocalDate dueDate;

        private Status status = Status.BACKLOG;

        public Issue(String title, Type type) {
            this.title = title;
            this.type = type;
            this.priority = Priority.values()[RANDOM.nextInt(Priority.values().length)];
            this.assignee = ASSIGNEES.get(RANDOM.nextInt(ASSIGNEES.size()));
            this.storyPoints = 1 << RANDOM.nextInt(4);
            this.comments = RANDOM.nextInt(12);
            this.attachments = RANDOM.nextInt(4);
            this.subtasks = 2 + RANDOM.nextInt(5);
            this.completedSubtasks = RANDOM.nextInt(subtasks + 1);
            this.dueDate = LocalDate.now().plusDays(RANDOM.nextInt(21) - 5L);

            List<String> availableLabels = new ArrayList<>(LABELS);
            Collections.shuffle(availableLabels);
            this.labels = List.copyOf(availableLabels.subList(0, 1 + RANDOM.nextInt(2)));
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Type getType() {
            return type;
        }

        public Priority getPriority() {
            return priority;
        }

        public String getAssignee() {
            return assignee;
        }

        /**
         * Returns the initials of the assignee, e.g. "DL" for "Dirk Lemmermann".
         */
        public String getAssigneeInitials() {
            return Arrays.stream(assignee.split(" "))
                    .filter(part -> !part.isBlank())
                    .map(part -> part.substring(0, 1).toUpperCase(Locale.ROOT))
                    .reduce("", String::concat);
        }

        public List<String> getLabels() {
            return labels;
        }

        public int getStoryPoints() {
            return storyPoints;
        }

        public int getComments() {
            return comments;
        }

        public int getAttachments() {
            return attachments;
        }

        public int getSubtasks() {
            return subtasks;
        }

        public int getCompletedSubtasks() {
            return completedSubtasks;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return id + " " + title;
        }

        /**
         * The type of an issue. Each type comes with its own icon and color.
         */
        public enum Type {

            BUG("Bug", MaterialDesign.MDI_BUG),
            FEATURE("Feature", MaterialDesign.MDI_STAR_CIRCLE),
            IMPROVEMENT("Improvement", MaterialDesign.MDI_TRENDING_UP),
            TASK("Task", MaterialDesign.MDI_CHECKBOX_MARKED_OUTLINE);

            private final String displayName;
            private final MaterialDesign icon;

            Type(String displayName, MaterialDesign icon) {
                this.displayName = displayName;
                this.icon = icon;
            }

            public String getDisplayName() {
                return displayName;
            }

            public MaterialDesign getIcon() {
                return icon;
            }

            public String getStyleClass() {
                return "type-" + name().toLowerCase(Locale.ROOT);
            }
        }

        /**
         * The priority of an issue. Each priority comes with its own icon and color.
         */
        public enum Priority {

            LOW("Low", MaterialDesign.MDI_ARROW_DOWN),
            MEDIUM("Medium", MaterialDesign.MDI_ARROW_RIGHT),
            HIGH("High", MaterialDesign.MDI_ARROW_UP),
            CRITICAL("Critical", MaterialDesign.MDI_ALERT_CIRCLE);

            private final String displayName;
            private final MaterialDesign icon;

            Priority(String displayName, MaterialDesign icon) {
                this.displayName = displayName;
                this.icon = icon;
            }

            public String getDisplayName() {
                return displayName;
            }

            public MaterialDesign getIcon() {
                return icon;
            }

            public String getStyleClass() {
                return "priority-" + name().toLowerCase(Locale.ROOT);
            }
        }

        /**
         * The workflow status of an issue, normally identical to the column the issue is located in.
         */
        public enum Status {

            BACKLOG("Backlog"),
            TODO("To Do"),
            IN_PROGRESS("In Progress"),
            IN_REVIEW("In Review"),
            DONE("Done");

            private final String displayName;

            Status(String displayName) {
                this.displayName = displayName;
            }

            public String getDisplayName() {
                return displayName;
            }

            public String getStyleClass() {
                return name().toLowerCase(Locale.ROOT).replace('_', '-');
            }
        }
    }

    /**
     * A list cell that visualizes an {@link Issue} as a rich kanban board card. The card shows the
     * issue type, the issue ID, its priority, the title, its labels, the progress made on its
     * subtasks, the avatar of the assignee, and various counters (comments, attachments, story
     * points, due date).
     */
    public static class IssueListCell extends ColumnListCell<Issue> {

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d");

        private final StackPane wrapper;

        private final FontIcon typeIcon = new FontIcon();
        private final Label idLabel = new Label();
        private final FontIcon priorityIcon = new FontIcon();
        private final Label priorityLabel = new Label();
        private final Label titleLabel = new Label();
        private final FlowPane labelsPane = new FlowPane();
        private final Label subtasksLabel = new Label();
        private final ProgressBar progressBar = new ProgressBar();
        private final AvatarView avatarView = new AvatarView();
        private final Label commentsLabel = new Label();
        private final Label attachmentsLabel = new Label();
        private final Label storyPointsLabel = new Label();
        private final Label dueDateLabel = new Label();
        private final VBox card = new VBox();

        public IssueListCell(MultiColumnListView<Issue> multiColumnListView) {
            super(multiColumnListView);

            getStyleClass().add("issue-list-cell");

            typeIcon.getStyleClass().add("type-icon");
            idLabel.getStyleClass().add("id-label");

            Region headerSpacer = new Region();
            HBox.setHgrow(headerSpacer, Priority.ALWAYS);

            priorityIcon.getStyleClass().add("priority-icon");
            priorityLabel.getStyleClass().add("priority-label");
            priorityLabel.setGraphic(priorityIcon);

            HBox header = new HBox(typeIcon, idLabel, headerSpacer, priorityLabel);
            header.getStyleClass().add("header");
            header.setAlignment(Pos.CENTER_LEFT);

            titleLabel.getStyleClass().add("title-label");
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(Double.MAX_VALUE);

            labelsPane.getStyleClass().add("labels-pane");
            labelsPane.managedProperty().bind(labelsPane.visibleProperty());

            subtasksLabel.getStyleClass().add("subtasks-label");

            progressBar.getStyleClass().add("subtasks-progress");
            progressBar.setMaxWidth(Double.MAX_VALUE);

            VBox progressBox = new VBox(subtasksLabel, progressBar);
            progressBox.getStyleClass().add("progress-box");

            avatarView.setSize(24);
            avatarView.setAvatarShape(AvatarView.AvatarShape.SQUARE);

            commentsLabel.getStyleClass().add("counter-label");
            commentsLabel.setGraphic(new FontIcon(MaterialDesign.MDI_COMMENT_OUTLINE));

            attachmentsLabel.getStyleClass().add("counter-label");
            attachmentsLabel.setGraphic(new FontIcon(MaterialDesign.MDI_PAPERCLIP));

            storyPointsLabel.getStyleClass().add("story-points-label");

            dueDateLabel.getStyleClass().add("due-date-label");
            dueDateLabel.setGraphic(new FontIcon(MaterialDesign.MDI_CALENDAR_CLOCK));

            Region footerSpacer = new Region();
            HBox.setHgrow(footerSpacer, Priority.ALWAYS);

            HBox footer = new HBox(avatarView, dueDateLabel, footerSpacer, commentsLabel, attachmentsLabel, storyPointsLabel);
            footer.getStyleClass().add("footer");
            footer.setAlignment(Pos.CENTER_LEFT);

            Region verticalSpacer = new Region();
            VBox.setVgrow(verticalSpacer, Priority.ALWAYS);

            card.getStyleClass().add("content");
            card.getChildren().setAll(header, titleLabel, labelsPane, verticalSpacer, progressBox, footer);
            card.visibleProperty().bind(placeholderProperty().not().and(emptyProperty().not()));
            card.managedProperty().bind(placeholderProperty().not().and(emptyProperty().not()));

            VBox contentPlaceholder = new VBox();
            contentPlaceholder.getStyleClass().add("placeholder");
            contentPlaceholder.visibleProperty().bind(placeholderProperty());
            contentPlaceholder.managedProperty().bind(placeholderProperty());

            Label placeholderLabel = new Label();
            placeholderLabel.getStyleClass().add("placeholder-label");
            placeholderLabel.textProperty().bind(textProperty());
            placeholderLabel.visibleProperty().bind(placeholderProperty());
            placeholderLabel.managedProperty().bind(placeholderProperty());

            wrapper = new StackPane(card, contentPlaceholder, placeholderLabel);
            wrapper.prefWidthProperty().bind(widthProperty().subtract(Bindings.createDoubleBinding(() -> getInsets().getLeft() + getInsets().getRight(), insetsProperty())));
            setGraphic(wrapper);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected Node getSnapshotNode() {
            return wrapper;
        }

        @Override
        protected void updateUserObject(Issue item, boolean empty) {
            getStyleClass().removeAll("backlog", "todo", "in-progress", "in-review", "done");
            card.getStyleClass().removeIf(style -> style.startsWith("type-") || style.startsWith("priority-"));

            if (isFromPlaceholder()) {
                setText("From");
            } else if (isToPlaceholder()) {
                setText("To");
            } else if (item != null && !empty) {
                setText("");
                updateCard(item);
            } else {
                setText("");
            }
        }

        private void updateCard(Issue item) {
            getStyleClass().add(item.getStatus().getStyleClass());
            card.getStyleClass().addAll(item.getType().getStyleClass(), item.getPriority().getStyleClass());

            typeIcon.setIconCode(item.getType().getIcon());
            idLabel.setText(item.getId());

            priorityIcon.setIconCode(item.getPriority().getIcon());
            priorityLabel.setText(item.getPriority().getDisplayName());

            titleLabel.setText(item.getTitle());

            labelsPane.getChildren().setAll(item.getLabels().stream().map(text -> {
                Label label = new Label(text);
                label.getStyleClass().addAll("issue-label", "issue-label-" + text);
                return (Node) label;
            }).toList());
            labelsPane.setVisible(!item.getLabels().isEmpty());

            subtasksLabel.setText(item.getCompletedSubtasks() + " of " + item.getSubtasks() + " subtasks");
            progressBar.setProgress((double) item.getCompletedSubtasks() / item.getSubtasks());

            avatarView.setInitials(item.getAssigneeInitials());
            avatarView.setAccessibleText(item.getAssignee());

            commentsLabel.setText(String.valueOf(item.getComments()));
            attachmentsLabel.setText(String.valueOf(item.getAttachments()));
            attachmentsLabel.setVisible(item.getAttachments() > 0);
            attachmentsLabel.setManaged(item.getAttachments() > 0);

            storyPointsLabel.setText(String.valueOf(item.getStoryPoints()));

            updateDueDate(item);
        }

        private void updateDueDate(Issue item) {
            LocalDate dueDate = item.getDueDate();
            long days = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);

            dueDateLabel.setText(DATE_FORMATTER.format(dueDate));
            dueDateLabel.getStyleClass().removeAll("overdue", "due-soon");

            if (item.getStatus() != Issue.Status.DONE) {
                if (days < 0) {
                    dueDateLabel.getStyleClass().add("overdue");
                } else if (days <= 2) {
                    dueDateLabel.getStyleClass().add("due-soon");
                }
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
