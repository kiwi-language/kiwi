package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.Element;
import org.metavm.entity.IndexDef;
import org.metavm.entity.SerializeContext;
import org.metavm.object.type.rest.dto.TypeDefDTO;

public abstract class TypeDef extends Element {

    public static final IndexDef<TypeDef> IDX_ALL_FLAG = IndexDef.create(TypeDef.class, "allFlag");

    @SuppressWarnings({"FieldMayBeFinal", "unused"})
    private boolean allFlag = true;

    public abstract @NotNull Type getType();

    public abstract TypeDefDTO toDTO(SerializeContext serContext);

}
