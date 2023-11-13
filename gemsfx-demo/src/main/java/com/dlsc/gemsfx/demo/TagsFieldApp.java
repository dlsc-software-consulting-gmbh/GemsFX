package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.TagsField;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.scenicview.ScenicView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class TagsFieldApp extends Application {

    private final List<Country> countries = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        StringTokenizer st = new StringTokenizer(getCountries(), ",");
        while (st.hasMoreTokens()) {
            countries.add(new Country(st.nextToken()));
        }

        CountriesTagsField field = new CountriesTagsField();
        field.getEditor().setPrefColumnCount(30);

        Region regionLeft = new Region();
        regionLeft.setPrefWidth(30);
        regionLeft.setStyle("-fx-background-color: red;");

        Region regionRight = new Region();
        regionRight.setPrefWidth(30);
        regionRight.setStyle("-fx-background-color: orange;");

        // show selected item
        Label label = new Label("Selected country:");
        Label value = new Label();
        value.textProperty().bind(Bindings.createStringBinding(() -> field.getSelectedItem() != null ? field.getSelectedItem().getName() : "<no selection>", field.selectedItemProperty()));
        HBox hBox = new HBox(10, label, value);

        Label label2 = new Label("Number of suggestions found:");
        Label value2 = new Label();
        value2.textProperty().bind(Bindings.createStringBinding(() -> Integer.toString(field.getSuggestions().size()), field.getSuggestions()));
        HBox hBox2 = new HBox(10, label2, value2);

        Label label2a = new Label("Selected tags indices:");
        Label value2a = new Label();
        value2a.setWrapText(true);
        value2a.textProperty().bind(Bindings.createStringBinding(() -> field.getTagSelectionModel().getSelectedIndices().stream()
                .map(index -> Integer.toString(index))
                .collect(Collectors.joining(", ")), field.getTagSelectionModel().getSelectedIndices()));
        HBox hBox2a = new HBox(10, label2a, value2a);

        Label label3 = new Label("Selected tags list:");
        Label value3 = new Label();
        value3.setWrapText(true);
        value3.textProperty().bind(Bindings.createStringBinding(() -> field.getTagSelectionModel().getSelectedItems().stream()
                .map(item -> field.getConverter().toString(item))
                .collect(Collectors.joining(", ")), field.getTagSelectionModel().getSelectedItems()));
        HBox hBox3 = new HBox(10, label3, value3);

        Label label4 = new Label("Selected tag item:");
        Label value4 = new Label();
        value4.textProperty().bind(Bindings.createStringBinding(() -> field.getTagSelectionModel().getSelectedItem() != null ?
                field.getConverter().toString(field.getTagSelectionModel().getSelectedItem()) : "---", field.getTagSelectionModel().selectedItemProperty()));
        HBox hBox4 = new HBox(10, label4, value4);

        CheckBox createNewItemBox = new CheckBox("Create new country 'on-the-fly' if it can't be found in the data set.");
        field.newItemProducerProperty().bind(Bindings.createObjectBinding(() -> createNewItemBox.isSelected() ? name -> new Country(name) : null, createNewItemBox.selectedProperty()));

        CheckBox showPromptText = new CheckBox("Show prompt text");
        showPromptText.setSelected(true);
        field.getEditor().promptTextProperty().bind(Bindings.createStringBinding(() -> showPromptText.isSelected() ? "Start typing country name ..." : null, showPromptText.selectedProperty()));

        CheckBox usePlaceholder = new CheckBox("Show placeholder when search result is empty");
        usePlaceholder.setSelected(true);
        field.placeholderProperty().bind(Bindings.createObjectBinding(() -> usePlaceholder.isSelected() ? new Label("No countries found") : null, usePlaceholder.selectedProperty()));

        CheckBox hideWithSingleChoiceBox = new CheckBox("Hide popup if it has only the currently selected item in it");
        hideWithSingleChoiceBox.selectedProperty().bindBidirectional(field.hidePopupWithSingleChoiceProperty());

        CheckBox showSearchIconBox = new CheckBox("Show search icon");
        showSearchIconBox.setSelected(true);
        showSearchIconBox.selectedProperty().bindBidirectional(field.showSearchIconProperty());

        CheckBox singleSelectionBox = new CheckBox("Single tag selection");
        singleSelectionBox.selectedProperty().addListener(it -> {
            if (singleSelectionBox.isSelected()) {
                field.getTagSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            } else {
                field.getTagSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            }
        });

        CheckBox showLeftRightNodes = new CheckBox("Show extra left & right nodes");
        showLeftRightNodes.setSelected(false);

        field.leftProperty().bind(Bindings.createObjectBinding(() -> showLeftRightNodes.isSelected() ? regionLeft : null, showLeftRightNodes.selectedProperty()));
        field.rightProperty().bind(Bindings.createObjectBinding(() -> showLeftRightNodes.isSelected() ? regionRight : null, showLeftRightNodes.selectedProperty()));

        TextField textField = new TextField();
        textField.setPromptText("Normal text field ...");

        HBox fieldsBox = new HBox(10, field, textField);
        fieldsBox.setFillHeight(false);
        fieldsBox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(field, Priority.ALWAYS);

        Button scenicViewButton = new Button("Scenic View");
        scenicViewButton.setOnAction(evt -> ScenicView.show(field.getScene()));

        Button generateButton = new Button("Generate Tags");
        generateButton.setOnAction(evt -> {
            int s = countries.size();
            for (int i = 0; i < 5; i++) {
                int index = (int) (Math.random() * s);
                Country country = countries.get(index);
                if (!field.getTags().contains(country)) {
                    field.addTags(country);
                }
            }
        });

        HBox buttonBox = new HBox(10, scenicViewButton, generateButton);

        VBox vbox = new VBox(20, createNewItemBox, showPromptText, usePlaceholder, hideWithSingleChoiceBox, showSearchIconBox, showLeftRightNodes, singleSelectionBox, hBox, hBox2, hBox2a, hBox3, hBox4, buttonBox, field);
        vbox.setPadding(new Insets(20));

        CSSFX.start();

        Scene scene = new Scene(vbox);
        primaryStage.setTitle("Tags Field");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public class CountriesTagsField extends TagsField<Country> {

        public CountriesTagsField() {
            setSuggestionProvider(request -> countries.stream().filter(country -> country.getName().toLowerCase().contains(request.getUserText().toLowerCase())).collect(Collectors.toList()));
            setConverter(new StringConverter<>() {
                @Override
                public String toString(Country country) {
                    if (country != null) {
                        return country.getName();
                    }
                    return "";
                }

                @Override
                public Country fromString(String string) {
                    return new Country(string);
                }
            });
            setMatcher((country, searchText) -> country.getName().toLowerCase().startsWith(searchText.toLowerCase()));
            setComparator(Comparator.comparing(Country::getName));
            getEditor().setPromptText("Start typing country name ...");
        }
    }

    public static class Country {
        private final String name;

        public Country(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private String getCountries() {
        return "Afghanistan," +
                "Albania," +
                "Algeria," +
                "Andorra," +
                "Angola," +
                "Antigua & Deps," +
                "Argentina," +
                "Armenia," +
                "Australia," +
                "Austria," +
                "Azerbaijan," +
                "Bahamas," +
                "Bahrain," +
                "Bangladesh," +
                "Barbados," +
                "Belarus," +
                "Belgium," +
                "Belize," +
                "Benin," +
                "Bhutan," +
                "Bolivia," +
                "Bosnia Herzegovina," +
                "Botswana," +
                "Brazil," +
                "Brunei," +
                "Bulgaria," +
                "Burkina," +
                "Burundi," +
                "Cambodia," +
                "Cameroon," +
                "Canada," +
                "Cape Verde," +
                "Central African Rep," +
                "Chad," +
                "Chile," +
                "China," +
                "Colombia," +
                "Comoros," +
                "Congo," +
                "Congo {Democratic Rep}," +
                "Costa Rica," +
                "Croatia," +
                "Cuba," +
                "Cyprus," +
                "Czech Republic," +
                "Denmark," +
                "Djibouti," +
                "Dominica," +
                "Dominican Republic," +
                "East Timor," +
                "Ecuador," +
                "Egypt," +
                "El Salvador," +
                "Equatorial Guinea," +
                "Eritrea," +
                "Estonia," +
                "Ethiopia," +
                "Fiji," +
                "Finland," +
                "France," +
                "Gabon," +
                "Gambia," +
                "Georgia," +
                "Germany," +
                "Ghana," +
                "Greece," +
                "Grenada," +
                "Guatemala," +
                "Guinea," +
                "Guinea-Bissau," +
                "Guyana," +
                "Haiti," +
                "Honduras," +
                "Hungary," +
                "Iceland," +
                "India," +
                "Indonesia," +
                "Iran," +
                "Iraq," +
                "Ireland," +
                "Israel," +
                "Italy," +
                "Ivory Coast," +
                "Jamaica," +
                "Japan," +
                "Jordan," +
                "Kazakhstan," +
                "Kenya," +
                "Kiribati," +
                "Korea North," +
                "Korea South," +
                "Kosovo," +
                "Kuwait," +
                "Kyrgyzstan," +
                "Laos," +
                "Latvia," +
                "Lebanon," +
                "Lesotho," +
                "Liberia," +
                "Libya," +
                "Liechtenstein," +
                "Lithuania," +
                "Luxembourg," +
                "Macedonia," +
                "Madagascar," +
                "Malawi," +
                "Malaysia," +
                "Maldives," +
                "Mali," +
                "Malta," +
                "Marshall Islands," +
                "Mauritania," +
                "Mauritius," +
                "Mexico," +
                "Micronesia," +
                "Moldova," +
                "Monaco," +
                "Mongolia," +
                "Montenegro," +
                "Morocco," +
                "Mozambique," +
                "Myanmar" +
                "Namibia," +
                "Nauru," +
                "Nepal," +
                "Netherlands," +
                "New Zealand," +
                "Nicaragua," +
                "Niger," +
                "Nigeria," +
                "Norway," +
                "Oman," +
                "Pakistan," +
                "Palau," +
                "Panama," +
                "Papua New Guinea," +
                "Paraguay," +
                "Peru," +
                "Philippines," +
                "Poland," +
                "Portugal," +
                "Qatar," +
                "Romania," +
                "Russian Federation," +
                "Rwanda," +
                "St Kitts & Nevis," +
                "St Lucia," +
                "Saint Vincent & the Grenadines," +
                "Samoa," +
                "San Marino," +
                "Sao Tome & Principe," +
                "Saudi Arabia," +
                "Senegal," +
                "Serbia," +
                "Seychelles," +
                "Sierra Leone," +
                "Singapore," +
                "Slovakia," +
                "Slovenia," +
                "Solomon Islands," +
                "Somalia," +
                "South Africa," +
                "South Sudan," +
                "Spain," +
                "Sri Lanka," +
                "Sudan," +
                "Suriname," +
                "Swaziland," +
                "Sweden," +
                "Switzerland," +
                "Syria," +
                "Taiwan," +
                "Tajikistan," +
                "Tanzania," +
                "Thailand," +
                "Togo," +
                "Tonga," +
                "Trinidad & Tobago," +
                "Tunisia," +
                "Turkey," +
                "Turkmenistan," +
                "Tuvalu," +
                "Uganda," +
                "Ukraine," +
                "United Arab Emirates," +
                "United Kingdom," +
                "United States," +
                "Uruguay," +
                "Uzbekistan," +
                "Vanuatu," +
                "Vatican City," +
                "Venezuela," +
                "Vietnam," +
                "Yemen," +
                "Zambia," +
                "Zimbabwe";
    }
}
