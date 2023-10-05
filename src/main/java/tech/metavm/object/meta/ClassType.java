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
import java.util.function.Predicate;

import static tech.metavm.util.ContextUtil.getTenantId;
import static tech.metavm.util.NncUtils.*;

@EntityType("Class类型")
public class ClassType extends Type implements GenericDeclaration {

    public static final IndexDef<ClassType> IDX_PARAMETERIZED_TYPE_KEY =
            new IndexDef<>(ClassType.class, "parameterizedTypeKey");

    @EntityField("超类")
    @Nullable
    private ClassType superType;
    //    @EntityField("范型超类")
//    private final GenericClass genericSuperType;
//    @ChildEntity("范型接口")
//    private final Table<GenericClass> genericInterfaces = new Table<>(GenericClass.class);
    @ChildEntity("接口")
    private final Table<ClassType> interfaces = new Table<>(ClassType.class, false);
    @EntityField("来源")
    private ClassSource source;
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
    @EntityField("集合名称")
    @Nullable
    private final String collectionName;
    @Nullable
    @EntityField("模板")
    private final ClassType template;
    @ChildEntity("类型参数")
    private final Table<TypeVariable> typeParameters = new Table<>(TypeVariable.class, true);
    @ChildEntity("类型实参")
    private final Table<Type> typeArguments = new Table<>(Type.class);
    // TODO (Important!) not scalable, must be optimized before going to production
    @ChildEntity("依赖")
    private final Table<ClassType> dependencies = new Table<>(ClassType.class);
    @Nullable
    private String parameterizedTypeKey;

    public transient ResolutionStage stage = ResolutionStage.CREATED;

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
        this.collectionName = templateName;
        this.typeArguments.addAll(typeArguments);
//        this.genericSuperType = genericSuperType;
//        this.genericInterfaces.addAll(genericInterfaces);
        this.template = template;
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

    void removeSubType(ClassType subType) {
        subTypes.remove(subType);
    }

    @Nullable
    @SuppressWarnings("unused")
    public String getDesc() {
        return desc;
    }

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
        var flow = NncUtils.find(flows,
                f -> Objects.equals(f.getCode(), code) && f.getInputTypes().equals(parameterTypes));
        if (flow != null) {
            return flow;
        }
        if (superType != null) {
            return superType.getFlow(code, parameterTypes);
        }
        throw new InternalException("Can not find flow '" + code + "(" +
                NncUtils.join(parameterTypes, Type::getName, ",")
                + ")' in type '" + getName() + "'");
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
    protected boolean isAssignableFrom0(Type that) {
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

    public List<ClassType> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    public List<ClassType> getSupers() {
        if (superType != null) {
            return NncUtils.append(interfaces, superType);
        } else {
            return interfaces;
        }
    }

    @Override
    protected ClassParamDTO getParam() {
        return getParam(true, false, false, false);
    }

    @Override
    public Class<? extends Instance> getInstanceClass() {
        return ClassInstance.class;
    }

    @Override
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
//        if(isCollection()) {
//            var collName = NncUtils.requireNonNull(getCollectionName());
//            String collClassName = COLLECTION_TEMPLATE_MAP.get(collName).getName();
//            if(templateName == null) {
//                return collClassName;
//            }
//            else {
//                return collClassName + "<" +
//                        NncUtils.join(typeArguments, typeArg -> typeArg.getCanonicalName(getJavaType), ",")
//                        + ">";
//            }
//        }
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

    public TypeDTO toDTO(boolean withFields, boolean withFieldTypes) {
        return toDTO(withFields, withFieldTypes, false, false);
    }

    public TypeDTO toDTO(boolean withFields, boolean withFieldTypes, boolean withFlows, boolean withCode) {
        try (var context = SerializeContext.enter()) {
            return super.toDTO(getParam(withFields, withFieldTypes, withFlows, withCode), context.getTmpId(this));
        }
    }

    protected ClassParamDTO getParam(boolean withFields, boolean withFieldTypes, boolean withFlows, boolean withCode) {
        try (var context = SerializeContext.enter()) {
            return new ClassParamDTO(
                    NncUtils.get(superType, context::getRef),
                    NncUtils.get(superType, s -> s.toDTO(withFields, withFieldTypes, false, false)),
                    NncUtils.map(interfaces, Type::toDTO),
                    NncUtils.map(interfaces, context::getRef),
                    source.code(),
                    withFields ? NncUtils.map(fields, f -> f.toDTO(withFieldTypes)) : List.of(),
                    withFields ? NncUtils.map(staticFields, f -> f.toDTO(withFieldTypes)) : List.of(),
                    NncUtils.map(constraints, Constraint::toDTO),
                    withFlows ? NncUtils.map(flows, flow -> flow.toDTO(withCode)) : List.of(),
                    collectionName,
                    desc,
                    getExtra(),
                    isEnum() ? NncUtils.map(getEnumConstants(), EnumConstantRT::toDTO) : List.of(),
                    NncUtils.map(typeParameters, Type::toDTO),
                    NncUtils.get(template, context::getRef),
                    NncUtils.map(typeArguments, context::getRef),
                    NncUtils.map(dependencies, context::getRef)
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

    public List<Constraint<?>> getConstraints() {
        return Collections.unmodifiableList(constraints);
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
        if (override != null) {
            return override;
        }
        if (superType != null) {
            return superType.getOverrideFlow(overriden);
        } else {
            return null;
        }
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
                    if (getOverrideFlow(flow) == null) {
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
        return Collections.unmodifiableList(typeParameters);
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
        typeParameters.add(typeParameter);
    }

    public boolean isCollection() {
        return collectionName != null && COLLECTION_TEMPLATE_MAP.containsKey(collectionName)
                || template != null && template.isCollection();
    }

    public List<Type> getTypeArguments() {
        return Collections.unmodifiableList(typeArguments);
    }

    public List<Type> getEffectiveTypeArguments() {
        return template == null ? Collections.unmodifiableList(typeParameters) : getTypeArguments();
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

    public List<Type> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    public Type getDependency(CollectionKind collectionKind) {
        return NncUtils.findRequired(dependencies, dep -> Objects.equals(dep.getCollectionName(),
                collectionKind.getCollectionName()));
    }

    public void setTypeParameters(List<TypeVariable> typeParameters) {
        this.typeParameters.clear();
        this.typeParameters.addAll(typeParameters);
    }

    @Nullable
    public String getParameterizedTypeKey() {
        return parameterizedTypeKey;
    }

    public void setParameterizedTypeKey(@Nullable String parameterizedTypeKey) {
        this.parameterizedTypeKey = parameterizedTypeKey;
    }

    @Override
    public boolean afterContextInitIds() {
        if (template != null && parameterizedTypeKey == null) {
            parameterizedTypeKey = pTypeKey(template, typeArguments);
            return true;
        } else {
            return false;
        }
    }

    public static String pTypeKey(ClassType template, List<Type> typeArguments) {
        return toBase64(template.getIdRequired()) + "-"
                + NncUtils.join(typeArguments, typeArg -> toBase64(typeArg.getIdRequired()), "-");
    }

}

