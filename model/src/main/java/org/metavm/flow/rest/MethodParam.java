package org.metavm.flow.rest;

import javax.annotation.Nullable;

public record MethodParam(
        boolean isConstructor,
        boolean isAbstract,
        boolean isStatic,
        @Nullable String verticalTemplateId,
        String declaringTypeId,
        String staticType,
        int access
) implements FlowParam {
    @Override
    public int getKind() {
        return 2;
    }
}
