package tech.metavm.flow.rest;

import javax.annotation.Nullable;
import java.util.List;

public record MethodParam(
        boolean isConstructor,
        boolean isAbstract,
        boolean isStatic,
        @Nullable String verticalTemplateId,
        String declaringTypeId,
        String staticType,
        List<MethodRefDTO> overriddenRefs,
        int access
) implements FlowParam {
    @Override
    public int getKind() {
        return 2;
    }
}
