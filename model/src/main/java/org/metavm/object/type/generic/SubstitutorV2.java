package org.metavm.object.type.generic;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.CopyVisitor;
import org.metavm.entity.Element;
import org.metavm.entity.EntityUtils;
import org.metavm.entity.GenericDeclarationRef;
import org.metavm.flow.MethodRef;
import org.metavm.object.type.*;
import org.metavm.util.DebugEnv;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class SubstitutorV2 extends CopyVisitor {

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
        super(root, true);
        if (DebugEnv.debugging) {
            log.debug("substituting {}, type parameters: {}, type arguments: {}, stage: {}",
                    EntityUtils.getEntityDesc(root), NncUtils.map(typeParameters, TypeVariable::getTypeDesc),
                    NncUtils.map(typeArguments, Type::getTypeDesc), stage.name());
        }
        this.typeSubstitutor = new TypeSubstitutor(NncUtils.map(typeParameters, TypeVariable::getType), typeArguments);
        this.stage = stage;
        if (existingRoot != null) {
            addExistingCopy(root, existingRoot);
        }
    }

    @Override
    protected @Nullable Object getExistingCopy(Object object) {
        return existingCopies.get(object);
    }

    @Override
    protected Object allocateCopy(Object entity) {
        return super.allocateCopy(entity);
    }

    @Override
    public Element visitConstantPool(ConstantPool constantPool) {
        var copy = (ConstantPool) Objects.requireNonNull(getExistingCopy(constantPool));
        copy.setStage(stage);
        copy.clear();
        for (CpEntry entry : constantPool.getEntries()) {
            copy.addEntry((CpEntry) copy0(entry));
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
                NncUtils.map(methodRef.getTypeArguments(), t -> (Type) t.accept(this))
        );
    }

    @Override
    public Element visitClassType(ClassType type) {
        return ClassType.create(
                (GenericDeclarationRef) NncUtils.get(type.getOwner(), k -> k.accept(this)),
                type.getKlass(),
                NncUtils.map(type.getTypeArguments(), t -> (Type) t.accept(this))
        );
    }

    @Override
    public Element visitVariableType(VariableType type) {
        return Objects.requireNonNullElse(typeSubstitutor.getVariableMap().get(type), type);
    }

    @Override
    public Element visitPrimitiveType(PrimitiveType type) {
        return type;
    }

    @Override
    public Element visitAnyType(AnyType type) {
        return type;
    }

    @Override
    public Element visitNeverType(NeverType type) {
        return type;
    }

    @Override
    public Element visitType(Type type) {
        return Objects.requireNonNullElseGet(typeSubstitutor.getVariableMap().get(type),
                () -> super.visitType(type));
    }

}
