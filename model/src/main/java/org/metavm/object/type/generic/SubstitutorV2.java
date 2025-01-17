package org.metavm.object.type.generic;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityUtils;
import org.metavm.entity.GenericDeclarationRef;
import org.metavm.flow.FlowRef;
import org.metavm.flow.FunctionRef;
import org.metavm.flow.LambdaRef;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.DebugEnv;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class SubstitutorV2 extends ElementVisitor<Element> {

    public static SubstitutorV2 create(Object root,
                                       List<TypeVariable> typeParameters,
                                       List<? extends Type> typeArguments,
                                       ResolutionStage stage) {
        return new SubstitutorV2(
                root, typeParameters, typeArguments, null, stage
        );
    }

    private final TypeSubstitutor typeSubstitutor;
    private final ResolutionStage stage;
    private final Map<Object, Object> existingCopies = new IdentityHashMap<>();

    public SubstitutorV2(Object root,
                         List<TypeVariable> typeParameters,
                         List<? extends Type> typeArguments,
                         Object existingRoot,
                         ResolutionStage stage) {
        if (DebugEnv.debugging) {
            log.info("substituting {}, type parameters: {}, type arguments: {}, stage: {}",
                    EntityUtils.getEntityDesc(root), Utils.map(typeParameters, TypeVariable::getTypeDesc),
                    Utils.map(typeArguments, Type::getTypeDesc), stage.name());
        }
        this.typeSubstitutor = new TypeSubstitutor(Utils.map(typeParameters, TypeVariable::getType), typeArguments);
        this.stage = stage;
        if (existingRoot != null) {
            addExistingCopy(root, existingRoot);
        }
    }

    protected @Nullable Object getExistingCopy(Object object) {
        return existingCopies.get(object);
    }

    @Override
    public Element visitConstantPool(ConstantPool constantPool) {
        var copy = (ConstantPool) Objects.requireNonNull(getExistingCopy(constantPool));
        copy.setStage(stage);
        copy.clear();
        int i = 0;
        for (var entry : constantPool.getEntries()) {
            if(entry instanceof Element e)
                copy.addEntry((Value) e.accept(this));
            else
                copy.addEntry(entry);
            i++;
        }
        copy.onLoadPrepare();
        copy.onLoad();
        return copy;
    }

    private void addExistingCopy(Object original, Object copy) {
        existingCopies.put(original, copy);
    }

    @Override
    public Element visitMethodRef(MethodRef methodRef) {
        return MethodRef.create(
                (ClassType) methodRef.getDeclaringType().accept(this),
                methodRef.getRawFlow(),
                Utils.map(methodRef.getTypeArguments(), t -> (Type) t.accept(this))
        );
    }

    @Override
    public Element visitKlassType(KlassType type) {
        return KlassType.create(
                (GenericDeclarationRef) Utils.safeCall(type.getOwner(), k -> k.accept(this)),
                type.getKlass(),
                Utils.map(type.getTypeArguments(), t -> (Type) t.accept(this))
        );
    }

    @Override
    public Element visitFunctionRef(FunctionRef functionRef) {
        return FunctionRef.create(functionRef.getRawFlow(),
                Utils.map(functionRef.getTypeArguments(), t -> (Type) t.accept(this)));
    }

    @Override
    public Element visitFieldRef(FieldRef fieldRef) {
        return new FieldRef(
                (ClassType) fieldRef.getDeclaringType().accept(this),
                fieldRef.getRawField()
        );
    }

    @Override
    public Element visitUncertainType(UncertainType type) {
        return new UncertainType(
                (Type) type.getLowerBound().accept(this),
                (Type) type.getUpperBound().accept(this)
        );
    }

    @Override
    public Element visitUnionType(UnionType type) {
        var members = Utils.mapToSet(type.getMembers(), t -> (Type) t.accept(this));
        if(members.size() == 1)
            return members.iterator().next();
        else
            return new UnionType(members);
    }


    @Override
    public Element visitIntersectionType(IntersectionType type) {
        var members = Utils.mapToSet(type.getTypes(), t -> (Type) t.accept(this));
        if(members.size() == 1)
            return members.iterator().next();
        else
            return new IntersectionType(members);
    }

    @Override
    public Element visitArrayType(ArrayType type) {
        return new ArrayType((Type) type.getElementType().accept(this), type.getKind());
    }

    @Override
    public Element visitFunctionType(FunctionType type) {
        return new FunctionType(
                Utils.map(type.getParameterTypes(), t -> (Type) t.accept(this)),
                (Type) type.getReturnType().accept(this)
        );
    }

    @Override
    public Element visitVariableType(VariableType type) {
        return Objects.requireNonNullElse(typeSubstitutor.getVariableMap().get(type), type);
    }

    @Override
    public Element visitLambdaRef(LambdaRef lambdaRef) {
        return new LambdaRef(
                (FlowRef) lambdaRef.getFlowRef().accept(this),
                lambdaRef.getRawLambda()
        );
    }

    @Override
    public Element visitIndexRef(IndexRef indexRef) {
        return new IndexRef(
                (ClassType) indexRef.getDeclaringType().accept(this),
                indexRef.getRawIndex()
        );
    }

    @Override
    public Element visitElement(Element element) {
        return element;
    }

    @Override
    public Element visitType(Type type) {
        return type;
    }

}
