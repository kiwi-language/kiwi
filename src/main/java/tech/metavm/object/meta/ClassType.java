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
import tech.metavm.object.instance.SQLType;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.object.meta.rest.dto.ClassParamDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static tech.metavm.util.ContextUtil.getTenantId;
import static tech.metavm.util.NncUtils.*;

@EntityType("Class类型")
public class ClassType extends AbsClassType {

    @EntityField("超类")
    @Nullable
    private final ClassType superType;
    @ChildEntity("接口")
    private final Table<ClassType> interfaces = new Table<>(ClassType.class, false);
    @EntityField("来源")
    private final ClassSource source;
    @ChildEntity("子类列表")
    private final Table<ClassType> subTypes = new Table<>(ClassType.class);
    @EntityField("描述")
    private @Nullable String desc;
    @ChildEntity("字段列表")
    private final Table<Field> fields = new Table<>(Field.class, true);
    @ChildEntity("流程列表")
    private final Table<Flow> flows = new Table<>(Flow.class, true);
    @ChildEntity("静态字段列表")
    private final Table<Field> staticFields = new Table<>(Field.class, true);
    @ChildEntity("约束列表")
    private final Table<Constraint<?>> constraints = new Table<>(new TypeReference<>() {
    }, true);
    @EntityField("模板名称")
    @Nullable
    private final String template;
    @ChildEntity("类型实参")
    private final Table<Type> typeArguments = new Table<>(Type.class);


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
            @Nullable String nativeClass,
            List<Type> typeArguments
            ) {
        super(name, anonymous, ephemeral, category);
        setTmpId(tmpId);
        this.setCode(code);
        this.interfaces.addAll(interfaces);
        for (ClassType it : interfaces) {
            it.addSubType(this);
        }
        this.source = source;
        this.superType = superType;
        if (superType != null) {
            superType.addSubType(this);
        }
        this.desc = desc;
        this.template = nativeClass;
        this.typeArguments.addAll(typeArguments);
    }

    public void update(TypeDTO typeDTO) {
        setName(typeDTO.name());
        ClassParamDTO param = (ClassParamDTO) typeDTO.param();
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

    @Nullable
    @SuppressWarnings("unused")
    public String getDesc() {
        return desc;
    }

    @Override
    public List<Field> getFields() {
        if (superType != null) {
            return merge(superType.getFields(), readyFields());
        } else {
            return readyFields();
        }
    }

    private List<Field> getAllFields() {
        if (superType != null) {
            return merge(superType.getAllFields(), fields);
        } else {
            return NncUtils.map(fields, Function.identity());
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


    @SuppressWarnings("unused")
    public Table<Flow> getFlows() {
        return flows;
    }

    public Table<Flow> getDeclaredFlows() {
        return flows;
    }

    public Flow getFlow(long id) {
        return findById(flows, id);
    }

    public Flow getFlow(String code, List<Type> parameterTypes) {
        return NncUtils.find(flows,
                flow -> Objects.equals(flow.getCode(), code) && flow.getInputTypes().equals(parameterTypes));
    }

    public Flow getFlowByCode(String code) {
        return flows.get(Flow::getCode, code);
    }

    public void removeFlow(Flow flow) {
        flows.remove(flow);
    }

    public void addFlow(Flow flow) {
        if (flows.contains(flow)) {
            throw new InternalException("Flow '" + flow + "' is already added to the class type");
        }
        flows.add(flow);
    }

    public Table<Field> getDeclaredFields() {
        return fields;
    }

    public Table<Constraint<?>> getDeclaredConstraints() {
        return constraints;
    }

    public void addField(Field field) {
        if (field.getId() != null && getField(field.getId()) != null) {
            throw new RuntimeException("Field " + field.getId() + " is already added");
        }
        if (getFieldByName(field.getName()) != null || getStaticFieldByName(field.getName()) != null) {
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
            staticFields.add(field);
        } else {
            fields.add(field);
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
        requireNonNull(constraints).add(constraint);
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

    @Override
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

    public Field getFieldByName(String fieldName) {
        if (superType != null) {
            Field superField = superType.getFieldByName(fieldName);
            if (superField != null) {
                return superField;
            }
        }
        return fields.get(Field::getName, fieldName);
    }

    public Field getStaticFieldByName(String fieldName) {
        if (superType != null) {
            Field superField = superType.getStaticFieldByName(fieldName);
            if (superField != null) {
                return superField;
            }
        }
        return staticFields.get(Field::getName, fieldName);
    }

    public Field getStaticFieldByVar(Var var) {
        if (var.isId()) {
            return getStaticField(var.getId());
        } else {
            return getStaticFieldByName(var.getName());
        }
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
        if (superType != null) {
            Field superField = superType.getFieldByCode(code);
            if (superField != null) {
                return superField;
            }
        }
        return fields.get(Field::getCode, code);
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
            return getFieldByName(var.getName());
        }
    }

    public Field getFieldByJavaField(java.lang.reflect.Field javaField) {
        String fieldName = ReflectUtils.getMetaFieldName(javaField);
        return requireNonNull(getFieldByName(fieldName),
                "Can not find field for java indexItem " + javaField);
    }

    @Override
    Column allocateColumn(Field field) {
        Type fieldType = field.getType();
        if (fieldType.isNullable()) {
            fieldType = fieldType.getUnderlyingType();
        }
        if (fieldType.getSQLType() == null) {
            return null;
        }
        Set<Column> usedColumns = filterAndMapUnique(
                getFieldsInHierarchy(),
                f -> !f.equals(field),
                Field::getColumn
        );
        Map<SQLType, Queue<Column>> columnMap = SQLType.getColumnMap(usedColumns);
        Queue<Column> columns = columnMap.get(fieldType.getSQLType());
        if (columns.isEmpty()) {
            throw BusinessException.invalidField(field, "属性数量超出限制");
        }
        return columns.poll().copy();
    }

    private List<Field> getFieldsInHierarchy() {
        List<Field> result = new ArrayList<>();
        if (superType != null) {
            result.addAll(superType.getAllFields());
        }
        getFieldsDownwardInHierarchy0(result);
        return result;
    }

    private void getFieldsDownwardInHierarchy0(List<Field> results) {
        results.addAll(fields);
        for (ClassType subType : subTypes) {
            subType.getFieldsDownwardInHierarchy0(results);
        }
    }

    public Field getFieldNyNameRequired(String fieldName) {
        return NncUtils.requireNonNull(
                getFieldByName(fieldName), "field not found: " + fieldName
        );
    }

    @Override
    public void removeField(Field field) {
        fields.remove(field);
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

    @Override
    public boolean isAssignableFrom(Type that) {
        if (equals(that)) {
            return true;
        }
        if (that instanceof ClassType thatType) {
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

    @Nullable
    public ClassType getSuperType() {
        return superType;
    }

    @Override
    protected ClassParamDTO getParam() {
        return getParam(true, false, false);
    }

    @Override
    public Class<? extends Instance> getInstanceClass() {
        return ClassInstance.class;
    }

    @Override
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        if(isCollection()) {
            String collName = COLLECTION_TEMPLATE_MAP.get(template).getName();
            return collName + "<" +
                    NncUtils.join(typeArguments, typeArg -> typeArg.getCanonicalName(getJavaType), ",")
                    + ">";
        }
        java.lang.reflect.Type javaType = NncUtils.requireNonNull(
                getJavaType.apply(this), "Can not get java type for type '" + this + "'");
        return javaType.getTypeName();
    }

    public TypeDTO toDTO(boolean withFields, boolean withFieldTypes) {
        return toDTO(withFields, withFieldTypes, false);
    }

    public TypeDTO toDTO(boolean withFields, boolean withFieldTypes, boolean withFlows) {
        try (var context = SerializeContext.enter()) {
            return super.toDTO(getParam(withFields, withFieldTypes, withFlows), context.getTmpId(this));
        }
    }

    protected ClassParamDTO getParam(boolean withFields, boolean withFieldTypes, boolean withFlows) {
        try(var context = SerializeContext.enter()) {
            return new ClassParamDTO(
                    NncUtils.get(superType, Type::getId),
                    NncUtils.get(superType, Type::toDTO),
                    NncUtils.map(interfaces, context::getRef),
                    source.code(),
                    withFields ? NncUtils.map(fields, f -> f.toDTO(withFieldTypes)) : List.of(),
                    withFields ? NncUtils.map(staticFields, f -> f.toDTO(withFieldTypes)) : List.of(),
                    NncUtils.map(constraints, Constraint::toDTO),
                    withFlows ? NncUtils.map(flows, Flow::toDTO) : List.of(),
                    template,
                    desc,
                    getExtra(),
                    isEnum() ? NncUtils.map(getEnumConstants(), EnumConstantRT::toDTO) : List.of(),
                    NncUtils.map(typeArguments, context::getRef)
            );
        }
    }

    protected Object getExtra() {
        return null;
    }

    @JsonIgnore
    public Field getTileField() {
        return find(getFields(), Field::isAsTitle);
    }

    public <T extends Constraint<?>> List<T> getConstraints(Class<T> constraintType) {
        List<T> result = filterAndMap(
                constraints,
                constraintType::isInstance,
                constraintType::cast
        );
        if (superType != null) {
            result = NncUtils.merge(
                    superType.getConstraints(constraintType),
                    result
            );
        }
        return result;
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
    public List<ReferencePO> extractReferences(InstancePO instancePO) {
        List<ReferencePO> refs = new ArrayList<>();
        for (Field field : getFields()) {
            NncUtils.invokeIfNotNull(
                    ReferencePO.convertToRefId(instancePO.get(field.getColumnName()), field.isReference()),
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

    public List<EnumConstantRT> getEnumConstants() {
        if (!isEnum()) {
            throw new InternalException("type " + this + " is not a enum type");
        }
        return NncUtils.filterAndMap(
                staticFields,
                this::isEnumConstantField,
                f -> createEnumConstant((ClassInstance) f.getStaticValue())
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
                getOverrideFlow(overriden),
                "Can not find implementation of flow " + overriden.getName()
                        + " in type " + getName()
        );
    }

    @Nullable
    public Flow getOverrideFlow(Flow overriden) {
        var override = flows.get(Flow::getOverridden, overriden);
        if(override != null) {
            return override;
        }
        if(superType != null) {
            return superType.getOverrideFlow(overriden);
        }
        else {
            return null;
        }
    }

    public Class<?> getNativeClass() {
        return Natives.getNative(
                NncUtils.requireNonNull(template, "类型'" + getName() + "'没有配置原生类名")
        );
    }

    @Nullable
    public String getTemplate() {
        return template;
    }

    @Override
    public void validate() {
        super.validate();
        if(!isInterface()) {
            for (ClassType it : interfaces) {
                for (Flow flow : it.getFlows()) {
                    if (getOverrideFlow(flow) == null) {
                        throw new BusinessException(ErrorCode.INTERFACE_FLOW_NOT_IMPLEMENTED,
                                getName(), it.getName(), flow.getName());
                    }
                }
            }
        }
    }

    public boolean isCollection() {
        return template != null && COLLECTION_TEMPLATE_MAP.containsKey(template);
    }

    public List<Type> getTypeArguments() {
        return Collections.unmodifiableList(typeArguments);
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
            "IteratorImpl", IteratorImpl.class
    );

}

