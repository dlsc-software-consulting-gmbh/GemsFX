package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.keyboard.Keyboard;
import com.dlsc.gemsfx.keyboard.KeyboardPane;
import com.dlsc.gemsfx.keyboard.KeyboardView;

import javax.xml.bind.JAXBException;

public class KeyboardPaneWithArrowKeysApp extends KeyboardPaneDemoApp {

    @Override
    protected KeyboardPane createKeyboardPane() {
        KeyboardPane pane = super.createKeyboardPane();

        try {
            final Keyboard keyboard = pane.getKeyboardView().loadKeyboard(KeyboardView.class.getResourceAsStream("keyboard-with-arrows-fi.xml"));
            pane.setKeyboardLookupStrategy(node -> new Keyboard[]{keyboard});
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return pane;
    }
}