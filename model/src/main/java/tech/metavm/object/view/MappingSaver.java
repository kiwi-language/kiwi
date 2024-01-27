package tech.metavm.object.view;

import tech.metavm.entity.DummyGenericDeclaration;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.TypeParsingContext;
import tech.metavm.flow.Flow;
import tech.metavm.flow.Method;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.flow.ValueFactory;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.type.*;
import tech.metavm.object.type.generic.SubstitutorV2;
import tech.metavm.object.type.generic.TypeSubstitutor;
import tech.metavm.object.view.rest.dto.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NamingUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

public class MappingSaver {

    public static MappingSaver create(IEntityContext context) {
        return new MappingSaver(context.getInstanceContext(),
                new ContextTypeRepository(context),
                new CompositeTypeFacadeImpl(
                        new ContextArrayTypeProvider(context),
                        context.getFunctionTypeContext(),
                        context.getUnionTypeContext(),
                        context.getIntersectionTypeContext(),
                        context.getUncertainTypeContext(),
                        context.getGenericContext()),
                context.getGenericContext(),
                context.getGenericContext(),
                context);
    }

    // TODO MOVE TO NamingUtils
    private static final Pattern GETTER_CODE_PATTERN = Pattern.compile("^get([A-Z][A-Za-z0-9_$]+$)");

    private final InstanceProvider instanceProvider;
    private final IndexedTypeProvider typeProvider;
    private final ParameterizedTypeProvider parameterizedTypeProvider;
    private final ParameterizedFlowProvider parameterizedFlowProvider;
    private final CompositeTypeFacade compositeTypeFacade;
    private final MappingProvider mappingProvider;

    public MappingSaver(InstanceProvider instanceProvider,
                        IndexedTypeProvider typeProvider,
                        CompositeTypeFacade compositeTypeFacade,
                        ParameterizedTypeProvider parameterizedTypeProvider,
                        ParameterizedFlowProvider parameterizedFlowProvider,
                        MappingProvider mappingProvider) {
        this.instanceProvider = instanceProvider;
        this.typeProvider = typeProvider;
        this.parameterizedTypeProvider = parameterizedTypeProvider;
        this.parameterizedFlowProvider = parameterizedFlowProvider;
        this.compositeTypeFacade = compositeTypeFacade;
        this.mappingProvider = mappingProvider;
    }

    public ObjectMapping save(ObjectMappingDTO mappingDTO) {
        if (mappingDTO.param() instanceof FieldsObjectMappingParam)
            return saveFieldsObjectMapping(mappingDTO);
        else
            throw new InternalException("Unrecognized object mapping param: " + mappingDTO.param());
    }

    private FieldsObjectMapping saveFieldsObjectMapping(ObjectMappingDTO mappingDTO) {
        var sourceType = typeProvider.getClassType(mappingDTO.sourceTypeRef());
        FieldsObjectMapping mapping = (FieldsObjectMapping) sourceType.findMapping(mappingDTO.getRef());
        if (mapping == null) {
            var targetType = createTargetType(sourceType, "预设视图");
            mapping = new FieldsObjectMapping(mappingDTO.tmpId(), mappingDTO.name(), mappingDTO.code(), sourceType, false,
                    targetType, NncUtils.map(mappingDTO.overriddenRefs(), sourceType::getMappingInAncestors));
            mapping.generateDeclarations(compositeTypeFacade);
        } else {
            mapping.setName(mappingDTO.name());
            mapping.setCode(mappingDTO.code());
            mapping.setOverridden(NncUtils.map(mappingDTO.overriddenRefs(), sourceType::getMappingInAncestors));
        }
        if (mappingDTO.isDefault())
            mapping.setDefault();
        var param = (FieldsObjectMappingParam) mappingDTO.param();
        final var m = mapping;
        mapping.setFieldMappings(
                NncUtils.map(param.fieldMappings(), f -> saveFieldMapping(f, m))
        );
        mapping.generateCode(compositeTypeFacade);
        return mapping;
    }

