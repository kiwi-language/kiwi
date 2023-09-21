package tech.metavm.object.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.dto.Page;
import tech.metavm.dto.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.expression.Var;
import tech.metavm.flow.*;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.job.JobManager;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.query.Path;
import tech.metavm.object.meta.rest.dto.*;
import tech.metavm.util.*;

import java.util.*;
import java.util.function.Function;

@Component
public class TypeManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(TypeManager.class);

    private final InstanceContextFactory instanceContextFactory;

    private final EntityQueryService entityQueryService;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final JobManager jobManager;

    private final TransactionOperations transactionTemplate;

    private FlowExecutionService flowExecutionService;

    private FlowManager flowManager;

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
        primitiveTypes.put("boolean", ModelDefRegistry.getType(Boolean.class).toDTO());
        primitiveTypes.put("string", ModelDefRegistry.getType(String.class).toDTO());
        primitiveTypes.put("time", ModelDefRegistry.getType(Date.class).toDTO());
        primitiveTypes.put("password", ModelDefRegistry.getType(Password.class).toDTO());
        primitiveTypes.put("null", ModelDefRegistry.getType(Null.class).toDTO());
        primitiveTypes.put("void", ModelDefRegistry.getType(Void.class).toDTO());
        primitiveTypes.put("object", ModelDefRegistry.getType(Object.class).toDTO());
        primitiveTypes.put("array", ModelDefRegistry.getType(Table.class).toDTO());
        return primitiveTypes;
    }

    public Page<TypeDTO> query(String searchText, List<Integer> categoryCodes, boolean includeBuiltin,
                               int page, int pageSize) {
        IEntityContext context = newContext();
        return query(searchText, categoryCodes, includeBuiltin, page, pageSize, context);
    }

    public Page<TypeDTO> query(String searchText,
                               List<Integer> categoryCodes,
                               boolean includeBuiltin,
                               int page,
                               int pageSize,
                               IEntityContext context) {
        List<TypeCategory> categories = categoryCodes != null ?
                NncUtils.map(categoryCodes, TypeCategory::getByCode)
                : List.of(TypeCategory.CLASS, TypeCategory.VALUE);
        Page<Type> typePage = entityQueryService.query(
                new EntityQuery<>(
                        Type.class,
                        searchText,
                        List.of("code"),
                        includeBuiltin,
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
        IEntityContext context = newContext();
        Type type = context.getType(id);
        if (type instanceof ClassType classType) {
            return NncUtils.get(classType, t -> t.toDTO(includingFields, includingFieldTypes));
        } else {
            return type.toDTO();
        }
    }

    public List<TypeDTO> batchGetTypes(List<Long> ids, boolean includingFields, boolean includingFieldTypes) {
        IEntityContext context = newContext();
        List<Type> types = NncUtils.map(ids, context::getType);
        return NncUtils.map(types, t -> {
            if (t instanceof ClassType classType) {
                return classType.toDTO(includingFields, includingFieldTypes);
            } else {
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
        if (compositeType.getId() != null) {
            return compositeType.toDTO();
        } else {
            return createCompositeType(id, mapper);
        }
    }

    private TypeDTO createCompositeType(long id, Function<Type, ? extends Type> mapper) {
        return transactionTemplate.execute(status -> {
            IEntityContext context = newContext();
            Type compositeType = mapper.apply(context.getType(id));
            if (!context.containsModel(compositeType)) {
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
        if (typeDTO.id() == null) {
            return createType(typeDTO, context);
        } else {
            return updateType(typeDTO, context.getClassType(typeDTO.id()), context);
        }
    }

    public ClassType saveTypeWithContent(TypeDTO typeDTO, IEntityContext context) {
        ClassType type = context.getEntity(ClassType.class, typeDTO.getRef());
        return saveTypeWithContent(typeDTO, type, context);
    }

    public ClassType saveTypeWithContent(TypeDTO typeDTO, ClassType type, IEntityContext context) {
        boolean isCreate;
        if (type == null) {
            isCreate = true;
            type = createType(typeDTO, context);
        } else {
            isCreate = false;
            updateType(typeDTO, type, context);
        }
        if(typeDTO.param() instanceof ClassParamDTO param) {
            List<FlowDTO> flows = param.flows();
            if (flows != null) {
                saveFlows(type, flows, context);
            }
        }
        if (isCreate) {
            initCLass(type, context);
        }
        return type;
    }

    private void initCLass(ClassType classType, IEntityContext context) {
        var classInit = classType.getFlowByCode("<cinit>");
        if (classInit != null) {
            flowExecutionService.executeInternal(
                    classInit, null,
                    new ClassInstance(Map.of(), classInit.getInputType()),
                    context.getInstanceContext()
            );
        }
    }

    private void saveFlows(ClassType type, List<FlowDTO> flows, IEntityContext context) {
        Set<Long> flowIds = NncUtils.mapNonNullUnique(flows, FlowDTO::id);
        for (Flow flow : type.getFlows()) {
            if (!flowIds.contains(flow.getId())) {
                flowManager.delete(flow, context);
            }
        }
        for (FlowDTO flowDTO : flows) {
            if (flowDTO.id() != null) flowManager.update(flowDTO, context);
            else flowManager.create(flowDTO, type, context);
        }
    }

    @Transactional
    public List<Long> batchSave(List<TypeDTO> typeDTOs) {
        FlowSavingContext.skipPreprocessing(true);
        IEntityContext context = newContext();
        Map<TypeDTO, ClassType> classTypes = new LinkedHashMap<>();
        List<ClassType> newTypes = new ArrayList<>();
        List<TypeDTO> classTypeDTOs = NncUtils.filter(typeDTOs,
                typeDTO -> typeDTO.category() == TypeCategory.CLASS.code()
                        || typeDTO.category() == TypeCategory.ENUM.code()
                        || typeDTO.category() == TypeCategory.INTERFACE.code());
        classTypeDTOs.sort(
                (t1,t2) -> {
                    var param1 = (ClassParamDTO) t1.param();
                    var param2 = (ClassParamDTO) t2.param();
                    if(param1.typeArgumentRefs().isEmpty() != param2.typeArgumentRefs().isEmpty()) {
                        return param1.typeArgumentRefs().isEmpty() ? -1 : 1;
                    }
                    if(t1.category() == TypeCategory.INTERFACE.code()
                            || t2.category() == TypeCategory.INTERFACE.code()) {
                        return t1.category() == TypeCategory.INTERFACE.code() ? -1 : 1;
                    }
                    else {
                        return 0;
                    }
                }
        );
        for (TypeDTO typeDTO : classTypeDTOs) {
            if (typeDTO.id() == null) {
                ClassType classType = TypeUtil.createAndBind(typeDTO, false, context);
                context.bind(classType);
                classTypes.put(typeDTO, classType);
                newTypes.add(classType);
            } else {
                classTypes.put(typeDTO, context.getClassType(typeDTO.id()));
            }
        }
        List<TypeDTO> arrayTypeDTOs = NncUtils.filter(typeDTOs,
                typeDTO -> typeDTO.category() == TypeCategory.ARRAY.code());
        for (TypeDTO typeDTO : arrayTypeDTOs) {
            if(typeDTO.id() == null) {
                var param = (ArrayTypeParamDTO) typeDTO.param();
                var elementType = context.getType(param.elementTypeRef());
                var arrayType = TypeUtil.createArrayType(elementType, typeDTO);
                elementType.setArrayType(arrayType);
                context.bind(arrayType);
            }
        }
        List<TypeDTO> nullableTypeDTOs = NncUtils.filter(typeDTOs,
                typeDTO -> typeDTO.category() == TypeCategory.UNION.code());
        for (TypeDTO typeDTO : nullableTypeDTOs) {
            if(typeDTO.id() == null) {
                var param = (UnionTypeParamDTO) typeDTO.param();
                var underlyingTypeTypeDTO = NncUtils.findRequired(param.typeMembers(),
                        mem -> !Objects.equals(mem.id(), StandardTypes.getNullType().getId()));
                var underlyingType = context.getType(underlyingTypeTypeDTO.getRef());
                var nullableType = TypeUtil.createNullableType(underlyingType, typeDTO);
                context.bind(nullableType);
            }
        }
//        List<TypeDTO> mapTypeDTOs = NncUtils.filter(typeDTOs,
//                typeDTO -> typeDTO.category() == TypeCategory.MAP.code()
//        );
//        for (TypeDTO typeDTO : mapTypeDTOs) {
//            if(typeDTO.id() == null) {
//                var param = (ClassParamDTO) typeDTO.param();
//                var keyArrayField = NncUtils.findRequired(
//                        param.fields(),
//                        fieldDTO -> Objects.equals(fieldDTO.code(), "keyArray")
//                );
//                var valueArrayField = NncUtils.findRequired(
//                        param.fields(),
//                        fieldDTO -> Objects.equals(fieldDTO.code(), "valueArray")
//                );
//                var keyType = ((ArrayType) context.getType(keyArrayField.typeRef())).getElementType();
//                var valueType = ((ArrayType) context.getType(valueArrayField.typeRef())).getElementType();
//                context.bind(TypeUtil.getMapType(keyType, valueType, typeDTO));
//            }
//        }
        Map<FlowDTO, Flow> flowMap = new HashMap<>();
        for (TypeDTO typeDTO : classTypeDTOs) {
            ClassParamDTO param = (ClassParamDTO) typeDTO.param();
            ClassType klass = classTypes.get(typeDTO);
            saveFields(param.fields(), klass, context);
            saveFields(param.staticFields(), klass, context);
            Set<Long> flowIds = new HashSet<>();
            for (FlowDTO flowDTO : param.flows()) {
                if (flowDTO.id() != null) flowIds.add(flowDTO.id());
                flowMap.put(flowDTO, flowManager.saveDeclaration(flowDTO, klass, context));
            }
            List<Flow> flowsToRemove = NncUtils.filter(klass.getFlows(),
                    flow -> flow.getId() != null && !flowIds.contains(flow.getId()));
            flowsToRemove.forEach(klass::removeFlow);
        }
        for (TypeDTO typeDTO : classTypeDTOs) {
            ClassParamDTO param = (ClassParamDTO) typeDTO.param();
            ClassType klass = classTypes.get(typeDTO);
            for (FlowDTO flowDTO : param.flows()) {
                flowManager.saveContent(flowDTO, flowMap.get(flowDTO), klass, context);
            }
        }
        for (ClassType newType : newTypes) {
            context.initIds();
            initCLass(newType, context);
        }
        context.finish();
        return NncUtils.map(classTypes.values(), Entity::getIdRequired);
    }

    private List<TypeDTO> extractTypeDTOsToSave(List<?> objects) {
        List<TypeDTO> result = new ArrayList<>();
        Set<Object> visited = new IdentitySet<>();
        extractTypeDTOsToSave0(objects, visited, result);
        return result;
    }

    private void extractTypeDTOsToSave0(List<?> objects, Set<Object> visited, List<TypeDTO> result) {
        List<Object> cascade = new ArrayList<>();
        for (Object object : objects) {
            if(visited.contains(object)) {
                continue;
            }
            visited.add(object);
            if(object instanceof TypeDTO typeDTO) {
                if(typeDTO.id()  == null || typeDTO.id() > IdConstants.SYSTEM_RESERVE_PER_REGION) {
                    result.add(typeDTO);
                    cascade.add(typeDTO);
                }
            }
            else {
                cascade.addAll(getReferences(object));
            }
        }
        if(!cascade.isEmpty()) {
            extractTypeDTOsToSave0(cascade, visited, result);
        }
    }

    private Collection<?> getReferences(Object object) {
        if(object instanceof Collection<?> coll) {
            return coll;
        }
        else if(object instanceof Map<?,?> map) {
            return map.values();
        }
        else {
            EntityDesc desc = DescStore.get(object.getClass());
            List<Object> references = new ArrayList<>();
            for (EntityProp prop : desc.getProps()) {
                var ref = prop.get(object);
                if (ref != null) {
                    references.add(ref);
                }
            }
            return references;
        }
    }

    @Transactional
    public void batchRemove(List<Long> typeIds) {
        IEntityContext context = newContext();
        List<Type> types = NncUtils.map(typeIds, context::getType);
        context.batchRemove(types);
        context.finish();
    }

    public ClassType createType(TypeDTO classDTO, IEntityContext context) {
        return createType(classDTO, true, context);
    }

    public ClassType createType(TypeDTO classDTO, boolean withContent, IEntityContext context) {
        NncUtils.requireNonNull(classDTO.name(), "类型名称不能为空");
        ensureTypeNameAvailable(classDTO, context);
        return TypeUtil.createAndBind(classDTO, withContent, context);
    }

    public ClassType updateType(TypeDTO typeDTO, ClassType type, IEntityContext context) {
        NncUtils.requireNonNull(typeDTO.name(), "类型名称不能为空");
        if (!type.getName().equals(typeDTO.name())) {
            ensureTypeNameAvailable(typeDTO, context);
        }
        type.update(typeDTO);
        ClassParamDTO param = (ClassParamDTO) typeDTO.param();
        if (param.fields() != null) {
            Set<RefDTO> fieldRefs = NncUtils.mapAndFilterUnique(param.fields(), FieldDTO::getRef, RefDTO::isNotEmpty);
            for (Field field : type.getFields()) {
                if (!fieldRefs.contains(field.getRef())) {
                    removeField(field, context);
                }
            }
            for (FieldDTO fieldDTO : param.fields()) {
                saveField(fieldDTO, type, context);
            }
        }
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
        if (type == null) {
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
        return field.getIdRequired();
    }

    public void saveFields(List<FieldDTO> fieldDTOs, ClassType declaringClass, IEntityContext context) {
        Set<Long> fieldIds = new HashSet<>();
        for (FieldDTO fieldDTO : fieldDTOs) {
            var field = context.getField(fieldDTO.getRef());
            if (field != null) {
                fieldIds.add(fieldDTO.id());
                updateField(fieldDTO, field, context);
            } else {
                createField(fieldDTO, declaringClass, context);
            }
        }
        List<Field> toRemove = NncUtils.filter(
                declaringClass.getFields(), f -> f.getId() != null && !fieldIds.contains(f.getId()));
        toRemove.forEach(declaringClass::removeField);
    }

    public Field saveField(FieldDTO fieldDTO, IEntityContext context) {
        return saveField(fieldDTO, context.getClassType(fieldDTO.declaringTypeId()), context);
    }

    private Field saveField(FieldDTO fieldDTO, ClassType declaringType, IEntityContext context) {
        Field field = context.getField(new RefDTO(fieldDTO.id(), fieldDTO.tmpId()));
        if (field == null) {
            return createField(fieldDTO, declaringType, context);
        } else {
            return updateField(fieldDTO, field, context);
        }
    }

    private Field createField(FieldDTO fieldDTO, ClassType declaringType, IEntityContext context) {
        return TypeUtil.createFieldAndBind(
                declaringType,
                fieldDTO,
                context
        );
    }

    private Field updateField(FieldDTO fieldDTO, Field field, IEntityContext context) {
        field.update(fieldDTO);
        if (fieldDTO.defaultValue() != null) {
            field.setDefaultValue(InstanceFactory.resolveValue(fieldDTO.defaultValue(), field.getType(), context));
        } else {
            field.setDefaultValue(InstanceUtils.nullInstance());
        }
        return field;
    }

    public FieldDTO getField(long fieldId) {
        IEntityContext context = newContext();
        Field field = context.getField(fieldId);
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
        if (field.isAsTitle()) {
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
        Page<Constraint<?>> dataPage = entityQueryService.query(
                EntityQuery.create(
                        new TypeReference<>() {
                        },
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
        if (constraint == null) {
            throw BusinessException.constraintNotFound(id);
        }
        return constraint.toDTO();
    }

    @Transactional
    public long saveConstraint(ConstraintDTO constraintDTO) {
        IEntityContext context = newContext();
        Constraint<?> constraint;
        if (constraintDTO.id() == null || constraintDTO.id() == 0L) {
            constraint = ConstraintFactory.createFromDTO(constraintDTO, context);
        } else {
            constraint = context.getEntity(Constraint.class, constraintDTO.id());
            ConstraintFactory.update(constraintDTO, context);
        }
        context.finish();
        return constraint.getIdRequired();
    }

    @Transactional
    public void removeConstraint(long id) {
        IEntityContext context = newContext();
        Constraint<?> constraint = context.getEntity(Constraint.class, id);
        if (constraint == null) {
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
            if (firstItem.startsWith(Constants.CONSTANT_ID_PREFIX)) {
                type = context.getType(ExpressionUtil.parseIdFromConstantVar(firstItem));
            } else {
                type = context.selectByUniqueKey(Type.UNIQUE_NAME, firstItem);
            }
            path2type.put(firstItem, type);
            maxLevels = Math.max(maxLevels, path.length());
        }

        for (int i = 1; i < maxLevels; i++) {
            for (Path path : pathList) {
                if (path.length() <= i) {
                    continue;
                }
                Var var = Var.parse(path.getItem(i));
                Path subPath = path.subPath(0, i + 1);
                Path parentPath = path.subPath(0, i);
                Type parent = NncUtils.requireNonNull(path2type.get(parentPath.toString()));
                if (parent.isUnionNullable()) {
                    parent = parent.getUnderlyingType();
                }
                if (parent instanceof ClassType classType) {
                    Field field = NncUtils.requireNonNull(
                            classType.getFieldByVar(var),
                            () -> BusinessException.invalidTypePath(path.toString())
                    );
                    path2type.put(subPath.toString(), field.getType());
                } else if ((parent instanceof ArrayType arrayType) && var.isName() &&
                        (var.getName().equals("*") || var.getName().equals("length"))) {
                    if (var.getName().equals("*")) {
                        path2type.put(subPath.toString(), arrayType.getElementType());
                    } else {
                        path2type.put(subPath.toString(), ModelDefRegistry.getType(int.class));
                    }
                } else {
                    throw BusinessException.invalidTypePath(path.toString());
                }
            }
        }
        Map<String, Long> path2typeId = new HashMap<>();
        List<TypeDTO> typeDTOs = new ArrayList<>();
        Set<Long> visitedTypeIds = new HashSet<>();
        Set<String> pathSet = new HashSet<>(paths);

        path2type.forEach((path, type) -> {
            if (pathSet.contains(path)) {
                path2typeId.put(path, type.getId());
                if (!visitedTypeIds.contains(type.getId())) {
                    visitedTypeIds.add(type.getId());
                    typeDTOs.add(type.toDTO());
                }
            }
        });
        return new LoadByPathsResponse(path2typeId, typeDTOs);
    }

    @Autowired
    public void setFlowManager(FlowManager flowManager) {
        this.flowManager = flowManager;
    }

    @Autowired
    public void setFlowExecutionService(FlowExecutionService flowExecutionService) {
        this.flowExecutionService = flowExecutionService;
    }

    @Transactional
    public void initCompositeTypes(long id) {
        IEntityContext context = newContext();
        initCompositeTypes(context.getType(id), context);
        context.finish();
    }

    private void initCompositeTypes(Type type, IEntityContext context) {
        if (type.getNullableType() == null) {
            UnionType nullableType = new UnionType(Set.of(type, context.getType(Null.class)));
            TypeUtil.getArrayType(nullableType);
            type.setNullableType(nullableType);
            context.bind(nullableType);
        }
        ArrayType arrayType = TypeUtil.getArrayType(type);
        if (!context.containsModel(arrayType)) {
            context.bind(arrayType);
        }
        if (arrayType.getNullableType() == null) {
            arrayType.setNullableType(new UnionType(Set.of(arrayType, context.getType(Null.class))));
            context.bind(arrayType.getNullableType());
        }
    }

}
