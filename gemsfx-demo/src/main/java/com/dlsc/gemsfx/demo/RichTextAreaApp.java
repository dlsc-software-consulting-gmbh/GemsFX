package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.richtextarea.RTDocument;
import com.dlsc.gemsfx.richtextarea.RTHeading;
import com.dlsc.gemsfx.richtextarea.RTLink;
import com.dlsc.gemsfx.richtextarea.RTList;
import com.dlsc.gemsfx.richtextarea.RTListItem;
import com.dlsc.gemsfx.richtextarea.RTParagraph;
import com.dlsc.gemsfx.richtextarea.RTText;
import com.dlsc.gemsfx.richtextarea.RichTextArea;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RichTextAreaApp extends Application {

    @Override
    public void start(Stage stage) {
        RichTextArea area = new RichTextArea();
        area.setDocument(
                RTDocument.create(
                        RTHeading.create("Heading 1"),
                        RTParagraph.create(
                                RTText.create("This is the first paragraph. "),
                                RTText.create("Some text comes here before the link that "),
                                RTLink.create("points to the website ", "https://www.dlsc.com"),
                                RTText.create("of DLSC Software & Consulting.")
                        ),
                        RTParagraph.create(
                                RTText.create("Here comes the second paragraph.")
                        ),
                        RTParagraph.create(),
                        RTHeading.create("Heading 2"),
                        RTParagraph.create(
                                RTText.create("Some text for the first paragraph after heading 2."),
                                RTList.create(
                                        RTListItem.create("List item 1"),
                                        RTListItem.create("List item 2"),
                                        RTListItem.create("List item 3",
                                                RTList.create(
                                                        RTListItem.create("Sub item A"),
                                                        RTListItem.create("Sub item B"),
                                                        RTListItem.create("Sub item C"),
                                                        RTListItem.create("Sub item D")
                                                )
                                        ),
                                        RTListItem.create("List item 4")
                                )
                        )
                )
        );
        Scene scene = new Scene(area);
        stage.setTitle("RichTextArea Demo");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(850);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
