package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.object.instance.ColumnKind;
import tech.metavm.object.type.rest.dto.ArrayTypeKey;
import tech.metavm.object.type.rest.dto.ArrayTypeParam;
import tech.metavm.object.type.rest.dto.TypeKey;

import java.util.List;
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
    public TypeKey getTypeKey() {
        return new ArrayTypeKey(kind.code(), elementType.getRef());
    }

    @Override
    public Type getConcreteType() {
        return elementType.getConcreteType();
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        if (that instanceof ArrayType arrayType) {
            return kind.isAssignableFrom(arrayType.kind, elementType, arrayType.elementType);
        } else {
            return false;
        }
    }

    @Override
    public ColumnKind getSQLType() {
        if(isChildArray()) {
            return elementType.getSQLType();
        }
        else {
            return super.getSQLType();
        }
    }

    @Override
    public boolean isArray() {
        return true;
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    protected ArrayTypeParam getParamInternal() {
        try (var context = SerializeContext.enter()) {
            return new ArrayTypeParam(context.getRef(elementType), kind.code());
        }
    }

    @Override
    public String getKey(Function<Type, java.lang.reflect.Type> getJavaType) {
        return kind.getEntityClass().getName() + "<" + elementType.getKey(getJavaType) + ">";
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

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitArrayType(this);
    }
}
