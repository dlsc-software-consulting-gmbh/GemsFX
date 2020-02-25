package com.dlsc.gemsfx.richtextarea;

public class RTHeading extends RTTextElement<RTHeading> {

    private int level = 1;

    private RTHeading() {
    }

    public static RTHeading create(String text) {
        return new RTHeading().withText(text);
    }

    public final RTHeading withLevel(int level) {
        this.level = level;
        return this;
    }

    public final int getLevel() {
        return level;
    }
}
