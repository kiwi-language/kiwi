package tech.metavm.object.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.dto.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.Natives;
import tech.metavm.expression.Var;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ReferenceKind;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.object.meta.rest.dto.ClassTypeParam;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static tech.metavm.util.ContextUtil.getTenantId;
import static tech.metavm.util.NncUtils.*;

@EntityType("Class类型")
public class ClassType extends Type implements GenericDeclaration {

    public static final IndexDef<ClassType> UNIQUE_NAME = IndexDef.uniqueKey(ClassType.class, "name");

    public static final IndexDef<ClassType> IDX_PARAMETERIZED_TYPE_KEY =
            IndexDef.uniqueKey(ClassType.class, "parameterizedTypeKey");

    public static final IndexDef<ClassType> UNIQUE_CODE = IndexDef.uniqueKey(ClassType.class, "code");

    public static final IndexDef<ClassType> TEMPLATE_IDX = IndexDef.normalKey(ClassType.class, "template");

    public static final IndexDef<ClassType> TYPE_ARGUMENTS_IDX = IndexDef.normalKey(ClassType.class, "typeArguments");

    @EntityField("超类")
    @Nullable
    private ClassType superType;
    @ChildEntity("接口")
    private final ReadWriteArray<ClassType> interfaces = new ReadWriteArray<>(ClassType.class);
    @EntityField("来源")
    private ClassSource source;
    @ChildEntity("子类列表")
    private final ReadWriteArray<ClassType> subTypes = new ReadWriteArray<>(ClassType.class);
    @EntityField("描述")
    private @Nullable String desc;
    @ChildEntity("字段列表")
    private final ChildArray<Field> fields = new ChildArray<>(Field.class);
    @ChildEntity("流程列表")
    private final ChildArray<Flow> flows = new ChildArray<>(Flow.class);
    @ChildEntity("静态字段列表")
    private final ChildArray<Field> staticFields = new ChildArray<>(Field.class);
    @ChildEntity("约束列表")
    private final ChildArray<Constraint<?>> constraints = new ChildArray<>(new TypeReference<>() {
    });
    @EntityField("集合名称")
    @Nullable
    private final String collectionName;
    @Nullable
    @EntityField("模板")
    private final ClassType template;
    @ChildEntity("类型参数")
    private final ChildArray<TypeVariable> typeParameters = new ChildArray<>(TypeVariable.class);
    @ChildEntity("类型实参")
    private final ReadWriteArray<Type> typeArguments = new ReadWriteArray<>(Type.class);
    // TODO (Important!) not scalable, must be optimized before going to production
    @ChildEntity("依赖")
    private final ReadWriteArray<ClassType> dependencies = new ReadWriteArray<>(ClassType.class);
    @Nullable
    private String parameterizedTypeKey;

    private transient ResolutionStage stage = ResolutionStage.CREATED;

    private transient FlowTable flowTable;

    public ClassType(
            Long tmpId,
            String name,
            @Nullable String code,
            @Nullable ClassType superType,
            List<ClassType> interfaces,
            TypeCategory category,
            ClassSource source,
            boolean anonymous,
            boolean ephemeral,
            @Nullable String desc,
            @Nullable String templateName,
            boolean isTemplate,
            @Nullable ClassType template,
            List<Type> typeArguments
//            @Nullable
//            GenericClass genericSuperType,
//            List<GenericClass> genericInterfaces
    ) {
        super(name, anonymous, ephemeral, category);
        setTmpId(tmpId);
        this.setCode(code);
        setSuperType(superType);
        setInterfaces(interfaces);
        this.source = source;
        this.desc = desc;
        setTemplate(isTemplate);
        this.collectionName = templateName;
        this.typeArguments.addAll(typeArguments);
//        this.genericSuperType = genericSuperType;
//        this.genericInterfaces.addAll(genericInterfaces);
        this.template = template;
        flowTable.rebuild();
    }

    public void update(TypeDTO typeDTO) {
        setName(typeDTO.name());
        ClassTypeParam param = (ClassTypeParam) typeDTO.param();
        setDesc(param.desc());
    }

