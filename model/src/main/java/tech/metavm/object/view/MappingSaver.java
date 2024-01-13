package tech.metavm.object.view;

import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.TypeParsingContext;
import tech.metavm.flow.Flow;
import tech.metavm.flow.Method;
import tech.metavm.flow.ValueFactory;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.type.*;
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
                context.getFunctionTypeContext(),
                new ContextArrayTypeProvider(context),
                context,
                new DefaultArrayMappingRepository(context));
    }

    // TODO MOVE TO NamingUtils
    private static final Pattern GETTER_CODE_PATTERN = Pattern.compile("^get([A-Z][A-Za-z0-9_$]+$)");

    private final InstanceProvider instanceProvider;
    private final IndexedTypeProvider typeProvider;
    private final FunctionTypeProvider functionTypeProvider;
    private final ArrayTypeProvider arrayTypeProvider;
    private final MappingProvider mappingProvider;
    private final ArrayMappingRepository arrayMappingRepository;

    public MappingSaver(InstanceProvider instanceProvider, IndexedTypeProvider typeProvider, FunctionTypeProvider functionTypeProvider, ArrayTypeProvider arrayTypeProvider, MappingProvider mappingProvider, ArrayMappingRepository arrayMappingRepository) {
        this.instanceProvider = instanceProvider;
        this.typeProvider = typeProvider;
        this.functionTypeProvider = functionTypeProvider;
        this.arrayTypeProvider = arrayTypeProvider;
        this.mappingProvider = mappingProvider;
        this.arrayMappingRepository = arrayMappingRepository;
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
            mapping = new FieldsObjectMapping(mappingDTO.tmpId(), mappingDTO.name(), mappingDTO.code(), sourceType, false,
                    NncUtils.map(mappingDTO.overriddenRefs(), sourceType::getMappingInAncestors));
            mapping.generateDeclarations(functionTypeProvider);
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
        mapping.generateCode(functionTypeProvider);
//        retransformMappingsIfRequired(mapping, context);
        return mapping;
    }

//    private static void retransformMappingsIfRequired(ObjectMapping mapping, IEntityContext context) {
//        var sourceType = mapping.getSourceType();
//        if (sourceType.isTemplate() && context.isPersisted(sourceType)) {
//            var templateInstances = context.getTemplateInstances(sourceType);
//            var genericContext = context.getGenericContext();
//            for (ClassType templateInstance : templateInstances) {
//                genericContext.retransformObjectMapping(mapping, templateInstance);
//                genericContext.retransformMethod(mapping.getReadMethod(), templateInstance);
//                genericContext.retransformMethod(mapping.getWriteMethod(), templateInstance);
//            }
//        }
//    }

    private FieldMapping saveFieldMapping(FieldMappingDTO fieldMappingDTO, FieldsObjectMapping containingMapping) {
        var nestedMapping = NncUtils.get(fieldMappingDTO.nestedMappingRef(), mappingProvider::getMapping);
        var fieldMapping = containingMapping.findFieldMapping(fieldMappingDTO.getRef());
        var sourceType = containingMapping.getSourceType();
        if (fieldMapping == null) {
            return switch (fieldMappingDTO.param()) {
                case DirectFieldMappingParam directParam -> new DirectFieldMapping(
                        fieldMappingDTO.tmpId(),
                        fieldMappingDTO.name(),
                        fieldMappingDTO.code(),
                        fieldMappingDTO.isChild(),
                        fieldMappingDTO.readonly(),
                        containingMapping,
                        nestedMapping,
                        sourceType.getField(Objects.requireNonNull(fieldMappingDTO.sourceFieldRef())),
                        null);
                case FlowFieldMappingParam flowParam -> new FlowFieldMapping(
                        fieldMappingDTO.tmpId(),
                        fieldMappingDTO.name(),
                        fieldMappingDTO.code(),
                        containingMapping,
                        nestedMapping,
                        fieldMappingDTO.isChild(),
                        sourceType.getMethod(flowParam.getterRef()),
                        NncUtils.get(flowParam.setterRef(), sourceType::getMethod),
                        null);
                case ComputedFieldMappingParam computedParam -> new ComputedFieldMapping(
                        fieldMappingDTO.tmpId(),
                        fieldMappingDTO.name(),
                        fieldMappingDTO.code(),
                        containingMapping,
                        nestedMapping,
                        fieldMappingDTO.isChild(),
                        ValueFactory.create(computedParam.value(),
                                createTypeParsingContext(containingMapping.getSourceType())));
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
                arrayTypeProvider, type
        );
    }

    public FieldsObjectMapping saveBuiltinMapping(ClassType type, boolean generateCode) {
        NncUtils.requireTrue(type.isClass());
        var mapping = (FieldsObjectMapping) NncUtils.find(type.getMappings(), ObjectMapping::isBuiltin);
        if (mapping == null) {
            mapping = new FieldsObjectMapping(null, "预设视图", "builtin", type, true, List.of());
            mapping.generateDeclarations(functionTypeProvider);
        }
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
            mapping.generateCode(functionTypeProvider);
//            retransformMappingsIfRequired(mapping, context);
        }
        return mapping;
    }

    private DirectFieldMapping saveBuiltinDirectFieldMapping(Field field,
                                                             FieldsObjectMapping declaringMapping,
                                                             Map<Field, DirectFieldMapping> directFieldMappings,
                                                             boolean generateCode) {
        var nestedMapping = field.isChild() ?
                tryGetBuiltinMapping(field.getType(), field.getType(), generateCode) : null;
        var fieldMapping = directFieldMappings.get(field);
        if (fieldMapping == null) {
            fieldMapping = new DirectFieldMapping(null, field.getName(), field.getCode(),
                    field.isChild(), field.isReadonly(), declaringMapping, nestedMapping, field, null);
        } else {
            fieldMapping.setName(field.getName());
            fieldMapping.setCode(field.getCode());
            fieldMapping.update(field, field.isReadonly());
            fieldMapping.setNestedMapping(nestedMapping);
        }
        return fieldMapping;
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
                                arrayTypeProvider.getArrayType(elementBuiltinMapping.getTargetType(), ArrayKind.CHILD);
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
            propertyMapping = new FlowFieldMapping(null, property.name, property.code,
                    declaringMapping, nestedMapping,
                    property.isChild(), property.getter, property.setter, null);
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
                                        @Nullable Mapping elementMapping,
                                        boolean generateCode) {
        var mapping = arrayMappingRepository.get(sourceType, targetType, elementMapping);
        if (mapping == null) {
            mapping = new ArrayMapping(null, sourceType, targetType, elementMapping);
            mapping.generateDeclarations(functionTypeProvider);
            arrayMappingRepository.add(mapping);
        }
        if (!mapping.isCodeGenerated() && generateCode)
            mapping.generateCode(functionTypeProvider);
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
