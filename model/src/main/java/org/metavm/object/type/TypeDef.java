package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.AttributedElement;
import org.metavm.entity.IndexDef;

public abstract class TypeDef extends AttributedElement implements ITypeDef {

    public static final IndexDef<TypeDef> IDX_ALL_FLAG = IndexDef.create(TypeDef.class, "allFlag");

    @SuppressWarnings({"FieldMayBeFinal", "unused"})
    private boolean allFlag = true;

    public abstract @NotNull Type getType();

}
