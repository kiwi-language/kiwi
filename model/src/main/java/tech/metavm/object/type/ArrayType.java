package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.ColumnKind;
import tech.metavm.object.type.rest.dto.ArrayTypeKey;
import tech.metavm.object.type.rest.dto.ArrayTypeParam;
import tech.metavm.object.type.rest.dto.TypeKey;

import javax.annotation.Nullable;
import java.util.List;

@EntityType("数组类型")
public class ArrayType extends CompositeType {

    public static final IndexDef<ArrayType> KEY_IDX = IndexDef.createUnique(ArrayType.class, "key");

    public static final IndexDef<ArrayType> ELEMENT_TYPE_IDX = IndexDef.create(ArrayType.class, "elementType");

    @EntityField("元素类型")
    private final Type elementType;

    @EntityField("数组类别")
    private final ArrayKind kind;

    public ArrayType(Long tmpId, Type elementType, ArrayKind kind) {
        super(getArrayTypeName(elementType, kind), getArrayTypeCode(elementType, kind),
                false, false, kind.category());
        setTmpId(tmpId);
        this.kind = kind;
        this.elementType = elementType;
    }

    private static String getArrayTypeName(Type elementType, ArrayKind kind) {
        if (elementType instanceof UnionType)
            return "(" + elementType.getName() + ")" + kind.getSuffix();
        else
            return elementType.getName() + kind.getSuffix();
    }

    private static @Nullable String getArrayTypeCode(Type elementType, ArrayKind kind) {
        if (elementType.getCode() == null)
            return null;
        if (elementType instanceof UnionType)
            return "(" + elementType.getCode() + ")" + kind.getSuffix();
        else
            return elementType.getCode() + kind.getSuffix();
    }

    @Override
    protected String getKey() {
        return getKey(elementType, kind);
    }

    @Override
    public TypeKey getTypeKey() {
        return new ArrayTypeKey(kind.code(), elementType.getStringId());
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
        if (isChildArray()) {
            return elementType.getSQLType();
        } else {
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
        try (var serContext = SerializeContext.enter()) {
            return new ArrayTypeParam(serContext.getId(elementType), kind.code());
        }
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return kind.getEntityClass().getName() + "<" + context.getModelName(elementType, this) + ">";
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        return kind.getInternalName(elementType, current);
    }

    public ArrayKind getKind() {
        return kind;
    }

    public boolean isChildArray() {
        return kind == ArrayKind.CHILD;
    }

    public Type getInnermostElementType() {
        Type type = elementType;
        while (type instanceof ArrayType arrayType) {
            type = arrayType.elementType;
        }
        return type;
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

    @Override
    public boolean isViewType(Type type) {
        if (super.isViewType(type))
            return true;
        if(type instanceof ArrayType arrayType)
            return elementType.isViewType(arrayType.getElementType());
        return false;
    }
}
