package com.dlsc.gemsfx.rtf;

public class RTHeading extends RTTextElement<RTHeading> {

    private int level = 1;

    private RTHeading() {
    }

    public static RTHeading create(String text) {
        return new RTHeading(text);
    }

    public RTHeading withLevel(int level) {
        this.level = level;
        return this;
    }

    public int getLevel() {
        return level;
    }
}
