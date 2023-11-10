package tech.metavm.object.meta.generic;

import tech.metavm.entity.ElementTransformer;
import tech.metavm.entity.GenericDeclaration;
import tech.metavm.flow.Flow;
import tech.metavm.flow.ScopeRT;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.rest.dto.ParameterizedTypeKey;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.task.FieldData;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final Map<ParameterizedTypeKey, TypeDTO> tmpIdMap;

    public GenericTransformer(TypeArgumentMap typeArgumentMap,
                              ResolutionStage stage,
                              TypeFactory typeFactory,
                              GenericContext context,
                              @Nullable ClassType existing,
                              Map<ParameterizedTypeKey, TypeDTO> tmpIdMap,
                              @Nullable Consumer<ClassType> classTransformCallback) {
        super(typeFactory, context.getEntityContext());
        this.context = context;
        this.typeArgumentMap = typeArgumentMap;
        this.stage = stage;
        this.tmpIdMap = new HashMap<>(tmpIdMap);
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
        var parameterized = context.getExisting(template, typeArgs);
        if (parameterized == null) {
            parameterized = ClassBuilder
                    .newBuilder(getParameterizedName(template.getName(), typeArgs),
                            getParameterizedCode(template.getCode(), typeArgs))
                    .anonymous(true)
                    .build();
            onClassTypeTransformed(parameterized);
        }
        if (stage.isAfterOrAt(ResolutionStage.SIGNATURE) && classType.getStage().isBefore(ResolutionStage.SIGNATURE)) {
            if (classType.getSubTypes() != null)
                parameterized.setSuperClass((ClassType) transformTypeReference(classType.getSuperClass()));
            parameterized.setInterfaces(
                    NncUtils.map(classType.getInterfaces(), it -> (ClassType) transformTypeReference(it))
            );
            parameterized.setStage(ResolutionStage.SIGNATURE);
        }
        substitute(template, typeArgs);
        transformClassBody(parameterized);
        return parameterized;
//        return transformClassType(
//                classType,
//                NncUtils.get(tmpIdMap.get(new ParameterizedTypeKey(template, typeArgs)), TypeDTO::tmpId),
//                getParameterizedName(template.getName(), typeArgs),
//                getParameterizedCode(template.getCode(), typeArgs),
//                template,
//                typeArgs,
//                true
//        );
    }

    @Override
    protected void transformClassBody(ClassType classType) {
        if (stage.isAfterOrAt(ResolutionStage.DECLARATION)
                && classType.getStage().isBefore(ResolutionStage.DEFINITION)) {
            classType.setStage(ResolutionStage.DECLARATION);
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
        if (stage.isAfterOrAt(ResolutionStage.DEFINITION)) {
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
