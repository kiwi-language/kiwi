package tech.metavm.object.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.flow.FlowManager;
import tech.metavm.object.meta.persistence.query.TypeQuery;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;

@Component
public class TypeManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(TypeManager.class);

    @Autowired
    private TypeStore typeStore;

    @Autowired
    private FieldStore fieldStore;

    @Autowired
    private FlowManager flowManager;

    @Autowired
    private EntityContextFactory contextFactory;

    public Page<TypeDTO> query(String searchText, List<Integer> categoryCodes, int page, int pageSize) {
        EntityContext context = newContext();
        return query(searchText, categoryCodes, page, pageSize, context);
    }

    public Page<TypeDTO> query(String searchText, List<Integer> categoryCodes, int page, int pageSize, EntityContext context) {

        List<TypeCategory> categories = categoryCodes != null ?
                NncUtils.map(categoryCodes, TypeCategory::getByCodeRequired) : List.of(TypeCategory.CLASS);
        TypeQuery query = new TypeQuery(
                ContextUtil.getTenantId(),
                categories,
                searchText,
                false,
                page,
                pageSize
        );
        long total = typeStore.count(query);
        List<Type> types = typeStore.query(query, context);
        List<TypeDTO> dtoList = NncUtils.map(types, t -> t.toDTO(true, false, false));
        return new Page<>(dtoList, total);
    }

    public TypeDTO getType(long id, boolean includingFields, boolean includingFieldTypes) {
        Type type = newContext().getType(id);
        return NncUtils.get(type, t -> t.toDTO(false, includingFields, includingFieldTypes));
    }

    public TypeDTO getArrayType(long id) {
        EntityContext context = newContext();
        return context.getType(id).getArrayType().toDTO();
    }

    public TypeDTO getNullableType(long id) {
        EntityContext context = newContext();
        return context.getType(id).getNullableType().toDTO();
    }

    public TypeDTO getNullableArrayType(long id) {
        EntityContext context = newContext();
        return context.getType(id).getNullableType().getArrayType().toDTO();
    }

    @Transactional
    public long saveType(TypeDTO typeDTO) {
        EntityContext context = newContext();
//        Type category = saveType(typeDTO, context);
        Type type = saveTypeWithFields(typeDTO, context);
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
        return new Type(typeDTO, context);
    }

    public Type updateType(TypeDTO typeDTO, EntityContext context) {
        NncUtils.requireNonNull(typeDTO.name(), "名称");
        NncUtils.requireNonNull(typeDTO.id(), "ID");
        Type type = context.getTypeRef(typeDTO.id());
        if(!type.getName().equals(typeDTO.name())) {
            ensureTypeNameAvailable(typeDTO, context);
        }
        type.update(typeDTO);
        return type;
    }

    private void ensureTypeNameAvailable(TypeDTO typeDTO, EntityContext context) {
        Type typeWithSameName = context.getTypeByName(typeDTO.name());
        if (typeWithSameName != null && !typeWithSameName.isAnonymous()) {
            throw BusinessException.invalidType(typeDTO, "对象名称已存在");
        }
    }

    @Transactional
    public void deleteType(long id) {
        EntityContext context = newContext();
        Type type = context.getTypeRef(id);
        if(type == null) {
            return;
        }
        deleteType0(type, new HashSet<>());
        context.sync();
    }

    private void deleteType0(Type type, Set<Long> visited) {
        if(visited.contains(type.getId())) {
            throw new InternalException("Circular reference. category: " + type + " is already visited");
        }
        visited.add(type.getId());
        EntityContext context = type.getContext();
        List<Type> dependentTypes = context.getTypeStore().getDependentTypes(type);
        if(NncUtils.isNotEmpty(dependentTypes)) {
            dependentTypes.forEach(t -> deleteType0(t, visited));
        }
        List<String> referringFieldNames = fieldStore.getReferringFieldNames(type);
        if(NncUtils.isNotEmpty(referringFieldNames)) {
            throw BusinessException.typeReferredByFields(type, referringFieldNames);
        }
        flowManager.deleteByOwner(type);
        type.remove();
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
        Type owner = context.getType(fieldDTO.declaringTypeId());
        Type type = context.getType(fieldDTO.typeId());
        return new Field(fieldDTO, owner, type);
//        TypeCategory typeCategory = TypeCategory.getByCodeRequired(fieldDTO.type());
//        Object defaultValue;
//        if(typeCategory.isEnum()) {
//            EnumEditContext enumContext = saveEnumType(fieldDTO, context);
//            type = enumContext.getType();
//            defaultValue = OptionUtil.getDefaultValue(enumContext.getDefaultOptions(), type.isArray());
//        }
//        else {
//            type = context.getType(fieldDTO.typeId());
//            defaultValue = fieldDTO.defaultValue();
//        }
//        return new Field(fieldDTO, owner, type);
//        field.setDefaultValue(defaultValue);
//        return field;
    }

    public Field updateField(FieldDTO fieldDTO, EntityContext context) {
        NncUtils.requireNonNull(fieldDTO.id(), "列ID必填");
//        TypeCategory typeCategory = TypeCategory.getByCodeRequired(fieldDTO.type());
        Field field = context.get(Field.class, fieldDTO.id());
        field.update(fieldDTO);
//        if(typeCategory.isEnum()) {
//            EnumEditContext enumContext = saveEnumType(fieldDTO, context);
//            Object defaultValue = OptionUtil.getDefaultValue(enumContext.getDefaultOptions(), field.isArray());
//            field.setDefaultValue(defaultValue);
//        }
        return field;
    }

//    private EnumEditContext saveEnumType(FieldDTO fieldDTO, EntityContext context) {
//        EnumEditContext enumEditContext = new EnumEditContext(
//                fieldDTO.typeId(),
//                fieldDTO.name(),
//                true,
//                fieldDTO.choiceOptions(),
//                context
//        );
//        enumEditContext.execute();
//        return enumEditContext;
//    }

    public FieldDTO getField(long fieldId) {
        Field field = newContext().getFieldRef(fieldId);
        return NncUtils.get(field, Field::toDTO);
    }

    @Transactional
    public void removeField(long fieldId) {
        EntityContext context = newContext();
        removeField(context.getFieldRef(fieldId), context);
        context.sync();
    }

    private void removeField(Field field, EntityContext context) {
        if(field.isCustomTyped()) {
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
        Field field = context.getFieldRef(fieldId);
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