    public void setDesc(@Nullable String desc) {
        this.desc = desc;
    }

    void addSubType(ClassType subType) {
        if (subTypes.contains(subType)) {
            throw new InternalException("Subtype '" + subType + "' is already added to this type");
        }
        subTypes.add(subType);
    }

    void removeSubType(ClassType subType) {
        subTypes.remove(subType);
    }

    @Nullable
    @SuppressWarnings("unused")
    public String getDesc() {
        return desc;
    }

    public List<Field> getFields() {
        return readyFields();
    }

    public List<Field> getAllFields() {
        if (superType != null) {
            return NncUtils.union(superType.getAllFields(), readyFields());
        } else {
            return readyFields();
        }
    }

    public List<Field> getFieldsByPath(List<String> path) {
        List<Field> result = new ArrayList<>();
        getFieldsByPath0(path, result);
        return result;
    }

    private void getFieldsByPath0(List<String> path, List<Field> result) {
        NncUtils.requireMinimumSize(path, 1);
        String fieldName = path.get(0);
        Field field = getFieldNyNameRequired(fieldName);
        result.add(field);
        if (path.size() > 1) {
            Type fieldType = field.getType();
            if (fieldType instanceof ClassType classType) {
                classType.getFieldsByPath0(path.subList(1, path.size()), result);
            } else {
                throw new InternalException("Invalid field path '" + NncUtils.join(path, ".") + "'");
            }
        }
    }

    public ReadonlyArray<Flow> getFlows() {
        return flows;
    }

    public List<Flow> getAllFlows() {
        if (superType != null) {
            return NncUtils.union(superType.getAllFlows(), NncUtils.listOf(flows));
        } else {
            return NncUtils.listOf(getFlows());
        }
    }

    public Flow getDefaultConstructor() {
        return getFlowByCodeAndParamTypes(getEffectiveTemplate().getCode(), List.of());
    }

    public ReadonlyArray<Flow> getDeclaredFlows() {
        return flows;
    }

    public Flow getFlow(long id) {
        return findById(flows, id);
    }

    public Flow tryGetFlow(String name, List<Type> parameterTypes) {
        var flow = NncUtils.find(flows,
                f -> Objects.equals(f.getName(), name) && f.getParameterTypes().equals(parameterTypes));
        if (flow != null) {
            return flow;
        }
        if (superType != null) {
            return superType.getFlow(name, parameterTypes);
        }
        return null;
    }

    public Flow getFlow(String name, List<Type> parameterTypes) {
        return NncUtils.requireNonNull(
                tryGetFlow(name, parameterTypes),
                () -> new InternalException("Can not find flow '" + name + "(" +
                        NncUtils.join(parameterTypes, Type::getName, ",")
                        + ")' in type '" + getName() + "'")
        );
    }

    public Flow getFlowByCodeAndParamTypes(String code, List<Type> parameterTypes) {
        var flow = NncUtils.find(flows,
                f -> Objects.equals(f.getCode(), code) && f.getParameterTypes().equals(parameterTypes));
        if (flow != null) {
            return flow;
        }
        if (superType != null) {
            return superType.getFlowByCodeAndParamTypes(code, parameterTypes);
        }
        throw new InternalException("Can not find flow '" + code + "(" +
                NncUtils.join(parameterTypes, Type::getName, ",")
                + ")' in type '" + getName() + "'");
    }

    public Flow getFlowByCode(String code) {
        return getFlow(Property::getCode, code);
    }

    public Flow getFlowByTemplate(Flow template) {
        return getFlow(Flow::getRootTemplate, template);
    }

    public <T> Flow getFlow(IndexMapper<Flow, T> property, T value) {
        var flow = flows.get(property, value);
        if (flow != null) {
            return flow;
        }
        if (superType != null) {
            return superType.getFlow(property, value);
        }
        return null;
    }

