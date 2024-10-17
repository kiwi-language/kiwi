package org.metavm.object.type;

import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.expression.TypeParsingContext;
import org.metavm.flow.*;
import org.metavm.flow.rest.FlowDTO;
import org.metavm.flow.rest.FunctionParam;
import org.metavm.flow.rest.MethodParam;
import org.metavm.flow.rest.ParameterDTO;
import org.metavm.object.instance.InstanceFactory;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.*;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public abstract class TypeFactory {

    public static final Logger logger = LoggerFactory.getLogger(TypeFactory.class);

    public TypeVariable saveTypeVariable(TypeVariableDTO typeVariableDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try (var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveTypeVariable")) {
            var context = batch.getContext();
            var type = batch.getContext().getTypeVariable(Id.parse(typeVariableDTO.id()));
            if (type == null) {
                type = new TypeVariable(typeVariableDTO.tmpId(), typeVariableDTO.name(), typeVariableDTO.code(),
                        context.getEntity(GenericDeclaration.class, typeVariableDTO.genericDeclarationId()));
                context.bind(type);
                var retrieved = context.getEntity(TypeVariable.class, type.getId());
                assert retrieved != null : "Fail to save type variable " + type.getId();
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
                type = new CapturedTypeVariable(UncertainType.asterisk, DummyCapturedTypeScope.INSTANCE);
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

    public Klass saveKlass(KlassDTO klassDTO, ResolutionStage stage, SaveTypeBatch batch) {
        try (var ignored = ContextUtil.getProfiler().enter("TypeFactory.saveClassType")) {
            var klass = batch.getContext().getKlass(klassDTO.id());
            var context = batch.getContext();
            var kind = ClassKind.fromCode(klassDTO.kind());
            if (klass == null) {
                klass = KlassBuilder.newBuilder(klassDTO.name(), klassDTO.code())
                        .tmpId(klassDTO.tmpId())
                        .kind(kind)
                        .ephemeral(klassDTO.ephemeral())
                        .anonymous(klassDTO.anonymous())
                        .isTemplate(klassDTO.isTemplate())
                        .isAbstract(klassDTO.isAbstract())
                        .struct(klassDTO.struct())
                        .searchable(klassDTO.searchable())
                        .desc(klassDTO.desc())
                        .source(ClassSource.getByCode(klassDTO.source()))
                        .tmpId(klassDTO.tmpId())
                        .tag(klassDTO.tag() != TypeTags.DEFAULT ? klassDTO.tag() :  KlassTagAssigner.getInstance(context).next())
                        .sourceCodeTag(klassDTO.sourceCodeTag() != null ? klassDTO.sourceCodeTag() :
                                KlassSourceCodeTagAssigner.getInstance(context).next())
                        .build();
                context.bind(klass);
            } else if (klass.getStage().isBeforeOrAt(ResolutionStage.INIT)) {
                klass.setCode(klassDTO.code());
                klass.setName(klassDTO.name());
                klass.setDesc(klassDTO.desc());
                klass.setSearchable(klassDTO.searchable());
                if(!klass.isEnum()) {
                    if(klass.getOldSuperType() == null)
                        klass.setOldSuperType(klass.getSuperType());
                    klass.setSuperType(null);
                }
                klass.setInterfaces(List.of());
                if(kind != klass.getKind()) {
                    if(!context.isNewEntity(klass)) {
                        if(kind == ClassKind.VALUE)
                            batch.addEntityToValueKlass(klass);
                        else if (klass.getKind() == ClassKind.VALUE)
                            batch.addValueToEntityKlass(klass);
                        if(kind == ClassKind.ENUM)
                            batch.addToEnumKlass(klass);
                        else if(klass.getKind() == ClassKind.ENUM)
                            batch.addFromEnumKlass(klass);
                    }
                    klass.setKind(kind);
                }
                klass.setStruct(klassDTO.struct());
                klass.setAbstract(klassDTO.isAbstract());
                batch.getContext().update(klass);
            }
            if(klass.getStage().isBeforeOrAt(ResolutionStage.INIT)) {
                if (klassDTO.flows() != null) {
                    var methods = NncUtils.filterAndMap(klassDTO.flows(), f -> !f.synthetic(), f -> saveMethod(f, ResolutionStage.INIT, batch));
                    klass.getMethods().forEach(m -> {
                        if(m.isSynthetic())
                            methods.add(m);
                    });
                    klass.setMethods(methods);
                }
            }
            if(klassDTO.attributes() != null)
                klass.setAttributes(Attribute.fromMap(klassDTO.attributes()));
            var curStage = klass.setStage(stage);
            if (stage.isAfterOrAt(ResolutionStage.SIGNATURE) && curStage.isBefore(ResolutionStage.SIGNATURE)) {
                klass.setTypeParameters(NncUtils.map(klassDTO.typeParameterIds(), batch::getTypeVariable));
                if (klass.isEnum()) {
                    var enumSuperClass = StdKlass.enum_.get().getParameterized(List.of(klass.getType()));
                    klass.setSuperType(enumSuperClass.getType());
                } else {
                    var superType = NncUtils.get(klassDTO.superType(), t -> (ClassType) TypeParser.parseType(t, batch));
                    if(!Objects.equals(superType, klass.getSuperType())) {
                        if (!context.isNewEntity(klass) && superType != null && !superType.equals(klass.getOldSuperType()))
                            batch.addChangingSuperKlass(klass);
                        klass.setSuperType(superType);
                    }
                }
                klass.setInterfaces(NncUtils.map(klassDTO.interfaces(), t -> (ClassType) TypeParser.parseType(t, batch)));
                if (!klass.isTemplate())
                    klass.setTypeArguments(NncUtils.map(klassDTO.typeArguments(), t -> TypeParser.parseType(t, batch)));
                klass.setStage(ResolutionStage.SIGNATURE);
            }
            if (stage.isAfterOrAt(ResolutionStage.DECLARATION) && curStage.isBefore(ResolutionStage.DECLARATION)) {
                var declaringType = klass;
                if (klassDTO.fields() != null)
                    klass.setFields(NncUtils.map(klassDTO.fields(), f -> saveField(declaringType, f, batch)));
                if (klassDTO.staticFields() != null)
                    klass.setStaticFields(NncUtils.map(klassDTO.staticFields(), f -> saveField(declaringType, f, batch)));
                if (klassDTO.constraints() != null)
                    klass.setConstraints(NncUtils.map(klassDTO.constraints(), c -> ConstraintFactory.save(c, context)));
                if(klassDTO.enumConstantDefs() != null)
                    klass.setEnumConstantDefs(NncUtils.map(klassDTO.enumConstantDefs(), ec -> saveEnumConstantDef(declaringType, ec, stage, batch)));
                if (klassDTO.flows() != null) {
                    for (FlowDTO flowDTO : klassDTO.flows()) {
                        if(!flowDTO.synthetic())
                            saveMethod(flowDTO, stage, batch);
                    }
                }
                if (klassDTO.titleFieldId() != null)
                    klass.setTitleField(NncUtils.find(klass.getFields(), f -> f.getStringId().equals(klassDTO.titleFieldId())));
                else
                    klass.setTitleField(null);
                klass.setStage(ResolutionStage.DECLARATION);
            }
            if (stage.isAfterOrAt(ResolutionStage.DEFINITION) && curStage.isBefore(ResolutionStage.DEFINITION)) {
                if (klassDTO.flows() != null) {
                    for (FlowDTO flowDTO : klassDTO.flows()) {
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

    public Field saveField(Klass declaringType, FieldDTO fieldDTO, SaveTypeBatch batch) {
        var context = batch.getContext();
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
                    .isTransient(fieldDTO.isTransient())
                    .staticValue(Instances.nullInstance())
                    .tag(declaringType.nextFieldTag())
                    .sourceCodeTag(fieldDTO.sourceCodeTag() != null ? fieldDTO.sourceCodeTag() : declaringType.nextFieldSourceCodeTag())
                    .state(context.isNewEntity(declaringType) ? MetadataState.READY : MetadataState.INITIALIZING)
                    .build();
            if(!context.isNewEntity(declaringType) && !field.isStatic())
                batch.addNewField(field);
            context.bind(field);
        } else {
            field.setName(fieldDTO.name());
            field.setCode(fieldDTO.code());
            field.setUnique(fieldDTO.unique());
            field.setTransient(fieldDTO.isTransient());
            if(fieldDTO.isChild() != field.isChild()) {
                field.setChild(fieldDTO.isChild());
                if(field.isChild())
                    batch.addToChildField(field);
                else
                    batch.addToNonChildField(field);
            }
            if(!fieldType.isAssignableFrom(field.getType())) {
                field.setType(fieldType);
                field.setTag(declaringType.nextFieldTag());
                if(!field.isStatic())
                    batch.addTypeChangedField(field);
            }
            field.setDefaultValue(defaultValue);
            field.setAccess(access);
            var state = MetadataState.fromCode(fieldDTO.state());
            if(state != field.getState()) {
                if(field.getState() == MetadataState.REMOVED)
                    batch.addNewField(field);
                field.setState(state);
                if(state == MetadataState.REMOVED && field.isChild()) {
                    batch.addRemovedChildField(field);
                }
            }
        }
        return field;
    }

    public Method saveMethod(FlowDTO flowDTO, ResolutionStage stage, SaveTypeBatch batch) {
        var context = batch.getContext();
        var method = context.getMethod(flowDTO.id());
        var param = (MethodParam) flowDTO.param();
        var access= Access.findByCode(param.access());
        if (method == null) {
            var declaringType = batch.getKlass(param.declaringTypeId());
            method = MethodBuilder.newBuilder(declaringType, flowDTO.name(), flowDTO.code())
                    .access(access)
                    .isStatic(param.isStatic())
                    .tmpId(flowDTO.tmpId())
                    .build();
            context.bind(method);
        } else {
            if (method.isSynthetic())
                throw new BusinessException(ErrorCode.MODIFYING_SYNTHETIC_FLOW, method.getQualifiedName());
            method.setName(flowDTO.name());
            method.setCode(flowDTO.code());
            method.setAccess(access);
        }
        if (flowDTO.attributes() != null)
            method.setAttributes(Attribute.fromMap(flowDTO.attributes()));
        var curStage = method.setStage(stage);
        if(curStage.isBefore(ResolutionStage.DECLARATION) && stage.isAfterOrAt(ResolutionStage.DECLARATION)) {
            method.setNative(flowDTO.isNative());
            method.setAbstract(param.isAbstract());
            method.setConstructor(param.isConstructor());
            method.setTypeParameters(NncUtils.map(flowDTO.typeParameterIds(), batch::getTypeVariable));
            method.setCapturedTypeVariables(NncUtils.map(flowDTO.capturedTypeIds(), batch::getCapturedTypeVariable));
            method.setParameters(NncUtils.map(flowDTO.parameters(), paramDTO -> saveParameter(paramDTO, batch)));
            method.setReturnType(TypeParser.parseType(flowDTO.returnType(), context));
            method.setAbstract(param.isAbstract());
            if (method.getParameters().isEmpty() && !method.isStatic() && method.getName().equals(Constants.RUN_METHOD_NAME))
                batch.addRunMethod(method);
        }
        return method;
    }

    public EnumConstantDef saveEnumConstantDef(Klass declaringKlass, EnumConstantDefDTO enumConstantDefDTO, ResolutionStage stage, SaveTypeBatch saveTypeBatch) {
        var id = enumConstantDefDTO.id() != null ? Id.parse(enumConstantDefDTO.id()) : null;
        EnumConstantDef ec = id != null ? declaringKlass.findEnumConstantDef(e -> e.idEquals(id)) : null;
        var context = saveTypeBatch.getContext();
        var parsingContext = new TypeParsingContext(context.getInstanceContext(), new ContextTypeDefRepository(context), declaringKlass);
        var args = NncUtils.map(enumConstantDefDTO.arguments(), a -> ValueFactory.create(a, parsingContext));
        if(ec == null) {
            ec = new EnumConstantDef(
                    declaringKlass,
                    enumConstantDefDTO.name(),
                    enumConstantDefDTO.ordinal(),
                    args
            );
            saveTypeBatch.addNewEnumConstantDef(ec);
        }
        else {
            if(enumConstantDefDTO.name().equals(ec.getName()) || enumConstantDefDTO.ordinal() != ec.getOrdinal()
                    || !args.equals(ec.getArguments())) {
                ec.setName(enumConstantDefDTO.name());
                ec.setOrdinal(enumConstantDefDTO.ordinal());
                ec.setArguments(args);
                saveTypeBatch.addChangedEnumConstantDef(ec);
            }
        }
        return ec;
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
        if (parameterDTO.attributes() != null)
            param.setAttributes(Attribute.fromMap(parameterDTO.attributes()));
        return param;
    }

    public void putType(java.lang.reflect.Type javaType, TypeDef typeDef) {
        throw new UnsupportedOperationException();
    }

}
