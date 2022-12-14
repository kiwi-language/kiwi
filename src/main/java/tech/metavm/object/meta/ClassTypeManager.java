package tech.metavm.object.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tech.metavm.dto.Page;
import tech.metavm.entity.*;
import tech.metavm.object.meta.persistence.FieldPO;
import tech.metavm.object.meta.rest.dto.ClassParamDTO;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Component
public class ClassTypeManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(ClassTypeManager.class);

    private final InstanceContextFactory instanceContextFactory;

    private final EntityQueryService entityQueryService;

    private final TransactionTemplate transactionTemplate;

    public ClassTypeManager(InstanceContextFactory instanceContextFactory, EntityQueryService entityQueryService, TransactionTemplate transactionTemplate) {
        this.instanceContextFactory = instanceContextFactory;
        this.entityQueryService = entityQueryService;
        this.transactionTemplate = transactionTemplate;
    }

    public Page<TypeDTO> query(String searchText, List<Integer> categoryCodes, int page, int pageSize) {
        IEntityContext context = newContext();
        return query(searchText, categoryCodes, page, pageSize, context);
    }

    public Page<TypeDTO> query(String searchText,
                               List<Integer> categoryCodes,
                               int page,
                               int pageSize,
                               IEntityContext context) {
        List<TypeCategory> categories = categoryCodes != null ?
                NncUtils.map(categoryCodes, TypeCategory::getByCodeRequired)
                : List.of(TypeCategory.CLASS, TypeCategory.VALUE);
        Page<ClassType> typePage = entityQueryService.query(
                new EntityQuery<>(
                        ClassType.class,
                        searchText,
                        List.of("code"),
                        page,
                        pageSize,
                        List.of(
                                new EntityQueryField("category", categories)
                        )
                ),
                context
        );

        return new Page<>(
                NncUtils.map(typePage.data(), t -> t.toDTO(false, false)),
                typePage.total()
        );
    }

    public TypeDTO getType(long id, boolean includingFields, boolean includingFieldTypes) {
        ClassType classType = newContext().getClassType(id);
        return NncUtils.get(classType, t -> t.toDTO(includingFields, includingFieldTypes));
    }

    public TypeDTO getArrayType(long id) {
        return getOrCreateCompositeType(id, TypeUtil::getArrayType);
    }

    public TypeDTO getNullableType(long id) {
        return getOrCreateCompositeType(id, TypeUtil::getNullableType);
    }

    public TypeDTO getNullableArrayType(long id) {
        return getOrCreateCompositeType(id, TypeUtil::getArrayNullableType);
    }

    private TypeDTO getOrCreateCompositeType(long id, Function<Type, ? extends Type> mapper) {
        IEntityContext context = newContext();
        Type type = context.getType(id);
        Type compositeType = mapper.apply(type);
        if(compositeType.getId() != null) {
            return compositeType.toDTO();
        }
        else {
            return createCompositeType(id, mapper);
        }
    }

    private TypeDTO createCompositeType(long id, Function<Type, ? extends Type> mapper) {
        return transactionTemplate.execute(status -> {
            IEntityContext context = newContext();
            Type compositeType = mapper.apply(context.getType(id));
            if(!context.containsModel(compositeType)) {
                context.bind(compositeType);
            }
            context.finish();
            return compositeType.toDTO();
        });
    }

    @Transactional
    public TypeDTO saveType(TypeDTO typeDTO) {
        IEntityContext context = newContext();
        ClassType type = saveTypeWithContent(typeDTO, context);
        context.finish();
        return type.toDTO(true, false);
    }

    public ClassType saveType(TypeDTO typeDTO, IEntityContext context) {
        if(typeDTO.id() == null) {
            return createType(typeDTO, context);
        }
        else {
            return updateType(typeDTO, context);
        }
    }

    public ClassType saveTypeWithContent(TypeDTO typeDTO, IEntityContext context) {
        ClassType type;
        ClassParamDTO param = (ClassParamDTO) typeDTO.param();
        if (typeDTO.id() == null || typeDTO.id() == 0L) {
            type = createType(typeDTO, context);
        } else {
            type = updateType(typeDTO, context);
        }
        List<Field> fieldsToRemove = new ArrayList<>();
        Set<Long> fieldIds = NncUtils.mapNonNullUnique(param.fields(), FieldDTO::id);
        for (Field field : type.getFields()) {
            if (!fieldIds.contains(field.getId())) {
                fieldsToRemove.add(field);
            }
        }
        fieldsToRemove.forEach(f -> removeField(f, context));
        for (FieldDTO fieldDTO : param.fields()) {
            saveField(fieldDTO, type, context);
        }
        return type;
    }

    public ClassType createType(TypeDTO classDTO, IEntityContext context) {
        NncUtils.requireNonNull(classDTO.name(), "名称");
        ensureTypeNameAvailable(classDTO, context);
        return TypeUtil.createAndBind(classDTO, context);
    }

    public ClassType updateType(TypeDTO typeDTO, IEntityContext context) {
        NncUtils.requireNonNull(typeDTO.name(), "名称");
        NncUtils.requireNonNull(typeDTO.id(), "ID");
        ClassType type = context.getClassType(typeDTO.id());
        if(!type.getName().equals(typeDTO.name())) {
            ensureTypeNameAvailable(typeDTO, context);
        }
        type.update(typeDTO);
        return type;
    }

    private void ensureTypeNameAvailable(TypeDTO typeDTO, IEntityContext context) {
        Type typeWithSameName = context.selectByUniqueKey(Type.UNIQUE_NAME, typeDTO.name());
        if (typeWithSameName != null && !typeWithSameName.isAnonymous()) {
            throw BusinessException.invalidType(typeDTO, "对象名称已存在");
        }
    }

    @Transactional
    public void deleteType(long id) {
        IEntityContext context = newContext();
        ClassType type = context.getClassType(id);
        if(type == null) {
            return;
        }
        deleteType0(type, new HashSet<>(), context);
        context.finish();
    }

    private void deleteType0(ClassType type, Set<Long> visited, IEntityContext context) {
        if(visited.contains(type.getId())) {
            throw new InternalException("Circular reference. category: " + type + " is already visited");
        }
        visited.add(type.getId());
//        List<Type> dependentTypes = getDependentTypes(type, context);
//        if(NncUtils.isNotEmpty(dependentTypes)) {
//            dependentTypes.forEach(t -> deleteType0(t, visited, context));
//        }
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

//    public List<Type> getDependentTypes(Type type, IEntityContext context) {
//        return NncUtils.merge(
//                context.selectByKey(TypePO.INDEX_RAW_TYPE_ID, type.getId()),
//                context.selectByKey(TypePO.INDEX_TYPE_ARG_ID, type.getId())
//        );
//    }

    @Transactional
    public long saveField(FieldDTO fieldDTO) {
        IEntityContext context = newContext();
        Field field = saveField(fieldDTO, context);
        context.finish();
        return field.getId();
    }

    public Field saveField(FieldDTO fieldDTO, IEntityContext context) {
        return saveField(fieldDTO, context.getClassType(fieldDTO.declaringTypeId()), context);
    }

    private Field saveField(FieldDTO fieldDTO, ClassType declaringType, IEntityContext context) {
        if(fieldDTO.id() == null || fieldDTO.id() == 0L) {
            return createField(fieldDTO, declaringType, context);
        }
        else {
            return updateField(fieldDTO, context);
        }
    }

    private Field createField(FieldDTO fieldDTO, ClassType declaringType, IEntityContext context) {
        return TypeUtil.createFieldAndBind(
                declaringType,
                fieldDTO,
                context
        );
    }

    private Field updateField(FieldDTO fieldDTO, IEntityContext context) {
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
        IEntityContext context = newContext();
        removeField(context.getField(fieldId), context);
        context.finish();
    }

    private void removeField(Field field, IEntityContext context) {
        Type type = field.getType();
        if(type.isAnonymous()) {
            context.remove(type);
        }
        field.remove();
    }

    @Transactional
    public void setFieldAsTitle(long fieldId) {
        IEntityContext context = newContext();
        Field field = context.getField(fieldId);
        if(field.isAsTitle()) {
            return;
        }
        field.setAsTitle(true);
        context.finish();
    }

    private IEntityContext newContext() {
        return instanceContextFactory.newContext().getEntityContext();
    }

    public Page<ConstraintDTO> listConstraints(long typeId, int page, int pageSize) {
        IEntityContext context = newContext();
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
        IEntityContext context = newContext();
        ConstraintRT<?> constraint = context.getEntity(ConstraintRT.class, id);
        if(constraint == null) {
            throw BusinessException.constraintNotFound(id);
        }
        return constraint.toDTO();
    }

    @Transactional
    public long saveConstraint(ConstraintDTO constraintDTO) {
        IEntityContext context = newContext();
        ClassType type = context.getClassType(constraintDTO.typeId());
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
        IEntityContext context = newContext();
        ConstraintRT<?> constraint = context.getEntity(ConstraintRT.class, id);
        if(constraint == null) {
            throw BusinessException.constraintNotFound(id);
        }
        constraint.remove();
        context.finish();
    }

}
