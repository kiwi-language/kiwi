package tech.metavm.object.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.FlowRT;
import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.SQLColumnType;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.*;

import static tech.metavm.util.ContextUtil.getTenantId;
import static tech.metavm.util.NncUtils.requireNonNull;

@EntityType("类型")
public class Type extends Entity {

    @EntityField("是否临时")
    private final boolean ephemeral;
    @EntityField("是否匿名")
    private boolean anonymous;
    @EntityField("名称")
    private String name;
    @EntityField("超类")
    @Nullable
    private Type superType;
    @EntityField("类别")
    private TypeCategory category;
    @EntityField("模板类型")
    private final @Nullable Type rawType;
    @EntityField("类型实参")
    private final @Nullable Table<Type> typeArguments;
    @EntityField("描述")
    private @Nullable String desc;
    @ChildEntity("字段")
    private final Table<Field> fields = new Table<>();
    @ChildEntity("枚举值")
    private final Table<EnumConstantRT> enumConstants = new Table<>();
    @ChildEntity("约束")
    private final Table<ConstraintRT<?>> constraints = new Table<>();
    @EntityField("类型成员")
    @Nullable
    private final Table<Type> typeParameters;
    @Nullable
    private final Table<Type> typeMembers;
    @EntityField("类型上限")
    @Nullable
    private final Table<Type> upperBounds;
    @EntityField("数组类型")
    @Nullable
    private Type arrayType;
    @EntityField("可空类型")
    @Nullable
    private Type nullableType;
    @ChildEntity("流程")
    private final Table<FlowRT> flows = new Table<>();

