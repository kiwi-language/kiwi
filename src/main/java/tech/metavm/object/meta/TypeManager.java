package tech.metavm.object.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.dto.Page;
import tech.metavm.entity.*;
import tech.metavm.job.JobManager;
import tech.metavm.object.meta.rest.dto.ClassParamDTO;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.*;

import java.util.*;
import java.util.function.Function;

@Component
public class TypeManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(TypeManager.class);

    private final InstanceContextFactory instanceContextFactory;

    private final EntityQueryService entityQueryService;

    private final JobManager jobManager;

    private final TransactionOperations transactionTemplate;

    public TypeManager(InstanceContextFactory instanceContextFactory,
                       EntityQueryService entityQueryService,
                       JobManager jobManager,
                       TransactionOperations transactionTemplate) {
        this.instanceContextFactory = instanceContextFactory;
        this.entityQueryService = entityQueryService;
        this.jobManager = jobManager;
        this.transactionTemplate = transactionTemplate;
    }

    public Map<String, TypeDTO> getPrimitiveTypes() {
        Map<String, TypeDTO> primitiveTypes = new HashMap<>();
        primitiveTypes.put("int", ModelDefRegistry.getType(Integer.class).toDTO());
        primitiveTypes.put("long", ModelDefRegistry.getType(Long.class).toDTO());
        primitiveTypes.put("number", ModelDefRegistry.getType(Double.class).toDTO());
        primitiveTypes.put("string", ModelDefRegistry.getType(String.class).toDTO());
        primitiveTypes.put("time", ModelDefRegistry.getType(Date.class).toDTO());
        primitiveTypes.put("password", ModelDefRegistry.getType(Password.class).toDTO());
        primitiveTypes.put("null", ModelDefRegistry.getType(Null.class).toDTO());
        return primitiveTypes;
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
        Page<Type> typePage = entityQueryService.query(
                new EntityQuery<>(
                        Type.class,
                        searchText,
                        List.of("code"),
                        page,
                        pageSize,
                        List.of(
                                new EntityQueryField("category", categories),
                                new EntityQueryField("anonymous", false)
                        )
                ),
                context
        );

        return new Page<>(
                NncUtils.map(typePage.data(), Type::toDTO),
                typePage.total()
        );
    }

    public TypeDTO getType(long id, boolean includingFields, boolean includingFieldTypes) {
        Type type = newContext().getType(id);
        if(type instanceof ClassType classType) {
            return NncUtils.get(classType, t -> t.toDTO(includingFields, includingFieldTypes));
        }
        else {
            return type.toDTO();
        }
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
    public void remove(long id) {
        IEntityContext context = newContext();
        ClassType type = context.getClassType(id);
        if(type == null) {
            return;
        }
        context.remove(type);
        context.finish();
    }

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
        if(fieldDTO.defaultValue() != null) {
            field.setDefaultValue(InstanceFactory.resolveValue(fieldDTO.defaultValue(), field.getType(), context));
        }
        else {
            field.setDefaultValue(InstanceUtils.nullInstance());
        }
        return field;
    }

    public FieldDTO getField(long fieldId) {
        Field field = newContext().getField(fieldId);
        return NncUtils.get(field, Field::toDTO);
    }

    @Transactional
    public void removeField(long fieldId) {
        IEntityContext context = newContext();
        Field field = context.getField(fieldId);
        context.remove(field);
        context.finish();
    }

    private void removeField(Field field, IEntityContext context) {
        context.remove(field);
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
        ClassType type = context.getClassType(typeId);
        Page<Constraint<?>> dataPage =  entityQueryService.query(
                EntityQuery.create(
                        new TypeReference<>() {},
                        null,
                        page,
                        pageSize,
                        List.of(new EntityQueryField("declaringType", type))
                ),
                context
        );
        return new Page<>(
                NncUtils.map(dataPage.data(), Constraint::toDTO),
                dataPage.total()
        );
    }

    public ConstraintDTO getConstraint(long id) {
        IEntityContext context = newContext();
        Constraint<?> constraint = context.getEntity(Constraint.class, id);
        if(constraint == null) {
            throw BusinessException.constraintNotFound(id);
        }
        return constraint.toDTO();
    }

    @Transactional
    public long saveConstraint(ConstraintDTO constraintDTO) {
        IEntityContext context = newContext();
        Constraint<?> constraint;
        if(constraintDTO.id() == null || constraintDTO.id() == 0L) {
            constraint = ConstraintFactory.createFromDTO(constraintDTO, context);
        }
        else {
            constraint = context.getEntity(Constraint.class, constraintDTO.id());
            constraint.update(constraintDTO);
        }
        context.finish();
        return constraint.getId();
    }

    @Transactional
    public void removeConstraint(long id) {
        IEntityContext context = newContext();
        Constraint<?> constraint = context.getEntity(Constraint.class, id);
        if(constraint == null) {
            throw BusinessException.constraintNotFound(id);
        }
        context.remove(constraint);
        context.finish();
    }

}
