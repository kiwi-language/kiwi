package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.flow.*;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.FunctionParam;
import tech.metavm.flow.rest.MethodParam;
import tech.metavm.flow.rest.ParameterDTO;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Null;

import javax.annotation.Nullable;
import java.util.*;

public abstract class TypeFactory {

    public PrimitiveType createPrimitive(PrimitiveKind kind) {
        var type = new PrimitiveType(kind);
        if (isPutTypeSupported())
            putType(kind.getJavaClass(), type);
        return type;
    }

    public TypeVariable saveTypeVariable(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try(var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveTypeVariable")) {
            var param = typeDTO.getTypeVariableParam();
            var context = batch.getContext();
            var type = batch.getContext().getTypeVariable(typeDTO.getRef());
            if (type == null) {
                type = new TypeVariable(typeDTO.tmpId(), typeDTO.name(), typeDTO.code(), DummyGenericDeclaration.INSTANCE);
                context.bind(type);
            } else if (type.getStage().isBeforeOrAt(ResolutionStage.INIT)) {
                type.setName(typeDTO.name());
                type.setCode(typeDTO.code());
            }
            var curStage = type.setStage(stage);
            if (stage.isAfterOrAt(ResolutionStage.DECLARATION) && curStage.isBefore(ResolutionStage.DECLARATION))
                type.setBounds(NncUtils.map(param.boundRefs(), batch::get));
            return type;
        }
    }

    public ArrayType saveArrayType(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try(var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveArrayType")) {
            var context = batch.getContext();
            var param = typeDTO.getArrayTypeParam();
            var elementType = context.getType(param.elementTypeRef());
            var kind = ArrayKind.getByCode(param.kind());
            return context.getArrayTypeContext(kind).get(elementType, typeDTO.tmpId());
        }
    }

    public UnionType saveUnionType(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try(var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveUnionType")) {
            var context = batch.getContext();
            var param = typeDTO.getUnionParam();
            var members = NncUtils.mapUnique(param.memberRefs(), batch::get);
            return context.getUnionTypeContext().getUnionType(members, typeDTO.tmpId());
        }
    }

    public UncertainType saveUncertainType(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try(var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveUncertainType")) {
            var context = batch.getContext();
            var param = typeDTO.getUncertainTypeParam();
            var lb = batch.get(param.lowerBoundRef());
            var ub = batch.get(param.upperBoundRef());
            return context.getUncertainTypeContext().getUncertainType(lb, ub, typeDTO.tmpId());
        }
    }

    public FunctionType saveFunctionType(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try(var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveFunctionType")) {
            var context = batch.getContext();
            var param = typeDTO.getFunctionTypeParam();
            var paramTypes = NncUtils.map(param.parameterTypeRefs(), batch::get);
            var returnType = batch.get(param.returnTypeRef());
            return context.getFunctionTypeContext().getFunctionType(paramTypes, returnType, typeDTO.tmpId());
        }
    }

    public ClassType saveParameterized(PTypeDTO pTypeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try(var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveParameterized")) {
            var template = batch.getClassType(pTypeDTO.getTemplateRef());
            var typeArgs = NncUtils.map(pTypeDTO.getTypeArgumentRefs(), batch::get);
            return batch.getContext().getGenericContext()
                    .getParameterizedType(template, typeArgs, stage, batch);
        }
    }

