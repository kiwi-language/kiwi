package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.LoadingList;
import tech.metavm.object.instance.ColumnType;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.Column;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;

public class Type extends Entity {
    private final boolean ephemeral;
    private boolean anonymous;
    private String name;
    private final TypeCategory category;
    private final Type baseType;
    private String desc;
    private transient boolean fieldAndOptionLoaded = false;
    private transient final List<Field> fields;
    private transient final List<ChoiceOption> choiceOptions;

    public Type(TypeDTO typeDTO, EntityContext context) {
        this(
                typeDTO.id(),
                typeDTO.name(),
                TypeCategory.getByCodeRequired(typeDTO.type()),
                false,
                typeDTO.ephemeral(),
                NncUtils.get(typeDTO.baseTypeId(), context::getType),
                typeDTO.desc(),
                new ArrayList<>(),
                new ArrayList<>(),
                context
        );
    }

    public Type(TypePO po, EntityContext context) {
        this(
                po.getId(),
                po.getName(),
                TypeCategory.getByCodeRequired(po.getCategory()),
                po.getAnonymous(),
                po.getEphemeral(),
                NncUtils.get(po.getBaseTypeId(), context::getType),
                po.getDesc(),
                null,
                null,
                context
        );
    }

    public Type(
            String name,
            TypeCategory type,
            boolean anonymous,
            boolean ephemeral,
            Type baseType,
            String desc,
            EntityContext context) {
        this(
                null,
                name,
                type,
                anonymous,
                ephemeral,
                baseType,
                desc,
                new ArrayList<>(),
                new ArrayList<>(),
                context
        );
    }

