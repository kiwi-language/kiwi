package tech.metavm.object.type;

import tech.metavm.entity.DummyGenericDeclaration;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.StandardTypes;
import tech.metavm.flow.*;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.FunctionParam;
import tech.metavm.flow.rest.MethodParam;
import tech.metavm.flow.rest.ParameterDTO;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TypeFactory {

    public PrimitiveType createPrimitive(PrimitiveKind kind) {
        var type = new PrimitiveType(kind);
        if (isPutTypeSupported())
            putType(kind.getJavaClass(), type);
        return type;
    }

    public TypeVariable saveTypeVariable(TypeVariableDTO typeVariableDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try (var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveTypeVariable")) {
            var context = batch.getContext();
            var type = batch.getContext().getTypeVariable(Id.parse(typeVariableDTO.id()));
            if (type == null) {
                type = new TypeVariable(typeVariableDTO.tmpId(), typeVariableDTO.name(), typeVariableDTO.code(), DummyGenericDeclaration.INSTANCE);
                context.bind(type);
            } else if (type.getStage().isBeforeOrAt(ResolutionStage.INIT)) {
                type.setName(typeVariableDTO.name());
                type.setCode(typeVariableDTO.code());
            }
            var curStage = type.setStage(stage);
            if (stage.isAfterOrAt(ResolutionStage.DECLARATION) && curStage.isBefore(ResolutionStage.DECLARATION))
                type.setBounds(NncUtils.map(typeVariableDTO.bounds(), bound -> TypeParser.parse(bound, batch.getContext())));
            return type;
        }
    }

    public ArrayType saveArrayType(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try (var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveArrayType")) {
            var context = batch.getContext();
            var param = typeDTO.getArrayTypeParam();
            var elementType = context.getType(Id.parse(param.elementTypeId()));
            var kind = ArrayKind.fromCode(param.kind());
            return context.getArrayTypeContext(kind).get(elementType, typeDTO.tmpId());
        }
    }

    /*public UnionType saveUnionType(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try (var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveUnionType")) {
            var context = batch.getContext();
            var param = typeDTO.getUnionParam();
            var members = NncUtils.mapUnique(param.memberIds(), batch::get);
            return context.getUnionTypeContext().getUnionType(members, typeDTO.tmpId());
        }
    }*/

    /*public UncertainType saveUncertainType(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try (var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveUncertainType")) {
            var context = batch.getContext();
            var param = typeDTO.getUncertainTypeParam();
            var lb = batch.get(param.lowerBoundId());
            var ub = batch.get(param.upperBoundId());
            return context.getUncertainTypeContext().getUncertainType(lb, ub, typeDTO.tmpId());
        }
    }*/

    public CapturedTypeVariable saveCapturedTypeVariable(CapturedTypeVariableDTO capturedTypeVariableDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try (var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveCapturedType")) {
            var context = batch.getContext();
            var type = context.getCapturedTypeVariable(capturedTypeVariableDTO.id());
            if (type == null) {
                var uncertainType = (UncertainType) TypeParser.parse(capturedTypeVariableDTO.uncertainType(), context);
                type = new CapturedTypeVariable(
                        uncertainType,
                        DummyCapturedTypeScope.INSTANCE
                );
                if (capturedTypeVariableDTO.tmpId() != null)
                    type.setTmpId(capturedTypeVariableDTO.tmpId());
                context.bind(type);
            }
            return type;
        }
    }

    /*public FunctionType saveFunctionType(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try (var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveFunctionType")) {
            var context = batch.getContext();
            var param = typeDTO.getFunctionTypeParam();
            var paramTypes = NncUtils.map(param.parameterTypeIds(), batch::get);
            var returnType = batch.get(param.returnTypeId());
            return context.getFunctionTypeContext().getFunctionType(paramTypes, returnType, typeDTO.tmpId());
        }
    }*/

    /*public Klass saveParameterized(PTypeDTO pTypeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try (var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveParameterized")) {
            var template = batch.getClassType(pTypeDTO.getTemplateId());
            var typeArgs = NncUtils.map(pTypeDTO.getTypeArgumentIds(), batch::get);
            return batch.getContext().getGenericContext()
                    .getParameterizedType(template, typeArgs, stage, batch);
        }
    }*/

    public Klass saveKlass(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try (var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveClassType")) {
            var param = typeDTO.getClassParam();
            var type = batch.getContext().getKlass(typeDTO.id());
            var context = batch.getContext();
            if (type == null) {
                type = ClassTypeBuilder.newBuilder(typeDTO.name(), typeDTO.code())
                        .tmpId(typeDTO.tmpId())
                        .category(TypeCategory.fromCode(typeDTO.category()))
                        .ephemeral(typeDTO.ephemeral())
                        .anonymous(typeDTO.anonymous())
                        .typeParameters(NncUtils.map(param.typeParameterIds(), batch::getTypeVariable))
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
                    // TODO handle memory leak
                    var enumSuperClass = StandardTypes.getEnumType().getParameterized(List.of(type.getType()));
                    type.setSuperClass(enumSuperClass);
                } else
                    type.setSuperClass(NncUtils.get(param.superClassId(), batch::getKlass));
                type.setInterfaces(NncUtils.map(param.interfaceIds(), batch::getKlass));
                if (!type.isTemplate())
                    type.setTypeArguments(NncUtils.map(param.typeArgumentIds(), id -> context.getType(Id.parse(id))));
                if (param.dependencyIds() != null)
                    type.setDependencies(NncUtils.map(param.dependencyIds(), id -> context.getKlass(Id.parse(id))));
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
                if (param.titleFieldId() != null)
                    type.setTitleField(NncUtils.find(type.getFields(), f -> f.getStringId().equals(param.titleFieldId())));
                type.setStage(ResolutionStage.DECLARATION);
            }
            if (stage.isAfterOrAt(ResolutionStage.DEFINITION) && curStage.isBefore(ResolutionStage.DEFINITION)) {
                if (param.flows() != null) {
                    for (FlowDTO flowDTO : param.flows()) {
                        setCapturedFlows(flowDTO, context);
                    }
                }
            }
            return type;
        }
    }

    private void setCapturedFlows(FlowDTO flowDTO, IEntityContext context) {
        context.getFlow(flowDTO.id()).setCapturedFlows(NncUtils.map(flowDTO.capturedFlowIds(), context::getFlow));
    }

    /*public TypeVariable createTypeVariable(TypeDTO typeDTO,
                                           boolean withBounds, IEntityContext context) {
        var param = (TypeVariableParam) typeDTO.param();
        var typeVariable = new TypeVariable(typeDTO.tmpId(), typeDTO.name(), typeDTO.code(),
                requireNonNull(context.getEntity(GenericDeclaration.class, Id.parse(param.genericDeclarationId()))));
        if (withBounds)
            typeVariable.setBounds(NncUtils.map(param.boundIds(), id -> context.getType(Id.parse(id))));
        context.bind(typeVariable);
        return typeVariable;
    }*/

    public boolean isNullable(Type type) {
        return type.isNullable();
    }

    public Field saveField(Klass declaringType, FieldDTO fieldDTO, IEntityContext context) {
        Field field = context.getField(fieldDTO.id());
        Type fieldType = context.getType(Id.parse(fieldDTO.typeId()));
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
        var flow = context.getMethod(flowDTO.id());
        var param = (MethodParam) flowDTO.param();
        if (flow == null) {
            var declaringType = batch.getKlass(param.declaringTypeId());
            flow = MethodBuilder.newBuilder(declaringType, flowDTO.name(), flowDTO.code())
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
        flow.setTypeParameters(NncUtils.map(flowDTO.typeParameterIds(), batch::getTypeVariable));
        flow.setCapturedTypeVariables(NncUtils.map(flowDTO.capturedTypeIds(), batch::getCapturedTypeVariable));
//        flow.setCapturedCompositeTypes(NncUtils.map(flowDTO.capturedCompositeTypeIds(), batch::get));
        flow.setParameters(NncUtils.map(flowDTO.parameters(), paramDTO -> saveParameter(paramDTO, batch)));
        flow.setReturnType(TypeParser.parse(flowDTO.returnType(), context));
        flow.setAbstract(param.isAbstract());
//        if(flowDTO.horizontalInstances() != null) {
//            for (FlowDTO templateInstance : flowDTO.horizontalInstances()) {
//                context.getGenericContext().getParameterizedFlow(
//                        flow,
//                        NncUtils.map(templateInstance.typeArgumentIds(), batch::get),
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
        var function = context.getFunction(Id.parse(flowDTO.id()));
        if (function == null) {
            function = FunctionBuilder.newBuilder(flowDTO.name(), flowDTO.code())
                    .tmpId(flowDTO.tmpId())
                    .build();
            context.bind(function);
        } else {
            function.setName(flowDTO.name());
            function.setCode(flowDTO.code());
        }
        function.setNative(flowDTO.isNative());
        function.setTypeParameters(NncUtils.map(flowDTO.typeParameterIds(), batch::getTypeVariable));
        function.setCapturedTypeVariables(NncUtils.map(flowDTO.capturedTypeIds(), batch::getCapturedTypeVariable));
//        function.setCapturedCompositeTypes(NncUtils.map(flowDTO.capturedCompositeTypeIds(), batch::get));
        function.setParameters(NncUtils.map(flowDTO.parameters(), paramDTO -> saveParameter(paramDTO, batch)));
        function.setReturnType(TypeParser.parse(flowDTO.returnType(), context));
//        if(flowDTO.horizontalInstances() != null) {
//            for (FlowDTO templateInstance : flowDTO.horizontalInstances()) {
//                context.getGenericContext().getParameterizedFlow(
//                        function,
//                        NncUtils.map(templateInstance.typeArgumentIds(), batch::get),
//                        stage,
//                        batch
//                );
//            }
//        }
        return function;
    }

    private Parameter saveParameter(ParameterDTO parameterDTO, SaveTypeBatch batch) {
        var context = batch.getContext();
        var param = context.getEntity(Parameter.class, parameterDTO.id());
        if (param == null) {
            param = new Parameter(
                    parameterDTO.tmpId(),
                    parameterDTO.name(),
                    parameterDTO.code(),
                    TypeParser.parse(parameterDTO.type(), context)
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

    public java.lang.reflect.Type getJavaType(Type type) {
        throw new UnsupportedOperationException();
    }

}
