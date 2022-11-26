package tech.metavm.object.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tech.metavm.dto.Page;
import tech.metavm.entity.*;
import tech.metavm.flow.FlowManager;
import tech.metavm.object.meta.persistence.FieldPO;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.object.meta.persistence.query.TypeQuery;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Component
public class TypeManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(TypeManager.class);

//    @Autowired
//    private FieldStore fieldStore;

    @Autowired
    private FlowManager flowManager;

    @Autowired
    private InstanceContextFactory contextFactory;

    @Autowired
    private EntityQueryService entityQueryService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public Page<TypeDTO> query(String searchText, List<Integer> categoryCodes, int page, int pageSize) {
        EntityContext context = newContext();
        return query(searchText, categoryCodes, page, pageSize, context);
    }

    public Page<TypeDTO> query(String searchText,
                               List<Integer> categoryCodes,
                               int page,
                               int pageSize,
                               EntityContext context) {
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

        Page<Type> typePage = entityQueryService.query(
                EntityQuery.create(
                        Type.class,
                        searchText,
                        page,
                        pageSize,
                        List.of(
                                new EntityQueryField("category", categoryCodes)
                        )
                ),
                context
        );

        return new Page<>(
                NncUtils.map(typePage.data(), t -> t.toDTO(true, false, false)),
                typePage.total()
        );
    }

    public TypeDTO getType(long id, boolean includingFields, boolean includingFieldTypes) {
        Type type = newContext().getType(id);
        return NncUtils.get(type, t -> t.toDTO(false, includingFields, includingFieldTypes));
    }

    public TypeDTO getArrayType(long id) {
        return getOrCreateCompositeType(id, Type::getArrayType);
    }

    public TypeDTO getNullableType(long id) {
        return getOrCreateCompositeType(id, Type::getNullableType);
    }

    public TypeDTO getNullableArrayType(long id) {
        return getOrCreateCompositeType(id, type -> type.getNullableType().getArrayType());
    }

    private TypeDTO getOrCreateCompositeType(long id, Function<Type, Type> mapper) {
        EntityContext context = newContext();
        Type type = context.getType(id);
        Type compositeType = mapper.apply(type);
        if(compositeType.getId() != null) {
            return compositeType.toDTO();
        }
        else {
            return createCompositeType(id, mapper);
        }
    }

    private TypeDTO createCompositeType(long id, Function<Type, Type> mapper) {
        return transactionTemplate.execute(status -> {
            EntityContext context = newContext();
            Type compositeType = mapper.apply(context.getType(id));
            context.finish();
            return compositeType.toDTO();
        });
    }

    @Transactional
    public long saveType(TypeDTO typeDTO) {
        EntityContext context = newContext();
        Type type = saveTypeWithContent(typeDTO, context);
        context.finish();
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

    public Type saveTypeWithContent(TypeDTO typeDTO, EntityContext context) {
        Type type;
        if(typeDTO.id() == null || typeDTO.id() == 0L) {
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
        return TypeFactory.createAndBind(typeDTO, context);
    }

    public Type updateType(TypeDTO typeDTO, EntityContext context) {
        NncUtils.requireNonNull(typeDTO.name(), "名称");
        NncUtils.requireNonNull(typeDTO.id(), "ID");
        Type type = context.getType(typeDTO.id());
        if(!type.getName().equals(typeDTO.name())) {
            ensureTypeNameAvailable(typeDTO, context);
        }
        type.update(typeDTO);
        return type;
    }

    private void ensureTypeNameAvailable(TypeDTO typeDTO, EntityContext context) {
        Type typeWithSameName = context.selectByUniqueKey(TypePO.UNIQUE_NAME, typeDTO.name());
        if (typeWithSameName != null && !typeWithSameName.isAnonymous()) {
            throw BusinessException.invalidType(typeDTO, "对象名称已存在");
        }
    }

    @Transactional
    public void deleteType(long id) {
        EntityContext context = newContext();
        Type type = context.getType(id);
        if(type == null) {
            return;
        }
        deleteType0(type, new HashSet<>(), context);
        context.finish();
    }

    private void deleteType0(Type type, Set<Long> visited, EntityContext context) {
        if(visited.contains(type.getId())) {
            throw new InternalException("Circular reference. category: " + type + " is already visited");
        }
        visited.add(type.getId());
        List<Type> dependentTypes = getDependentTypes(type, context);
        if(NncUtils.isNotEmpty(dependentTypes)) {
            dependentTypes.forEach(t -> deleteType0(t, visited, context));
        }
        List<Field> referringFields = context.selectByKey(
                FieldPO.INDEX_TYPE_ID, type.getId()
        );
        List<String> referringFieldNames = NncUtils.map(referringFields, Field::getName);
        if(NncUtils.isNotEmpty(referringFieldNames)) {
            throw BusinessException.typeReferredByFields(type, referringFieldNames);
        }
//        flowManager.deleteByOwner(type);
        type.remove();
    }

    public List<Type> getDependentTypes(Type type, EntityContext context) {
        return NncUtils.merge(
                context.selectByKey(TypePO.INDEX_RAW_TYPE_ID, type.getId()),
                context.selectByKey(TypePO.INDEX_TYPE_ARG_ID, type.getId())
        );
    }

    @Transactional
    public long saveField(FieldDTO fieldDTO) {
        EntityContext context = newContext();
        Field field = saveField(fieldDTO, context);
        context.finish();
        return field.getId();
    }

    public Field saveField(FieldDTO fieldDTO, EntityContext context) {
        if(fieldDTO.id() == null || fieldDTO.id() == 0L) {
            return createField(fieldDTO, context);
        }
        else {
            return updateField(fieldDTO, context);
        }
    }

    public Field createField(FieldDTO fieldDTO, EntityContext context) {
        return TypeFactory.createField(
                context.getType(fieldDTO.declaringTypeId()),
                fieldDTO,
                context
        );
    }

    public Field updateField(FieldDTO fieldDTO, EntityContext context) {
        NncUtils.requireNonNull(fieldDTO.id(), "列ID必填");
        Field field = context.getEntity(Field.class, fieldDTO.id());
        field.update(fieldDTO);
        return field;
    }

    public FieldDTO getField(long fieldId) {
        Field field = newContext().getField(fieldId);
        return NncUtils.get(field, Field::toDTO);
    }

    @Transactional
    public void removeField(long fieldId) {
        EntityContext context = newContext();
        removeField(context.getField(fieldId), context);
        context.finish();
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
        Field field = context.getField(fieldId);
        if(field.isAsTitle()) {
            return;
        }
        field.setAsTitle(true);
        context.finish();
    }

    private EntityContext newContext() {
        return contextFactory.newContext().getEntityContext();
    }

    public Page<ConstraintDTO> listConstraints(long typeId, int page, int pageSize) {
        EntityContext context = newContext();
        Page<ConstraintRT<?>> dataPage =  entityQueryService.query(
                EntityQuery.create(
                        new TypeReference<>() {},
                        null,
                        page,
                        pageSize,
                        List.of(new EntityQueryField("typeId", typeId))
                ),
                context
        );
        return new Page<>(
                NncUtils.map(dataPage.data(), ConstraintRT::toDTO),
                dataPage.total()
        );
    }

    public ConstraintDTO getConstraint(long id) {
        EntityContext context = newContext();
        ConstraintRT<?> constraint = context.getEntity(ConstraintRT.class, id);
        if(constraint == null) {
            throw BusinessException.constraintNotFound(id);
        }
        return constraint.toDTO();
    }

    @Transactional
    public long saveConstraint(ConstraintDTO constraintDTO) {
        EntityContext context = newContext();
        Type type = context.getType(constraintDTO.typeId());
        ConstraintRT<?> constraint;
        if(constraintDTO.id() == null || constraintDTO.id() == 0L) {
            constraint = ConstraintFactory.createFromDTO(constraintDTO, type);
        }
        else {
            constraint = context.getEntity(ConstraintRT.class, constraintDTO.id());
            constraint.update(constraintDTO);
        }
        context.finish();
        return constraint.getId();
    }

    @Transactional
    public void removeConstraint(long id) {
        EntityContext context = newContext();
        ConstraintRT<?> constraint = context.getEntity(ConstraintRT.class, id);
        if(constraint == null) {
            throw BusinessException.constraintNotFound(id);
        }
        constraint.remove();
        context.finish();
    }

}