    @Override
    public Set<TypeVariable> getVariables() {
        return NncUtils.flatMapUnique(typeArguments, Type::getVariables);
    }

    public void removeFlow(Flow flow) {
        flows.remove(flow);
        getFlowTable().rebuild();
    }

    public void addFlow(Flow flow) {
        if (flows.contains(flow)) {
            throw new InternalException("Flow '" + flow + "' is already added to the class type");
        }
        flows.addChild(flow);
        getFlowTable().rebuild();
    }

    public ReadonlyArray<Field> getDeclaredFields() {
        return fields;
    }

    public ReadonlyArray<Constraint<?>> getDeclaredConstraints() {
        return constraints;
    }

    public List<ClassType> getSubTypes() {
        return subTypes.toList();
    }

    public void addField(Field field) {
        if (field.getId() != null && getField(field.getId()) != null) {
            throw new RuntimeException("Field " + field.getId() + " is already added");
        }
        if (tryGetFieldByName(field.getName()) != null || tryGetStaticFieldByName(field.getName()) != null) {
            throw BusinessException.invalidField(field, "字段名称'" + field.getName() + "'已存在");
        }
        if (field.getCode() != null &&
                (getFieldByCode(field.getCode()) != null || getStaticFieldByCode(field.getCode()) != null)) {
            throw BusinessException.invalidField(field, "字段编号" + field.getCode() + "已存在");
        }
        if (field.isAsTitle() && getTileField() != null) {
            throw BusinessException.multipleTitleFields();
        }
        if (field.isStatic()) {
            staticFields.addChild(field);
        } else {
            fields.addChild(field);
        }
    }

    public Set<Long> getTypeIdsInHierarchy() {
        Set<Long> typeIds = new HashSet<>();
        typeIds.add(this.id);
        for (ClassType subType : subTypes) {
            typeIds.addAll(subType.getTypeIdsInHierarchy());
        }
        return typeIds;
    }

    public List<Index> getFieldIndices(Field field) {
        return NncUtils.filter(
                getConstraints(Index.class),
                index -> index.isFieldIndex(field)
        );
    }

    public void addConstraint(Constraint<?> constraint) {
        constraints.addChild(constraint);
    }

    public void removeConstraint(Constraint<?> constraint) {
        constraints.remove(constraint);
    }

    @JsonIgnore
    public boolean isEnum() {
        return category.isEnum();
    }

    public boolean isInterface() {
        return category == TypeCategory.INTERFACE;
    }

    public boolean isAbstract() {
        // TODO support abstract class
        return isInterface();
    }

    @JsonIgnore
    public boolean isClass() {
        return category.isClass();
    }

    @JsonIgnore
    public boolean isValue() {
        return category.isValue();
    }

    @JsonIgnore
    public boolean isReference() {
        return isEnum() || isClass() || isArray();
    }

    public Field getField(long fieldId) {
        if (superType != null && superType.containsField(fieldId)) {
            return superType.getField(fieldId);
        }
        Field field = fields.get(Entity::getId, fieldId);
        if (field != null && field.isReady()) {
            return field;
        }
        throw new InternalException("Field '" + fieldId + "' does not exist or is not ready");
    }

    private List<Field> readyFields() {
        return fields.filter(Field::isReady, true);
    }

    public boolean containsField(long fieldId) {
        return fields.get(Entity::getId, fieldId) != null || superType != null && superType.containsField(fieldId);
    }

    public boolean containsStaticField(long fieldId) {
        return staticFields.get(Entity::getId, fieldId) != null || superType != null && superType.containsStaticField(fieldId);
    }

    public Field tryGetFieldByName(String fieldName) {
        if (superType != null) {
            Field superField = superType.tryGetFieldByName(fieldName);
            if (superField != null) {
                return superField;
            }
        }
        return fields.get(Field::getName, fieldName);
    }

    public Field getFieldByName(String fieldName) {
        return NncUtils.requireNonNull(tryGetFieldByName(fieldName));
    }

