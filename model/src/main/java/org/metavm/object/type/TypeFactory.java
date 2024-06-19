package org.metavm.object.type;

import org.metavm.common.ErrorCode;
import org.metavm.entity.Attribute;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.StdKlass;
import org.metavm.flow.*;
import org.metavm.flow.rest.FlowDTO;
import org.metavm.flow.rest.FunctionParam;
import org.metavm.flow.rest.MethodParam;
import org.metavm.flow.rest.ParameterDTO;
import org.metavm.object.instance.InstanceFactory;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.CapturedTypeVariableDTO;
import org.metavm.object.type.rest.dto.FieldDTO;
import org.metavm.object.type.rest.dto.TypeDTO;
import org.metavm.object.type.rest.dto.TypeVariableDTO;
import org.metavm.util.BusinessException;
import org.metavm.util.ContextUtil;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

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
                type.setBounds(NncUtils.map(typeVariableDTO.bounds(), bound -> TypeParser.parseType(bound, batch.getContext())));
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
                type.setUncertainType((UncertainType) TypeParser.parseType(capturedTypeVariableDTO.uncertainType(), context));
            return type;
        }
    }

    public Klass saveKlass(TypeDTO typeDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try (var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveClassType")) {
            var param = typeDTO.getClassParam();
            var klass = batch.getContext().getKlass(typeDTO.id());
            var context = batch.getContext();
            if (klass == null) {
                klass = KlassBuilder.newBuilder(typeDTO.name(), typeDTO.code())
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
                context.bind(klass);
            } else if (klass.getStage().isBeforeOrAt(ResolutionStage.INIT)) {
                klass.setCode(typeDTO.code());
                klass.setName(typeDTO.name());
                klass.setDesc(param.desc());
                klass.setStruct(param.struct());
                klass.setAbstract(param.isAbstract());
                batch.getContext().update(klass);
            }
            if(typeDTO.attributes() != null)
                klass.setAttributes(Attribute.fromMap(typeDTO.attributes()));
            var curStage = klass.setStage(stage);
            if (stage.isAfterOrAt(ResolutionStage.SIGNATURE) && curStage.isBefore(ResolutionStage.SIGNATURE)) {
                if (klass.isEnum()) {
                    // TODO handle memory leak
                    var enumSuperClass = StdKlass.enum_.get().getParameterized(List.of(klass.getType()));
                    klass.setSuperType(enumSuperClass.getType());
                } else
                    klass.setSuperType(NncUtils.get(param.superType(), t -> (ClassType) TypeParser.parseType(t, batch)));
                klass.setInterfaces(NncUtils.map(param.interfaces(), t -> (ClassType) TypeParser.parseType(t, batch)));
                if (!klass.isTemplate())
                    klass.setTypeArguments(NncUtils.map(param.typeArguments(), t -> TypeParser.parseType(t, batch)));
                klass.setStage(ResolutionStage.SIGNATURE);
            }
            if (stage.isAfterOrAt(ResolutionStage.DECLARATION) && curStage.isBefore(ResolutionStage.DECLARATION)) {
                var declaringType = klass;
                if (param.fields() != null)
                    klass.setFields(NncUtils.map(param.fields(), f -> saveField(declaringType, f, context)));
                if (param.staticFields() != null)
                    klass.setStaticFields(NncUtils.map(param.staticFields(), f -> saveField(declaringType, f, context)));
                if (param.constraints() != null)
                    klass.setConstraints(NncUtils.map(param.constraints(), c -> ConstraintFactory.save(c, context)));
                if (param.flows() != null) {
                    var methods = NncUtils.filterAndMap(param.flows(), f -> !f.synthetic(), f -> saveMethod(f, stage, batch));
                    klass.getMethods().forEach(m -> {
                        if(m.isSynthetic())
                            methods.add(m);
                    });
                    klass.setMethods(methods);
                }
                if (param.titleFieldId() != null)
                    klass.setTitleField(NncUtils.find(klass.getFields(), f -> f.getStringId().equals(param.titleFieldId())));
                klass.setStage(ResolutionStage.DECLARATION);
            }
            if (stage.isAfterOrAt(ResolutionStage.DEFINITION) && curStage.isBefore(ResolutionStage.DEFINITION)) {
                if (param.flows() != null) {
                    for (FlowDTO flowDTO : param.flows()) {
                        setCapturedFlows(flowDTO, context);
                    }
                }
            }
            return klass;
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
        Type fieldType = TypeParser.parseType(fieldDTO.type(), context);
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
            if(method.isSynthetic())
                throw new BusinessException(ErrorCode.MODIFYING_SYNTHETIC_FLOW, method.getQualifiedName());
            method.setName(flowDTO.name());
            method.setCode(flowDTO.code());
        }
        if(flowDTO.attributes() != null)
            method.setAttributes(Attribute.fromMap(flowDTO.attributes()));
        method.setNative(flowDTO.isNative());
        method.setAbstract(param.isAbstract());
        method.setConstructor(param.isConstructor());
        method.setTypeParameters(NncUtils.map(flowDTO.typeParameterIds(), batch::getTypeVariable));
        method.setCapturedTypeVariables(NncUtils.map(flowDTO.capturedTypeIds(), batch::getCapturedTypeVariable));
        method.setParameters(NncUtils.map(flowDTO.parameters(), paramDTO -> saveParameter(paramDTO, batch)));
        method.setReturnType(TypeParser.parseType(flowDTO.returnType(), context));
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
        function.setReturnType(TypeParser.parseType(flowDTO.returnType(), context));
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
                    TypeParser.parseType(parameterDTO.type(), context)
            );
        } else {
            param.setName(parameterDTO.name());
            param.setCode(parameterDTO.code());
        }
        if(parameterDTO.attributes() != null)
            param.setAttributes(Attribute.fromMap(parameterDTO.attributes()));
        return param;
    }

    public void putType(java.lang.reflect.Type javaType, TypeDef typeDef) {
        throw new UnsupportedOperationException();
    }

}
