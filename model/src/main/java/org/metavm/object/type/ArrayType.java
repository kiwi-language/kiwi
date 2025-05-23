package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Flow;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.InstanceVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.rest.dto.ArrayTypeKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@Entity
public class ArrayType extends CompositeType {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final Type elementType;

    private final ArrayKind kind;

    public ArrayType(Type elementType, ArrayKind kind) {
        super();
        this.kind = kind;
        this.elementType = elementType;
    }

    private static String getArrayTypeName(Type elementType, ArrayKind kind) {
        if (elementType instanceof UnionType)
            return "(" + elementType.getName() + ")" + kind.getSuffix();
        else
            return elementType.getName() + kind.getSuffix();
    }

    @Override
    public TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return new ArrayTypeKey(kind.code(), elementType.toTypeKey(getTypeDefId));
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
    public boolean isArray() {
        return true;
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        return kind.getInternalName(elementType, current);
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr) {
        var elementExpr = elementType.toExpression(serializeContext, getTypeDefExpr);
        if(elementType.getPrecedence() > getPrecedence())
            elementExpr = "(" + elementExpr + ")";
        return elementExpr + kind.getSuffix().toLowerCase();
    }

    @Override
    public int getTypeKeyCode() {
        return ArrayTypeKey.getTypeKeyCode(kind.code());
    }

    @Override
    public void write(MvOutput output) {
        output.write(ArrayTypeKey.getTypeKeyCode(kind.code()));
        elementType.write(output);
    }

    public static ArrayType read(MvInput input, ArrayKind kind) {
        return new ArrayType(input.readType(), kind);
    }

    public ArrayKind getKind() {
        return kind;
    }

    @Override
    public String getTypeDesc() {
        if(elementType.getPrecedence() > getPrecedence())
            return "(" + elementType.getTypeDesc()  + ")" + kind.getSuffix();
        else
            return elementType.getTypeDesc()  + kind.getSuffix();
    }

    @Override
    public TypeCategory getCategory() {
        return kind.category();
    }

    @Override
    public boolean isEphemeral() {
        return elementType.isEphemeral();
    }

    @Override
    public List<Type> getComponentTypes() {
        return List.of(elementType);
    }

    @Override
    public String getName() {
        return getArrayTypeName(elementType, kind);
    }

    @Override
    public int getTypeTag() {
        return kind.typeTag();
    }

    @Override
    public int getPrecedence() {
        return 1;
    }

    @Override
    public boolean isValueType() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ArrayType that && kind == that.kind && elementType.equals(that.elementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, elementType);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitArrayType(this);
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitArrayType(this, s);
    }

    @Override
    public ClassType getValueType() {
        return __klass__.getType();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        elementType.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        elementType.forEachReference(action);
    }
}
