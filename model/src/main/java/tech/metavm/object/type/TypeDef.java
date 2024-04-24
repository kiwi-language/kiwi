package tech.metavm.object.type;

import tech.metavm.entity.Element;
import tech.metavm.entity.IndexDef;
import tech.metavm.entity.SerializeContext;
import tech.metavm.object.type.rest.dto.TypeDefDTO;

public abstract class TypeDef extends Element {

    public static final IndexDef<TypeDef> IDX_ALL_FLAG = IndexDef.create(TypeDef.class, "allFlag");

    public abstract Type getType();

    public abstract TypeDefDTO toDTO(SerializeContext serContext);

}
