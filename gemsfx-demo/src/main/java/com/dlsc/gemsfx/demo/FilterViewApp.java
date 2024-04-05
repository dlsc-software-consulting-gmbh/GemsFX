package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.FilterView;
import com.dlsc.gemsfx.FilterView.Filter;
import com.dlsc.gemsfx.FilterView.FilterGroup;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;

public class FilterViewApp extends Application {

    @Override
    public void start(Stage stage) {
        FilterView<Person> filterView = new FilterView<>();
        filterView.setTitle("Title Here");
        filterView.setSubtitle("Subtitle can be displayed here");
        filterView.setTextFilterProvider(text -> person -> person.getFirstName().toLowerCase().contains(text) || person.getLastName().toLowerCase().contains(text));

        TableView<Person> tableView = new TableView<>();

        FilterGroup<Person> firstNameGroup = new FilterGroup<>("First Name");
        FilterGroup<Person> lastNameGroup = new FilterGroup<>("Last Name");
        FilterGroup<Person> birthdayGroup = new FilterGroup<>("Birthday");
        FilterGroup<Person> roleGroup = new FilterGroup<>("Role");

        firstNameGroup.getFilters().add(new Filter<>("Steve or Jennifer") {
            @Override
            public boolean test(Person person) {
                return switch (person.getFirstName()) {
                    case "Steve", "Jennifer" -> true;
                    default -> false;
                };
            }
        });

        firstNameGroup.getFilters().add(new Filter<>("Paul, Eric") {
            @Override
            public boolean test(Person person) {
                return switch (person.getFirstName()) {
                    case "Paul", "Eric" -> true;
                    default -> false;
                };
            }
        });

        firstNameGroup.getFilters().add(new Filter<>("Elizabeth") {
            @Override
            public boolean test(Person person) {
                return person.getFirstName().equals("Elizabeth");
            }
        });

        lastNameGroup.getFilters().add(new Filter<>("Miller") {
            @Override
            public boolean test(Person person) {
                return person.getLastName().equals("Miller");
            }
        });

        lastNameGroup.getFilters().add(new Filter<>("Smith") {
            @Override
            public boolean test(Person person) {
                return person.getLastName().equals("Smith");
            }
        });

        birthdayGroup.getFilters().add(new Filter<>("1900 - 2100", true) {
            @Override
            public boolean test(Person person) {
                if (person.getBirthday().getYear() < 1900) {
                    return false;
                }
                return person.getBirthday().getYear() <= 2100;
            }
        });

        birthdayGroup.getFilters().add(new Filter<>("1970 - 1980") {
            @Override
            public boolean test(Person person) {
                if (person.getBirthday().getYear() < 1970) {
                    return false;
                }
                return person.getBirthday().getYear() <= 1980;
            }
        });

        birthdayGroup.getFilters().add(new Filter<>("1980 - 1990") {
            @Override
            public boolean test(Person person) {
                if (person.getBirthday().getYear() < 1980) {
                    return false;
                }
                return person.getBirthday().getYear() <= 1990;
            }
        });

        birthdayGroup.getFilters().add(new Filter<>("1990 - 2000") {
            @Override
            public boolean test(Person person) {
                if (person.getBirthday().getYear() < 1990) {
                    return false;
                }
                return person.getBirthday().getYear() <= 2000;
            }
        });

        birthdayGroup.getFilters().add(new Filter<>("2000 - 2010") {
            @Override
            public boolean test(Person person) {
                if (person.getBirthday().getYear() < 2000) {
                    return false;
                }
                return person.getBirthday().getYear() <= 2010;
            }
        });

        roleGroup.getFilters().add(new Filter<>("Parent") {
            @Override
            public boolean test(Person person) {
                return person.getRole().equals("Parent");
            }
        });

        roleGroup.getFilters().add(new Filter<>("Children") {
            @Override
            public boolean test(Person person) {
                return person.getRole().equals("Son") || person.getRole().equals("Daughter");
            }
        });

        filterView.getFilterGroups().setAll(firstNameGroup, lastNameGroup, birthdayGroup, roleGroup);

        SortedList<Person> sortedList = new SortedList<>(filterView.getFilteredItems());
        tableView.setItems(sortedList);
        sortedList.comparatorProperty().bind(tableView.comparatorProperty());

        filterView.getItems().add(new Person("Steve", "Miller", LocalDate.of(1971, 3, 16), "Parent"));
        filterView.getItems().add(new Person("Jennifer", "Miller", LocalDate.of(1975, 1, 12), "Parent"));
        filterView.getItems().add(new Person("Paul", "Miller", LocalDate.of(1996, 11, 6), "Son"));
        filterView.getItems().add(new Person("Eric", "Miller", LocalDate.of(1998, 8, 30), "Son"));
        filterView.getItems().add(new Person("Elizabeth", "Smith", LocalDate.of(2000, 3, 5), "Daughter"));

        TableColumn<Person, String> firstNameColumn = new TableColumn<>("First Name");
        TableColumn<Person, String> lastNameColumn = new TableColumn<>("Last Name");
        TableColumn<Person, LocalDate> birthdayColumn = new TableColumn<>("Birthday");
        TableColumn<Person, String> roleColumn = new TableColumn<>("Role");

        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        birthdayColumn.setCellValueFactory(new PropertyValueFactory<>("birthday"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        tableView.getColumns().setAll(firstNameColumn, lastNameColumn, birthdayColumn, roleColumn);

        VBox box = new VBox(filterView, tableView);
        box.setPadding(new Insets(20));
        box.setSpacing(10);

        VBox.setVgrow(tableView, Priority.ALWAYS);

        Scene scene = new Scene(box);
        stage.setTitle("Filter View Demo");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(850);
        stage.centerOnScreen();
        stage.show();

        CSSFX.start();
    }

    public static class Person {

        private String firstName;
        private String lastName;
        private LocalDate birthday;
        private String role;

        public Person(String firstName, String lastName, LocalDate birthday, String role) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.birthday = birthday;
            this.role = role;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public LocalDate getBirthday() {
            return birthday;
        }

        public void setBirthday(LocalDate birthday) {
            this.birthday = birthday;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
