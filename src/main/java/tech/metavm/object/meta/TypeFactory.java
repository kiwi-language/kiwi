package tech.metavm.object.meta;

import tech.metavm.dto.RefDTO;
import tech.metavm.entity.DummyGenericDeclaration;
import tech.metavm.entity.Entity;
import tech.metavm.entity.GenericDeclaration;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.Flow;
import tech.metavm.flow.FlowBuilder;
import tech.metavm.flow.Parameter;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.ParameterDTO;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.meta.rest.dto.*;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Null;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class TypeFactory {

    public PrimitiveType createPrimitive(PrimitiveKind kind) {
        var type = new PrimitiveType(kind);
        if (isPutTypeSupported()) {
            putType(kind.getJavaClass(), type);
        }
        return type;
    }

    public ClassType createClassType(TypeDTO typeDTO, IEntityContext context) {
        ClassTypeParam param = typeDTO.getClassParam();
        var type = ClassBuilder.newBuilder(typeDTO.name(), typeDTO.code())
                .category(TypeCategory.getByCode(typeDTO.category()))
                .ephemeral(typeDTO.ephemeral())
                .anonymous(typeDTO.anonymous())
                .isTemplate(param.isTemplate())
                .typeParameters(NncUtils.map(param.typeParameterRefs(), context::getTypeVariable))
                .template(param.templateRef() != null ? context.getClassType(param.templateRef()) : null)
                .source(ClassSource.getByCode(param.source()))
                .tmpId(typeDTO.tmpId()).build();
        context.bind(type);
        return type;
    }

    public TypeVariable saveTypeVariable(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        var param = typeDTO.getTypeVariableParam();
        var context = batch.getContext();
        var type = batch.getContext().getTypeVariable(typeDTO.getRef());
        if (type == null) {
            type = new TypeVariable(typeDTO.tmpId(), typeDTO.name(), typeDTO.code(), DummyGenericDeclaration.INSTANCE);
        } else if (type.getStage().isBeforeOrAt(ResolutionStage.INIT)) {
            type.setName(typeDTO.name());
            type.setCode(typeDTO.code());
        }
        if (stage.isAfterOrAt(ResolutionStage.DECLARATION) && type.getStage().isBefore(ResolutionStage.DECLARATION)) {
//            if (context.isNewEntity(type)) {
//                type.setGenericDeclaration(
//                        context.getEntity(GenericDeclaration.class, param.genericDeclarationRef())
//                );
//            }
//            var genericDecl = context.getEntity(GenericDeclaration.class, param.genericDeclarationRef());
//            type.setGenericDeclaration(genericDecl);
            type.setBounds(NncUtils.map(param.boundRefs(), batch::get));
            type.setStage(ResolutionStage.DECLARATION);
        }
        return type;
    }

    public ArrayType saveArrayType(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        var context = batch.getContext();
        var param = typeDTO.getArrayTypeParam();
        var elementType = context.getType(param.elementTypeRef());
        var kind = ArrayKind.getByCode(param.kind());
        return context.getArrayTypeContext(kind).get(elementType, typeDTO.tmpId());
    }

    public UnionType saveUnionType(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        var context = batch.getContext();
        var param = typeDTO.getUnionParam();
        var members = NncUtils.mapUnique(param.memberRefs(), batch::get);
        return context.getUnionTypeContext().get(members, typeDTO.tmpId());
    }

    public UncertainType saveUncertainType(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        var context = batch.getContext();
        var param = typeDTO.getUncertainTypeParam();
        var lb = batch.get(param.lowerBoundRef());
        var ub = batch.get(param.upperBoundRef());
        return context.getUncertainTypeContext().get(lb, ub, typeDTO.tmpId());
    }

    public FunctionType saveFunctionType(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        var context = batch.getContext();
        var param = typeDTO.getFunctionTypeParam();
        var paramTypes = NncUtils.map(param.parameterTypeRefs(), batch::get);
        var returnType = batch.get(param.returnTypeRef());
        return context.getFunctionTypeContext().get(paramTypes, returnType, typeDTO.tmpId());
    }

    public ClassType saveParameterized(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        var param = typeDTO.getClassParam();
        var template = batch.getClassType(param.templateRef());
        var typeArgs = NncUtils.map(param.typeArgumentRefs(), batch::get);
        return batch.getContext().getGenericContext()
                .getParameterizedType(template, typeArgs, stage, batch);
    }

    public ClassType saveClassType(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        var param = typeDTO.getClassParam();
        if (param.templateRef() != null) {
            return saveParameterized(typeDTO, stage, batch);
        }
        var type = batch.getContext().getClassType(typeDTO.getRef());
        var context = batch.getContext();
        if (type == null) {
            type = ClassBuilder.newBuilder(typeDTO.name(), typeDTO.code())
                    .tmpId(typeDTO.tmpId())
                    .category(TypeCategory.getByCode(typeDTO.category()))
                    .ephemeral(typeDTO.ephemeral())
                    .anonymous(typeDTO.anonymous())
                    .isTemplate(param.isTemplate())
                    .source(ClassSource.getByCode(param.source()))
                    .tmpId(typeDTO.tmpId()).build();
            context.bind(type);
        } else if (type.getStage().isBeforeOrAt(ResolutionStage.INIT)) {
            type.setCode(typeDTO.code());
            type.setName(typeDTO.name());
        }
        if (stage.isBeforeOrAt(ResolutionStage.INIT)) {
            type.setDesc(param.desc());
        }
        if (stage.isAfterOrAt(ResolutionStage.SIGNATURE) && type.getStage().isBefore(ResolutionStage.SIGNATURE)) {
            var typeVars = NncUtils.map(param.typeParameterRefs(), batch::getTypeVariable);
            type.clearTypeParameters();
            for (TypeVariable typeVar : typeVars) {
                if (typeVar.getGenericDeclaration() == DummyGenericDeclaration.INSTANCE)
                    typeVar.setGenericDeclaration(type);
                else
                    throw new InternalException("Type variable " + typeVar + " is not defined in type " + type.getName());
            }
            type.setSuperClass(NncUtils.get(param.superClassRef(), batch::getClassType));
            type.setInterfaces(NncUtils.map(param.interfaceRefs(), batch::getClassType));
            if (!type.isTemplate())
                type.setTypeArguments(NncUtils.map(param.typeArgumentRefs(), context::getType));
            if (param.dependencyRefs() != null)
                type.setDependencies(NncUtils.map(param.dependencyRefs(), context::getClassType));
            type.setStage(ResolutionStage.SIGNATURE);
        }
        if (stage.isAfterOrAt(ResolutionStage.DECLARATION) && type.getStage().isBefore(ResolutionStage.DECLARATION)) {
            var declaringType = type;
            if (param.fields() != null) {
                type.setFields(NncUtils.map(param.fields(), f -> saveField(declaringType, f, context)));
            }
            if (param.staticFields() != null) {
                type.setStaticFields(NncUtils.map(param.staticFields(), f -> saveField(declaringType, f, context)));
            }
            if (param.constraints() != null) {
                type.setConstraints(NncUtils.map(param.constraints(), c -> ConstraintFactory.save(c, context)));
            }
            if (param.flows() != null) {
                type.setFlows(NncUtils.map(param.flows(), f -> saveFlow(f, batch)));
            }
            type.setStage(ResolutionStage.DECLARATION);
        }
        return type;
    }

    public TypeVariable createTypeVariable(TypeDTO typeDTO,
                                           boolean withBounds, IEntityContext context) {
        var param = (TypeVariableParam) typeDTO.param();
        var typeVariable = new TypeVariable(typeDTO.tmpId(), typeDTO.name(), typeDTO.code(),
                context.getEntity(GenericDeclaration.class, param.genericDeclarationRef()));
        if (withBounds) {
            typeVariable.setBounds(NncUtils.map(param.boundRefs(), context::getType));
        }
        context.bind(typeVariable);
        return typeVariable;
    }

    public boolean isNullable(Type type) {
        return type.isNullable();
    }

    public Field saveField(ClassType declaringType, FieldDTO fieldDTO, IEntityContext context) {
        Field field = context.getField(fieldDTO.getRef());
        Type fieldType = context.getType(fieldDTO.typeRef());
        var defaultValue = InstanceFactory.resolveValue(fieldDTO.defaultValue(), fieldType, context);
        var access = Access.getByCodeRequired(fieldDTO.access());
        if (field == null) {
            field = FieldBuilder.newBuilder(fieldDTO.name(), fieldDTO.code(), declaringType, fieldType)
                    .tmpId(fieldDTO.tmpId())
                    .access(access)
                    .unique(fieldDTO.unique())
                    .asTitle(fieldDTO.asTitle())
                    .defaultValue(defaultValue)
                    .isChild(fieldDTO.isChild())
                    .isStatic(fieldDTO.isStatic())
                    .staticValue(InstanceUtils.nullInstance())
                    .build();
            context.bind(field);
        } else {
            field.setName(fieldDTO.name());
            field.setCode(fieldDTO.code());
            field.setAsTitle(fieldDTO.asTitle());
            field.setUnique(fieldDTO.unique());
            field.setType(fieldType);
            field.setDefaultValue(defaultValue);
            field.setAccess(access);
        }
        return field;
    }

    public Flow saveFlow(FlowDTO flowDTO, SaveTypeBatch batch) {
        var context = batch.getContext();
        var flow = context.getFlow(flowDTO.getRef());
        if (flow == null) {
            var declaringType = batch.getClassType(flowDTO.declaringTypeRef());
            flow = FlowBuilder.newBuilder(declaringType, flowDTO.name(), flowDTO.code(), context.getFunctionTypeContext())
                    .build();
        } else {
            flow.setName(flowDTO.name());
            flow.setCode(flowDTO.code());
        }
        flow.setTypeParameters(NncUtils.map(flowDTO.typeParameterRefs(), batch::getTypeVariable));
        flow.setParameters(NncUtils.map(flowDTO.parameters(), paramDTO -> saveParameter(paramDTO, batch)));
        flow.setAbstract(flowDTO.isAbstract());
        flow.setOverridden(NncUtils.map(flowDTO.overriddenRefs(), batch::getFlow));
        flow.setReturnType(batch.get(flowDTO.returnTypeRef()));
        flow.setType(
                context.getFunctionTypeContext().get(
                        flow.getParameterTypes(),
                        flow.getReturnType(),
                        NncUtils.get(flowDTO.typeRef(), RefDTO::tmpId)
                )
        );
        flow.setStaticType(
                context.getFunctionTypeContext().get(
                        NncUtils.prepend(flow.getDeclaringType(), flow.getParameterTypes()),
                        flow.getReturnType(),
                        NncUtils.get(flowDTO.staticTypeRef(), RefDTO::tmpId)
                )
        );
        return flow;
    }

    private Parameter saveParameter(ParameterDTO parameterDTO, SaveTypeBatch batch) {
        var context = batch.getContext();
        var param = context.getEntity(Parameter.class, parameterDTO.getRef());
        if (param == null) {
            param = new Parameter(
                    parameterDTO.tmpId(),
                    parameterDTO.name(),
                    parameterDTO.code(),
                    batch.get(parameterDTO.typeRef()),
                    null,
                    null
            );
        } else {
            param.setName(parameterDTO.name());
            param.setCode(parameterDTO.code());
        }
        return param;
    }

    private Map<String, FlowDTO> getFlowDTOMap(@Nullable TypeDTO typeDTO) {
        Map<String, FlowDTO> flowDTOMap = new HashMap<>();
        if (typeDTO != null && typeDTO.param() instanceof ClassTypeParam param) {
            flowDTOMap.putAll(NncUtils.toMap(param.flows(), FlowDTO::code));
        }
        return flowDTOMap;
    }

    public void putType(java.lang.reflect.Type javaType, Type type) {
        throw new UnsupportedOperationException();
    }

    public void addType(Type type) {
        throw new UnsupportedOperationException();
    }

    public boolean isPutTypeSupported() {
        return false;
    }

    public boolean isAddTypeSupported() {
        return false;
    }

    public PrimitiveType getNullType() {
        return (PrimitiveType) getType(Null.class);
    }

    public PrimitiveType getStringType() {
        return (PrimitiveType) getType(String.class);
    }

    public abstract Type getType(java.lang.reflect.Type javaType);

    public ClassType getClassType(java.lang.reflect.Type javaType) {
        return (ClassType) getType(javaType);
    }

    public java.lang.reflect.Type getJavaType(Type type) {
        throw new UnsupportedOperationException();
    }

    public PrimitiveType getVoidType() {
        return (PrimitiveType) getType(Void.class);
    }

    public PrimitiveType getLongType() {
        return (PrimitiveType) getType(Long.class);
    }

    public ObjectType getObjectType() {
        return (ObjectType) getType(Object.class);
    }

    public ClassType getEntityType() {
        return (ClassType) getType(Entity.class);
    }

    public UnionType createUnion(Set<Type> types) {
        return new UnionType(null, types);
    }

    public PrimitiveType getBooleanType() {
        return (PrimitiveType) getType(Boolean.class);
    }

    public boolean containsJavaType(java.lang.reflect.Type javaType) {
        return getType(javaType) != null;
    }

}
