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
import tech.metavm.object.type.rest.dto.CapturedTypeVariableDTO;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.type.rest.dto.TypeVariableDTO;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import java.util.List;

public abstract class TypeFactory {

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

    public CapturedTypeVariable saveCapturedTypeVariable(CapturedTypeVariableDTO capturedTypeVariableDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try (var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveCapturedType")) {
            var context = batch.getContext();
            var type = context.getCapturedTypeVariable(capturedTypeVariableDTO.id());
            if (type == null) {
                type = new CapturedTypeVariable(
                        new UncertainType(new NeverType(), new AnyType()),
                        DummyCapturedTypeScope.INSTANCE
                );
                if (capturedTypeVariableDTO.tmpId() != null)
                    type.setTmpId(capturedTypeVariableDTO.tmpId());
                context.bind(type);
            }
            var curStage = type.setStage(stage);
            if (stage.isAfterOrAt(ResolutionStage.DECLARATION) && curStage.isBefore(ResolutionStage.DECLARATION))
                type.setUncertainType((UncertainType) TypeParser.parse(capturedTypeVariableDTO.uncertainType(), context));
            return type;
        }
    }

    public Klass saveKlass(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try (var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveClassType")) {
            var param = typeDTO.getClassParam();
            var type = batch.getContext().getKlass(typeDTO.id());
            var context = batch.getContext();
            if (type == null) {
                type = ClassTypeBuilder.newBuilder(typeDTO.name(), typeDTO.code())
                        .tmpId(typeDTO.tmpId())
                        .kind(ClassKind.fromCode(typeDTO.kind()))
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
                    var enumSuperClass = StandardTypes.getEnumKlass().getParameterized(List.of(type.getType()));
                    type.setSuperType(enumSuperClass.getType());
                } else
                    type.setSuperType(NncUtils.get(param.superType(), t -> (ClassType) TypeParser.parse(t, batch)));
                type.setInterfaces(NncUtils.map(param.interfaces(), t -> (ClassType) TypeParser.parse(t, batch)));
                if (!type.isTemplate())
                    type.setTypeArguments(NncUtils.map(param.typeArguments(), t -> TypeParser.parse(t, batch)));
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

    public boolean isNullable(Type type) {
        return type.isNullable();
    }

    public Field saveField(Klass declaringType, FieldDTO fieldDTO, IEntityContext context) {
        Field field = context.getField(fieldDTO.id());
        Type fieldType = TypeParser.parse(fieldDTO.type(), context);
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
        var method = context.getMethod(flowDTO.id());
        var param = (MethodParam) flowDTO.param();
        if (method == null) {
            var declaringType = batch.getKlass(param.declaringTypeId());
            method = MethodBuilder.newBuilder(declaringType, flowDTO.name(), flowDTO.code())
                    .access(Access.findByCode(param.access()))
                    .isStatic(param.isStatic())
                    .tmpId(flowDTO.tmpId())
                    .build();
            context.bind(method);
        } else {
            method.setName(flowDTO.name());
            method.setCode(flowDTO.code());
        }
        method.setNative(flowDTO.isNative());
        method.setAbstract(param.isAbstract());
        method.setConstructor(param.isConstructor());
        method.setTypeParameters(NncUtils.map(flowDTO.typeParameterIds(), batch::getTypeVariable));
        method.setCapturedTypeVariables(NncUtils.map(flowDTO.capturedTypeIds(), batch::getCapturedTypeVariable));
        method.setParameters(NncUtils.map(flowDTO.parameters(), paramDTO -> saveParameter(paramDTO, batch)));
        method.setReturnType(TypeParser.parse(flowDTO.returnType(), context));
        method.setOverridden(NncUtils.map(param.overriddenRefs(), r -> MethodRef.create(r, context).resolve()));
        method.setAbstract(param.isAbstract());
        return method;
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
        function.setParameters(NncUtils.map(flowDTO.parameters(), paramDTO -> saveParameter(paramDTO, batch)));
        function.setReturnType(TypeParser.parse(flowDTO.returnType(), context));
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

    public void putType(java.lang.reflect.Type javaType, TypeDef typeDef) {
        throw new UnsupportedOperationException();
    }

}