    public Type(
            String name,
            Type superType,
            TypeCategory category
    ) {
        this(name,
                superType,
                category,
                false,
                false,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public Type(
                String name,
                @Nullable Type superType,
                TypeCategory type,
                boolean anonymous,
                boolean ephemeral,
                @Nullable List<Type> typeParameters,
                @Nullable Type rawType,
                @Nullable List<Type> typeArguments,
                @Nullable Set<Type> typeMembers,
                @Nullable Set<Type> upperBounds,
                @Nullable String desc) {
        this.name = name;
        this.superType = superType;
        this.category = type;
        this.anonymous = anonymous;
        this.ephemeral = ephemeral;
        this.rawType = rawType;
        this.typeParameters = NncUtils.get(typeParameters, Table::new);
        this.typeArguments = NncUtils.get(typeArguments, Table::new);
        this.desc = desc;
        this.typeMembers = NncUtils.get(typeMembers, Table::new);
        this.upperBounds = NncUtils.get(upperBounds, Table::new);
    }

    public TypeCategory getCategory() {
        return category;
    }

    public void update(TypeDTO typeDTO) {
        setName(typeDTO.name());
        setDesc(typeDTO.desc());
    }

    public String getName() {
        return name;
    }

    public Type getSuperType() {
        return superType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public void setDesc(@Nullable String desc) {
        this.desc = desc;
    }

    @Nullable
    @SuppressWarnings("unused")
    public String getDesc() {
        return desc;
    }

    @JsonIgnore
    @SuppressWarnings("unused")
    public boolean isStandard() {
        return StdTypeManager.isStandardTypeId(id);
    }

    public List<Field> getFields() {
        if(superType != null) {
            return NncUtils.merge(superType.getFields(), fields);
        }
        else {
            return new ArrayList<>(fields);
        }
    }

    @JsonIgnore
    public Type getArrayType() {
        if(arrayType == null) {
            arrayType = TypeFactory.createParameterized(StandardTypes.ARRAY, List.of(this));
        }
        return arrayType;
    }

    @JsonIgnore
    public Type getNullableType() {
        if(nullableType == null) {
             nullableType = TypeFactory.createUnion(new LinkedHashSet<>(Set.of(this, StandardTypes.NULL)));
        }
        return nullableType;
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
        fields.add(field);
    }

    public void addEnumConstant(EnumConstantRT enumConstant) {
        for (EnumConstantRT e : enumConstants) {
            if(enumConstant.getId() != null && enumConstant.getId().equals(e.getId())
                    || enumConstant.getName().equals(e.getName())
                    || enumConstant.getOrdinal() == e.getOrdinal()) {
                throw BusinessException.duplicateOption(enumConstant);
            }
        }
        enumConstants.add(enumConstant);
    }

    public void setSuperType(@Nullable Type superType) {
        this.superType = superType;
    }

    public void setCategory(TypeCategory category) {
        this.category = category;
    }

    public void addConstraint(ConstraintRT<?> constraint) {
        this.constraints.add(constraint);
    }

    public void removeConstraint(long id) {
        this.constraints.removeIf(c -> c.getId() == id);
    }

    public UniqueConstraintRT getUniqueConstraint(List<Field> fields) {
        return NncUtils.find(getUniqueConstraints(), c -> c.getFields().equals(fields));
    }

    @JsonIgnore
    public boolean isEnum() {
        return category.isEnum();
    }

    @JsonIgnore
    public boolean isPojo() {
        return isClass() || isValue();
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

    @JsonIgnore
    public boolean isArray() {
        return category.isParameterized() && rawType == StandardTypes.ARRAY;
    }

    @JsonIgnore
    public boolean isPrimitive() {
        return isString() || isBool() || isTime() || isDate() || isDouble() || isInt() || isLong() || isPassword();
    }

    @JsonIgnore
    public boolean isString() {
        return this == StandardTypes.STRING;
    }

    @JsonIgnore
    public boolean isBool() {
        return this == StandardTypes.BOOL;
    }

    @JsonIgnore
    public boolean isTime() {
        return this == StandardTypes.TIME;
    }

    @JsonIgnore
    public boolean isDate() {
        return category.isDate();
    }

    @JsonIgnore
    public boolean isPassword() {
        return this == StandardTypes.PASSWORD;
    }

    @JsonIgnore
    public boolean isDouble() {
        return this == StandardTypes.DOUBLE;
    }

    @JsonIgnore
    public boolean isLong() {
        return this == StandardTypes.LONG;
    }

    @JsonIgnore
    public boolean isInt() {
        return this == StandardTypes.INT;
    }

    @JsonIgnore
    public boolean isNull() {
        return this == StandardTypes.NULL;
    }

    @JsonIgnore
    public boolean isNullable() {
        return category.isUnion() && typeMembers.contains(StandardTypes.NULL);
    }

    @JsonIgnore
    public boolean isTypeVariable() {
        return category.isVariable();
    }

    public boolean rawTypeEquals(Type type) {
        return Objects.equals(rawType, type);
    }

    @JsonIgnore
    public boolean isNotNull() {
        return !isNullable();
    }

    @Nullable
    public Table<Type> getUpperBounds() {
        return upperBounds;
    }

    @JsonIgnore
    public Type getElementType() {
        NncUtils.requireTrue(isArray(), () -> new InternalException("Type " + id + " is not an array"));
        Objects.requireNonNull(typeArguments);
        return typeArguments.get(0);
    }

    @JsonIgnore
    public Type getUnderlyingType() {
        NncUtils.requireTrue(isNullable(), () -> new InternalException("Type " + id + " is not nullable"));
        Objects.requireNonNull(typeMembers);
        return NncUtils.findRequired(typeMembers, t -> !t.equals(StandardTypes.NULL));
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public boolean isPersistent() {
        return !isEphemeral();
    }

    public Field getField(long fieldId) {
        if(superType != null) {
            Field superField = superType.getField(fieldId);
            if(superField != null) {
                return superField;
            }
        }
        return NncUtils.find(fields, f -> f.getId() == fieldId);
    }

    public Field getFieldByName(String fieldName) {
        if(superType != null) {
            Field superField = superType.getFieldByName(fieldName);
            if(superField != null) {
                return superField;
            }
        }
        return NncUtils.find(fields, f -> f.getName().equals(fieldName));
    }

    Column allocateColumn(Field field) {
        Type fieldType = field.getType();
        if(fieldType.getColumnType() == null) {
            return null;
        }
        List<Column> usedColumns = NncUtils.filterAndMap(
                getFields(),
                f -> !f.equals(field),
                Field::getColumn
        );
        Map<SQLColumnType, Queue<Column>> columnMap = SQLColumnType.getColumnMap(usedColumns);
        Queue<Column> columns = columnMap.get(fieldType.getColumnType());
        if(columns.isEmpty()) {
            throw BusinessException.invalidField(field, "属性数量超出限制");
        }
        return columns.poll();
    }

    private SQLColumnType getColumnType() {
        if(isArray()) {
            return getElementType().getColumnType();
        }
        if(isNullable()) {
            return getUnderlyingType().getColumnType();
        }
        if(isPrimitive()) {
            return StdTypeManager.getSQLColumnType(this.id);
        }
        if(isClass() || isEnum()) {
            return SQLColumnType.INT64;
        }
        return null;
    }

    public Field getFieldNyNameRequired(String fieldName) {
        return NncUtils.filterOneRequired(
                fields,
                f -> f.getName().equals(fieldName),
                "field not found: " + fieldName
        );
    }

    public List<EnumConstantRT> getEnumConstants() {
        return Collections.unmodifiableList(enumConstants);
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public EnumConstantRT getEnumConstant(long id) {
        return NncUtils.filterOneRequired(
                enumConstants,
                opt -> opt.getId() == id,
                "选项ID不存在: " + id
        );
    }

    public void remove() {
        if(NncUtils.isNotEmpty(fields)) {
            new ArrayList<>(fields).forEach(Field::remove);
        }
        if(NncUtils.isNotEmpty(enumConstants)) {
            new ArrayList<>(enumConstants).forEach(EnumConstantRT::remove);
        }
//        context.remove(this);
    }

    public void removeField(Field field) {
        if(fields.contains(field)) {
            fields.remove(field);
            field.remove();
        }
    }

    @Nullable
    public Table<Type> getTypeParameters() {
        return typeParameters;
    }

    @Nullable
    public Table<Type> getTypeArguments() {
        return typeArguments;
    }

    public Type getEffectiveType(Map<Type, Type> argumentMapping) {
        if(isTypeVariable()) {
            Type typeArgument = argumentMapping.get(this);
            return requireNonNull(typeArgument, "Can not resolve type variable: " + this.getName());
        }
        if(category.isUnion()) {
            Table<Type> effectiveMembers = new Table<>();
            for (Type typeMember : requireNonNull(typeMembers)) {
                effectiveMembers.add(
                        typeMember.getEffectiveType(argumentMapping)
                );
            }
            if(typeMembers.equals(effectiveMembers)) {
                return this;
            }
            else {
                return new Type(
                        NncUtils.join(effectiveMembers, Type::getName, "|"),
                        superType,
                        TypeCategory.UNION,
                        anonymous,
                        ephemeral,
                        null,
                        null,
                        null,
                        new LinkedHashSet<>(effectiveMembers),
                        null,
                        null
                );
            }
        }
        if(category.isParameterized()) {
            Type effectiveRawType = requireNonNull(rawType).getEffectiveType(argumentMapping);
            List<Type> effectiveTypeArgs = NncUtils.map(
                    requireNonNull(typeArguments),
                    typeArg -> typeArg.getEffectiveType(argumentMapping)
            );
            if(rawType.equals(effectiveRawType) && NncUtils.listEquals(typeArguments, effectiveTypeArgs)) {
                return this;
            }
            else {
                return new Type(
                        rawType.getName() + "<" + NncUtils.join(typeArguments, Type::getName, ",") + ">",
                        superType,
                        TypeCategory.UNION,
                        anonymous,
                        ephemeral,
                        null,
                        effectiveRawType,
                        effectiveTypeArgs,
                        null,
                        null,
                        null
                );
            }
        }
        else {
            return this;
        }
    }

    public TypePO toPO() {
        return new TypePO(
                id,
                getTenantId(),
                NncUtils.get(superType, Entity::getId),
                name,
                category.code(),
                desc,
                ephemeral,
                anonymous,
                NncUtils.get(rawType, Entity::getId),
                NncUtils.map(typeArguments, Entity::getId),
                NncUtils.mapUnique(typeMembers, Entity::getId)
        );
    }

    protected Type getFirstTypeArgument() {
        return NncUtils.getFirst(typeArguments);
    }

    @SuppressWarnings("unused")
    public boolean isInstance(IInstance instance) {
        return instance != null && this.equals(instance.getType());
    }

    public boolean isAssignableFrom(Type that) {
        if(equals(that)) {
            return true;
        }
        return that.getSuperType() != null && isAssignableFrom(that.getSuperType());
    }

    public TypeDTO toDTO() {
        return toDTO(true, true, true);
    }

    public TypeDTO toDTO(boolean withTitleField, boolean withFields, boolean withFieldTypes) {
        return TypeDTO.create(
                id,
                name,
                NncUtils.get(superType, Entity::getId),
                category.code(),
                anonymous,
                ephemeral,
                NncUtils.map(typeParameters, t -> t.toDTO(withTitleField, withFields, withFieldTypes)),
                NncUtils.get(rawType, t -> t.toDTO(withTitleField, withFields, withFieldTypes)),
                NncUtils.map(typeArguments, t -> t.toDTO(withTitleField, withFields, withFieldTypes)),
                NncUtils.map(typeMembers, t -> t.toDTO(withTitleField, withFields, withFieldTypes)),
                NncUtils.map(upperBounds, t -> t.toDTO(withTitleField, withFields, withFieldTypes)),
                desc,
                getFieldDTOs(withFields, withTitleField, withFieldTypes),
                NncUtils.map(constraints, ConstraintRT::toDTO),
                NncUtils.sortByIntAndMap(enumConstants, EnumConstantRT::getOrdinal, EnumConstantRT::toEnumConstantDTO)
//                getNullableType().id,
//                getArrayType().id
        );
    }

    private List<FieldDTO> getFieldDTOs(boolean withFields, boolean withTitleField, boolean withFieldTypes) {
        if(withFields) {
            return NncUtils.map(fields, f -> f.toDTO(withFieldTypes));
        }
        else if(withTitleField) {
            return NncUtils.filterAndMap(fields, Field::isAsTitle, f -> f.toDTO(withFieldTypes));
        }
        else {
            return List.of();
        }
    }

    @JsonIgnore
    public Field getTileField() {
        return NncUtils.find(getFields(), Field::isAsTitle);
    }

    public <T extends ConstraintRT<?>> List<T> getConstraints(Class<T> constraintType) {
        return NncUtils.filterAndMap(
                constraints,
                constraintType::isInstance,
                constraintType::cast
        );
    }

    public <T extends ConstraintRT<?>> T getConstraint(Class<T> constraintType, long id) {
        return NncUtils.find(getConstraints(constraintType), c -> c.getId() == id);
    }

    public ConstraintRT<?> getConstraint(long id) {
        return NncUtils.find(constraints, c -> c.getId() == id);
    }

    public List<UniqueConstraintRT> getUniqueConstraints() {
        return getConstraints(UniqueConstraintRT.class);
    }

    public UniqueConstraintRT getUniqueConstraint(long id) {
        return getConstraint(UniqueConstraintRT.class, id);
    }

    public Table<FlowRT> getFlows() {
        return flows;
    }

    public FlowRT getFlow(long id) {
        return NncUtils.findById(flows, id);
    }

    @JsonIgnore
    public Type getConcreteType() {
        if(isArray()) {
            return getElementType().getConcreteType();
        }
        if(isNullable()) {
            return getUnderlyingType();
        }
        return this;
    }

    @Override
    public String toString() {
        return "Type {name: " + name + "}";
    }
}

