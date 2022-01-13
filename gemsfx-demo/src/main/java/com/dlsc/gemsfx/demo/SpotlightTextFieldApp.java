package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.SpotlightTextField;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class SpotlightTextFieldApp extends Application {

    private List<Country> countries = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        StringTokenizer st = new StringTokenizer(getCountries(), ",");
        while (st.hasMoreTokens()) {
            countries.add(new Country(st.nextToken()));
        }

        CountriesSpotlightField field = new CountriesSpotlightField();
        field.setPrefColumnCount(30);

        // show selected item
        Label label = new Label("Selected country:");
        Label value = new Label();
        value.textProperty().bind(Bindings.createStringBinding(() -> field.getSelectedItem() != null ? field.getSelectedItem().getName() : "<no selection>", field.selectedItemProperty()));
        HBox hBox = new HBox(10, label, value);

        VBox vbox = new VBox(20, hBox, field);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public class CountriesSpotlightField extends SpotlightTextField<Country> {

        public CountriesSpotlightField() {
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
            setPlaceholder(new Label("No countries found"));
        }
    }

    public class Country {
        private final String name;

        public Country(String name) {
            this.name = name;
        }

        public String getName() {
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
                "Ireland {Republic}," +
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
                "Myanmar, {Burma}," +
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
