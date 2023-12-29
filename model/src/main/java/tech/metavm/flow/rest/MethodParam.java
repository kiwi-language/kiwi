package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

import javax.annotation.Nullable;
import java.util.List;

public record MethodParam(
        boolean isConstructor,
        boolean isAbstract,
        boolean isStatic,
        @Nullable RefDTO verticalTemplateRef,
        RefDTO declaringTypeRef,
        RefDTO staticTypeRef,
        List<RefDTO> overriddenRefs,
        int access
) implements FlowParam {
    @Override
    public int getKind() {
        return 2;
    }
}
