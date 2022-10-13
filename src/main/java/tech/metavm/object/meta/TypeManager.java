package tech.metavm.object.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.object.meta.persistence.query.TypeQuery;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TitleFieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;
import tech.metavm.util.OptionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class TypeManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(TypeManager.class);

    @Autowired
    private TypeStore metadataStore;

    @Autowired
    private EntityContextFactory contextFactory;

    public Page<TypeDTO> query(String searchText, int page, int pageSize) {
        EntityContext context = newContext();
        TypeQuery query = new TypeQuery(
                ContextUtil.getTenantId(),
                TypeCategory.TABLE.code(),
                searchText,
                page,
                pageSize
        );
        long total = metadataStore.count(query);
        List<Type> types = metadataStore.query(query, context);
        List<TypeDTO> dtoList = NncUtils.map(types, Type::toDTO);
        return new Page<>(dtoList, total);
    }

    public TypeDTO getType(long id) {
        Type type = newContext().getType(id);
        return NncUtils.get(type, Type::toDTO);
    }

    @Transactional
    public long saveType(TypeDTO typeDTO) {
        EntityContext context = newContext();
        Type type = saveType(typeDTO, context);
        context.sync();
        return type.getId();
    }

    public Type saveType(TypeDTO typeDTO, EntityContext context) {
        if(typeDTO.id() == null) {
            return createType(typeDTO, context);
        }
        else {
            return updateType(typeDTO, context);
        }
    }

    public Type saveTypeWithFields(TypeDTO typeDTO, EntityContext context) {
        Type type;
        if(typeDTO.id() == null) {
            type = createType(typeDTO, context);
        }
        else {
            type = updateType(typeDTO, context);
        }
        List<Field> fieldsToRemove = new ArrayList<>();
        Set<Long> fieldIds = NncUtils.mapNonNullUnique(typeDTO.fields(), FieldDTO::id);
        for (Field field : type.getFields()) {
            if(!fieldIds.contains(field.getId())) {
                fieldsToRemove.add(field);
            }
        }
        fieldsToRemove.forEach(f -> removeField(f, context));
        for (FieldDTO fieldDTO : typeDTO.fields()) {
            saveField(fieldDTO, context);
        }
        return type;
    }

    public Type createType(TypeDTO typeDTO, EntityContext context) {
        NncUtils.requireNonNull(typeDTO.name(), "名称");
        ensureTypeNameAvailable(typeDTO, context);
        Type type = new Type(typeDTO, context);
        saveTitleField(typeDTO.titleField(), type, context);
        return type;
    }

    public Type updateType(TypeDTO typeDTO, EntityContext context) {
        NncUtils.requireNonNull(typeDTO.name(), "名称");
        NncUtils.requireNonNull(typeDTO.id(), "ID");
        Type type = context.getType(typeDTO.id());
        if(!type.getName().equals(typeDTO.name())) {
            ensureTypeNameAvailable(typeDTO, context);
        }
        type.update(typeDTO);
        saveTitleField(typeDTO.titleField(), type, context);
        return type;
    }

    private void saveTitleField(TitleFieldDTO titleFieldDTO, Type type, EntityContext context) {
        if(titleFieldDTO != null) {
            Field titleField = type.getTileField();
            TypeCategory titleTypeCategory = TypeCategory.getByCodeRequired(titleFieldDTO.type());
            Type titleType = context.getTypeByCategory(titleTypeCategory);
            if (titleField == null) {
                new Field(
                        null,
                        titleFieldDTO.name(),
                        type,
                        Access.Public,
                        titleFieldDTO.unique(),
                        true,
                        null,
                        null,
                        titleType,
                        context,
                        false
                );
            } else {
                titleField.setName(titleFieldDTO.name());
                titleField.setUnique(titleFieldDTO.unique());
                titleField.setDefaultValue(titleFieldDTO.defaultValue());
            }
        }
    }

    private void ensureTypeNameAvailable(TypeDTO typeDTO, EntityContext context) {
        Type typeWithSameName = context.getTypeByName(typeDTO.name());
        if (typeWithSameName != null) {
            throw BusinessException.invalidNClass(typeDTO, "对象名称已存在");
        }
    }

    @Transactional
    public void deleteType(long id) {
        EntityContext context = newContext();
        Type type = context.getType(id);
        if(type == null) {
            return;
        }
        type.getFields().forEach(context::remove);
        context.remove(type);
        context.sync();
    }

    public long saveField(FieldDTO fieldDTO) {
        EntityContext context = newContext();
        Field field = saveField(fieldDTO, context);
        context.sync();
        return field.getId();
    }

    public Field saveField(FieldDTO fieldDTO, EntityContext context) {
        if(fieldDTO.id() == null) {
            return createField(fieldDTO, context);
        }
        else {
            return updateField(fieldDTO, context);
        }
    }

    public Field createField(FieldDTO fieldDTO, EntityContext context) {
        Type owner = context.getType(fieldDTO.ownerId());
        TypeCategory typeCategory = TypeCategory.getByCodeRequired(fieldDTO.type());
        Type type;
        Object defaultValue;
        if(typeCategory.isEnum()) {
            EnumEditContext enumContext = saveEnumType(fieldDTO, context);
            type = enumContext.getType();
            defaultValue = OptionUtil.getDefaultValue(enumContext.getDefaultOptions(), fieldDTO.multiValued());
        }
        else {
            type = context.resolveType(fieldDTO);
            defaultValue = fieldDTO.defaultValue();
        }
        Field field = new Field(fieldDTO, owner, type);
        field.setDefaultValue(defaultValue);
        return field;
    }

    public Field updateField(FieldDTO fieldDTO, EntityContext context) {
        NncUtils.require(fieldDTO.id(), "列ID");
        TypeCategory typeCategory = TypeCategory.getByCodeRequired(fieldDTO.type());
        Field field = context.get(Field.class, fieldDTO.id());
        field.update(fieldDTO);
        if(typeCategory.isEnum()) {
            EnumEditContext enumContext = saveEnumType(fieldDTO, context);
            Object defaultValue = OptionUtil.getDefaultValue(enumContext.getDefaultOptions(), fieldDTO.multiValued());
            field.setDefaultValue(defaultValue);
        }
        return field;
    }

    private EnumEditContext saveEnumType(FieldDTO fieldDTO, EntityContext context) {
        EnumEditContext enumEditContext = new EnumEditContext(
                fieldDTO.targetId(),
                fieldDTO.name(),
                true,
                fieldDTO.choiceOptions(),
                context
        );
        enumEditContext.execute();
        return enumEditContext;
    }

    public FieldDTO getField(long fieldId) {
        Field field = newContext().getField(fieldId);
        return NncUtils.get(field, Field::toDTO);
    }

    @Transactional
    public void removeField(long fieldId) {
        EntityContext context = newContext();
        removeField(context.getField(fieldId), context);
        context.sync();
    }

    private void removeField(Field field, EntityContext context) {
        if(field.isComposite()) {
            Type type = field.getType();
            if(type.isAnonymous()) {
                context.remove(type);
            }
        }
        field.remove();
    }

    @Transactional
    public void setFieldAsTitle(long fieldId) {
        EntityContext context = newContext();
        Field field = context.getField(fieldId);
        if(field.isAsTitle()) {
            return;
        }
        field.setAsTitle(true);
        context.sync();
    }

    private EntityContext newContext() {
        return contextFactory.newContext();
    }

}
