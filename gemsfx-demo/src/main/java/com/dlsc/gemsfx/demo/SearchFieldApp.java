package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.SearchField;
import com.dlsc.gemsfx.util.HistoryManager;
import com.dlsc.gemsfx.util.StringHistoryManager;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * This demo shows how to use the {@link SearchField} control.
 * <p>
 * About the HistoryManager, you can refer to: {@link HistoryManager} {@link SearchTextFieldApp}, {@link HistoryManagerApp}
 */
public class SearchFieldApp extends Application {

    private StringHistoryManager historyManager;

    private final List<Country> countries = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        StringTokenizer st = new StringTokenizer(getCountries(), ",");
        while (st.hasMoreTokens()) {
            countries.add(new Country(st.nextToken()));
        }

        CountriesSearchField field = new CountriesSearchField();
        field.getEditor().setPrefColumnCount(30);
        field.setOnCommit(country -> System.out.println("on commit listener in demo was invoked, country = " + country));

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

        CheckBox hideWithNoChoiceBox = new CheckBox("Hide popup if it has no selectable items in it");
        hideWithNoChoiceBox.selectedProperty().bindBidirectional(field.hidePopupWithNoChoiceProperty());

        CheckBox showSearchIconBox = new CheckBox("Show search icon");
        showSearchIconBox.selectedProperty().bindBidirectional(field.showSearchIconProperty());

        CheckBox showLeftRightNodes = new CheckBox("Show extra left & right nodes");
        showLeftRightNodes.setSelected(false);

        CheckBox autoCommitOnFocusLostBox = new CheckBox("Auto commit on field lost focus.");
        autoCommitOnFocusLostBox.selectedProperty().bindBidirectional(field.autoCommitOnFocusLostProperty());

        CheckBox enableHistoryBox = new CheckBox("Enable History");
        enableHistoryBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (field.getHistoryManager() == null) {
                    historyManager = new StringHistoryManager(Preferences.userNodeForPackage(SearchFieldApp.class), "search-field-id");
                    // Optional: Set the maximum history size. default is 30.
                    historyManager.setMaxHistorySize(20);
                    // Optional: If the history items is empty, we can set a default history list.
                    if (historyManager.getAllUnmodifiable().isEmpty()) {
                        historyManager.set(List.of("United Kingdom", "Switzerland"));
                    }
                }
                // If the history manager is not null, the search field will have a history feature.
                field.setHistoryManager(historyManager);
            } else {
                // If the history manager is null, the search field will not have a history feature.
                field.setHistoryManager(null);
            }
            primaryStage.sizeToScene();
        });
        enableHistoryBox.setSelected(true);

        CheckBox addHistoryOnActionBox = new CheckBox("Add History on Enter");
        addHistoryOnActionBox.setSelected(true);
        field.addingItemToHistoryOnEnterProperty().bind(addHistoryOnActionBox.selectedProperty());

        CheckBox addHistoryOnFocusLossBox = new CheckBox("Add History on Focus Loss");
        addHistoryOnFocusLossBox.setSelected(true);
        field.addingItemToHistoryOnFocusLostProperty().bind(addHistoryOnFocusLossBox.selectedProperty());

        VBox historyControls = new VBox(10, new Separator(), addHistoryOnActionBox, addHistoryOnFocusLossBox);
        historyControls.managedProperty().bind(enableHistoryBox.selectedProperty());
        historyControls.visibleProperty().bind(enableHistoryBox.selectedProperty());

        field.leftProperty().bind(Bindings.createObjectBinding(() -> showLeftRightNodes.isSelected() ? regionLeft : null, showLeftRightNodes.selectedProperty()));
        field.rightProperty().bind(Bindings.createObjectBinding(() -> showLeftRightNodes.isSelected() ? regionRight : null, showLeftRightNodes.selectedProperty()));

        VBox vbox = new VBox(20, createNewItemBox, showPromptText, usePlaceholder, hideWithSingleChoiceBox, hideWithNoChoiceBox, showSearchIconBox, showLeftRightNodes,
                autoCommitOnFocusLostBox, hBox, hBox2, enableHistoryBox, historyControls, field);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox);
        scene.focusOwnerProperty().addListener(it -> System.out.println("owner: " + scene.getFocusOwner()));

        primaryStage.setTitle("Search Field");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();

        CSSFX.start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public class CountriesSearchField extends SearchField<Country> {

        public CountriesSearchField() {
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
            setMatcher((broker, searchText) -> broker.getName().toLowerCase().startsWith(searchText.toLowerCase()));
            setComparator(Comparator.comparing(Country::getName));
            getEditor().setPromptText("Start typing country name ...");
            // Tips: If we don't set a HistoryManager, the search field will not have a history feature.
            // setHistoryManager(new StringHistoryManager(Preferences.userNodeForPackage(SearchFieldApp.class).node("field")));
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
