package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.FunctionRefKey;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.ITypeDef;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeMetadata;
import org.metavm.object.type.rest.dto.GenericDeclarationRefKey;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Entity
public class FunctionRef extends FlowRef {

    @SuppressWarnings("unused")
    private static Klass __klass__;

    public static FunctionRef create(Function rawFlow, List<Type> typeArguments) {
        if(typeArguments.equals(rawFlow.getDefaultTypeArguments()))
            typeArguments = List.of();
        return new FunctionRef(rawFlow, typeArguments);
    }

    public FunctionRef(Function rawFlow, List<? extends Type> typeArguments) {
        super(rawFlow, typeArguments);
    }

    public FunctionRef(Reference functionReference, List<? extends Type> typeArguments) {
        super(functionReference, typeArguments);
    }

    @Override
    public Function getRawFlow() {
        return (Function) super.getRawFlow();
    }

    @Override
    public FunctionRef getParameterized(List<? extends Type> typeArguments) {
        if (typeArguments.equals(getRawFlow().getDefaultTypeArguments()))
            typeArguments = List.of();
        return new FunctionRef(getRawFlow(), typeArguments);
    }

    @Override
    protected TypeMetadata getTypeMetadata0() {
        return getRawFlow().getConstantPool().parameterize(getTypeArguments());
    }

    @Override
    public GenericDeclarationRefKey toGenericDeclarationKey(java.util.function.Function<ITypeDef, Id> getTypeDefId) {
        try(var serContext = SerializeContext.enter()) {
            return toDTO(serContext, typeDef -> Constants.addIdPrefix(getTypeDefId.apply(typeDef).toString()));
        }
    }

    public String toExpression(@Nullable java.util.function.Function<ITypeDef, String> getTypeDefExpr) {
        try(var serContext = SerializeContext.enter()) {
            return toExpression(serContext, getTypeDefExpr);
        }
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable java.util.function.Function<ITypeDef, String> getTypeDefExpr) {
        return "func " +
                (getTypeDefExpr != null ?
                        getTypeDefExpr.apply(getRawFlow()) :
                        Constants.addIdPrefix(serializeContext.getStringId(getRawFlow()))
                )
                + (isParameterized() ?
                        "<" + Utils.join(getTypeArguments(), t -> t.toExpression(serializeContext, getTypeDefExpr)) + ">"
                        : ""
        );
    }

    public FunctionRefKey toDTO(SerializeContext serializeContext, java.util.function.Function<ITypeDef, String> getTypeDefId) {
        return new FunctionRefKey(
                serializeContext.getStringId(getRawFlow()),
                Utils.map(getTypeArguments(), t -> t.toExpression(serializeContext, getTypeDefId))
        );
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.FUNCTION_REF);
        output.writeReference(flowReference);
        output.writeInt(typeArguments.size());
        for (Type typeArgument : typeArguments) {
            typeArgument.write(output);
        }
    }

    public static FunctionRef read(MvInput input) {
        var funcitonReference = input.readReference();
        var typeArgsCount = input.readInt();
        var typeArgs = new ArrayList<Type>(typeArgsCount);
        for (int i = 0; i < typeArgsCount; i++) {
            typeArgs.add(input.readType());
        }
        return new FunctionRef(funcitonReference, typeArgs);
    }

    @Override
    public String getTypeDesc() {
        var name = getRawFlow().getName();
        return typeArguments.isEmpty() ? name : name + "<" + Utils.join(typeArguments, Type::getTypeDesc) + ">";
    }

    @Override
    public String toString() {
        return getTypeDesc();
    }

    public boolean isNative() {
        return getRawFlow().isNative();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionRef(this);
    }

    @Override
    public ClassType getValueType() {
        return __klass__.getType();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }
}
