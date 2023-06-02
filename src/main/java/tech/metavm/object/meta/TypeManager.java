package tech.metavm.object.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.dto.Page;
import tech.metavm.entity.*;
import tech.metavm.job.JobManager;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.object.instance.query.Path;
import tech.metavm.expression.Var;
import tech.metavm.object.meta.rest.dto.*;
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

    public List<TypeDTO> batchGetTypes(List<Long> ids, boolean includingFields, boolean includingFieldTypes) {
        IEntityContext context = newContext();
        List<Type> types = NncUtils.map(ids, context::getType);
        return NncUtils.map(types, t -> {
            if(t instanceof ClassType classType) {
                return classType.toDTO(includingFields, includingFieldTypes);
            }
            else {
                return t.toDTO();
            }
        });
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
        ClassType type = TypeUtil.createAndBind(classDTO, context);
        initCompositeTypes(type, context);
        return type;
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
            ConstraintFactory.update(constraintDTO, context);
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

    public LoadByPathsResponse loadByPaths(List<String> paths) {
        IEntityContext context = newContext();
        Map<String, Type> path2type = new HashMap<>();

        List<Path> pathList = new ArrayList<>();
        for (String path : paths) {
            pathList.add(Path.create(path));
        }
        int maxLevels = 1;
        for (Path path : pathList) {
            String firstItem = path.firstItem();
            Type type;
            if(firstItem.startsWith(Constants.CONSTANT_ID_PREFIX)) {
                type = context.getType(ExpressionUtil.parseIdFromConstantVar(firstItem));
            }
            else {
                type = context.selectByUniqueKey(Type.UNIQUE_NAME, firstItem);
            }
            path2type.put(firstItem, type);
            maxLevels = Math.max(maxLevels, path.length());
        }

        for (int i = 1; i < maxLevels; i++) {
            for (Path path : pathList) {
                if(path.length() <= i) {
                    continue;
                }
                Var var = Var.parse(path.getItem(i));
                Path subPath = path.subPath(0, i + 1);
                Path parentPath = path.subPath(0, i);
                Type parent = NncUtils.requireNonNull(path2type.get(parentPath.toString()));
                if(parent.isUnionNullable()) {
                    parent = parent.getUnderlyingType();
                }
                if(parent instanceof ClassType classType) {
                    Field field = NncUtils.requireNonNull(
                            classType.getFieldByVar(var),
                            () -> BusinessException.invalidTypePath(path.toString())
                    );
                    path2type.put(subPath.toString(), field.getType());
                }
                else if((parent instanceof ArrayType arrayType) && var.isName() && var.getName().equals("*")) {
                    path2type.put(subPath.toString(), arrayType.getElementType());
                }
                else {
                    throw BusinessException.invalidTypePath(path.toString());
                }
            }
        }
        Map<String, Long> path2typeId = new HashMap<>();
        List<TypeDTO> typeDTOs = new ArrayList<>();
        Set<Long> visitedTypeIds = new HashSet<>();
        Set<String> pathSet = new HashSet<>(paths);

        path2type.forEach((path, type) -> {
            if(pathSet.contains(path)) {
                path2typeId.put(path, type.getId());
                if (!visitedTypeIds.contains(type.getId())) {
                    visitedTypeIds.add(type.getId());
                    typeDTOs.add(type.toDTO());
                }
            }
        });
        return new LoadByPathsResponse(path2typeId, typeDTOs);
    }

    @Transactional
    public void initCompositeTypes(long id) {
        IEntityContext context = newContext();
        initCompositeTypes(context.getType(id), context);
        context.finish();
    }

    private void initCompositeTypes(Type type, IEntityContext context) {
        if(type.getNullableType() == null) {
            UnionType nullableType = new UnionType(Set.of(type, context.getType(Null.class)));
            nullableType.getArrayType();
            type.setNullableType(nullableType);
        }
        ArrayType arrayType = type.getArrayType();
        if(arrayType.getNullableType() == null) {
            arrayType.setNullableType(new UnionType(Set.of(arrayType, context.getType(Null.class))));
        }
    }

}