    private void retransformClassType(ClassType sourceType) {
        if (sourceType.isTemplate()) {
            var templateInstances = parameterizedTypeProvider.getTemplateInstances(sourceType);
            for (ClassType templateInstance : templateInstances) {
                templateInstance.setStage(ResolutionStage.INIT);
                var subst = new SubstitutorV2(
                        sourceType, sourceType.getTypeParameters(), templateInstance.getTypeArguments(),
                        ResolutionStage.DEFINITION, null, compositeTypeFacade, parameterizedTypeProvider,
                        parameterizedFlowProvider, new MockDTOProvider()
                );
                sourceType.accept(subst);
            }
        }
    }

    private FieldMapping saveFieldMapping(FieldMappingDTO fieldMappingDTO, FieldsObjectMapping containingMapping) {
        var nestedMapping = NncUtils.get(fieldMappingDTO.nestedMappingRef(), mappingProvider::getMapping);
        var fieldMapping = containingMapping.findFieldMapping(fieldMappingDTO.getRef());
        var sourceType = containingMapping.getSourceType();
        if (fieldMapping == null) {
            return switch (fieldMappingDTO.param()) {
                case DirectFieldMappingParam directParam -> {
                    var sourceField = sourceType.getField(Objects.requireNonNull(fieldMappingDTO.sourceFieldRef()));
                    yield new DirectFieldMapping(
                            fieldMappingDTO.tmpId(),
                            createTargetField(
                                    containingMapping.getTargetType(),
                                    fieldMappingDTO.name(),
                                    fieldMappingDTO.code(),
                                    nestedMapping != null ? nestedMapping.getTargetType() : sourceField.getType(),
                                    fieldMappingDTO.isChild(),
                                    sourceField.isTitle(),
                                    DirectFieldMapping.checkReadonly(sourceField, fieldMappingDTO.readonly())
                            ),
                            containingMapping,
                            nestedMapping,
                            sourceField);
                }
                case FlowFieldMappingParam flowParam -> {
                    var getter = sourceType.getMethod(flowParam.getterRef());
                    var setter = NncUtils.get(flowParam.setterRef(), sourceType::getMethod);
                    yield new FlowFieldMapping(
                            fieldMappingDTO.tmpId(),
                            containingMapping,
                            nestedMapping,
                            createTargetField(
                                    containingMapping.getTargetType(),
                                    fieldMappingDTO.name(),
                                    fieldMappingDTO.code(),
                                    nestedMapping != null ? nestedMapping.getTargetType() : getter.getReturnType(),
                                    fieldMappingDTO.isChild(),
                                    false,
                                    setter == null
                            ),
                            getter,
                            setter,
                            null);
                }
                case ComputedFieldMappingParam computedParam -> {
                    var value = ValueFactory.create(computedParam.value(),
                            createTypeParsingContext(containingMapping.getSourceType()));
                    yield new ComputedFieldMapping(
                            fieldMappingDTO.tmpId(),
                            createTargetField(
                                    containingMapping.getTargetType(),
                                    fieldMappingDTO.name(),
                                    fieldMappingDTO.code(),
                                    nestedMapping != null ? nestedMapping.getTargetType() : value.getType(),
                                    fieldMappingDTO.isChild(),
                                    false,
                                    true
                            ),
                            containingMapping,
                            nestedMapping,
                            value);
                }
                default -> throw new IllegalStateException("Unexpected value: " + fieldMappingDTO);
            };
        } else {
            fieldMapping.setName(fieldMappingDTO.name());
            fieldMapping.setCode(fieldMappingDTO.code());
            fieldMapping.setNestedMapping(nestedMapping);
            var param = fieldMappingDTO.param();
            switch (fieldMapping) {
                case DirectFieldMapping directFieldMapping -> directFieldMapping.update(
                        sourceType.getField(Objects.requireNonNull(fieldMappingDTO.sourceFieldRef())),
                        fieldMappingDTO.readonly());
                case FlowFieldMapping flowFieldMapping -> {
                    var flowParam = (FlowFieldMappingParam) param;
                    flowFieldMapping.setFlows(
                            sourceType.getMethod(flowParam.getterRef()),
                            NncUtils.get(flowParam.setterRef(), sourceType::getMethod)
                    );
                }
                case ComputedFieldMapping computedFieldMapping -> {
                    var computedParam = (ComputedFieldMappingParam) param;
                    computedFieldMapping.setValue(
                            ValueFactory.create(computedParam.value(),
                                    createTypeParsingContext(containingMapping.getSourceType()))
                    );
                }
                default -> throw new IllegalStateException("Unexpected value: " + fieldMapping);
            }
            return fieldMapping;
        }
    }

