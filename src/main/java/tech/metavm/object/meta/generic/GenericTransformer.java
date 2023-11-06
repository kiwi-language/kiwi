package tech.metavm.object.meta.generic;

import tech.metavm.entity.GenericDeclaration;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.expression.ElementTransformer;
import tech.metavm.flow.Flow;
import tech.metavm.flow.ScopeRT;
import tech.metavm.task.FieldData;
import tech.metavm.object.meta.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static tech.metavm.object.meta.TypeUtils.getParameterizedCode;
import static tech.metavm.object.meta.TypeUtils.getParameterizedName;
import static tech.metavm.util.NncUtils.zip;

public class GenericTransformer extends ElementTransformer {

    private TypeArgumentMap typeArgumentMap;
    private final GenericContext context;
    private final Consumer<ClassType> classTransformCallback;
    private final ResolutionStage stage;
    @Nullable
    private final ClassType existing;

    public GenericTransformer(TypeArgumentMap typeArgumentMap, ResolutionStage stage,
                              IEntityContext entityContext,
                              @Nullable ClassType existing
    ) {
        this(
                typeArgumentMap,
                stage,
                new DefaultTypeFactory(ModelDefRegistry::getType),
                null,
                entityContext,
                existing,
                null
        );
    }

    public GenericTransformer(TypeArgumentMap typeArgumentMap,
                              ResolutionStage stage,
                              TypeFactory typeFactory,
                              GenericContext context,
                              IEntityContext entityContext,
                              @Nullable ClassType existing,
                              @Nullable Consumer<ClassType> classTransformCallback) {
        super(typeFactory, entityContext);
        this.context = context;
        this.typeArgumentMap = typeArgumentMap;
        this.stage = stage;
        this.existing = existing;
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
        var typeArgs = NncUtils.map(classType.getEffectiveTypeArguments(), this::transformTypeReference);
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
        if (stage.isAfterOrAt(ResolutionStage.DECLARED)) {
            super.transformClassBody(classType);
        }
    }

    public void substitute(GenericDeclaration genericDeclaration, List<Type> typeArguments) {
        typeArgumentMap = typeArgumentMap.merge(zip(genericDeclaration.getTypeParameters(), typeArguments));
    }

    @Override
    protected void onClassTypeTransformed(ClassType transformedClassType) {
        if (classTransformCallback != null) {
            classTransformCallback.accept(transformedClassType);
        }
    }

    public FieldData transformFieldData(FieldData fieldData) {
        return new FieldData(
                null,
                fieldData.getName(),
                fieldData.getCode(),
                fieldData.getColumn(),
                fieldData.isUnique(),
                fieldData.isAsTitle(),
                currentClass(),
                fieldData.getAccess(),
                transformTypeReference(fieldData.getType()),
                fieldData.isChild(),
                fieldData.isStatic(),
                fieldData.getStaticValue(),
                fieldData.getDefaultValue()
        );
    }

    @Override
    @Nullable
    protected ClassType getExistingClass() {
        return existing;
    }

    @Override
    public Type transformTypeReference(Type typeReference) {
        var transformed = getTransformedType(typeReference);
        if (transformed != null) {
            return transformed;
        }
        if (typeReference instanceof TypeVariable typeVariable) {
            return typeArgumentMap.get(typeVariable);
        }
        var variables = typeReference.getVariables();
        if (NncUtils.anyMatch(variables, v -> typeArgumentMap.get(v) != v)) {
            return transformType(typeReference);
        } else {
            return typeReference;
        }
    }

    @Override
    public Flow transformFlow(Flow flow) {
        var typeArgs = NncUtils.map(flow.getTypeArguments(), this::transformTypeReference);
        List<TypeVariable> typeParams = NncUtils.map(
                flow.getTypeParameters(), this::transformTypeVariable
        );
        return super.transformFlow(flow, flow, flow.getName(), flow.getCode(), typeParams, typeArgs);
    }

    @Override
    public Flow transformFlowReference(ClassType declaringType, Flow flow) {
        if (flow.getTemplate() == null || flow.getTemplate().getDeclaringType() != flow.getDeclaringType()) {
            return super.transformFlowReference(declaringType, flow);
        }
        var typeArgs = NncUtils.map(flow.getTypeArguments(), this::transformTypeReference);
        return context.getParameterizedFlow(flow.getTemplate(), typeArgs);
    }

    @Override
    public ScopeRT transformScope(ScopeRT scope) {
        if (stage.isAfterOrAt(ResolutionStage.GENERATED)) {
            return super.transformScope(scope);
        } else {
            return new ScopeRT(flow(), NncUtils.get(scope.getOwner(), this::getTransformedNode));
        }
    }

    private boolean isResolved(Type type) {
        return switch (type) {
            case PrimitiveType primitiveType -> true;
            case ObjectType objectType -> true;
            case NothingType nothingType -> true;
            case TypeVariable typeVariable -> typeArgumentMap.get(typeVariable) == typeVariable;
            case ClassType classType -> {
                if (classType.getTemplate() == null) {
                    yield classType.getTypeParameters().isEmpty();
                } else {
                    yield NncUtils.allMatch(classType.getTypeArguments(), this::isResolved);
                }
            }
            case CompositeType compositeType -> NncUtils.allMatch(compositeType.getComponentTypes(), this::isResolved);
            case null, default -> throw new InternalException("Invalid type: " + type);
        };
    }

}
