package org.metavm.event;

public enum UserEventKind {

    RECEIVE_MESSAGE(1),
    READ_MESSAGE(2),
    JOIN_APP(10),
    LEAVE_APP(11),

    ;

    private final int code;

    UserEventKind(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }


}
