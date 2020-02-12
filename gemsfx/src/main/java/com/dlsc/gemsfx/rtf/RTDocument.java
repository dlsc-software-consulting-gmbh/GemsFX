package com.dlsc.gemsfx.rtf;

public class RTDocument extends RTElementContainer<RTDocument> {

    private RTDocument() {
        super();
    }

    public static RTDocument create(RTElement... elements) {
        return new RTDocument().withElements(elements);
    }
}
