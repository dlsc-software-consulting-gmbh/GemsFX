package com.dlsc.gemsfx.richtextarea;

public class RTTable extends RTElement<RTTable> {

    private RTTableHead head;
    private RTTableBody body;

    private RTTable() {
    }

    public static RTTable create() {
        return new RTTable();
    }

    public RTTable withHead(RTTableHead head) {
        this.head = head;
        return this;
    }

    public RTTable withBody(RTTableBody body) {
        this.body = body;
        return this;
    }

    public RTTableHead getHead() {
        return head;
    }

    public RTTableBody getBody() {
        return body;
    }
}
