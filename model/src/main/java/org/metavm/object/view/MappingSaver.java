package org.metavm.object.view;

import org.metavm.common.ErrorCode;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.EntityRepository;
import org.metavm.entity.IEntityContext;
import org.metavm.expression.TypeParsingContext;
import org.metavm.flow.*;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.InstanceProvider;
import org.metavm.object.type.*;
import org.metavm.object.type.generic.SubstitutorV2;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.object.view.rest.dto.*;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class MappingSaver {

    public static final Logger logger = LoggerFactory.getLogger(MappingSaver.class);

    public static MappingSaver create(IEntityContext context) {
        return new MappingSaver(context.getInstanceContext(),
                new ContextTypeDefRepository(context),
                context,
                context
        );
    }

    // TODO MOVE TO NamingUtils
    private static final Pattern GETTER_CODE_PATTERN = Pattern.compile("^get([A-Z][A-Za-z0-9_$]*$)");

    private static final Pattern BOOL_GETTER_CODE_PATTERN = Pattern.compile("^is([A-Z][A-Za-z0-9_$]*$)");


    private final InstanceProvider instanceProvider;
    private final IndexedTypeDefProvider klassProvider;
    private final MappingProvider mappingProvider;
    private final EntityRepository entityRepository;

    public MappingSaver(InstanceProvider instanceProvider,
                        IndexedTypeDefProvider klassProvider,
                        MappingProvider mappingProvider,
                        EntityRepository entityRepository) {
        this.instanceProvider = instanceProvider;
        this.klassProvider = klassProvider;
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
        var sourceType = klassProvider.getKlass(Id.parse(mappingDTO.sourceType()));
        FieldsObjectMapping mapping = (FieldsObjectMapping) sourceType.findMapping(Id.parse(mappingDTO.id()));
        if (mapping == null) {
            var targetKlass = createTargetKlass(sourceType, "builtin", "builtin");
            mapping = new FieldsObjectMapping(mappingDTO.tmpId(), mappingDTO.name(), mappingDTO.code(), sourceType, false,
                    targetKlass.getType(), NncUtils.map(mappingDTO.overriddenIds(), id -> sourceType.getMappingInAncestors(Id.parse(id))));
            mapping.generateDeclarations();
        } else {
            mapping.setName(mappingDTO.name());
            mapping.setCode(mappingDTO.code());
            mapping.setOverridden(NncUtils.map(mappingDTO.overriddenIds(), id -> sourceType.getMappingInAncestors(Id.parse(id))));
        }
        if (mappingDTO.isDefault())
            mapping.setDefault();
        var param = (FieldsObjectMappingParam) mappingDTO.param();
        final var m = mapping;
        mapping.setFieldMappings(
                NncUtils.map(param.fieldMappings(), f -> saveFieldMapping(f, m))
        );
        mapping.generateCode();
        return mapping;
    }

    private void retransformClassType(Klass sourceKlass) {
        if (DebugEnv.debugging) {
            debugLogger.info("MappingSaver.retransformClassType sourceClass: {}", sourceKlass.getTypeDesc());
        }
        if (sourceKlass.isTemplate()) {
            for (Klass templateInstance : sourceKlass.getParameterized()) {
                templateInstance.setStage(ResolutionStage.INIT);
                var subst = new SubstitutorV2(
                        sourceKlass, sourceKlass.getTypeParameters(), templateInstance.getTypeArguments(),
                        ResolutionStage.DEFINITION
                );
                sourceKlass.accept(subst);
            }
        }
    }

    private FieldMapping saveFieldMapping(FieldMappingDTO fieldMappingDTO, FieldsObjectMapping containingMapping) {
        var nestedMapping = NncUtils.get(fieldMappingDTO.nestedMappingId(), id -> mappingProvider.getObjectMapping(Id.parse(id)));
        var codeGenerator = nestedMapping != null ? new ObjectNestedMapping(nestedMapping.getRef()) :
                new IdentityNestedMapping(FieldRef.create(fieldMappingDTO.targetFieldRef(), entityRepository).resolve().getType());
        var fieldMapping = containingMapping.findFieldMapping(Id.parse(fieldMappingDTO.id()));
        var sourceType = containingMapping.getSourceKlass();
        if (fieldMapping == null) {
            return switch (fieldMappingDTO.param()) {
                case DirectFieldMappingParam directParam -> {
                    var sourceField = FieldRef.create(requireNonNull(fieldMappingDTO.sourceFieldRef()), entityRepository).resolve();
                    yield new DirectFieldMapping(
                            fieldMappingDTO.tmpId(),
                            createTargetField(
                                    containingMapping.getTargetKlass(),
                                    fieldMappingDTO.name(),
                                    fieldMappingDTO.code(),
                                    getTargetFieldType(sourceField.getType(), codeGenerator),
                                    fieldMappingDTO.isChild(),
                                    sourceField.isTitle(),
                                    DirectFieldMapping.checkReadonly(sourceField, fieldMappingDTO.readonly())
                            ).getRef(),
                            containingMapping,
                            codeGenerator,
                            sourceField);
                }
                case FlowFieldMappingParam flowParam -> {
                    var getter = sourceType.getMethod(Id.parse(flowParam.getterId()));
                    var setter = NncUtils.get(flowParam.setterId(), id -> sourceType.getMethod(Id.parse(id)));
                    var objectNestedMapping = nestedMapping != null ? new ObjectNestedMapping(nestedMapping.getRef()) : null;
                    yield new FlowFieldMapping(
                            fieldMappingDTO.tmpId(),
                            containingMapping,
                            objectNestedMapping,
                            createTargetField(
                                    containingMapping.getTargetKlass(),
                                    fieldMappingDTO.name(),
                                    fieldMappingDTO.code(),
                                    getTargetFieldType(getter.getReturnType(), objectNestedMapping),
                                    fieldMappingDTO.isChild(),
                                    false,
                                    setter == null
                            ).getRef(),
                            getter,
                            setter,
                            null);
                }
                case ComputedFieldMappingParam computedParam -> {
                    var value = ValueFactory.create(computedParam.value(),
                            createTypeParsingContext(containingMapping.getSourceKlass()));
                    yield new ComputedFieldMapping(
                            fieldMappingDTO.tmpId(),
                            createTargetField(
                                    containingMapping.getTargetKlass(),
                                    fieldMappingDTO.name(),
                                    fieldMappingDTO.code(),
                                    getTargetFieldType(value.getType(), codeGenerator),
                                    fieldMappingDTO.isChild(),
                                    false,
                                    true
                            ).getRef(),
                            containingMapping,
                            codeGenerator,
                            value);
                }
                default -> throw new IllegalStateException("Unexpected value: " + fieldMappingDTO);
            };
        } else {
            fieldMapping.setName(fieldMappingDTO.name());
            fieldMapping.setCode(fieldMappingDTO.code());
            fieldMapping.setNestedMapping(nestedMapping != null ? new ObjectNestedMapping(nestedMapping.getRef()) : null);
            var param = fieldMappingDTO.param();
            switch (fieldMapping) {
                case DirectFieldMapping directFieldMapping -> directFieldMapping.update(
                        FieldRef.create(requireNonNull(fieldMappingDTO.sourceFieldRef()), entityRepository).resolve(),
                        fieldMappingDTO.readonly());
                case FlowFieldMapping flowFieldMapping -> {
                    var flowParam = (FlowFieldMappingParam) param;
                    var getter = sourceType.getMethod(Id.parse(flowParam.getterId()));
                    flowFieldMapping.setFlows(
                            getter,
                            NncUtils.get(flowParam.setterId(), id -> sourceType.getMethod(Id.parse(id))),
                            getTargetFieldType(getter.getReturnType(), codeGenerator)
                    );
                }
                case ComputedFieldMapping computedFieldMapping -> {
                    var computedParam = (ComputedFieldMappingParam) param;
                    computedFieldMapping.setValue(
                            ValueFactory.create(computedParam.value(),
                                    createTypeParsingContext(containingMapping.getSourceKlass()))
                    );
                }
                default -> throw new IllegalStateException("Unexpected value: " + fieldMapping);
            }
            return fieldMapping;
        }
    }

    private TypeParsingContext createTypeParsingContext(Klass type) {
        return new TypeParsingContext(
                instanceProvider,
                klassProvider,
                type
        );
    }

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public FieldsObjectMapping saveBuiltinMapping(Klass klass, boolean generateCode) {
        if(DebugEnv.printMapping)
            logger.info("saveBuiltinMapping. type: {}, generateCode: {}", klass.getTypeDesc(), generateCode);
        NncUtils.requireTrue(klass.isClass() || klass.isValue());
        var mapping = (FieldsObjectMapping) NncUtils.find(klass.getMappings(), ObjectMapping::isBuiltin);
        if (mapping == null) {
            var targetKlass = createTargetKlass(klass, "builtin", "builtin");
            mapping = new FieldsObjectMapping(null, "builtin", "builtin", klass, true, targetKlass.getType(), List.of());
            mapping.generateDeclarations();
        }
        retransformClassType(klass);
        if (klass.isStruct())
            saveFromViewMethod(klass, mapping, false);
        if (generateCode) {
            var directFieldMappings = NncUtils.toMap(
                    NncUtils.filterByType(mapping.getFieldMappings(), DirectFieldMapping.class),
                    DirectFieldMapping::getSourceField,
                    Function.identity()
            );
            var fieldMappings = new ArrayList<FieldMapping>();
            for (var field : getVisibleFields(klass)) {
                fieldMappings.add(saveBuiltinDirectFieldMapping(field, mapping, directFieldMappings));
            }
            var propertyFieldMappings = NncUtils.toMap(
                    NncUtils.filterByType(mapping.getFieldMappings(), FlowFieldMapping.class),
                    FlowFieldMapping::getGetter,
                    Function.identity()
            );
            for (var accessor : getAccessors(klass))
                fieldMappings.add(saveBuiltinFlowFieldMapping(accessor, mapping, propertyFieldMappings));
            mapping.setFieldMappings(fieldMappings);
            mapping.generateCode();
            if (klass.isStruct())
                saveFromViewMethod(klass, mapping, true);
            retransformClassType(mapping.getTargetKlass().getEffectiveTemplate());
            retransformClassType(klass);
        }
        if(DebugEnv.printMapping)
            logger.info(mapping.getText());
        return mapping;
    }

    private void saveFromViewMethod(Klass klass, FieldsObjectMapping mapping, boolean generateCode) {
        var viewType = mapping.getTargetType();
        var canonicalConstructor = getCanonicalConstructor(klass);
        var fromView = klass.findMethodByCodeAndParamTypes("fromView", List.of(viewType));
        if (fromView == null) {
            fromView = MethodBuilder.newBuilder(klass, "fromView", "fromView")
                    .parameters(new Parameter(null, "view", "view", viewType))
                    .returnType(klass.getType())
                    .isStatic(true)
                    .isSynthetic(true)
                    .build();
        }
        if (generateCode) {
            fromView.clearContent();
            var scope = fromView.getRootScope();
            var inputNode = Nodes.input(fromView);
            var view = Nodes.value(scope.nextNodeName("view"), Values.nodeProperty(inputNode, inputNode.getType().resolve().getFieldByCode("view")), scope);

            var fieldValues = new HashMap<String, Supplier<Value>>();
            for (FieldMapping fieldMapping : mapping.getFieldMappings()) {
                var nestedMapping = fieldMapping.getNestedMapping();
                if (nestedMapping == null)
                    fieldValues.put(fieldMapping.getTargetField().getCode(), () -> Values.nodeProperty(view, fieldMapping.getTargetField()));
                else {
                    var fieldValue = nestedMapping.generateUnmappingCode(
                            () -> Values.nodeProperty(view, fieldMapping.getTargetField()),
                            scope
                    );
                    fieldValues.put(fieldMapping.getTargetField().getCode(), fieldValue);
                }
            }

            var newNode = Nodes.newObject(
                    scope.nextNodeName("newObject"),
                    fromView.getRootScope(),
                    canonicalConstructor,
                    NncUtils.biMap(
                            viewType.resolve().getAllFields(),
                            canonicalConstructor.getParameters(),
                            (f, p) -> new Argument(
                                    null,
                                    p.getRef(),
                                    Objects.requireNonNull(fieldValues.get(p.getCode()),
                                            () -> "Can not find field value for: " + p.getCode()).get()
                            )
                    ),
                    false,
                    false
            );
            Nodes.ret(scope.nextNodeName("return"), scope, Values.node(newNode));
        }
    }

    private Method getCanonicalConstructor(Klass klass) {
        var fields = NncUtils.merge(
                NncUtils.map(getVisibleFields(klass), f -> new NameAndType(f.getCodeRequired(), f.getType())),
                NncUtils.map(getAccessors(klass), a -> new NameAndType(requireNonNull(a.code), a.getter.getReturnType()))
        );
        var fieldTypes = NncUtils.toMap(fields, f -> f.name, f -> f.type);
        var constructor = NncUtils.find(
                klass.getMethods(),
                m -> m.isConstructor() && isCanonicalConstructor(m, fieldTypes)
        );
        if (constructor == null)
            throw new BusinessException(ErrorCode.ENTITY_STRUCT_LACKS_CANONICAL_CONSTRUCTOR, klass.getName());
        return constructor;
    }

    private boolean isCanonicalConstructor(Method constructor, Map<String, Type> fieldTypes) {
        if (constructor.getParameters().size() == fieldTypes.size()) {
            return NncUtils.allMatch(
                    constructor.getParameters(),
                    p -> Objects.equals(p.getType(), fieldTypes.get(p.getName()))
            );
        } else
            return false;
    }

    private record NameAndType(
            String name,
            Type type
    ) {
    }

    private DirectFieldMapping saveBuiltinDirectFieldMapping(Field field,
                                                             FieldsObjectMapping containingMapping,
                                                             Map<Field, DirectFieldMapping> directFieldMappings) {
        var codeGenerator = field.isChild() ? getNestedMapping(field.getType(), field.getType()) :
                new IdentityNestedMapping(field.getType());
        var fieldMapping = directFieldMappings.get(field);
        if (fieldMapping == null) {
            fieldMapping = new DirectFieldMapping(
                    null, createTargetField(containingMapping.getTargetKlass(), field.getName(), field.getCode(),
                    codeGenerator.getTargetType(),
                    field.isChild(), field.isTitle(), field.isReadonly()).getRef(),
                    containingMapping, codeGenerator, field
            );
        } else {
            fieldMapping.setName(field.getName());
            fieldMapping.setCode(field.getCode());
            fieldMapping.update(field, field.isReadonly());
            fieldMapping.setTargetFieldType(codeGenerator.getTargetType());
            fieldMapping.setNestedMapping(codeGenerator);
        }
        return fieldMapping;
    }

    private Type getTargetFieldType(Type targetFieldType, @Nullable NestedMapping nestedMapping) {
        return FieldMapping.getTargetFieldType(targetFieldType, nestedMapping);
    }

    private Klass createTargetKlass(Klass sourceKlass, String name, @Nullable String code) {
        var viewTypeName = getTargetTypeName(sourceKlass, name);
        var viewTypeCode = getTargetTypeCode(sourceKlass, code);
        if (sourceKlass.isTemplate()) {
            var template = KlassBuilder.newBuilder(viewTypeName, viewTypeCode)
                    .isTemplate(true)
                    .ephemeral(true)
                    .struct(true)
                    .anonymous(true)
                    .typeParameters(NncUtils.map(
                            sourceKlass.getTypeParameters(),
                            p -> new TypeVariable(null, p.getName(), p.getCode(), DummyGenericDeclaration.INSTANCE)
                    ))
                    .build();
            var subst = new SubstitutorV2(
                    template, template.getTypeParameters(),
                    NncUtils.map(sourceKlass.getTypeParameters(), TypeVariable::getType), ResolutionStage.INIT
            );
            return (Klass) template.accept(subst);
        } else {
            return KlassBuilder.newBuilder(viewTypeName, viewTypeCode)
                    .ephemeral(true)
                    .anonymous(true)
                    .struct(true)
                    .build();
        }
    }

    public static String getTargetTypeName(Klass sourceType, String mappingName) {
        if (mappingName.endsWith("View") && mappingName.length() > 4)
            mappingName = mappingName.substring(0, mappingName.length() - 4);
        return NamingUtils.escapeTypeName(sourceType.getName()) + mappingName + "View";
    }

    public static @Nullable String getTargetTypeCode(Klass sourceType, @Nullable String mappingCode) {
        if (sourceType.getCode() == null || mappingCode == null)
            return null;
        if (mappingCode.endsWith("View") && mappingCode.length() > 4)
            mappingCode = mappingCode.substring(0, mappingCode.length() - 4);
        return NamingUtils.escapeTypeName(sourceType.getCode()) + mappingCode + "View";
    }

    private Field createTargetField(Klass targetType, String name, String code, Type type,
                                    boolean isChild, boolean asTitle, boolean readonly) {
        if (targetType.getTemplate() != null) {
            var template = requireNonNull(targetType.getTemplate());
            var typeSubst = new TypeSubstitutor(
                    targetType.getTypeArguments(),
                    NncUtils.map(template.getTypeParameters(), TypeVariable::getType)
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
                    targetType.getTypeArguments(), ResolutionStage.DECLARATION
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
                        var underlyingElementType = underlyingClassType.getFirstTypeArgument();
                        var elementNestedMapping = underlyingClassType.isChildList() ?
                                getNestedMapping(underlyingClassType.getFirstTypeArgument(), underlyingElementType) :
                                new IdentityNestedMapping(classType.getFirstTypeArgument());
                        var targetType = (ClassType) underlyingClassType.accept(new TypeSubstitutor(
                                List.of(underlyingClassType.getFirstTypeArgument()),
                                List.of(elementNestedMapping.getTargetType())
                        ));
                        yield new ListNestedMapping(classType, targetType, elementNestedMapping);
                    }
                    yield new IdentityNestedMapping(type);
                } else {
                    var nestedMapping = classType.resolve().getBuiltinMapping();
                    yield nestedMapping != null ? new ObjectNestedMapping(nestedMapping.getRef()) : new IdentityNestedMapping(type);
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
                            List.of(elementNestedMapping.getTargetType())
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
                var targetType = new UnionType(targetMemberTypes);
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
                            declaringMapping.getTargetKlass(),
                            property.name,
                            property.code,
                            codeGenerator.getTargetType(),
                            property.isChild(),
                            false,
                            property.setter == null
                    ).getRef(),
                    property.getter,
                    property.setter, null);
        } else {
            propertyMapping.setName(property.name);
            propertyMapping.setCode(property.code);
            propertyMapping.setNestedMapping(codeGenerator);
            propertyMapping.setFlows(property.getter, property.setter, codeGenerator.getTargetType());
        }
        return propertyMapping;
    }

    private static @Nullable Method getSetter(Klass type, String propertyCode, Type propertyType) {
        var flowCode = NamingUtils.getSetterName(propertyCode);
        var setter = type.findMethodByCodeAndParamTypes(flowCode, List.of(propertyType));
        if (setter != null && setter.isPublic() && !setter.isSynthetic())
            return setter;
        else
            return null;
    }

    private static List<Field> getVisibleFields(Klass type) {
        return NncUtils.filter(type.getAllFields(), f -> f.getAccess() == Access.PUBLIC);
    }

    private static List<Accessor> getAccessors(Klass type) {
        var accessors = new ArrayList<Accessor>();
        for (var method : type.getAllMethods()) {
            var p = getAccessor(method);
            if (p != null)
                accessors.add(p);
        }
        return accessors;
    }

    public static ClassInstance getSource(ClassInstance view) {
        var sourceField = view.getKlass().getFieldByCode("source");
        return (ClassInstance) view.getField(sourceField);
    }

    private static @Nullable Accessor getAccessor(Method getter) {
        if (!getter.isSynthetic() && getter.isPublic() && getter.getCode() != null
                && !getter.getReturnType().isVoid() && getter.getParameters().isEmpty()) {
            var code2field = getter.getDeclaringType().getAllFields().stream()
                    .filter(f -> f.getCode() != null)
                    .collect(Collectors.toMap(Field::getCode, Function.identity()));
            var field = code2field.get(getter.getCode());
            if (field != null)
                return new Accessor(getter, null, field, field.getName(), field.getCode());
            var matcher = GETTER_CODE_PATTERN.matcher(getter.getCode());
            if (matcher.matches()) {
                return getAccessor0(matcher, getter, "get");
            }
            if (getter.getReturnType().isBoolean()) {
                matcher = BOOL_GETTER_CODE_PATTERN.matcher(getter.getCode());
                if (matcher.matches()) {
                    return getAccessor0(matcher, getter, "is");
                }
            }
        }
        return null;
    }

    private static Accessor getAccessor0(Matcher matcher, Method getter, String getterPrefix) {
        String propertyCode = NamingUtils.firstCharToLowerCase(matcher.group(1));
        var setter = getSetter(getter.getDeclaringType(), propertyCode, getter.getReturnType());
        var field = getter.getDeclaringType().findFieldByCode(propertyCode);
        if (field != null)
            return new Accessor(getter, setter, field, field.getName(), propertyCode);
        else if (getter.getName().startsWith("get") && getter.getName().length() > 3)
            return new Accessor(getter, setter, null, getter.getName().substring(2), propertyCode);
        else if (getter.getName().startsWith(getterPrefix) && getter.getName().length() > getterPrefix.length())
            return new Accessor(getter, setter, null,
                    NamingUtils.firstCharToLowerCase(getter.getName().substring(3)), propertyCode);
        else
            return new Accessor(getter, setter, null, getter.getName(), propertyCode);
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