    private TypeParsingContext createTypeParsingContext(ClassType type) {
        return new TypeParsingContext(
                instanceProvider,
                typeProvider,
                compositeTypeFacade, type
        );
    }

    public FieldsObjectMapping saveBuiltinMapping(ClassType type, boolean generateCode) {
        NncUtils.requireTrue(type.isClass());
        var mapping = (FieldsObjectMapping) NncUtils.find(type.getMappings(), ObjectMapping::isBuiltin);
        if (mapping == null) {
            var targetType = createTargetType(type, "预设视图");
            mapping = new FieldsObjectMapping(null, "预设视图", "builtin", type, true, targetType, List.of());
            mapping.generateDeclarations(compositeTypeFacade);
        }
        retransformClassType(type);
        if (generateCode) {
            var directFieldMappings = NncUtils.toMap(
                    NncUtils.filterByType(mapping.getFieldMappings(), DirectFieldMapping.class),
                    DirectFieldMapping::getSourceField,
                    Function.identity()
            );
            var fieldMappings = new ArrayList<FieldMapping>();
            for (var field : getVisibleFields(type))
                fieldMappings.add(saveBuiltinDirectFieldMapping(field, mapping, directFieldMappings, generateCode));
            var propertyFieldMappings = NncUtils.toMap(
                    NncUtils.filterByType(mapping.getFieldMappings(), FlowFieldMapping.class),
                    FlowFieldMapping::getGetter,
                    Function.identity()
            );
            for (var accessor : getAccessors(type))
                fieldMappings.add(saveBuiltinFlowFieldMapping(accessor, mapping, propertyFieldMappings, generateCode));
            mapping.setFieldMappings(fieldMappings);
            mapping.generateCode(compositeTypeFacade);
            retransformClassType(mapping.getTargetType().getEffectiveTemplate());
            retransformClassType(type);
        }
        return mapping;
    }

    private DirectFieldMapping saveBuiltinDirectFieldMapping(Field field,
                                                             FieldsObjectMapping containingMapping,
                                                             Map<Field, DirectFieldMapping> directFieldMappings,
                                                             boolean generateCode) {
        var nestedMapping = field.isChild() ?
                tryGetBuiltinMapping(field.getType(), field.getType(), generateCode) : null;
        var fieldMapping = directFieldMappings.get(field);
        if (fieldMapping == null) {
            fieldMapping = new DirectFieldMapping(
                    null, createTargetField(containingMapping.getTargetType(), field.getName(), field.getCode(),
                    nestedMapping != null ? nestedMapping.getTargetType() : field.getType(),
                    field.isChild(), field.isTitle(), field.isReadonly()),
                    containingMapping, nestedMapping, field
            );
        } else {
            fieldMapping.setName(field.getName());
            fieldMapping.setCode(field.getCode());
            fieldMapping.update(field, field.isReadonly());
            fieldMapping.setNestedMapping(nestedMapping);
        }
        return fieldMapping;
    }

    private ClassType createTargetType(ClassType sourceType, String name) {
        if (sourceType.isTemplate()) {
            var template = ClassTypeBuilder.newBuilder(getTargetTypeName(sourceType, name), null)
                    .isTemplate(true)
                    .ephemeral(true)
                    .anonymous(true)
                    .typeParameters(NncUtils.map(
                            sourceType.getTypeParameters(),
                            p -> new TypeVariable(null, p.getName(), p.getCode(), DummyGenericDeclaration.INSTANCE)
                    ))
                    .build();
            var subst = new SubstitutorV2(
                    template, template.getTypeParameters(),
                    sourceType.getTypeParameters(), ResolutionStage.INIT,
                    null, compositeTypeFacade,
                    parameterizedTypeProvider,
                    null, new MockDTOProvider()
            );
            return (ClassType) template.accept(subst);
        } else {
            return ClassTypeBuilder.newBuilder(getTargetTypeName(sourceType, name), null)
                    .ephemeral(true)
                    .anonymous(true)
                    .build();
        }
    }

