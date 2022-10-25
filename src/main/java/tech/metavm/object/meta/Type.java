package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.LoadingList;
import tech.metavm.object.instance.SQLColumnType;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.Column;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

public class Type extends Entity {
    private final boolean ephemeral;
    private boolean anonymous;
    private String name;
    private final TypeCategory category;
    private final @Nullable Type rawType;
    private final @Nullable List<Type> typeArguments;
    private @Nullable String desc;
    private transient final List<Field> fields;
    private transient final List<EnumConstant> enumConstants;

    public Type(TypeDTO typeDTO, EntityContext context) {
        this(
                typeDTO.id(),
                typeDTO.name(),
                TypeCategory.getByCodeRequired(typeDTO.category()),
                typeDTO.anonymous(),
                typeDTO.ephemeral(),
                NncUtils.get(typeDTO.rawType(), TypeDTO::id, context::getTypeRef),
                NncUtils.map(typeDTO.typeArguments(), TypeDTO::id, context::getTypeRef),
                typeDTO.desc(),
                new ArrayList<>(),
                new ArrayList<>(),
                context
        );
    }

    public Type(TypePO po, List<InstancePO> choiceOptions, EntityContext context) {
        this(
                po.getId(),
                po.getName(),
                TypeCategory.getByCodeRequired(po.getCategory()),
                po.getAnonymous(),
                po.getEphemeral(),
                NncUtils.get(po.getRawTypeId(), context::getTypeRef),
                NncUtils.map(po.getTypeArgumentIds(), context::getTypeRef),
                po.getDesc(),
                null,
                choiceOptions,
                context
        );
    }

    public Type(
            String name,
            TypeCategory type,
            boolean anonymous,
            boolean ephemeral,
            Type rawType,
            List<Type> typeArguments,
            String desc,
            EntityContext context) {
        this(
                null,
                name,
                type,
                anonymous,
                ephemeral,
                rawType,
                typeArguments,
                desc,
                new ArrayList<>(),
                new ArrayList<>(),
                context
        );
    }

