package tech.metavm.object.meta;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexDef;
import tech.metavm.entity.SerializeContext;
import tech.metavm.object.instance.ReferenceKind;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.meta.rest.dto.ArrayTypeParam;
import tech.metavm.util.NncUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@EntityType("数组类型")
public class ArrayType extends CompositeType {

    public static final IndexDef<ArrayType> KEY_IDX = IndexDef.uniqueKey(ArrayType.class, "key");

    public static final IndexDef<ArrayType> ELEMENT_TYPE_IDX = IndexDef.normalKey(ArrayType.class, "elementType");

    @EntityField("元素类型")
    private final Type elementType;

    @EntityField("数组类别")
    private final ArrayKind kind;

    public ArrayType(Long tmpId, Type elementType, ArrayKind kind) {
        super(getArrayTypeName(elementType), false, false, kind.category());
        setTmpId(tmpId);
        this.kind = kind;
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
    protected String getKey() {
        return getKey(elementType, kind);
    }

    @Override
    public Type getConcreteType() {
        return elementType.getConcreteType();
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        if (that instanceof ArrayType arrayType) {
            return kind.isAssignableFrom(arrayType.kind) && elementType.isWithinRange(arrayType.elementType);
        } else {
            return false;
        }
    }

    public Type getElementType() {
        return elementType;
    }

    public Type getInnerMostElementType() {
        return elementType instanceof ArrayType arrayType ? arrayType.getInnerMostElementType() : elementType;
    }

    public int getDimensions() {
        int dim = 1;
        Type type = elementType;
        while (type instanceof ArrayType arrayType) {
            type = arrayType.getElementType();
            dim++;
        }
        return dim;
    }

    @Override
    public Set<ReferencePO> extractReferences(InstancePO instancePO) {
        InstanceArrayPO arrayPO = (InstanceArrayPO) instancePO;
        Set<ReferencePO> refs = new HashSet<>();
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
    protected ArrayTypeParam getParamInternal() {
        try (var context = SerializeContext.enter()) {
            return new ArrayTypeParam(context.getRef(elementType), kind.code());
        }
    }

    @Override
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        return kind.getEntityClass().getName() + "<" + elementType.getCanonicalName(getJavaType) + ">";
    }

    public ArrayKind getKind() {
        return kind;
    }

    public boolean isChildArray() {
        return kind == ArrayKind.CHILD;
    }

    @Override
    public List<Type> getComponentTypes() {
        return List.of(elementType);
    }

    public static String getKey(Type elementType, ArrayKind kind) {
        String key = CompositeType.getKey(List.of(elementType));
        return switch (kind) {
            case READ_WRITE -> key;
            case READ_ONLY -> "r-" + key;
            case CHILD -> "c-" + key;
        };
    }

}
