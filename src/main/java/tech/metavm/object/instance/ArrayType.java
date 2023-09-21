package tech.metavm.object.instance;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.entity.natives.ArrayNative;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.object.meta.UnionType;
import tech.metavm.object.meta.rest.dto.ArrayTypeParamDTO;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@EntityType("数组类型")
public class ArrayType extends Type {

    @EntityField("元素类型")
    private final Type elementType;

    public ArrayType(Type elementType) {
        this(elementType, true);
    }

    public ArrayType(Type elementType, boolean ephemeral) {
        super(getArrayTypeName(elementType), false, ephemeral, TypeCategory.ARRAY);
        if (elementType.getCode() != null) {
            setCode(elementType.getCode() + "[]");
        }
        this.elementType = elementType;
    }

    private static String getArrayTypeName(Type elementType) {
        if (elementType instanceof UnionType) {
            return "(" + elementType.getName() + ")[]";
        } else {
            return elementType.getName() + "[]";
        }
    }

    @Override
    public Type getConcreteType() {
        return elementType.getConcreteType();
    }

    @Override
    public boolean isAssignableFrom(Type that) {
        if (that instanceof ArrayType arrayType) {
            return elementType.isAssignableFrom(arrayType.elementType);
        } else {
            return false;
        }
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public List<ReferencePO> extractReferences(InstancePO instancePO) {
        InstanceArrayPO arrayPO = (InstanceArrayPO) instancePO;
        List<ReferencePO> refs = new ArrayList<>();
        boolean isRefType = getElementType().isReference();
        for (Object element : arrayPO.getElements()) {
            NncUtils.invokeIfNotNull(
                    ReferencePO.convertToRefId(element, isRefType),
                    targetId -> refs.add(new ReferencePO(
                            arrayPO.getTenantId(),
                            arrayPO.getId(),
                            targetId,
                            -1L,
                            ReferenceKind.getFromType(elementType).code()
                    ))
            );
        }
        return refs;
    }

    @Override
    protected ArrayTypeParamDTO getParam() {
        try (var context = SerializeContext.enter()) {
            return new ArrayTypeParamDTO(
                    context.getRef(elementType),
                    elementType.toDTO()
            );
        }
    }

    @Override
    public Class<? extends Instance> getInstanceClass() {
        return ArrayInstance.class;
    }

    @Override
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        return Table.class.getName() + "<" + elementType.getCanonicalName(getJavaType) + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayType arrayType = (ArrayType) o;
        return Objects.equals(elementType, arrayType.elementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementType);
    }
}
