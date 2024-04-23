package com.dlsc.gemsfx.demo.binding;

import com.dlsc.gemsfx.binding.AggregatedListBinding;
import com.dlsc.gemsfx.binding.GeneralAggregatedListBinding;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * This demo shows how to use the {@link AggregatedListBinding} and {@link GeneralAggregatedListBinding} classes to create
 * bindings that aggregate values from a list of objects. In this example, we have a list of students where each student
 * has a list of scores. We create bindings that calculate the total and average score of all students, as well as the
 * number of students who have at least one failing score.
 */
public class AggregatedListBindingApp extends Application {

    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final TableView<Student> tableView = new TableView<>();
    private final Random random = new Random();
    private int index;

    @Override
    public void start(Stage primaryStage) {
        students.add(new Student("Alice", FXCollections.observableArrayList(80, 90, 88)));
        students.add(new Student("Bob", FXCollections.observableArrayList(75, 82, 91)));
        initTableView();

        Label averageLabel = new Label();
        // Creates a binding to calculate the average score of all students. Each student has a list of scores,
        // and this binding computes the average of all these scores across all students. This is achieved by mapping each
        // student's scores to their integer values, calculating the average of these values, and handling cases where
        // there are no scores (using orElse(0.0) to return 0.0 if no scores are present).
        AggregatedListBinding<Student, Integer, Double> averageBinding = new AggregatedListBinding<>(
                students, // the list of students
                Student::getScores, // function to get scores from each student
                stream -> stream.mapToInt(Integer::intValue).average().orElse(0.0) // function to calculate average of scores
        );
        averageLabel.textProperty().bind(averageBinding.asString("Average Score: %.2f"));

        Label sumLabel = new Label();
        // Creates a binding to calculate the total score of all students. This binding sums up all scores
        // from all students. It uses a mapping function to convert scores to integers and then sums them up.
        AggregatedListBinding<Student, Integer, Integer> sumBinding = new AggregatedListBinding<>(
                students, // the list of students
                Student::getScores, // function to get scores from each student
                stream -> stream.mapToInt(Integer::intValue).sum() // function to sum all scores
        );
        sumLabel.textProperty().bind(sumBinding.asString("Total Score: %d"));

        Label averageLabel2 = new Label();
        // This binding calculates a more complex form of average using a pair to hold the sum of scores and the count of scores,
        // allowing the calculation of the average in a subsequent step. It demonstrates a more advanced use of aggregation
        // with intermediary transformations.
        GeneralAggregatedListBinding<Student, Integer, Pair<Integer, Integer>, Double> averageBinding2 = new GeneralAggregatedListBinding<>(
                students, // the list of students
                Student::getScores, // function to extract scores from each student
                scores -> new Pair<>(scores.stream().mapToInt(Integer::intValue).sum(), scores.size()), // maps scores to a pair of sum and count
                results -> {
                    List<Pair<Integer, Integer>> resultsList = results.toList();
                    return resultsList.stream().mapToDouble(Pair::getKey).sum() / resultsList.stream().mapToDouble(Pair::getValue).sum();
                }
        );
        averageLabel2.textProperty().bind(averageBinding2.asString("Average Score: %.2f"));

        Label sumLabel2 = new Label();
        // Creates a binding to calculate the total score of all students using a generalized binding approach that sums up individual results.
        GeneralAggregatedListBinding<Student, Integer, Integer, Integer> sumBinding2 = new GeneralAggregatedListBinding<>(
                students, // the list of students
                Student::getScores, // function to extract scores from each student
                scores -> scores.stream().mapToInt(Integer::intValue).sum(), // function to sum scores of a single student
                results -> results.reduce(0, Integer::sum) // reduces all individual sums into one sum
        );
        sumLabel2.textProperty().bind(sumBinding2.asString("Total Score: %d"));

        // This label displays the number of students who have at least one failing score (<60).
        Label failingCountLabel = new Label();
        // This binding counts how many students have at least one failing score (<60). It demonstrates using a conditional aggregation
        // where each student's score list is evaluated for failing scores, and each failing student contributes '1' to the total count.
        GeneralAggregatedListBinding<Student, Integer, Long, Long> failingCountBinding = new GeneralAggregatedListBinding<>(
                students, // the list of students
                Student::getScores, // function to extract scores from each student
                scores -> scores.stream().anyMatch(score -> score < 60) ? 1L : 0L, // checks if any score is below 60, returns 1 if true, else 0
                results -> results.reduce(0L, Long::sum) // sums up all '1's representing failing students
        );
        failingCountLabel.textProperty().bind(failingCountBinding.asString("Number of Failing Students: %d"));

        Node statistics1 = createStatisticBox("AggregatedListBinding", averageLabel, sumLabel);
        Node statistics2 = createStatisticBox("GeneralAggregatedListBinding", averageLabel2, sumLabel2, failingCountLabel);

        VBox root = new VBox(10, tableView, statistics1, statistics2, createButtonBox());
        root.setStyle("-fx-padding: 10px;");
        Scene scene = new Scene(root);
        primaryStage.setTitle("Student Scores Management");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    private Node createStatisticBox(String title, Node... children) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle(" -fx-font-size: 15px;-fx-text-fill: #9a9999");
        HBox box = new HBox(10, children);
        VBox wrapper = new VBox(15, titleLabel, box);
        wrapper.setStyle("-fx-border-color: lightgray; -fx-border-width: 1px; -fx-padding: 10px; -fx-border-radius: 5px;-fx-background-color: white;");
        return wrapper;
    }