    Type(
                Long id,
                String name,
                TypeCategory type,
                boolean anonymous,
                boolean ephemeral,
                @Nullable Type rawType,
                @Nullable List<Type> typeArguments,
                @Nullable String desc,
                List<Field> fields,
                List<InstancePO> choiceOptionPOs,
                EntityContext context) {
        super(id, context);
        this.id = id;
        this.name = name;
        this.category = type;
        this.anonymous = anonymous;
        this.ephemeral = ephemeral;
        this.rawType = rawType;
        this.typeArguments = typeArguments;
        this.desc = desc;
        TypeStore typeStore = context.getTypeStore();
        this.fields = fields != null ? fields : typeStore.getFieldsLoadingList(this);
        this.enumConstants = new ArrayList<>();
        if (NncUtils.isNotEmpty(choiceOptionPOs)) {
            for (InstancePO choiceOption : choiceOptionPOs) {
                new EnumConstant(choiceOption, this);
            }
        }
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

    public void setName(String name) {
        this.name = name;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public void setDesc(@Nullable String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    void preloadFields(List<Field> fields) {
        if(this.fields instanceof LoadingList<Field> loadingList) {
            if(!loadingList.isLoaded()) {
                loadingList.preload(fields);
            }
        }
        else {
            throw new InternalException("the list of fields is not a loading list");
        }
    }
//
//    void preloadChoiceOptions(List<ChoiceOption> choiceOptions) {
//        if(this.choiceOptions instanceof LoadingList<ChoiceOption> loadingList) {
//            loadingList.preload(choiceOptions);
//        }
//        else {
//            throw new InternalException("the list of fields is not a loading list");
//        }
//    }

    public List<Field> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public Type getArrayType() {
        return context.getParameterizedType(context.getRawArrayType(), List.of(this));
    }

    public Type getNullableType() {
        return context.getParameterizedType(context.getRawNullableType(), List.of(this));
    }

    void initField(Field field) {
        fields.add(field);
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

    public void addEnumConstant(EnumConstant enumConstant) {
        for (EnumConstant e : enumConstants) {
            if(enumConstant.getId() != null && enumConstant.getId().equals(e.getId())
                    || enumConstant.getName().equals(e.getName())
                    || enumConstant.getOrdinal() == e.getOrdinal()) {
                throw BusinessException.duplicateOption(enumConstant);
            }
        }
        enumConstants.add(enumConstant);
    }

    public boolean isEnum() {
        return category.isEnum();
    }

    public boolean isClass() {
        return category.isClass();
    }

    public boolean isArray() {
        return rawTypeEquals(context.getRawArrayType());
    }

    public boolean isPrimitive() {
        return isString() || isBool() || isTime() || isDate() || isDouble() || isInt() || isLong();
    }

    public boolean isString() {
        return equals(context.getStringType());
    }

    public boolean isBool() {
        return equals(context.getBoolType());
    }

    public boolean isTime() {
        return equals(context.getTimeType());
    }

    public boolean isDate() {
        return equals(context.getDateType());
    }

    public boolean isDouble() {
        return equals(context.getDoubleType());
    }

    public boolean isLong() {
        return equals(context.getLongType());
    }

    public boolean isInt() {
        return equals(context.getIntType());
    }

    public boolean isNullable() {
        return rawTypeEquals(context.getRawNullableType());
    }

    public boolean rawTypeEquals(Type type) {
        return Objects.equals(rawType, type);
    }

    public boolean isNotNull() {
        return !isNullable();
    }

    public Type getElementType() {
        NncUtils.requireTrue(isArray(), () -> new InternalException("Type " + id + " is not an array"));
        Objects.requireNonNull(typeArguments);
        return typeArguments.get(0);
    }

    public Type getUnderlyingType() {
        NncUtils.requireTrue(isNullable(), () -> new InternalException("Type " + id + " is not nullable"));
        Objects.requireNonNull(typeArguments);
        return typeArguments.get(0);
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public boolean isPersistent() {
        return !isEphemeral();
    }

    public Field getField(long fieldId) {
        return NncUtils.filterOne(fields, f -> f.getId() == fieldId);
    }

    public Field getFieldNyPath(String fieldPath) {
        int idx = fieldPath.indexOf('.');
        if(idx == -1) {
            return getFieldByName(fieldPath);
        }
        else {
            String fieldName = fieldPath.substring(0, idx);
            String subPath = fieldPath.substring(idx + 1);
            Field field = getFieldByName(fieldName);
            if(field == null) {
                throw new RuntimeException("Invalid field path '" + fieldPath + "', field '" + fieldName + "' not found");
            }
            return field.getType().getFieldNyPath(subPath);
        }
    }

    public Field getFieldByName(String fieldName) {
        return NncUtils.filterOne(fields, f -> f.getName().equals(fieldName));
    }

    Column allocateColumn(Field field) {
        Type fieldType = field.getType();
        if(fieldType.getColumnType() == null) {
            return null;
        }
        List<Column> usedColumns = NncUtils.filterAndMap(
                fields,
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
            return PrimitiveTypes.get(this.id).columnType();
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

    public List<EnumConstant> getEnumConstants() {
        return Collections.unmodifiableList(enumConstants);
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public EnumConstant getEnumConstant(long id) {
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
            new ArrayList<>(enumConstants).forEach(context::remove);
        }
        context.remove(this);
    }

    public void removeField(Field field) {
        ListIterator<Field> it = fields.listIterator();
        while (it.hasNext()) {
            if(EntityUtils.entityEquals(it.next(), field)) {
                it.remove();
                return;
            }
        }
    }

    public TypePO toPO() {
        TypePO po = new TypePO();
        po.setName(name);
        po.setTenantId(getTenantId());
        po.setId(id);
        po.setAnonymous(anonymous);
        po.setEphemeral(ephemeral);
        po.setRawTypeId(NncUtils.get(rawType, Entity::getId));
        po.setTypeArgumentIds(NncUtils.map(typeArguments, Type::getId));
        po.setCategory(category.code());
        po.setDesc(desc);
        return po;
    }

    protected Type getFirstTypeArgument() {
        return NncUtils.getFirst(typeArguments);
    }

    public boolean isInstance(Instance instance) {
        return instance != null && this.equals(instance.getType());
    }

    public TypeDTO toDTO() {
        return toDTO(true, true, false);
    }

    public TypeDTO toDTO(boolean withTitleField, boolean withFields, boolean withFieldTypes) {
        return new TypeDTO(
                id,
                name,
                category.code(),
                anonymous,
                ephemeral,
                NncUtils.get(rawType, Type::toDTO),
                NncUtils.map(typeArguments, Type::toDTO),
                desc,
                getFieldDTOs(withFields, withTitleField, withFieldTypes),
                NncUtils.sortByIntAndMap(enumConstants, EnumConstant::getOrdinal, EnumConstant::toDTO)
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

    public Field getTileField() {
        return NncUtils.filterOne(fields, Field::isAsTitle);
    }

    public Type getConcreteType() {
        Type t = this;
        Type b = getFirstTypeArgument();
        while(b != null) {
            t = b;
            b = b.getFirstTypeArgument();
        }
        return t;
    }

    @Override
    public String toString() {
        return "Type {name: " + name + "}";
    }
}

