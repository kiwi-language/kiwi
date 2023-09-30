package tech.metavm.object.meta.generic;

import tech.metavm.entity.GenericDeclaration;
import tech.metavm.expression.ElementTransformer;
import tech.metavm.flow.Flow;
import tech.metavm.flow.ScopeRT;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static tech.metavm.object.meta.TypeUtil.getParameterizedCode;
import static tech.metavm.object.meta.TypeUtil.getParameterizedName;
import static tech.metavm.util.NncUtils.allMatch;
import static tech.metavm.util.NncUtils.zip;

public class GenericTransformer extends ElementTransformer {

    private MetaSubstitutor substitutor;
    private final GenericContext context;
    private final Consumer<ClassType> classTransformCallback;
    private final ResolutionStage stage;

    public GenericTransformer(MetaSubstitutor substitutor,
                              ResolutionStage stage,
                              TypeFactory typeFactory,
                              GenericContext context,
                              @Nullable Consumer<ClassType> classTransformCallback) {
        super(typeFactory);
        this.context = context;
        this.substitutor = substitutor;
        this.stage = stage;
        this.classTransformCallback = classTransformCallback;
    }

    @Override
    public Type transformType(Type type) {
        if (isResolved(type)) {
            return type;
        }
        return super.transformType(type);
    }

    @Override
    public ClassType transformClassType(ClassType classType) {
        var template = classType.getEffectiveTemplate();
        var typeArgs = NncUtils.map(classType.getEffectiveTypeArguments(), this::transformType);
        var templateInst = context.getExisting(template, typeArgs);
        if (templateInst != null) {
            return templateInst;
        }
        substitute(template, typeArgs);
        return transformClassType(
                classType,
                getParameterizedName(template.getName(), typeArgs),
                getParameterizedCode(template.getCode(), typeArgs),
                template,
                typeArgs
        );
    }

    @Override
    protected void transformClassBody(ClassType classType) {
        if(stage.isAfterOrAt(ResolutionStage.DECLARED)) {
            super.transformClassBody(classType);
        }
    }

    public void substitute(GenericDeclaration genericDeclaration, List<Type> typeArguments) {
        substitutor = substitutor.merge(zip(genericDeclaration.getTypeParameters(), typeArguments));
    }

    @Override
    protected void onClassTypeTransformed(ClassType transformedClassType) {
        if (classTransformCallback != null) {
            classTransformCallback.accept(transformedClassType);
        }
    }

    @Override
    public Type transformUnionType(UnionType unionType) {
        if (unionType.isNullable()) {
            return TypeUtil.getNullableType(transformType(unionType.getUnderlyingType()));
        } else {
            throw new InternalException("General union types not supported yet");
        }
    }

    @Override
    public ArrayType transformArrayType(ArrayType arrayType) {
        return TypeUtil.getArrayType(transformType(arrayType.getElementType()), arrayType.getDimensions());
    }

    @Override
    public Type transformTypeVariable(TypeVariable typeVariable) {
        return substitutor.substitute(typeVariable);
    }

    @Override
    public Flow transformFlow(Flow flow) {
        substitute(flow, NncUtils.map(flow.getTypeParameters(), substitutor::substitute));
        return super.transformFlow(flow);
    }

    @Override
    protected Flow transformFlowReference(Flow flow) {
        if(flow.getTemplate() == null) {
            return flow;
        }
        var typeArgs = NncUtils.map(flow.getTypeArguments(), this::transformType);
        if(typeArgs.equals(flow.getTypeArguments())) {
            return flow;
        }
        return context.getParameterizedFlow(flow.getTemplate(), typeArgs);
    }

    @Override
    public ScopeRT transformScope(ScopeRT scope) {
        if(stage.isAfterOrAt(ResolutionStage.GENERATED)) {
            return super.transformScope(scope);
        }
        else {
            return new ScopeRT(flow(), NncUtils.get(scope.getOwner(), this::getTransformedNode));
        }
    }

    private boolean isResolved(Type type) {
        switch (type) {
            case PrimitiveType primitiveType -> {
                return true;
            }
            case TypeVariable typeVariable -> {
                return substitutor.substitute(typeVariable) == typeVariable;
            }
            case ClassType classType -> {
                if (classType.getTemplate() == null) {
                    return classType.getTypeParameters().isEmpty();
                } else {
                    return NncUtils.allMatch(classType.getTypeArguments(), this::isResolved);
                }
            }
            case ArrayType arrayType -> {
                return isResolved(arrayType.getInnerMostElementType());
            }
            case UnionType unionType -> {
                return allMatch(unionType.getMembers(), this::isResolved);
            }
            case null, default -> throw new InternalException("Invalid type: " + type);
        }
    }

}