    public static String getTargetTypeName(ClassType sourceType, String mappingName) {
        if (mappingName.endsWith("视图") && mappingName.length() > 2)
            mappingName = mappingName.substring(0, mappingName.length() - 2);
        return NamingUtils.escapeTypeName(sourceType.getName()) + mappingName + "视图";
    }

    private Field createTargetField(ClassType targetType, String name, String code, Type type,
                                    boolean isChild, boolean asTitle, boolean readonly) {
        if (targetType.getTemplate() != null) {
            var template = Objects.requireNonNull(targetType.getTemplate());
            var typeSubst = new TypeSubstitutor(
                    NncUtils.map(targetType.getTypeArguments(), t -> (TypeVariable) t),
                    template.getTypeParameters(),
                    compositeTypeFacade,
                    new MockDTOProvider()
            );
            type = type.accept(typeSubst);
            var fieldTemplate = FieldBuilder
                    .newBuilder(NamingUtils.ensureValidName(name),
                            NamingUtils.ensureValidCode(code), template, type)
                    .isChild(isChild)
                    .asTitle(asTitle)
                    .readonly(readonly)
                    .build();
            var subst = new SubstitutorV2(
                    template, template.getTypeParameters(),
                    targetType.getTypeArguments(), ResolutionStage.DECLARATION,
                    null,
                    compositeTypeFacade,
                    parameterizedTypeProvider,
                    null, new MockDTOProvider()
            );
            targetType.setStage(ResolutionStage.INIT);
            template.accept(subst);
            return NncUtils.findRequired(targetType.getFields(), f -> f.getCopySource() == fieldTemplate);
        } else {
            return FieldBuilder
                    .newBuilder(NamingUtils.ensureValidName(name),
                            NamingUtils.ensureValidCode(code), targetType, type)
                    .isChild(isChild)
                    .asTitle(asTitle)
                    .readonly(readonly)
                    .build();
        }
    }

    private @Nullable Mapping tryGetBuiltinMapping(Type type, @Nullable Type underlyingType, boolean generateCode) {
        return switch (type) {
            case ClassType classType -> classType.getBuiltinMapping();
            case ArrayType arrayType -> {
                if (underlyingType instanceof ArrayType underlyingArrayType && underlyingArrayType.isChildArray()) {
                    var underlyingElementType = underlyingArrayType.getElementType();
                    var elementBuiltinMapping = tryGetBuiltinMapping(arrayType.getElementType(), underlyingElementType, generateCode);
                    if (elementBuiltinMapping != null) {
                        var targetType =
                                compositeTypeFacade.getArrayType(elementBuiltinMapping.getTargetType(), ArrayKind.CHILD);
                        yield getArrayMapping(arrayType, targetType, elementBuiltinMapping, generateCode);
                    }
                }
                yield null;
            }
            default -> null;
        };
    }

    private FlowFieldMapping saveBuiltinFlowFieldMapping(Accessor property, FieldsObjectMapping declaringMapping,
                                                         Map<Flow, FlowFieldMapping> propertyFieldMappings, boolean generateCode) {
        var propertyMapping = propertyFieldMappings.get(property.getter);
        var underlyingField = property.underlyingField;
        var nestedMapping = tryGetBuiltinMapping(property.getter.getReturnType(),
                NncUtils.get(underlyingField, Property::getType), generateCode);
        if (propertyMapping == null) {
            propertyMapping = new FlowFieldMapping(null,
                    declaringMapping,
                    nestedMapping,
                    createTargetField(
                            declaringMapping.getTargetType(),
                            property.name,
                            property.code,
                            nestedMapping != null ? nestedMapping.getTargetType() : property.getter.getReturnType(),
                            property.isChild(),
                            false,
                            property.setter == null
                    ),
                    property.getter,
                    property.setter, null);
        } else {
            propertyMapping.setName(property.name);
            propertyMapping.setCode(property.code);
            propertyMapping.setFlows(property.getter, property.setter);
            propertyMapping.setNestedMapping(nestedMapping);
        }
        return propertyMapping;
    }

