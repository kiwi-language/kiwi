package tech.metavm.object.view;

import tech.metavm.entity.DummyGenericDeclaration;
import tech.metavm.entity.EntityRepository;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.StandardTypes;
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
import java.util.*;
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
                context,
                context
                );
    }

    // TODO MOVE TO NamingUtils
    private static final Pattern GETTER_CODE_PATTERN = Pattern.compile("^get([A-Z][A-Za-z0-9_$]+$)");


    private final InstanceProvider instanceProvider;
    private final IndexedTypeProvider typeProvider;
    private final ParameterizedTypeProvider parameterizedTypeProvider;
    private final ParameterizedFlowProvider parameterizedFlowProvider;
    private final CompositeTypeFacade compositeTypeFacade;
    private final MappingProvider mappingProvider;
    private final EntityRepository entityRepository;

    public MappingSaver(InstanceProvider instanceProvider,
                        IndexedTypeProvider typeProvider,
                        CompositeTypeFacade compositeTypeFacade,
                        ParameterizedTypeProvider parameterizedTypeProvider,
                        ParameterizedFlowProvider parameterizedFlowProvider,
                        MappingProvider mappingProvider,
                        EntityRepository entityRepository) {
        this.instanceProvider = instanceProvider;
        this.typeProvider = typeProvider;
        this.parameterizedTypeProvider = parameterizedTypeProvider;
        this.parameterizedFlowProvider = parameterizedFlowProvider;
        this.compositeTypeFacade = compositeTypeFacade;
        this.mappingProvider = mappingProvider;
        this.entityRepository = entityRepository;
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
            var targetType = createTargetType(sourceType, "预设视图", "builtin");
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
                        ResolutionStage.DEFINITION, entityRepository, compositeTypeFacade, parameterizedTypeProvider,
                        parameterizedFlowProvider, new MockDTOProvider()
                );
                sourceType.accept(subst);
            }
        }
    }

    private FieldMapping saveFieldMapping(FieldMappingDTO fieldMappingDTO, FieldsObjectMapping containingMapping) {
        var nestedMapping = NncUtils.get(fieldMappingDTO.nestedMappingRef(), mappingProvider::getObjectMapping);
        var codeGenerator = nestedMapping != null ? new ObjectNestedMapping(nestedMapping) :
                new IdentityNestedMapping(typeProvider.getType(fieldMappingDTO.targetFieldRef()));
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
                                    getTargetFieldType(sourceField.getType(), nestedMapping),
                                    fieldMappingDTO.isChild(),
                                    sourceField.isTitle(),
                                    DirectFieldMapping.checkReadonly(sourceField, fieldMappingDTO.readonly())
                            ),
                            containingMapping,
                            codeGenerator,
                            sourceField);
                }
                case FlowFieldMappingParam flowParam -> {
                    var getter = sourceType.getMethod(flowParam.getterRef());
                    var setter = NncUtils.get(flowParam.setterRef(), sourceType::getMethod);
                    yield new FlowFieldMapping(
                            fieldMappingDTO.tmpId(),
                            containingMapping,
                            nestedMapping != null ? new ObjectNestedMapping(nestedMapping) : null,
                            createTargetField(
                                    containingMapping.getTargetType(),
                                    fieldMappingDTO.name(),
                                    fieldMappingDTO.code(),
                                    getTargetFieldType(getter.getReturnType(), nestedMapping),
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
                                    getTargetFieldType(value.getType(), nestedMapping),
                                    fieldMappingDTO.isChild(),
                                    false,
                                    true
                            ),
                            containingMapping,
                            codeGenerator,
                            value);
                }
                default -> throw new IllegalStateException("Unexpected value: " + fieldMappingDTO);
            };
        } else {
            fieldMapping.setName(fieldMappingDTO.name());
            fieldMapping.setCode(fieldMappingDTO.code());
            fieldMapping.setNestedMapping(nestedMapping != null ? new ObjectNestedMapping(nestedMapping) : null, compositeTypeFacade);
            var param = fieldMappingDTO.param();
            switch (fieldMapping) {
                case DirectFieldMapping directFieldMapping -> directFieldMapping.update(
                        sourceType.getField(Objects.requireNonNull(fieldMappingDTO.sourceFieldRef())),
                        fieldMappingDTO.readonly(), compositeTypeFacade);
                case FlowFieldMapping flowFieldMapping -> {
                    var flowParam = (FlowFieldMappingParam) param;
                    var getter = sourceType.getMethod(flowParam.getterRef());
                    flowFieldMapping.setFlows(
                            getter,
                            NncUtils.get(flowParam.setterRef(), sourceType::getMethod),
                            getTargetFieldType(getter.getReturnType(), nestedMapping),
                            compositeTypeFacade);
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
            var targetType = createTargetType(type, "预设视图", "builtin");
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
                fieldMappings.add(saveBuiltinDirectFieldMapping(field, mapping, directFieldMappings));
            var propertyFieldMappings = NncUtils.toMap(
                    NncUtils.filterByType(mapping.getFieldMappings(), FlowFieldMapping.class),
                    FlowFieldMapping::getGetter,
                    Function.identity()
            );
            for (var accessor : getAccessors(type))
                fieldMappings.add(saveBuiltinFlowFieldMapping(accessor, mapping, propertyFieldMappings));
            mapping.setFieldMappings(fieldMappings);
            mapping.generateCode(compositeTypeFacade);
            retransformClassType(mapping.getTargetType().getEffectiveTemplate());
            retransformClassType(type);
        }
        return mapping;
    }

    private DirectFieldMapping saveBuiltinDirectFieldMapping(Field field,
                                                             FieldsObjectMapping containingMapping,
                                                             Map<Field, DirectFieldMapping> directFieldMappings) {
        var codeGenerator = field.isChild() ? getNestedMapping(field.getType(), field.getType()) :
                new IdentityNestedMapping(field.getType());
        var fieldMapping = directFieldMappings.get(field);
        if (fieldMapping == null) {
            fieldMapping = new DirectFieldMapping(
                    null, createTargetField(containingMapping.getTargetType(), field.getName(), field.getCode(),
                    codeGenerator.getTargetType(),
                    field.isChild(), field.isTitle(), field.isReadonly()),
                    containingMapping, codeGenerator, field
            );
        } else {
            fieldMapping.setName(field.getName());
            fieldMapping.setCode(field.getCode());
            fieldMapping.update(field, field.isReadonly(), compositeTypeFacade);
            fieldMapping.setTargetFieldType(codeGenerator.getTargetType());
            fieldMapping.setNestedMapping(codeGenerator, compositeTypeFacade);
        }
        return fieldMapping;
    }

    private Type getTargetFieldType(Type targetFieldType, @Nullable ObjectMapping nestedMapping) {
        return FieldMapping.getTargetFieldType(targetFieldType, nestedMapping, compositeTypeFacade);
    }

    private ClassType createTargetType(ClassType sourceType, String name, @Nullable String code) {
        var viewTypeName = getTargetTypeName(sourceType, name);
        var viewTypeCode = getTargetTypeCode(sourceType, code);
        if (sourceType.isTemplate()) {
            var template = ClassTypeBuilder.newBuilder(viewTypeName, viewTypeCode)
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
                    entityRepository, compositeTypeFacade,
                    parameterizedTypeProvider,
                    null, new MockDTOProvider()
            );
            return (ClassType) template.accept(subst);
        } else {
            return ClassTypeBuilder.newBuilder(viewTypeName, viewTypeCode)
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

    public static @Nullable String getTargetTypeCode(ClassType sourceType, @Nullable String mappingCode) {
        if (sourceType.getCode() == null || mappingCode == null)
            return null;
        if (mappingCode.endsWith("View") && mappingCode.length() > 4)
            mappingCode = mappingCode.substring(0, mappingCode.length() - 4);
        return NamingUtils.escapeTypeName(sourceType.getCode()) + mappingCode + "View";
    }

    private Field createTargetField(ClassType targetType, String name, String code, Type type,
                                    boolean isChild, boolean asTitle, boolean readonly) {
        if (targetType.getTemplate() != null) {
            var template = Objects.requireNonNull(targetType.getTemplate());
            var typeSubst = new TypeSubstitutor(
                    targetType.getTypeArguments(),
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
                    entityRepository,
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

    private NestedMapping getNestedMapping(Type type, @Nullable Type underlyingType) {
        return switch (type) {
            case ClassType classType -> {
                if (classType.isList()) {
                    if (underlyingType instanceof ClassType underlyingClassType && underlyingClassType.isList()) {
                        var underlyingElementType = underlyingClassType.getListElementType();
                        var elementNestedMapping = underlyingClassType.isChildList() ?
                                getNestedMapping(underlyingClassType.getListElementType(), underlyingElementType) :
                                new IdentityNestedMapping(classType.getListElementType());
                        var targetType = (ClassType) underlyingClassType.accept(new TypeSubstitutor(
                                List.of(underlyingClassType.getListElementType()),
                                List.of(elementNestedMapping.getTargetType()),
                                compositeTypeFacade,
                                new MockDTOProvider()
                        ));;
                        yield new ListNestedMapping(classType, targetType,
                                parameterizedTypeProvider.getParameterizedType(
                                        StandardTypes.getReadWriteListType(),
                                        List.of(classType.getListElementType())
                                ),
                                parameterizedTypeProvider.getParameterizedType(
                                        StandardTypes.getReadWriteListType(),
                                        List.of(targetType.getListElementType())
                                ),
                                elementNestedMapping);
                    }
                    yield new IdentityNestedMapping(type);
                } else {
                    var nestedMapping = classType.getBuiltinMapping();
                    yield nestedMapping != null ? new ObjectNestedMapping(nestedMapping) : new IdentityNestedMapping(type);
                }
            }
            case ArrayType arrayType -> {
                if (underlyingType instanceof ArrayType underlyingArrayType) {
                    var underlyingElementType = underlyingArrayType.getElementType();
                    var elementNestedMapping = underlyingArrayType.isChildArray() ?
                            getNestedMapping(arrayType.getElementType(), underlyingElementType) :
                            new IdentityNestedMapping(arrayType.getElementType());
                    var typeSubst = (ArrayType) underlyingArrayType.accept(new TypeSubstitutor(
                            List.of(underlyingArrayType.getElementType()),
                            List.of(elementNestedMapping.getTargetType()),
                            compositeTypeFacade,
                            new MockDTOProvider()
                    ));
                    yield new ArrayNestedMapping(arrayType, typeSubst, elementNestedMapping);
                }
                yield new IdentityNestedMapping(type);
            }
            case UnionType unionType -> {
                List<NestedMapping> memberNestedMappings = new ArrayList<>();
                Set<Type> targetMemberTypes = new HashSet<>();
                for (Type member : unionType.getMembers()) {
                    var codeGenerator = getNestedMapping(member, NncUtils.get(underlyingType,
                            t -> Types.getViewType(member, (UnionType) t)));
                    targetMemberTypes.add(codeGenerator.getTargetType());
                    memberNestedMappings.add(codeGenerator);
                }
                var targetType = compositeTypeFacade.getUnionType(targetMemberTypes);
                yield new UnionNestedMapping(unionType, targetType, memberNestedMappings);
            }
            default -> new IdentityNestedMapping(type);
        };
    }

    private FlowFieldMapping saveBuiltinFlowFieldMapping(Accessor property, FieldsObjectMapping declaringMapping,
                                                         Map<Flow, FlowFieldMapping> propertyFieldMappings) {
        var propertyMapping = propertyFieldMappings.get(property.getter);
        var underlyingField = property.underlyingField;
        var codeGenerator = underlyingField != null && underlyingField.isChild() ?
                getNestedMapping(property.getter.getReturnType(), underlyingField.getType()) :
                new IdentityNestedMapping(property.getter.getReturnType());
        if (propertyMapping == null) {
            propertyMapping = new FlowFieldMapping(null,
                    declaringMapping,
                    codeGenerator,
                    createTargetField(
                            declaringMapping.getTargetType(),
                            property.name,
                            property.code,
                            codeGenerator.getTargetType(),
                            property.isChild(),
                            false,
                            property.setter == null
                    ),
                    property.getter,
                    property.setter, null);
        } else {
            propertyMapping.setName(property.name);
            propertyMapping.setCode(property.code);
            propertyMapping.setNestedMapping(codeGenerator, compositeTypeFacade);
            propertyMapping.setFlows(property.getter, property.setter, codeGenerator.getTargetType(), compositeTypeFacade);
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
                else if (getter.getName().startsWith("get") && getter.getName().length() > 3)
                    return new Accessor(getter, setter, null,
                            NamingUtils.firstCharToLowerCase(getter.getName().substring(3)), propertyCode);
                else
                    return new Accessor(getter, setter, null, getter.getName(), propertyCode);
            }
        }
        return null;
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
