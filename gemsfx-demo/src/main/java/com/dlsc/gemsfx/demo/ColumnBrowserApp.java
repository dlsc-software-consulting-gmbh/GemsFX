/**
 * Copyright (C) 2014, 2015 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 * This file is part of FlexGanttFX.
 */
package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.columnbrowser.ColumnBrowser;
import com.dlsc.gemsfx.columnbrowser.ColumnValuesList;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.MasterDetailPane;

import java.util.ArrayList;
import java.util.List;

public class ColumnBrowserApp extends Application {

	public String getSampleName() {
		return "Column Browser";
	}

	private TableView<Person> table;

	private ColumnBrowser<Person> browser;

	@Override
	public void start(Stage stage) throws Exception {
		table = new TableView<>();
		browser = new ColumnBrowser<>(table);

		TableColumn<Person, String> nameColumn = new TableColumn<>("Name");
		TableColumn<Person, String> professionColumn = new TableColumn<>("Profession");
		TableColumn<Person, Integer> ageColumn = new TableColumn<>("Age");
		TableColumn<Person, Gender> genderColumn = new TableColumn<>("Gender");
		TableColumn<Person, Color> colorColumn = new TableColumn<>("Color");

		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		professionColumn.setCellValueFactory(new PropertyValueFactory<>("profession"));
		ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
		genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
		colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));

		colorColumn.setCellFactory(new Callback<TableColumn<Person, Color>, TableCell<Person, Color>>() {

			@Override
			public TableCell<Person, Color> call(TableColumn<Person, Color> param) {
				return new TableCell<Person, Color>() {
					private Region region;

					@Override
					protected void updateItem(Color item, boolean empty) {
						super.updateItem(item, empty);

						if (region == null) {
							region = new Region();
							setGraphic(region);
							setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
						}

						region.setBackground(new Background(new BackgroundFill(item, CornerRadii.EMPTY, Insets.EMPTY)));
					}
				};
			}
		});

		table.getColumns().add(nameColumn);
		table.getColumns().add(professionColumn);
		table.getColumns().add(ageColumn);
		table.getColumns().add(genderColumn);
		table.getColumns().add(colorColumn);

		ColumnValuesList<Person, String> professionList = new ColumnValuesList<>(browser, professionColumn);
		ColumnValuesList<Person, Integer> ageList = new ColumnValuesList<>(browser, ageColumn);
		ColumnValuesList<Person, Gender> genderList = new ColumnValuesList<>(browser, genderColumn);
		ColumnValuesList<Person, Color> colorList = new ColumnValuesList<>(browser, colorColumn);

		colorList.getListView().setCellFactory(new Callback<ListView<Color>, ListCell<Color>>() {

			@Override
			public ListCell<Color> call(ListView<Color> param) {
				return new ListCell<Color>() {
					private Region region;

					@Override
					protected void updateItem(Color item, boolean empty) {
						super.updateItem(item, empty);

						if (region == null) {
							region = new Region();
							setGraphic(region);
							setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
						}

						region.setBackground(new Background(new BackgroundFill(item, CornerRadii.EMPTY, Insets.EMPTY)));
					}
				};
			}
		});

		browser.getColumnValuesLists().add(professionList);
		browser.getColumnValuesLists().add(ageList);
		browser.getColumnValuesLists().add(genderList);
		browser.getColumnValuesLists().add(colorList);

		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(table);

		HBox buttonPane = new HBox();
		buttonPane.setAlignment(Pos.CENTER_RIGHT);
		borderPane.setBottom(buttonPane);

		Button massItems = new Button("Stress");
		massItems.setOnAction(evt -> massItems());
		HBox.setMargin(massItems, new Insets(5));
		buttonPane.getChildren().add(massItems);

		Button removeItems = new Button("Remove");
		removeItems.setOnAction(evt -> removeItems());
		HBox.setMargin(removeItems, new Insets(5));
		buttonPane.getChildren().add(removeItems);

		Button addItems = new Button("Add");
		addItems.setOnAction(evt -> addItem());
		HBox.setMargin(addItems, new Insets(5));
		buttonPane.getChildren().add(addItems);

		MasterDetailPane masterDetailPane = new MasterDetailPane(Side.TOP);
		masterDetailPane.setDetailNode(browser);
		masterDetailPane.setMasterNode(borderPane);

		addItem();
		addItem();
		addItem();
		addItem();

		stage.setScene(new Scene(masterDetailPane));
		stage.setWidth(500);
		stage.setHeight(500);
		stage.centerOnScreen();
		stage.show();
	}

	private int counter = 1;

	private void massItems() {
		List<Person> list = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			list.add(createItem());
		}

		browser.getItems().addAll(list);
	}

	private Person createItem() {
		return new Person("Person " + (counter++), Math.max(20, (int) (Math.random() * 100)), Math.random() < .5 ? Gender.MALE : Gender.FEMALE);
	}

	private void addItem() {
		browser.getItems().add(createItem());
	}

	private void removeItems() {
		for (Person person : table.getSelectionModel().getSelectedItems()) {
			table.getItems().remove(person);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	enum Gender {
		MALE, FEMALE
	}

	public class Person {
		private String name;
		private String profession;
		private Integer age;
		private Gender gender;
		private Color color;

		public Person(String name, int age, Gender gender) {
			this.name = name;
			this.age = age;
			this.gender = gender;

			switch ((int) (Math.random() * 5)) {
			case 0:
				color = Color.ORANGE;
				break;
			case 1:
				color = Color.LIGHTCYAN;
				break;
			case 2:
				color = Color.CORNFLOWERBLUE;
				break;
			case 3:
				color = Color.RED;
				break;
			case 4:
				color = Color.MOCCASIN;
				break;
			}

			switch ((int) (Math.random() * 5)) {
			case 0:
				profession = "Software Developer";
				break;
			case 1:
				profession = "Marketing Manager";
				break;
			case 2:
				profession = "Project Manager";
				break;
			case 3:
				profession = "Requirements Engineer";
				break;
			case 4:
				profession = "Tester";
				break;
			case 5:
				profession = "Presales Consultant";
				break;
			case 6:
				profession = "Architect";
				break;

			}
		}

		public String getName() {
			return name;
		}

		public String getProfession() {
			return profession;
		}

		public Integer getAge() {
			return age;
		}

		public Gender getGender() {
			return gender;
		}

		public Color getColor() {
			return color;
		}
	}
}