    public Field tryGetStaticFieldByName(String fieldName) {
        if (superType != null) {
            Field superField = superType.tryGetStaticFieldByName(fieldName);
            if (superField != null) {
                return superField;
            }
        }
        return staticFields.get(Field::getName, fieldName);
    }

    public Field getStaticFieldByName(String fieldName) {
        return NncUtils.requireNonNull(tryGetStaticFieldByName(fieldName));
    }

    public Field getStaticFieldByVar(Var var) {
        if (var.isId()) {
            return getStaticField(var.getId());
        } else {
            return tryGetStaticFieldByName(var.getName());
        }
    }

    public void setSource(ClassSource source) {
        this.source = source;
    }

    public Field getStaticField(long id) {
        if (superType != null && superType.containsStaticField(id)) {
            return superType.getStaticField(id);
        }
        Field field = staticFields.get(Entity::getId, id);
        if (field != null && field.isReady()) {
            return field;
        }
        throw new InternalException("Field '" + id + "' does not exist or is not ready");
    }

    public Field getFieldByCode(String code) {
        var field = fields.get(Field::getCode, code);
        if (field != null) {
            return field;
        }
        if (superType != null) {
            return superType.getFieldByCode(code);
        }
        return null;
    }

    public Property getAttributeByVar(Var var) {
        return switch (var.getType()) {
            case NAME -> getAttributeByName(var.getName());
            case ID -> getAttribute(var.getId());
        };
    }

    public Property getAttribute(long id) {
        return NncUtils.requireNonNull(getAttribute(Entity::getId, id),
                "Can not find attribute with id: " + id + " in type " + this);
    }

    public Property getAttributeByCode(String code) {
        return getAttribute(Property::getCode, code);
    }

    public Property getAttributeByName(String name) {
        return getAttribute(Property::getName, name);
    }

    private <T> Property getAttribute(IndexMapper<Property, T> property, T value) {
        var field = fields.get(property, value);
        if (field != null) {
            return field;
        }
        var flow = flows.get(property, value);
        if (flow != null) {
            return flow;
        }
        if (superType != null) {
            return superType.getAttribute(property, value);
        }
        return null;
    }

    public Field getFieldByCodeRequired(String code) {
        return NncUtils.requireNonNull(getFieldByCode(code),
                "在类型'" + getName() + "'中未找到编号为'" + code + "'的字段");
    }

    public Field getStaticFieldByCode(String code) {
        if (superType != null) {
            Field superField = superType.getStaticFieldByCode(code);
            if (superField != null) {
                return superField;
            }
        }
        return staticFields.get(Field::getCode, code);
    }

    public ClassSource getSource() {
        return source;
    }

    public boolean isFromReflection() {
        return source == ClassSource.REFLECTION;
    }

    public Field getFieldByVar(Var var) {
        if (var.isId()) {
            return getField(var.getId());
        } else {
            return tryGetFieldByName(var.getName());
        }
    }

    public Field getFieldByJavaField(java.lang.reflect.Field javaField) {
        String fieldName = ReflectUtils.getMetaFieldName(javaField);
        return requireNonNull(tryGetFieldByName(fieldName),
                "Can not find field for java indexItem " + javaField);
    }

    public boolean checkColumnAvailable(Column column) {
        return NncUtils.find(fields, f -> f.getColumn() == column) == null;
    }

    Column allocateColumn(Field field) {
        Type fieldType = field.getType();
        if (fieldType.isNullable()) {
            fieldType = fieldType.getUnderlyingType();
        }
        if (fieldType.getSQLType() == null) {
            return null;
        }
        return allocateColumn(fieldType, field);
    }

    public Column allocateColumn(Type fieldType, Field field) {
        Set<Column> usedColumns = filterAndMapUnique(
                getFieldsInHierarchy(),
                f -> !f.equals(field),
                Field::getColumn
        );
        return Column.allocate(usedColumns, fieldType.getSQLType());
    }


    private ReadonlyArray<Field> getFieldsInHierarchy() {
        return fields;
//        List<Field> result = new ArrayList<>();
//        if (superType != null) {
//            result.addAll(superType.getAllFields());
//        }
//        getFieldsDownwardInHierarchy0(result);
//        return result;
    }

