package com.dlsc.gemsfx.rtf;

public class RTListItem extends RTTextElement<RTListItem> {

    private RTList sublist;

    private RTListItem() {
    }

    public static RTListItem create(String text) {
        return new RTListItem().withText(text);
    }

    public static RTListItem create(String text, RTList sublist) {
        return new RTListItem().withText(text).withSublist(sublist);
    }

    public RTListItem withSublist(RTList list) {
        this.sublist = sublist;
        return this;
    }
}
