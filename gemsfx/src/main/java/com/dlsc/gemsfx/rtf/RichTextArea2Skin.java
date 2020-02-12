package com.dlsc.gemsfx.rtf;

import javafx.scene.control.SkinBase;

public class RichTextArea2Skin extends SkinBase<RichTextArea2> {

    public RichTextArea2Skin(RichTextArea2 control) {
        super(control);

        control.documentProperty().addListener(it -> updateView());
        updateView();
    }

    private void updateView() {

    }
}
