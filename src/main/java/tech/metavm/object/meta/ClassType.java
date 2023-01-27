package tech.metavm.object.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.FlowRT;
import tech.metavm.object.instance.SQLType;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.object.meta.rest.dto.ClassParamDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static tech.metavm.util.ContextUtil.getTenantId;
import static tech.metavm.util.NncUtils.*;

@EntityType("Class类型")
public class ClassType extends AbsClassType {

    @EntityField("超类")
    @Nullable
    private final ClassType superType;
    @EntityField("子类列表")
    private final Table<ClassType> subTypes = new Table<>(ClassType.class);
    @EntityField("描述")
    private @Nullable String desc;
    @ChildEntity("字段列表")
    private final Table<Field> fields = new Table<>(Field.class, true);
    @ChildEntity("约束列表")
    private final Table<Constraint<?>> constraints = new Table<>(new TypeReference<>() {}, true);
    @ChildEntity("流程列表")
    private final Table<FlowRT> flows = new Table<>(FlowRT.class, true);

    public static volatile AtomicInteger CNT1 = new AtomicInteger(0);

    public static volatile AtomicInteger CNT2 = new AtomicInteger(0);

    public ClassType(String name) {
        this(name, null, TypeCategory.CLASS, false, false, null);
    }

    public ClassType(
                String name,
                @Nullable ClassType superType,
                TypeCategory category,
                boolean anonymous,
                boolean ephemeral,
                @Nullable String desc) {
        super(name, anonymous, ephemeral, category);
        this.superType = superType;
        if(superType != null) {
            superType.addSubType(this);
        }
        this.desc = desc;
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
        if(subTypes.contains(subType)) {
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
        if(superType != null) {
            return merge(superType.getFields(), readyFields());
        }
        else {
            return readyFields();
        }
    }

    private List<Field> getAllFields() {
        if(superType != null) {
            return merge(superType.getAllFields(), fields);
        }
        else {
            return NncUtils.map(fields, Function.identity());
        }
    }

    public Table<Field> getDeclaredFields() {
        return fields;
    }

    public Table<Constraint<?>> getDeclaredConstraints() {
        return constraints;
    }

    public Table<FlowRT> getDeclaredFlows() {
        return flows;
    }

    public void addField(Field field) {
        if(field.getId() != null && getField(field.getId()) != null) {
            throw new RuntimeException("Field " + field.getId() + " is already added");
        }
        if(getFieldByName(field.getName()) != null) {
            throw BusinessException.invalidField(field, "属性名称'" + field.getName() + "'已存在");
        }
        if(field.isAsTitle() && getTileField() != null) {
            throw BusinessException.multipleTitleFields();
        }
        requireNonNull(fields).add(field);
    }

    public void addFlow(FlowRT flow) {
        if(flows.contains(flow)) {
            throw new InternalException("Flow '" + flow + "' is already added to the class type");
        }
        flows.add(flow);
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
        if(superType != null && superType.containsField(fieldId)) {
            return superType.getField(fieldId);
        }
        Field field = fields.get(Entity::getId, fieldId);
        if(field != null && field.isReady()) {
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

    public Field getFieldByName(String fieldName) {
        if(superType != null) {
            Field superField = superType.getFieldByName(fieldName);
            if(superField != null) {
                return superField;
            }
        }
        return find(fields, f -> f.getName().equals(fieldName));
    }

    public Field getFieldByJavaField(java.lang.reflect.Field javaField) {
        String fieldName = ReflectUtils.getMetaFieldName(javaField);
        return requireNonNull(getFieldByName(fieldName),
                "Can not find indexItem for java indexItem " + javaField);
    }

    @Override
    Column allocateColumn(Field field) {
        Type fieldType = field.getType();
        if(fieldType.isNullable()) {
            fieldType = fieldType.getUnderlyingType();
        }
        if(fieldType.getSQLType() == null) {
            return null;
        }
        Set<Column> usedColumns = filterAndMapUnique(
                getFieldsInHierarchy(),
                f -> !f.equals(field),
                Field::getColumn
        );
        Map<SQLType, Queue<Column>> columnMap = SQLType.getColumnMap(usedColumns);
        Queue<Column> columns = columnMap.get(fieldType.getSQLType());
        if(columns.isEmpty()) {
            throw BusinessException.invalidField(field, "属性数量超出限制");
        }
        return columns.poll().copy();
    }

    private List<Field> getFieldsInHierarchy() {
        List<Field> result = new ArrayList<>();
        if(superType != null) {
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
        return filterOneRequired(
                requireNonNull(fields),
                f -> f.getName().equals(fieldName),
                "indexItem not found: " + fieldName
        );
    }

    @Override
    public void removeField(Field field) {
        fields.remove(field);
    }

    public void removeFlow(FlowRT flow) {
        flows.remove(flow);
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
        if(equals(that)) {
            return true;
        }
        if(that instanceof ClassType thatType) {
            return thatType.getSuperType() != null && isAssignableFrom(thatType.getSuperType());
        }
        return false;
    }

    @Nullable
    public ClassType getSuperType() {
        return superType;
    }

    @Override
    protected ClassParamDTO getParam() {
        return getParam(true, false);
    }

    @Override
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        java.lang.reflect.Type javaType = NncUtils.requireNonNull(
                getJavaType.apply(this), "Can not get java type for type '" + this + "'");
        return javaType.getTypeName();
    }

    public TypeDTO toDTO(boolean withFields, boolean withFieldTypes) {
        return super.toDTO(getParam(withFields, withFieldTypes));
    }

    protected ClassParamDTO getParam(boolean withFields, boolean withFieldTypes) {
        return new ClassParamDTO(
                NncUtils.get(superType, Type::getId),
                NncUtils.get(superType, Type::toDTO),
                withFields ? NncUtils.map(getFields(), f -> f.toDTO(withFieldTypes)) : List.of(),
                NncUtils.map(constraints, Constraint::toDTO),
                desc,
                getExtra()
        );
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
        if(superType != null) {
            result = NncUtils.merge(
                    superType.getConstraints(constraintType),
                    result
            );
        }
        return result;
    }

    public <T extends Constraint<?>> T getConstraint(Class<T> constraintType, long id) {
        return find(getConstraints(constraintType), c -> c.getId() == id);
    }

    @SuppressWarnings("unused")
    public Constraint<?> getConstraint(long id) {
        return find(requireNonNull(constraints), c -> c.getId() == id);
    }

    public Index getUniqueConstraint(long id) {
        return getConstraint(Index.class, id);
    }

    @SuppressWarnings("unused")
    @Nullable
    public Table<FlowRT> getFlows() {
        return flows;
    }

    public FlowRT getFlow(long id) {
        return findById(flows, id);
    }

    @JsonIgnore
    public ClassType getConcreteType() {
        return this;
    }

    @Override
    public String toString() {
        return "ClassType " + name + " (id:" + id + ")";
    }
}

