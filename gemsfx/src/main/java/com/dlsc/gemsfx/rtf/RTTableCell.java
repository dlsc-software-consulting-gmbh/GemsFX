package com.dlsc.gemsfx.rtf;

import javafx.geometry.Pos;

public class RTTableCell extends RTElementContainer<RTTableCell> {

    private int rowSpan = 1;
    private int colSpan = 1;
    private Pos alignment = Pos.TOP_LEFT;

    private RTTableCell() {
    }

    public static RTTableCell create() {
        return new RTTableCell();
    }

    public RTTableCell withRowSpan(int span) {
        this.rowSpan = rowSpan;
        return this;
    }

    public RTTableCell withColSpan(int span) {
        this.colSpan = colSpan;
        return this;
    }

    public RTTableCell withAlignment(Pos alignment) {
        this.alignment = alignment;
        return this;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public int getColSpan() {
        return colSpan;
    }

    public Pos getAlignment() {
        return alignment;
    }
}