    private HBox createButtonBox() {
        Button addStudentButton = new Button("Add New Student");
        addStudentButton.setOnAction(event -> addNewStudent());

        Button removeStudentButton = new Button("Remove Selected Student");
        removeStudentButton.setOnAction(event -> removeSelectedStudent());
        removeStudentButton.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());

        Button updateStudentButton = new Button("Update Selected Student");
        updateStudentButton.setOnAction(event -> updateSelectedStudentScores());
        updateStudentButton.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        return new HBox(10, addStudentButton, removeStudentButton, updateStudentButton);
    }

    private void initTableView() {
        TableColumn<Student, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setPrefWidth(100);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, String> scoresColumn = new TableColumn<>("Scores");
        scoresColumn.setPrefWidth(280);
        scoresColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getScores().stream().map(String::valueOf).collect(Collectors.joining(", "))
        ));

        tableView.getColumns().addAll(nameColumn, scoresColumn);
        tableView.setItems(students);
    }

    private void addNewStudent() {
        ObservableList<Integer> newScores = FXCollections.observableArrayList(randomScore(), randomScore(), randomScore());
        students.add(new Student("Student " + (++index), newScores));
    }

    private void removeSelectedStudent() {
        Student selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            students.remove(selected);
        }
    }

    private void updateSelectedStudentScores() {
        Student selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            double operation = random.nextInt(0, 3);
            if (operation == 0) {
                ObservableList<Integer> newScores = FXCollections.observableArrayList(randomScore(), randomScore(), randomScore());
                selected.scores.setAll(newScores);
            } else if (operation == 1) {
                selected.scores.add(randomScore());
            } else {
                if (!selected.scores.isEmpty()) {
                    selected.scores.remove(selected.scores.size() - 1);
                } else {
                    ObservableList<Integer> newScores = FXCollections.observableArrayList(randomScore());
                    selected.scores.setAll(newScores);
                }
            }
            tableView.refresh();
        }
    }

    private int randomScore() {
        return random.nextInt(0, 50) + 50;
    }

    public static class Student {
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final ObservableList<Integer> scores;

        public Student(String name, ObservableList<Integer> scores) {
            this.name.set(name);
            this.scores = scores;
        }

        public String getName() {
            return name.get();
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public ObservableList<Integer> getScores() {
            return scores;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