    private Type(
                Long id,
                String name,
                TypeCategory type,
                boolean anonymous,
                boolean ephemeral,
                Type baseType,
                String desc,
                List<Field> fields,
                List<ChoiceOption> choiceOptions,
                EntityContext context) {
        super(id, context);
        this.id = id;
        this.name = name;
        this.category = type;
        this.anonymous = anonymous;
        this.ephemeral = ephemeral;
        this.baseType = baseType;
        this.desc = desc;
        TypeStore typeStore = context.getTypeStore();
        this.fields = fields != null ? fields : typeStore.getFieldsLoadingList(this);
        this.choiceOptions = choiceOptions != null ? choiceOptions : typeStore.getChoiceOptionsLoadingList(this);
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

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    void preloadFields(List<Field> fields) {
        if(this.fields instanceof LoadingList<Field> loadingList) {
            loadingList.preload(fields);
        }
        else {
            throw new InternalException("the list of fields is not a loading list");
        }
    }

    void preloadChoiceOptions(List<ChoiceOption> choiceOptions) {
        if(this.choiceOptions instanceof LoadingList<ChoiceOption> loadingList) {
            loadingList.preload(choiceOptions);
        }
        else {
            throw new InternalException("the list of fields is not a loading list");
        }
    }

    public List<Field> getFields() {
        return Collections.unmodifiableList(fields());
    }

    public Type getArrayType() {
        return getContext().getTypeStore().getArrayType(this, getContext());
    }

    private List<Field> fields() {
        ensureFieldAndOptionLoaded();
        return fields;
    }

    private void ensureFieldAndOptionLoaded() {
//        if(!fieldAndOptionLoaded) {
//            fieldAndOptionLoaded = true;
//            loadFieldsAndOptions();
//        }
    }

    private List<ChoiceOption> choiceOptions() {
        ensureFieldAndOptionLoaded();
        return choiceOptions;
    }

    void initField(Field field) {
        fields.add(field);
    }

    public void addField(Field field) {
        ensureFieldAndOptionLoaded();
        if(field.getId() != null && getField(field.getId()) != null) {
            throw new RuntimeException("Field " + field.getId() + " is already added");
        }
        if(getFieldByName(field.getName()) != null) {
            throw BusinessException.invalidField(field, "属性名称'" + field.getName() + "'已存在");
        }
        if(field.isAsTitle() && getTileField() != null) {
            throw BusinessException.multipleTitleFields();
        }
        fields().add(field);
    }

    public void addChoiceOption(ChoiceOption option) {
        ensureFieldAndOptionLoaded();
        for (ChoiceOption choiceOption : choiceOptions()) {
            if(option.getId() != null && option.getId().equals(choiceOption.getId())
                    || option.getName().equals(choiceOption.getName())
                    || option.getOrder() == choiceOption.getOrder()) {
                throw BusinessException.duplicateOption(option);
            }
        }
        choiceOptions().add(option);
    }

    private void loadFieldsAndOptions() {
        context.loadFieldsAndOptions(List.of(this));
    }

    public boolean isEnum() {
        return category.isEnum();
    }

    public boolean isTable() {
        return category.isTable();
    }

    public boolean isArray() {
        return category.isArray();
    }

    public boolean isPrimitive() {
        return category.isPrimitive();
    }

    public boolean isString() {
        return category == TypeCategory.STRING;
    }

    public boolean isBool() {
        return category == TypeCategory.BOOL;
    }

    public boolean isTime() {
        return category == TypeCategory.TIME;
    }

    public boolean isDate() {
        return category == TypeCategory.DATE;
    }

    public boolean isNumber() {
        return category == TypeCategory.DOUBLE;
    }

    public boolean isInt64() {
        return category == TypeCategory.INT64;
    }

    public boolean isInt32() {
        return category == TypeCategory.INT32;
    }

    public boolean isNullable() {
        return category.isNullable();
    }

    public boolean isNotNull() {
        return !isNullable();
    }

    public Type getBaseType() {
        return baseType;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public boolean isPersistent() {
        return !isEphemeral();
    }

    public Field getField(long fieldId) {
        return NncUtils.filterOne(fields(), f -> f.getId() == fieldId);
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
        return NncUtils.filterOne(fields(), f -> f.getName().equals(fieldName));
    }

    Column allocateColumn(Field field) {
        TypeCategory fieldTypeCategory = field.getBaseTypeCategory();
        if(fieldTypeCategory.getColumnType() == null) {
            return null;
        }
        List<Column> usedColumns = NncUtils.filterAndMap(
                fields(),
                f -> !f.equals(field),
                Field::getColumn
        );
        Map<ColumnType, Queue<Column>> columnMap = ColumnType.getColumnMap(usedColumns);
        Queue<Column> columns = columnMap.get(fieldTypeCategory.getColumnType());
        if(columns.isEmpty()) {
            throw BusinessException.invalidField(field, "属性数量超出限制");
        }
        return columns.poll();
    }

    public Field getFieldNyNameRequired(String fieldName) {
        return NncUtils.filterOneRequired(
                fields(),
                f -> f.getName().equals(fieldName),
                "field not found: " + fieldName
        );
    }

    public List<ChoiceOption> getChoiceOptions() {
        return choiceOptions();
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public ChoiceOption getChoiceOption(long id) {
        return NncUtils.filterOneRequired(
                choiceOptions(),
                opt -> opt.getId() == id,
                "选项ID不存在: " + id
        );
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
        po.setBaseTypeId(NncUtils.get(baseType, Entity::getId));
        po.setCategory(category.code());
        po.setDesc(desc);
        return po;
    }

    public boolean isInstance(Instance instance) {
        return instance != null && this.equals(instance.getType());
    }

    public TypeDTO toDTO() {
        return new TypeDTO(
                id,
                name,
                category.code(),
                ephemeral,
                NncUtils.get(baseType, Entity::getId),
                desc,
                NncUtils.get(getTileField(), Field::toTitleDTO),
                NncUtils.map(fields(), Field::toDTO)
        );
    }

    public Field getTileField() {
        return NncUtils.filterOne(fields(), Field::isAsTitle);
    }

    void initFieldsAndOptions(List<Field> fields, List<ChoiceOption> options) {
        fieldAndOptionLoaded = true;
        this.fields.addAll(fields);
        this.choiceOptions.addAll(options);
    }

    public Type getConcreteType() {
        if (isArray() || isNullable()) {
            return getBaseType();
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        return "Type {name: " + name + "}";
    }
}

