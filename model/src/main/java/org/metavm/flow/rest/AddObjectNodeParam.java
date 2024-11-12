package org.metavm.flow.rest;

public final class AddObjectNodeParam {
    private final String type;
    private final boolean ephemeral;

    public AddObjectNodeParam(String typeId, boolean ephemeral) {
        this.type = typeId;
        this.ephemeral = ephemeral;
    }

    public String getType() {
        return type;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

}
