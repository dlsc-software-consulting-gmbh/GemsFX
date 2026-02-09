package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ChipView;
import com.dlsc.gemsfx.TagsField;
import com.dlsc.gemsfx.util.SimpleStringConverter;
import com.dlsc.gemsfx.util.StageManager;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class TagsFieldEmailApp extends GemApplication {

    @Override
    public void start(Stage stage) {
        super.start(stage);

        Label toLabel = new Label("To:");
        Label ccLabel = new Label("Copy:");
        Label subjectLabel = new Label("Subject:");
        Label fromLabel = new Label("From:");

        ComboBox<String> fromComboBox = new ComboBox<>();
        fromComboBox.getItems().addAll("dlemmermann@gmail.com", "dlemmermann@me.com", "dlemmermann@icloud.com");

        TextField subjectTextField = new TextField();

        HTMLEditor editor = new HTMLEditor();
        GridPane.setVgrow(editor, Priority.ALWAYS);

        TagsField<EmailAddress> toTagsField = createEmailAddressesTagsField();
        TagsField<EmailAddress> ccTagsField = createEmailAddressesTagsField();

        GridPane gridPane = new GridPane(10, 5);
        gridPane.setPadding(new Insets(20));

        gridPane.add(toLabel, 0, 0, 1, 1);
        gridPane.add(ccLabel, 0, 2, 1, 1);
        gridPane.add(subjectLabel, 0, 4, 1, 1);
        gridPane.add(fromLabel, 0, 6, 1, 1);

        gridPane.add(toTagsField, 1, 0, 3, 1);
        gridPane.add(ccTagsField, 1, 2, 3, 1);
        gridPane.add(subjectTextField, 1, 4, 3, 1);
        gridPane.add(fromComboBox, 1, 6, 3, 1);

        gridPane.add(createSeparator(), 0, 1, 4, 1);
        gridPane.add(createSeparator(), 0, 3, 4, 1);
        gridPane.add(createSeparator(), 0, 5, 4, 1);
        gridPane.add(createSeparator(), 0, 7, 4, 1);

        gridPane.add(editor, 0, 8, 4, 1);

        ColumnConstraints col1 = new ColumnConstraints();

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        ColumnConstraints col3 = new ColumnConstraints();
        ColumnConstraints col4 = new ColumnConstraints();

        gridPane.getColumnConstraints().addAll(col1, col2, col3, col4);

        Scene scene = new Scene(gridPane);

        stage.setTitle("Tags Field Email Demo");
        stage.setScene(scene);
        stage.show();

        StageManager.install(stage, "tags.field.email.demo", 600, 800);
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("small");
        return separator;
    }

    static class EmailAddress {
        String address;

        public EmailAddress(String text) {
            this.address = text;
        }

        public String getAddress() {
            return address;
        }
    }

    private TagsField<EmailAddress> createEmailAddressesTagsField() {
        List<EmailAddress> emailAddresses = generateEmailAddresses();
        TagsField<EmailAddress> tagsField = new TagsField<>();

        tagsField.setMatcher((email, text) -> email.getAddress().toLowerCase().contains(text.toLowerCase()));
        tagsField.setNewItemProducer(text -> {
            if (StringUtils.isNotBlank(text)) {
                return new EmailAddress(text);
            }
            return null;
        });

        tagsField.setConverter(new SimpleStringConverter<>(EmailAddress::getAddress));
        tagsField.setSuggestionProvider(request -> {
            String userText = request.getUserText();
            return new ArrayList<>(emailAddresses.stream().filter(email -> email.getAddress().contains(userText.toLowerCase())).toList());
        });

        return tagsField;
    }

    private List<EmailAddress> generateEmailAddresses() {
        String addresses = "dirk.lemmermann@gmail.com,alex.morrow@fakemail.io,jamie.holt@mockinbox.net,taylor.banks@nowhereemail.com,chris.dawson@phantommail.org,jordan.lee@dummyinbox.co,morgan.frost@pretendmail.net,sam.irwin@fauxpost.io,riley.kent@nullmail.org,casey.blake@imaginary.co,pat.quinn@mockpost.net,drew.hansen@fakemail.io,logan.price@nowhereemail.com,ashley.turner@dummyinbox.co,cameron.wells@phantommail.org,blair.cole@pretendmail.net,robin.miles@fauxpost.io,devon.brooks@mockinbox.net,hayden.scott@nullmail.org,skyler.reed@imaginary.co,parker.young@mockpost.net,jesse.king@fakemail.io,avery.green@nowhereemail.com,quinn.adams@dummyinbox.co,rowan.clark@phantommail.org,emerson.lewis@pretendmail.net,finley.walker@fauxpost.io,dakota.hall@mockinbox.net,remy.allen@nullmail.org,sawyer.wright@imaginary.co,kendall.lopez@mockpost.net,charlie.martin@fakemail.io,bailey.thompson@nowhereemail.com,payton.white@dummyinbox.co,shawn.harris@phantommail.org,lane.moore@pretendmail.net,elliot.jackson@fauxpost.io,noel.robinson@mockinbox.net,spencer.mitchell@nullmail.org,teagan.carter@imaginary.co,marley.evans@mockpost.net,harper.bell@fakemail.io,phoenix.ward@nowhereemail.com,joel.cooper@dummyinbox.co,kira.richards@phantommail.org,micah.bailey@pretendmail.net,sage.howard@fauxpost.io,leo.torres@mockinbox.net,nico.peterson@nullmail.org,arden.gray@imaginary.co,billie.rivera@mockpost.net,max.anderson@fakemail.io,zoe.murphy@nowhereemail.com,ivan.wood@dummyinbox.co,nina.barnes@phantommail.org,oliver.ross@pretendmail.net,luna.hughes@fauxpost.io,benjamin.patel@mockinbox.net,isla.kim@nullmail.org,victor.nguyen@imaginary.co,maya.ortiz@mockpost.net,theo.sullivan@fakemail.io,priya.shah@nowhereemail.com,daniel.fischer@dummyinbox.co,sofia.mendes@phantommail.org,lucas.novak@pretendmail.net,elena.popov@fauxpost.io,marco.rinaldi@mockinbox.net,hana.tanaka@nullmail.org,omar.hassan@imaginary.co,irene.kowalski@mockpost.net,caleb.brown@fakemail.io,natalie.perez@nowhereemail.com,ethan.gold@dummyinbox.co,paula.silva@phantommail.org,rohan.mehta@pretendmail.net,anika.rahman@fauxpost.io,tomas.svensson@mockinbox.net,leila.benali@nullmail.org,yusuf.demir@imaginary.co,magda.nowak@mockpost.net,oscar.lind@fakemail.io,claire.dubois@nowhereemail.com,pablo.ruiz@dummyinbox.co,helen.papadopoulos@phantommail.org,jonas.becker@pretendmail.net,sara.conti@fauxpost.io,milan.jovanovic@mockinbox.net,ines.rodriguez@nullmail.org,abdul.karim@imaginary.co,eva.schmidt@mockpost.net,liam.ocean@fakemail.io,willow.stone@nowhereemail.com,atlas.north@dummyinbox.co,juniper.sky@phantommail.org,river.ember@pretendmail.net,solstice.moon@fauxpost.io,echo.field@mockinbox.net,nova.light@nullmail.org,zen.orbit@imaginary.co,halo.comet@mockpost.net";
        List<EmailAddress> list = new ArrayList<>(Stream.of(addresses.split(",")).map(EmailAddress::new).toList());
        list.sort(Comparator.comparing(EmailAddress::getAddress));
        return list;
    }
}
