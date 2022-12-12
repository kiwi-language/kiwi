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
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

import static tech.metavm.util.ContextUtil.getTenantId;
import static tech.metavm.util.NncUtils.*;

@EntityType("Class类型")
public class ClassType extends AbsClassType {

    @EntityField("超类")
    @Nullable
    private final ClassType superType;
    @EntityField("描述")
    private @Nullable String desc;
    @ChildEntity("字段列表")
    private final Table<Field> fields = new Table<>(Field.class);
    @ChildEntity("约束列表")
    private final Table<ConstraintRT<?>> constraints = new Table<>(new TypeReference<>() {});
//    @EntityField("类型参数列表")
//    private final Table<TypeVariable> typeParameters = new Table<>();
//    @EntityField("模板实现列表")
//    private final Table<ParameterizedType> templateImplementations = new Table<>();
    @EntityField("流程列表")
    private final Table<FlowRT> flows = new Table<>(FlowRT.class);

    public ClassType(
                String name,
                @Nullable ClassType superType,
                TypeCategory category,
                boolean anonymous,
                boolean ephemeral,
                @Nullable String desc) {
        super(name, anonymous, ephemeral, category);
        this.superType = superType;
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

    @Nullable
    @SuppressWarnings("unused")
    public String getDesc() {
        return desc;
    }

    @Override
    public List<Field> getFields() {
        if(superType != null) {
            return merge(superType.getFields(), fields);
        }
        else {
            return map(fields, Function.identity());
        }
    }

//    public List<TypeVariable> getTypeParameters() {
//        return typeParameters;
//    }

//    public Table<ParameterizedType> getTemplateImplementations() {
//        return templateImplementations;
//    }

    public Table<Field> getDeclaredFields() {
        return fields;
    }

    public Table<ConstraintRT<?>> getDeclaredConstraints() {
        return constraints;
    }

    public Table<FlowRT> getDeclaredFlows() {
        return flows;
    }

//    public Table<TypeVariable> getDeclaredTypeParameters() {
//        return typeParameters;
//    }

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

    public void addConstraint(ConstraintRT<?> constraint) {
        requireNonNull(constraints).add(constraint);
    }

    @Override
    public void removeConstraint(long id) {
        requireNonNull(constraints).removeIf(c -> c.getId() == id);
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
        return findRequired(fields, f -> f.getId() == fieldId,
                        "Field id " + id + "  not found in type " + name);
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
                "Can not find field for java field " + javaField);
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
        List<Column> usedColumns = filterAndMap(
                getFields(),
                f -> !f.equals(field),
                Field::getColumn
        );
        Map<SQLType, Queue<Column>> columnMap = SQLType.getColumnMap(usedColumns);
        Queue<Column> columns = columnMap.get(fieldType.getSQLType());
        if(columns.isEmpty()) {
            throw BusinessException.invalidField(field, "属性数量超出限制");
        }
        return columns.poll();
    }

    public Field getFieldNyNameRequired(String fieldName) {
        return filterOneRequired(
                requireNonNull(fields),
                f -> f.getName().equals(fieldName),
                "field not found: " + fieldName
        );
    }

    @Override
    public void removeField(Field field) {
        if(requireNonNull(fields).contains(field)) {
            fields.remove(field);
//            field.remove();
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
        return getParam(false, false);
    }

    public TypeDTO toDTO(boolean withFields, boolean withFieldTypes) {
        return super.toDTO(getParam(withFields, withFieldTypes));
    }

    protected ClassParamDTO getParam(boolean withFields, boolean withFieldTypes) {
        return new ClassParamDTO(
                NncUtils.get(superType, Type::getId),
                NncUtils.get(superType, Type::toDTO),
                withFields ? NncUtils.map(fields, f -> f.toDTO(withFieldTypes)) : List.of(),
                NncUtils.map(constraints, ConstraintRT::toDTO),
//                NncUtils.map(typeParameters, TypeVariable::toDTO),
                desc
        );
    }

//    @Override
//    public ClassType getEffectiveType(TypeMapping mapping) {
//        return new ParameterizedType(
//                this,
//                NncUtils.map(typeParameters, mapping::getEffectiveType)
//        );
//    }

    private List<FieldDTO> getFieldDTOs(boolean withFields, boolean withTitleField, boolean withFieldTypes) {
        if(withFields) {
            return map(fields, f -> f.toDTO(withFieldTypes));
        }
        else if(withTitleField) {
            return filterAndMap(fields, Field::isAsTitle, f -> f.toDTO(withFieldTypes));
        }
        else {
            return List.of();
        }
    }

    @JsonIgnore
    public Field getTileField() {
        return find(getFields(), Field::isAsTitle);
    }

    public <T extends ConstraintRT<?>> List<T> getConstraints(Class<T> constraintType) {
        return filterAndMap(
                constraints,
                constraintType::isInstance,
                constraintType::cast
        );
    }

    public <T extends ConstraintRT<?>> T getConstraint(Class<T> constraintType, long id) {
        return find(getConstraints(constraintType), c -> c.getId() == id);
    }

    @SuppressWarnings("unused")
    public ConstraintRT<?> getConstraint(long id) {
        return find(requireNonNull(constraints), c -> c.getId() == id);
    }

    public UniqueConstraintRT getUniqueConstraint(long id) {
        return getConstraint(UniqueConstraintRT.class, id);
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

}

