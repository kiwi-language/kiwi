package tech.metavm.flow.rest;

import tech.metavm.object.instance.core.Id;

import javax.annotation.Nullable;
import java.util.List;

public record MethodParam(
        boolean isConstructor,
        boolean isAbstract,
        boolean isStatic,
        @Nullable String verticalTemplateId,
        String declaringTypeId,
        String staticTypeId,
        List<String> overriddenIds,
        int access
) implements FlowParam {
    @Override
    public int getKind() {
        return 2;
    }
}