    private void getFieldsDownwardInHierarchy0(List<Field> results) {
        listAddAll(results, fields);
        for (ClassType subType : subTypes) {
            subType.getFieldsDownwardInHierarchy0(results);
        }
    }

    public Field getFieldNyNameRequired(String fieldName) {
        return NncUtils.requireNonNull(
                tryGetFieldByName(fieldName), "field not found: " + fieldName
        );
    }

    public void removeField(Field field) {
        if (field.isStatic()) {
            staticFields.remove(field);
        } else {
            fields.remove(field);
        }
    }

    public TypePO toPO() {
        return new TypePO(
                id,
                getTenantId(),
                get(superType, Entity::getId),
                name,
                category.code(),
                desc,
                ephemeral,
                anonymous,
                null,
                null,
                null
        );
    }

    private FlowTable getFlowTable() {
        if (flowTable == null) {
            flowTable = new FlowTable(this);
        }
        return flowTable;
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        if (equals(that)) {
            return true;
        }
        if (that instanceof ClassType thatType) {
            if (template != null) {
                var s = ((ClassType) that).getSuperByTemplate(template);
                if (s != null) {
                    return NncUtils.biAllMatch(typeArguments, s.typeArguments, Type::isWithinRange);
                } else {
                    return false;
                }
            }
            if (thatType.getSuperType() != null && isAssignableFrom(thatType.getSuperType())) {
                return true;
            }
            if (isInterface()) {
                for (ClassType it : thatType.interfaces) {
                    if (isAssignableFrom(it)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private ClassType getSuperByTemplate(ClassType template) {
        Queue<ClassType> supers = new LinkedList<>();
        supers.add(this);
        while (!supers.isEmpty()) {
            var s = supers.poll();
            if (s.getTemplate() == template) {
                return s;
            }
            supers.addAll(s.getSupers());
        }
        return null;
    }

    @Nullable
    public ClassType getSuperType() {
        return superType;
    }

    public ReadonlyArray<ClassType> getInterfaces() {
        return interfaces;
    }

    public List<ClassType> getSupers() {
        if (superType != null) {
            return NncUtils.prepend(superType, interfaces.toList());
        } else {
            return interfaces.toList();
        }
    }

    @Override
    public Class<? extends Instance> getInstanceClass() {
        return ClassInstance.class;
    }

    @Override
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        if (template == null) {
            java.lang.reflect.Type javaType = NncUtils.requireNonNull(
                    getJavaType.apply(this), "Can not get java type for type '" + this + "'");
            return javaType.getTypeName();
        } else {
            java.lang.reflect.Type javaType = NncUtils.requireNonNull(
                    getJavaType.apply(template), "Can not get java type for type '" + this + "'");
            return javaType.getTypeName() + "<"
                    + NncUtils.join(typeArguments, arg -> arg.getCanonicalName(getJavaType))
                    + ">";
        }
    }

    @Override
    protected ClassTypeParam getParam() {
        try (var context = SerializeContext.enter()) {
            typeParameters.forEach(context::writeType);
            typeArguments.forEach(context::writeType);
            if (superType != null) {
                context.writeType(superType);
            }
            interfaces.forEach(context::writeType);
            if (template != null) {
                context.writeType(template);
            }
            return new ClassTypeParam(
                    NncUtils.get(superType, context::getRef),
                    NncUtils.map(interfaces, context::getRef),
                    source.code(),
                    NncUtils.map(fields, Field::toDTO),
                    NncUtils.map(staticFields, Field::toDTO),
                    NncUtils.map(constraints, Constraint::toDTO),
                    NncUtils.map(flows, f -> f.toDTO(false)),
                    collectionName,
                    desc,
                    getExtra(),
                    isEnum() ? NncUtils.map(getEnumConstants(), ClassInstance::toDTO) : List.of(),
                    isTemplate(),
                    NncUtils.map(typeParameters, context::getRef),
                    NncUtils.map(typeParameters, Type::toDTO),
                    NncUtils.get(template, context::getRef),
                    NncUtils.map(typeArguments, context::getRef),
                    NncUtils.map(dependencies, context::getRef),
                    !subTypes.isEmpty()
            );
        }
    }

    protected Object getExtra() {
        return null;
    }

    @JsonIgnore
    public Field getTileField() {
        return find(getAllFields(), Field::isAsTitle);
    }

    public <T extends Constraint<?>> List<T> getConstraints(Class<T> constraintType) {
        List<T> result = filterAndMap(
                constraints,
                constraintType::isInstance,
                constraintType::cast
        );
        if (superType != null) {
            result = NncUtils.union(
                    superType.getConstraints(constraintType),
                    result
            );
        }
        return result;
    }

    public List<Constraint<?>> getConstraints() {
        return constraints.toList();
    }

    public <T extends Constraint<?>> T getConstraint(Class<T> constraintType, long id) {
        return find(getConstraints(constraintType), c -> c.getIdRequired() == id);
    }

    @SuppressWarnings("unused")
    public Constraint<?> getConstraint(long id) {
        return find(requireNonNull(constraints), c -> c.getIdRequired() == id);
    }

    public Index getUniqueConstraint(long id) {
        return getConstraint(Index.class, id);
    }

    @JsonIgnore
    public ClassType getConcreteType() {
        return this;
    }

    @Override
    public Set<ReferencePO> extractReferences(InstancePO instancePO) {
        Set<ReferencePO> refs = new HashSet<>();
        for (Field field : getAllFields()) {
            NncUtils.invokeIfNotNull(
                    ReferencePO.convertToRefId(instancePO.get(
                            field.getDeclaringType().getIdRequired(), field.getColumnName()), field.isReference()
                    ),
                    targetId -> refs.add(new ReferencePO(
                            instancePO.getTenantId(),
                            instancePO.getId(),
                            targetId,
                            field.getId(),
                            ReferenceKind.getFromType(field.getType()).code()
                    ))
            );
        }
        return refs;
    }

    public List<ClassInstance> getEnumConstants() {
        if (!isEnum()) {
            throw new InternalException("type " + this + " is not a enum type");
        }
        return NncUtils.filterAndMap(
                staticFields,
                this::isEnumConstantField,
                f -> (ClassInstance) f.getStaticValue()
        );
    }

    public EnumConstantRT getEnumConstant(long id) {
        if (!isEnum()) {
            throw new InternalException("type " + this + " is not a enum type");
        }
        for (Field field : staticFields) {
            if (isEnumConstantField(field) && Objects.equals(field.getStaticValue().getId(), id)) {
                return createEnumConstant((ClassInstance) field.getStaticValue());
            }
        }
        throw new InternalException("Can not find enum constant with id " + id);
    }

    private EnumConstantRT createEnumConstant(ClassInstance instance) {
        return new EnumConstantRT(instance);
    }

    boolean isEnumConstantField(Field field) {
        // TODO be more precise
        return isEnum() && field.isStatic() && field.getType() == this
                && field.getStaticValue() instanceof ClassInstance;
    }

    public Flow getOverrideFlowRequired(Flow overriden) {
        return NncUtils.requireNonNull(
                resolveFlow(overriden),
                "Can not find implementation of flow " + overriden.getName()
                        + " in type " + getName()
        );
    }

    @Nullable
    public Flow resolveFlow(Flow flowRef) {
        if (flowRef.getDeclaringType().isUncertain()) {
            var template = NncUtils.requireNonNull(flowRef.getDeclaringType().getTemplate());
            Queue<ClassType> types = new LinkedList<>();
            types.add(this);
            while (!types.isEmpty()) {
                var type = types.poll();
                if (type.getTemplate() == template) {
                    var flowTemplate = NncUtils.requireNonNull(flowRef.getRootTemplate());
                    if (flowTemplate.getTypeParameters().isEmpty()) {
                        flowRef = NncUtils.requireNonNull(type.getFlowByTemplate(flowTemplate));
                        break;
                    } else {
                        // TODO support flow template instance
                        throw new InternalException("Can not resolve flow '" + flowRef.getName() + "' in type '"
                                + getName() + "'");
                    }
                }
                types.addAll(type.getSupers());
            }
        }
        return getFlowTable().tryLookup(flowRef);
    }

    @Override
    public boolean isUncertain() {
        return NncUtils.anyMatch(typeArguments, Type::isUncertain);
    }

    public Class<?> getNativeClass() {
        var nativeClass = getNativeClass0();
        if (nativeClass != null) {
            return nativeClass;
        }
        if (superType != null) {
            nativeClass = superType.getNativeClass0();
        }
        if (nativeClass != null) {
            return nativeClass;
        }
        throw new InternalException("类型'" + getName() + "'未配置原生类型");
    }

    private Class<?> getNativeClass0() {
        if (template == null) {
            return collectionName != null ? Natives.getNative(collectionName) : null;
        } else {
            return template.getNativeClass0();
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (!isInterface()) {
            for (ClassType it : interfaces) {
                for (Flow flow : it.getFlows()) {
                    if (resolveFlow(flow) == null) {
                        throw new BusinessException(ErrorCode.INTERFACE_FLOW_NOT_IMPLEMENTED,
                                getName(), it.getName(), flow.getName());
                    }
                }
            }
        }
    }

    @Nullable
    public ClassType getTemplate() {
        return template;
    }

    public ClassType getEffectiveTemplate() {
        return template != null ? template : this;
    }

    public boolean templateEquals(ClassType that) {
        return this == that || this.getTemplate() == that;
    }

    public List<TypeVariable> getTypeParameters() {
        return typeParameters.toList();
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
        typeParameters.addChild(typeParameter);
    }

    public boolean isCollection() {
        return collectionName != null && COLLECTION_TEMPLATE_MAP.containsKey(collectionName)
                || template != null && template.isCollection();
    }

    public List<Type> getTypeArguments() {
        return typeArguments.toList();
    }

    public List<? extends Type> getEffectiveTypeArguments() {
        return template == null ? typeParameters.toList() : getTypeArguments();
    }


//    @Override
//    public ClassType getRawClass() {
//        return this;
//    }

//    public GenericClass getGenericSuperType() {
//        return genericSuperType;
//    }

//    public List<GenericClass> getGenericInterfaces() {
//        return Collections.unmodifiableList(genericInterfaces);
//    }

    public Index getUniqueConstraint(List<Field> fields) {
        return find(getUniqueConstraints(), c -> c.getTypeFields().equals(fields));
    }

    public List<Index> getUniqueConstraints() {
        return getConstraints(Index.class);
    }


    @Override
    public String toString() {
        return "ClassType " + name + " (id:" + id + ")";
    }

    private static final Map<String, Class<?>> COLLECTION_TEMPLATE_MAP = Map.of(
            "Collection", Collection.class,
            "Map", Map.class,
            "Set", Set.class,
            "List", List.class,
            "Iterator", Iterator.class,
            "IteratorImpl", IteratorImpl.class,
            "Throwable", Throwable.class,
            "Exception", Exception.class,
            "RuntimeException", RuntimeException.class
    );

    public void setTypeArguments(List<Type> typeArguments) {
        this.typeArguments.clear();
        this.typeArguments.addAll(typeArguments);
        parameterizedTypeKey = null;
    }

    public ClassType findSuperTypeRequired(Predicate<ClassType> test) {
        return NncUtils.requireNonNull(findSuperType(test),
                "Can not find the required super type");
    }

    public ClassType findSuperType(Predicate<ClassType> test) {
        if (test.test(this)) {
            return this;
        }
        ClassType result;
        if (superType != null && (result = superType.findSuperType(test)) != null) {
            return result;
        }
        for (ClassType anInterface : interfaces) {
            if ((result = anInterface.findSuperType(test)) != null) {
                return result;
            }
        }
        return null;
    }

    public void setSuperType(@Nullable ClassType superType) {
        if (this.superType != null) {
            this.superType.removeSubType(this);
        }
        this.superType = superType;
        if (superType != null) {
            superType.addSubType(this);
        }
        getFlowTable().rebuild();
    }

    public void setInterfaces(List<ClassType> interfaces) {
        for (ClassType anInterface : this.interfaces) {
            anInterface.removeSubType(this);
        }
        this.interfaces.clear();
        this.interfaces.addAll(interfaces);
        for (ClassType anInterface : interfaces) {
            anInterface.addSubType(this);
        }
    }

    @Nullable
    public String getCollectionName() {
        return template != null ? template.collectionName : collectionName;
    }

    public void addDependency(ClassType dependency) {
        NncUtils.requireFalse(dependencies.contains(dependency),
                "Dependency " + dependency + " already exists in type " + getName());
        dependencies.add(dependency);
    }

    public void setDependencies(List<ClassType> dependencies) {
        this.dependencies.clear();
        this.dependencies.addAll(dependencies);
    }

    public List<ClassType> getDependencies() {
        return dependencies.toList();
    }

    public Type getDependency(CollectionKind collectionKind) {
        return NncUtils.findRequired(dependencies, dep -> Objects.equals(dep.getCollectionName(),
                collectionKind.getCollectionName()));
    }

    public void setTypeParameters(List<TypeVariable> typeParameters) {
        requireTrue(allMatch(typeParameters, typeParam -> typeParam.getGenericDeclaration() == this));
        this.typeParameters.resetChildren(typeParameters);
    }

    public void setFields(List<Field> fields) {
        requireTrue(allMatch(fields, f -> f.getDeclaringType() == this));
        this.fields.resetChildren(fields);
    }

    public void moveField(Field field, int index) {
        moveProperty(fields, field, index);
    }

    public void moveFlow(Flow flow, int index) {
        moveProperty(flows, flow, index);
    }

    private <T extends Property> void moveProperty(ChildArray<T> properties, T property, int index) {
        if (index < 0 || index >= properties.size()) {
            throw new BusinessException(ErrorCode.INDEX_OUT_OF_BOUND);
        }
        if (!properties.remove(property)) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND, property.getName());
        }
        if (index >= properties.size()) {
            properties.addChild(property);
        } else {
            properties.addChild(index, property);
        }
    }

    public void setStaticFields(List<Field> staticFields) {
        requireTrue(allMatch(staticFields, f -> f.getDeclaringType() == this));
        this.staticFields.resetChildren(staticFields);
    }

    public void setConstraints(List<Constraint<?>> constraints) {
        requireTrue(allMatch(constraints, c -> c.getDeclaringType() == this));
        this.constraints.resetChildren(constraints);
    }

    @Override
    protected boolean afterContextInitIdsInternal() {
        if (template != null) {
            if (parameterizedTypeKey == null) {
                parameterizedTypeKey = pTypeKey(template, typeArguments);
            }
        }
        return true;
    }

    @Override
    protected boolean isParameterized() {
        return template != null;
    }

    public static String pTypeKey(ClassType template, Iterable<Type> typeArguments) {
        return toBase64(template.getIdRequired()) + "-"
                + NncUtils.join(typeArguments, typeArg -> toBase64(typeArg.getIdRequired()), "-");
    }

    @Override
    protected List<Object> beforeRemoveInternal(IEntityContext context) {
        if (superType != null) {
            superType.removeSubType(this);
        }
        for (ClassType anInterface : interfaces) {
            anInterface.removeSubType(this);
        }
        return List.of();
    }

    public void setStage(ResolutionStage stage) {
        this.stage = stage;
    }

    public ResolutionStage getStage() {
        if (stage == null) {
            stage = ResolutionStage.GENERATED;
        }
        return stage;
    }

    public void rebuildFlowTable() {
        getFlowTable().rebuild();
    }

}

