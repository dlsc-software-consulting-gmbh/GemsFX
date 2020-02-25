package com.dlsc.gemsfx.richtextarea;

public class RTTable extends RTElement<RTTable> {

    private RTTableHead head;
    private RTTableBody body;

    private RTTable() {
    }

    public static RTTable create() {
        return new RTTable();
    }

    public final RTTable withHead(RTTableHead head) {
        this.head = head;
        return this;
    }

    public final RTTable withBody(RTTableBody body) {
        this.body = body;
        return this;
    }

    public final RTTableHead getHead() {
        return head;
    }

    public final RTTableBody getBody() {
        return body;
    }
}