    public ClassType saveClassType(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try(var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveClassType")) {
            var param = typeDTO.getClassParam();
            var type = batch.getContext().getClassType(typeDTO.getRef());
            var context = batch.getContext();
            if (type == null) {
                type = ClassTypeBuilder.newBuilder(typeDTO.name(), typeDTO.code())
                        .tmpId(typeDTO.tmpId())
                        .category(TypeCategory.getByCode(typeDTO.category()))
                        .ephemeral(typeDTO.ephemeral())
                        .anonymous(typeDTO.anonymous())
                        .typeParameters(NncUtils.map(param.typeParameterRefs(), batch::getTypeVariable))
                        .isTemplate(param.isTemplate())
                        .isAbstract(param.isAbstract())
                        .struct(param.struct())
                        .desc(param.desc())
                        .source(ClassSource.getByCode(param.source()))
                        .tmpId(typeDTO.tmpId())
                        .build();
                context.bind(type);
            } else if (type.getStage().isBeforeOrAt(ResolutionStage.INIT)) {
                type.setCode(typeDTO.code());
                type.setName(typeDTO.name());
                type.setDesc(param.desc());
                type.setStruct(param.struct());
                type.setAbstract(param.isAbstract());
            }
            var curStage = type.setStage(stage);
            if (stage.isAfterOrAt(ResolutionStage.SIGNATURE) && curStage.isBefore(ResolutionStage.SIGNATURE)) {
                if (type.isEnum()) {
                    var enumSuperClass = context.getGenericContext().getParameterizedType(
                            StandardTypes.getEnumType(), List.of(type), ResolutionStage.DEFINITION, batch
                    );
                    type.setSuperClass(enumSuperClass);
                } else
                    type.setSuperClass(NncUtils.get(param.superClassRef(), batch::getClassType));
                type.setInterfaces(NncUtils.map(param.interfaceRefs(), batch::getClassType));
                if (!type.isTemplate())
                    type.setTypeArguments(NncUtils.map(param.typeArgumentRefs(), context::getType));
                if (param.dependencyRefs() != null)
                    type.setDependencies(NncUtils.map(param.dependencyRefs(), context::getClassType));
                type.setStage(ResolutionStage.SIGNATURE);
            }
            if (stage.isAfterOrAt(ResolutionStage.DECLARATION) && curStage.isBefore(ResolutionStage.DECLARATION)) {
                var declaringType = type;
                if (param.fields() != null)
                    type.setFields(NncUtils.map(param.fields(), f -> saveField(declaringType, f, context)));
                if (param.staticFields() != null)
                    type.setStaticFields(NncUtils.map(param.staticFields(), f -> saveField(declaringType, f, context)));
                if (param.constraints() != null)
                    type.setConstraints(NncUtils.map(param.constraints(), c -> ConstraintFactory.save(c, context)));
                if (param.flows() != null)
                    type.setMethods(NncUtils.map(param.flows(), f -> saveMethod(f, stage, batch)));
                if (param.titleFieldRef() != null)
                    type.setTitleField(NncUtils.find(type.getFields(), f -> f.getRef().equals(param.titleFieldRef())));
                type.setStage(ResolutionStage.DECLARATION);
            }
            return type;
        }
    }

    public TypeVariable createTypeVariable(TypeDTO typeDTO,
                                           boolean withBounds, IEntityContext context) {
        var param = (TypeVariableParam) typeDTO.param();
        var typeVariable = new TypeVariable(typeDTO.tmpId(), typeDTO.name(), typeDTO.code(),
                Objects.requireNonNull(context.getEntity(GenericDeclaration.class, param.genericDeclarationRef())));
        if (withBounds)
            typeVariable.setBounds(NncUtils.map(param.boundRefs(), context::getType));
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
        var access = Access.getByCode(fieldDTO.access());
        if (field == null) {
            field = FieldBuilder.newBuilder(fieldDTO.name(), fieldDTO.code(), declaringType, fieldType)
                    .tmpId(fieldDTO.tmpId())
                    .access(access)
                    .unique(fieldDTO.unique())
                    .defaultValue(defaultValue)
                    .isChild(fieldDTO.isChild())
                    .isStatic(fieldDTO.isStatic())
                    .staticValue(Instances.nullInstance())
                    .build();
            context.bind(field);
        } else {
            field.setName(fieldDTO.name());
            field.setCode(fieldDTO.code());
            field.setUnique(fieldDTO.unique());
            field.setType(fieldType);
            field.setDefaultValue(defaultValue);
            field.setAccess(access);
        }
        return field;
    }

