package com.dlsc.gemsfx.richtextarea;

public class RTListItem extends RTElementContainer<RTListItem> {

    private RTListItem() {
    }

    public static RTListItem create() {
        return new RTListItem();
    }

    public static RTListItem create(String text) {
        RTListItem item = create();
        return item.withElements(RTText.create(text));
    }

    public static RTListItem create(String text, RTList sublist) {
        RTListItem item = create(text);
        return item.withElements(sublist);
    }
}
