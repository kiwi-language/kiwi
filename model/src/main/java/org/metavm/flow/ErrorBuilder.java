package org.metavm.flow;

import javax.annotation.Nullable;

public class ErrorBuilder {

    private final StringBuilder builder = new StringBuilder();

    public void addError(String error) {
        if (builder.isEmpty()) {
            builder.append(error);
        } else
            builder.append(", ").append(error);
    }

    @Nullable
    public String getMessage() {
        return builder.isEmpty() ? null : builder.toString();
    }

}
