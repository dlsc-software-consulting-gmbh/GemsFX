package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.richtextarea.RichTextArea;
import com.dlsc.gemsfx.richtextarea.model.Document;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RichTextAreaApp extends Application {

    @Override
    public void start(Stage stage) {
        RichTextArea area = new RichTextArea();
        area.setDocument(Document.fromString(createString2()));
        Scene scene = new Scene(area);
        stage.setTitle("RichTextArea Demo");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(850);
        stage.centerOnScreen();
        stage.show();
    }

    private String createString() {
        return "<root>\n" +
                "          <h id=\"1\">Definition</h>\n" +
                "          <ul id=\"2\">\n" +
                "            <li><p><t>Asymptomatische Hypertonie: Hypertonie und beschwerdefreier Patient und ohne klinischen Hinweis auf akuten Endorganschaden</t></p></li>\n" +
                "          </ul>\n" +
                "\n" +
                "          <h id=\"3\">Einteilung</h>\n" +
                "          <ul id=\"4\">\n" +
                "            <li><p><t>Weisskittelhypertonie = Erhöhte Praxisblutdruckwerte, aber normale 24-Stunden- bzw. häusliche Blutdruckwerte</t></p></li>\n" +
                "            <li><p><t>Maskierte Hypertonie = Normaler Praxisblutdruck, aber erhöhte 24-Stunden- bzw. häusliche Blutdruckwerte</t></p></li>\n" +
                "          </ul>\n" +
                "\n" +
                "          <h id=\"5\">Epidemiologie</h>\n" +
                "          <ul id=\"6\">\n" +
                "            <li><p><t>Prävalenz: ca. 30-45% der Allgemeinbevölkerung in Europa</t></p></li>\n" +
                "            <li><p><t>Mit steigendem Alter ist die Prävalenz stark zunehmend</t></p></li>\n" +
                "          </ul>\n" +
                "        </root>";
    }

    private String createString2() {
        return "        <root>\n" +
                "          <h id=\"1\">Definition</h>\n" +
                "          <p id=\"2\">\n" +
                "            <t>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec fringilla, orci eu faucibus maximus, erat mi eleifend justo, sed volutpat arcu eros nec tortor. Ut eget urna in sem tempor semper. In commodo diam tempus enim sollicitudin, vel maximus elit vestibulum. Suspendisse auctor scelerisque dui eu semper. Pellentesque vehicula tempor condimentum. Aliquam erat volutpat. Pellentesque nec risus rhoncus, blandit libero a, porttitor odio.</t>\n" +
                "          </p>\n" +
                "          <h id=\"3\">Einteilung</h>\n" +
                "          <p id=\"4\">\n" +
                "            <t>Quisque porta erat ultrices, congue sem sed, egestas nisl. Suspendisse sit amet eros a dolor viverra aliquam. Cras blandit dapibus nunc a porttitor. Integer fringilla, dui et sagittis blandit, arcu arcu lacinia diam, non pretium nunc quam vel sapien. Nam a accumsan neque. Nunc id commodo nibh. Nulla finibus ut eros quis ultricies. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Integer a tortor id lacus dapibus viverra sed et nibh. Pellentesque ut dui at diam condimentum ultrices ac et est. Etiam a enim quis ex euismod elementum. Vivamus dignissim tellus nec lectus sollicitudin, scelerisque faucibus neque luctus.</t>\n" +
                "          </p>\n" +
                "<p/>" +
                "          <p id=\"5\">\n" +
                "            <t>Nunc viverra risus sit amet ligula bibendum, faucibus ullamcorper ipsum ullamcorper. Curabitur a blandit arcu. Aenean ut dolor ipsum. Vivamus ut porta orci, in finibus neque. Vivamus at dictum augue. Duis ac pulvinar nibh. Nullam scelerisque tellus sit amet risus gravida bibendum. Mauris eget lacinia enim. Duis mattis ac ipsum ac ultricies. Sed aliquam venenatis ipsum viverra commodo. Cras blandit quam nunc, nec interdum nibh laoreet nec. Nulla eu mattis ex.</t>\n" +
                "          </p><p/>\n" +
                "          <p id=\"6\">\n" +
                "            <t>Quisque imperdiet felis dui, at tempor nisl molestie ac. Praesent aliquam orci vitae maximus porttitor. Proin egestas a velit ultricies malesuada. Aliquam sagittis molestie aliquam. Praesent egestas felis at leo eleifend rhoncus. Sed pellentesque nisl tellus, eget fringilla nulla tempus in. Vestibulum et lacus quis arcu volutpat porta. Nunc et dui tortor. Nulla ac urna vel magna finibus sagittis quis ac mauris. Nunc sed nibh tempus, suscipit nisi eu, ultricies tortor. Vivamus sed interdum nisi. Sed dignissim egestas lacinia. Curabitur feugiat id ante at mattis. Integer laoreet id metus in luctus.</t>\n" +
                "          </p>\n" +
                "          <h id=\"7\">Epidemiologie</h>\n" +
                "          <p id=\"8\">\n" +
                "            <t>Nam tempor nisl egestas dui vestibulum scelerisque. Sed venenatis, ante vitae fermentum tristique, justo felis aliquam eros, vel dapibus dolor mauris at quam. Sed ultricies semper lectus, sollicitudin tristique elit. Duis vitae lacinia leo. Nullam rutrum dui ultrices, finibus augue eu, vulputate odio. Suspendisse varius nec massa at tempus. Etiam mollis lorem eu nibh sollicitudin gravida. Nunc posuere nisl iaculis sapien aliquam pretium. Nam a consectetur lorem. In suscipit volutpat auctor. Nam pulvinar neque sem, id malesuada neque suscipit vel. Etiam ut erat iaculis, fringilla odio tempus, pellentesque ante. Nullam vitae ullamcorper metus. Nullam sagittis enim lacus, ut interdum nulla auctor ut. Nam sodales tellus consectetur neque varius, vel consectetur tellus iaculis.</t>\n" +
                "          </p>\n" +
                "        </root>";
    }

    public static void main(String[] args) {
        launch();
    }
}