    public Method saveMethod(FlowDTO flowDTO, ResolutionStage stage, SaveTypeBatch batch) {
        var context = batch.getContext();
        var flow = context.getMethod(flowDTO.getRef());
        var param  = (MethodParam) flowDTO.param();
        if (flow == null) {
            var declaringType = batch.getClassType(param.declaringTypeRef());
            flow = MethodBuilder.newBuilder(declaringType, flowDTO.name(), flowDTO.code(), context.getFunctionTypeContext())
                    .access(Access.findByCode(param.access()))
                    .isStatic(param.isStatic())
                    .tmpId(flowDTO.tmpId())
                    .build();
            context.bind(flow);
        } else {
            flow.setName(flowDTO.name());
            flow.setCode(flowDTO.code());
        }
        flow.setNative(flowDTO.isNative());
        flow.setAbstract(param.isAbstract());
        flow.setConstructor(param.isConstructor());
        flow.setTypeParameters(NncUtils.map(flowDTO.typeParameterRefs(), batch::getTypeVariable));
        flow.update(
                NncUtils.map(flowDTO.parameters(), paramDTO -> saveParameter(paramDTO, batch)),
                batch.get(flowDTO.returnTypeRef()),
                NncUtils.map(param.overriddenRefs(), batch::getMethod),
                context.getFunctionTypeContext()
        );
        flow.setAbstract(param.isAbstract());
//        if(flowDTO.horizontalInstances() != null) {
//            for (FlowDTO templateInstance : flowDTO.horizontalInstances()) {
//                context.getGenericContext().getParameterizedFlow(
//                        flow,
//                        NncUtils.map(templateInstance.typeArgumentRefs(), batch::get),
//                        stage,
//                        batch
//                );
//            }
//        }
        return flow;
    }

    public Function saveFunction(FlowDTO flowDTO, ResolutionStage stage, SaveTypeBatch batch) {
        NncUtils.requireTrue(flowDTO.param() instanceof FunctionParam);
        var context = batch.getContext();
        var function = context.getFunction(flowDTO.getRef());
        if (function == null) {
            function = FunctionBuilder.newBuilder(flowDTO.name(), flowDTO.code(), context.getFunctionTypeContext())
                    .tmpId(flowDTO.tmpId())
                    .build();
            context.bind(function);
        } else {
            function.setName(flowDTO.name());
            function.setCode(flowDTO.code());
        }
        function.setNative(flowDTO.isNative());
        function.setTypeParameters(NncUtils.map(flowDTO.typeParameterRefs(), batch::getTypeVariable));
        function.update(
                NncUtils.map(flowDTO.parameters(), paramDTO -> saveParameter(paramDTO, batch)),
                batch.get(flowDTO.returnTypeRef()),
                context.getFunctionTypeContext()
        );
//        if(flowDTO.horizontalInstances() != null) {
//            for (FlowDTO templateInstance : flowDTO.horizontalInstances()) {
//                context.getGenericContext().getParameterizedFlow(
//                        function,
//                        NncUtils.map(templateInstance.typeArgumentRefs(), batch::get),
//                        stage,
//                        batch
//                );
//            }
//        }
        return function;
    }

    private Parameter saveParameter(ParameterDTO parameterDTO, SaveTypeBatch batch) {
        var context = batch.getContext();
        var param = context.getEntity(Parameter.class, parameterDTO.getRef());
        if (param == null) {
            param = new Parameter(
                    parameterDTO.tmpId(),
                    parameterDTO.name(),
                    parameterDTO.code(),
                    batch.get(parameterDTO.typeRef())
            );
        } else {
            param.setName(parameterDTO.name());
            param.setCode(parameterDTO.code());
        }
        return param;
    }

    private Map<String, FlowDTO> getFlowDTOMap(@Nullable TypeDTO typeDTO) {
        Map<String, FlowDTO> flowDTOMap = new HashMap<>();
        if (typeDTO != null && typeDTO.param() instanceof ClassTypeParam param)
            flowDTOMap.putAll(NncUtils.toMap(param.flows(), FlowDTO::code));
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

    public AnyType getObjectType() {
        return (AnyType) getType(Object.class);
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