    private static @Nullable Method getSetter(ClassType type, String propertyCode, Type propertyType) {
        var flowCode = NamingUtils.getSetterName(propertyCode);
        var setter = type.findMethodByCodeAndParamTypes(flowCode, List.of(propertyType));
        if (setter != null && setter.isPublic() && !setter.isSynthetic())
            return setter;
        else
            return null;
    }

    private static List<Field> getVisibleFields(ClassType type) {
        return NncUtils.filter(type.getAllFields(), f -> f.getAccess() == Access.PUBLIC);
    }

    private static List<Accessor> getAccessors(ClassType type) {
        var accessors = new ArrayList<Accessor>();
        for (var method : type.getAllMethods()) {
            var p = getAccessor(method);
            if (p != null)
                accessors.add(p);
        }
        return accessors;
    }

    public static ClassInstance getSource(ClassInstance view) {
        var sourceField = view.getType().getFieldByCode("source");
        return (ClassInstance) view.getField(sourceField);
    }

    private static @Nullable Accessor getAccessor(Method getter) {
        if (!getter.isSynthetic() && getter.isPublic() && getter.getCode() != null
                && !getter.getReturnType().isVoid() && getter.getParameters().isEmpty()) {
            var matcher = GETTER_CODE_PATTERN.matcher(getter.getCode());
            if (matcher.matches()) {
                String propertyCode = NamingUtils.firstCharToLowerCase(matcher.group(1));
                var setter = getSetter(getter.getDeclaringType(), propertyCode, getter.getReturnType());
                var field = getter.getDeclaringType().findFieldByCode(propertyCode);
                if (field != null)
                    return new Accessor(getter, setter, field, field.getName(), propertyCode);
                else if (getter.getName().startsWith("获取") && getter.getName().length() > 2)
                    return new Accessor(getter, setter, null, getter.getName().substring(2), propertyCode);
                else
                    return new Accessor(getter, setter, null, getter.getName(), propertyCode);
            }
        }
        return null;
    }

    public ArrayMapping getArrayMapping(ArrayType sourceType,
                                        ArrayType targetType,
                                        Mapping elementMapping,
                                        boolean generateCode) {
        var sourceClass = (ClassType) sourceType.getInnermostElementType();
        var targetClass = (ClassType) targetType.getInnermostElementType();
        var mapping = sourceClass.findArrayMapping(sourceType, targetType, elementMapping);
        if (mapping == null) {
            if (sourceClass.getTemplate() == null) {
                mapping = new ArrayMapping(null, sourceType, targetType, elementMapping);
                mapping.generateDeclarations(compositeTypeFacade);
                sourceClass.addArrayMapping(mapping);
            } else {
                var sourceClassTemplate = sourceClass.getTemplate();
                var elementMappingTemplate = Objects.requireNonNull(elementMapping.getCopySource());
                Mapping m = elementMappingTemplate;
                while (m instanceof ArrayMapping am)
                    m = am.getElementMapping();
                var om = (ObjectMapping) m;
                var targetClassTemplate = (ClassType) Objects.requireNonNull(om).getTargetType();
                var sourceTemplate = (ArrayType) sourceType.accept(new TypeSubstitutor(
                        List.of(sourceClass), List.of(sourceClassTemplate),
                        compositeTypeFacade, new MockDTOProvider()
                ));
                var targetTemplate = (ArrayType) targetType.accept(new TypeSubstitutor(
                        List.of(targetClass), List.of(targetClassTemplate),
                        compositeTypeFacade, new MockDTOProvider()
                ));
                getArrayMapping(sourceTemplate, targetTemplate, elementMappingTemplate, generateCode);
                retransformClassType(sourceClassTemplate);
                mapping = Objects.requireNonNull(sourceClass.findArrayMapping(sourceType, targetType, elementMapping));
            }
        }
        if (!mapping.isCodeGenerated() && generateCode)
            mapping.generateCode(compositeTypeFacade);
        return mapping;
    }

    private record Accessor(
            Method getter,
            @Nullable Method setter,
            @Nullable Field underlyingField,
            String name,
            @Nullable String code
    ) {

        public boolean isChild() {
            return underlyingField != null && underlyingField.isChild();
        }

    }

}
