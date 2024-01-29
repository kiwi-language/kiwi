package tech.metavm.object.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.metavm.common.Page;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.expression.Expressions;
import tech.metavm.expression.Var;
import tech.metavm.flow.*;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.query.Path;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.object.version.VersionManager;
import tech.metavm.object.version.Versions;
import tech.metavm.task.AddFieldJobGroup;
import tech.metavm.task.TaskManager;
import tech.metavm.util.BusinessException;
import tech.metavm.util.Constants;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;
import tech.metavm.view.ViewManager;

import java.util.*;
import java.util.function.BiFunction;

@Component
public class TypeManager extends EntityContextFactoryBean {

    public static final Logger LOGGER = LoggerFactory.getLogger(TypeManager.class);

    private final EntityQueryService entityQueryService;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final TaskManager jobManager;

    private final TransactionOperations transactionTemplate;

    private FlowExecutionService flowExecutionService;

    private FlowManager flowManager;

    private InstanceManager instanceManager;

    private VersionManager versionManager;

    public TypeManager(EntityContextFactory entityContextFactory,
                       EntityQueryService entityQueryService,
                       TaskManager jobManager,
                       TransactionOperations transactionTemplate) {
        super(entityContextFactory);
        this.entityQueryService = entityQueryService;
        this.jobManager = jobManager;
        this.transactionTemplate = transactionTemplate;
    }

    public Map<Integer, Long> getPrimitiveMap() {
        Map<Integer, Long> primitiveTypes = new HashMap<>();
        primitiveTypes.put(PrimitiveKind.LONG.code(), StandardTypes.getLongType().tryGetId());
        primitiveTypes.put(PrimitiveKind.DOUBLE.code(), StandardTypes.getDoubleType().tryGetId());
        primitiveTypes.put(PrimitiveKind.STRING.code(), StandardTypes.getStringType().tryGetId());
        primitiveTypes.put(PrimitiveKind.BOOLEAN.code(), StandardTypes.getBooleanType().tryGetId());
        primitiveTypes.put(PrimitiveKind.TIME.code(), StandardTypes.getTimeType().tryGetId());
        primitiveTypes.put(PrimitiveKind.PASSWORD.code(), StandardTypes.getPasswordType().tryGetId());
        primitiveTypes.put(PrimitiveKind.NULL.code(), StandardTypes.getNullType().tryGetId());
        primitiveTypes.put(PrimitiveKind.VOID.code(), StandardTypes.getVoidType().tryGetId());
        return primitiveTypes;
    }

    public TypeTreeResponse queryTypeTrees(TypeTreeQuery query) {
        try (var context = newContext()) {
            List<Type> types;
            List<Long> removedTypeIds;
            long version;
            if (query.version() == -1L) {
                types = getAllTypes(context);
                removedTypeIds = List.of();
                version = Versions.getLatestVersion(context);
            } else {
                var patch = versionManager.pullInternal(query.version(), context);
                types = NncUtils.map(patch.changedTypeIds(), context::getType);
                removedTypeIds = patch.removedTypeIds();
                version = patch.version();
            }
            return new TypeTreeResponse(
                    version,
                    NncUtils.filterAndMap(types,
                            t -> context.getInstance(t).isRoot(),
                            t -> getTypeTree(t, context)),
                    removedTypeIds
            );
        }
    }

    private TreeDTO getTypeTree(Type type, IEntityContext context) {
        var typeInstance = context.getInstance(type);
        return typeInstance.toTree(true).toDTO();
    }

    public Page<TypeDTO> query(TypeQuery request) {
        try (IEntityContext context = newContext()) {
            return query(request, context);
        }
    }

    private Page<TypeDTO> query(TypeQuery query,
                                IEntityContext context) {
        var typePage = query0(query, context);
        return new Page<>(
                NncUtils.map(typePage.data(), Type::toDTO),
                typePage.total()
        );
    }

    public static final List<Class<? extends Type>> CUSTOM_TYPE_CLASSES = List.of(
            ClassType.class, FunctionType.class, UnionType.class,
            IntersectionType.class, ArrayType.class, UncertainType.class
    );

    private List<Type> getAllTypes(IEntityContext context) {
        var defContext = context.getDefContext();
        List<Type> types = new ArrayList<>(
                NncUtils.exclude(defContext.getAllBufferedEntities(Type.class), Entity::isEphemeralEntity)
        );
        for (Class<? extends Type> customTypeClass : CUSTOM_TYPE_CLASSES) {
            context.getAllByType(customTypeClass, types);
        }
        return types;
    }

    private List<Function> getAllFunctions(IEntityContext context) {
        var defContext = context.getDefContext();
        var functions = new ArrayList<>(defContext.getAllBufferedEntities(Function.class));
        context.getAllByType(Function.class, functions);
        return functions;
    }

    public LoadAllMetadataResponse loadAllMetadata() {
        try (var context = newContext();
             var serContext = SerializeContext.enter()) {
            var types = getAllTypes(context);
            var functions = getAllFunctions(context);
            return new LoadAllMetadataResponse(
                    Versions.getLatestVersion(context),
                    SerializeContext.forceWriteTypes(types),
                    List.of(),
                    NncUtils.map(functions, f -> f.toDTO(false, serContext))
            );
        }
    }

    private Page<? extends Type> query0(TypeQuery query, IEntityContext context) {
        List<TypeCategory> categories = query.categories() != null ?
                NncUtils.map(query.categories(), TypeCategory::getByCode)
                : List.of(TypeCategory.CLASS, TypeCategory.VALUE);
        if (categories.isEmpty())
            return new Page<>(List.of(), 0);
        return entityQueryService.query(
                EntityQueryBuilder.newBuilder(Type.class)
                        .searchText(query.searchText())
                        .searchFields(List.of("code"))
                        .addField("category", categories)
                        .addFieldIf(!query.includeAnonymous(), "anonymous", false)
                        .addFieldIfNotNull("templateFlag", query.isTemplate())
                        .addFieldIfNotNull("error", query.error())
                        .includeBuiltin(query.includeBuiltin())
                        .page(query.page())
                        .pageSize(query.pageSize())
                        .newlyCreated(query.newlyCreated())
                        .build(),
                context
        );
    }

    public List<CreatingFieldDTO> getCreatingFields(long typeId) {
        try (var context = newContext()) {
            var creatingFields = context.selectByKey(FieldData.IDX_DECLARING_TYPE, context.getClassType(typeId));
            return NncUtils.map(creatingFields, FieldData::toDTO);
        }
    }

    public GetTypeResponse getType(GetTypeRequest request) {
        try (var context = newContext()) {
            Type type = context.getType(request.getId());

            try (var serContext = SerializeContext.enter()) {
                serContext.forceWriteType(type);
                serContext.writeDependencies(context);
                return new GetTypeResponse(
                        NncUtils.findRequired(serContext.getTypes(), t -> t.id() == request.getId()),
                        NncUtils.filter(serContext.getTypes(), t -> !Objects.equals(t.id(), type.tryGetId()))
                );
            }
        }
    }

    public GetTypesResponse batchGetTypes(GetTypesRequest request) {
        try (var context = newContext()) {
            Set<Long> idSet = new HashSet<>(request.ids());
            try (var serContext = SerializeContext.enter()) {
                for (Long id : request.ids()) {
                    serContext.forceWriteType(context.getType(id));
                }
                serContext.writeDependencies(context);
                return new GetTypesResponse(
                        NncUtils.map(request.ids(), serContext::getType),
                        NncUtils.filter(serContext.getTypes(), t -> !idSet.contains(t.id()))
                );
            }
        }
    }

    public TypeDTO getNullableType(long id) {
        return getOrCreateCompositeType(id, (ctx, type) -> ctx.getUnionType(Set.of(
                type, StandardTypes.getNullType()
        )));
    }

    public TypeDTO getNullableArrayType(long id) {
        return getOrCreateCompositeType(id, (context, type) -> context.getArrayType(type, ArrayKind.READ_WRITE));
    }

    private TypeDTO getOrCreateCompositeType(long id, BiFunction<IEntityContext, Type, ? extends Type> mapper) {
        try (IEntityContext context = newContext()) {
            Type type = context.getType(id);
            Type compositeType = mapper.apply(context, type);
            if (compositeType.tryGetId() != null) {
                return compositeType.toDTO();
            } else {
                return createCompositeType(id, mapper);
            }
        }
    }

    private TypeDTO createCompositeType(long id, BiFunction<IEntityContext, Type, ? extends Type> mapper) {
        return transactionTemplate.execute(status -> {
            try (IEntityContext context = newContext()) {
                Type compositeType = mapper.apply(context, context.getType(id));
                if (!context.containsModel(compositeType)) {
                    context.bind(compositeType);
                }
                context.finish();
                return compositeType.toDTO();
            }
        });
    }

    @Transactional
    public TypeDTO saveType(TypeDTO typeDTO) {
        try (IEntityContext context = newContext()) {
            ClassType type = saveTypeWithContent(typeDTO, context);
            context.finish();
            return type.toDTO();
        }
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
        if (typeDTO.param() instanceof ClassTypeParam param) {
            List<FlowDTO> flows = param.flows();
            if (flows != null) {
                saveFlows(type, flows, context);
            }
        }
        if (isCreate) {
            initClass(type, context);
        }
        return type;
    }

    private void initClass(ClassType classType, IEntityContext context) {
        var classInit = classType.findMethodByCode("<cinit>");
        if (classInit != null) {
            flowExecutionService.executeInternal(
                    classInit, null,
                    List.of(),
                    context
            );
        }
    }

    private void saveFlows(ClassType type, List<FlowDTO> flows, IEntityContext context) {
        Set<Long> flowIds = NncUtils.mapNonNullUnique(flows, FlowDTO::id);
        for (Flow flow : new ArrayList<>(type.getMethods())) {
            if (flow.tryGetId() != null && !flowIds.contains(flow.tryGetId())) {
                flowManager.remove(flow, context);
            }
        }
        for (FlowDTO flowDTO : flows) {
            flowManager.save(flowDTO, context);
        }
    }

    @Transactional
    public List<Long> batchSave(BatchSaveRequest request) {
        List<TypeDTO> typeDTOs = request.types();
        FlowSavingContext.skipPreprocessing(true);
        try (var context = newContext()) {
            batchSave(typeDTOs, request.functions(), request.parameterizedFlows(), context);
            List<ClassType> newClasses = NncUtils.filterAndMap(
                    typeDTOs, t -> TypeCategory.getByCode(t.category()).isPojo() && t.id() == null,
                    t -> context.getClassType(t.getRef())
            );
            for (ClassType newClass : newClasses) {
                if (!newClass.isInterface()) {
                    context.initIds();
                    initClass(newClass, context);
                }
            }
            context.finish();
            return NncUtils.map(typeDTOs, typeDTO -> context.getType(typeDTO.getRef()).tryGetId());
        }
    }

    public List<Type> batchSave(List<TypeDTO> typeDTOs,
                                List<FlowDTO> functions,
                                List<ParameterizedFlowDTO> parameterizedFlowDTOs,
                                IEntityContext context) {
        var batch = SaveTypeBatch.create(context, typeDTOs, functions, parameterizedFlowDTOs);
        for (TypeDTO typeDTO : batch.getClassTypeDTOs()) {
            ClassTypeParam param = typeDTO.getClassParam();
            if (param.flows() != null) {
                for (FlowDTO flowDTO : param.flows()) {
                    flowManager.saveContent(flowDTO, context.getMethod(flowDTO.getRef()), context);
                }
            }
        }
        for (FlowDTO function : functions) {
            flowManager.saveContent(function, context.getFunction(function.getRef()), context);
        }
        for (ParameterizedFlowDTO parameterizedFlowDTO : parameterizedFlowDTOs) {
            var templateFlow = context.getFlow(parameterizedFlowDTO.getTemplateRef());
            var typeArgs = NncUtils.map(parameterizedFlowDTO.getTypeArgumentRefs(), context::getType);
            context.getGenericContext().getParameterizedFlow(templateFlow, typeArgs, ResolutionStage.DEFINITION, batch);
        }
        List<ClassType> templates = new ArrayList<>();
        for (TypeDTO typeDTO : typeDTOs) {
            var type = context.getType(typeDTO.getRef());
            if (type instanceof ClassType classType) {
                if (classType.isTemplate())
                    templates.add(classType);
                createOverridingFlows(classType, context);
            }
        }
        for (ClassType updatedTemplate : templates) {
            retransformClassTypeIfRequired(updatedTemplate, context);
        }
        return batch.getTypes();
    }

    private void createOverridingFlows(ClassType type, IEntityContext context) {
        if (type.isParameterized())
            return;
        for (ClassType it : type.getInterfaces()) {
            var methods = it.getAllMethods();
            for (var overridden : methods) {
                if (overridden.isAbstract())
                    flowManager.createOverridingFlows(overridden, type, context);
            }
        }
    }

    public GetTypeResponse getUnionType(List<Long> memberIds) {
        try (var context = newContext()) {
            var members = NncUtils.mapUnique(memberIds, context::getType);
            var type = context.getUnionType(members);
            if (type.tryGetId() == null) {
                if (TransactionSynchronizationManager.isActualTransactionActive())
                    context.finish();
                else
                    return transactionTemplate.execute(s -> getUnionType(memberIds));
            }
            return makeResponse(type, context);
        }
    }

    public GetTypeResponse getArrayType(long elementId, int kind) {
        try (var context = newContext()) {
            var elementType = context.getType(elementId);
            var type = context.getArrayType(elementType, ArrayKind.getByCode(kind));
            if (type.tryGetId() == null) {
                if (TransactionSynchronizationManager.isActualTransactionActive())
                    context.finish();
                else
                    return transactionTemplate.execute(status -> getArrayType(elementId, kind));
            }
            return makeResponse(type, context);
        }
    }

    public GetTypeResponse getParameterizedType(GetParameterizedTypeRequest request) {
        if (request.templateRef().isPersisted() && NncUtils.allMatch(request.typeArgumentRefs(), RefDTO::isPersisted)) {
            try (var context = newContext()) {
                var template = context.getClassType(request.templateRef());
                var typeArgs = NncUtils.map(request.typeArgumentRefs(), context::getType);
                var existing = context.getGenericContext().getExisting(template, typeArgs);
                if (existing != null) {
                    return makeResponse(existing, context);
                } else {
                    return transactionTemplate.execute(s -> createParameterizedType(request));
                }
            }
        } else {
            return createParameterizedType(request);
        }
    }

    private GetTypeResponse createParameterizedType(GetParameterizedTypeRequest request) {
        try (var context = newContext()) {
            if (NncUtils.isNotEmpty(request.contextTypes())) {
                batchSave(request.contextTypes(), List.of(), List.of(), context);
            }
            var template = context.getClassType(request.templateRef());
            var typeArgs = NncUtils.map(request.typeArgumentRefs(), context::getType);
            var type = context.getParameterizedType(template, typeArgs);
            if (type.tryGetId() == null && request.templateRef().isPersisted()
                    && NncUtils.allMatch(request.typeArgumentRefs(), RefDTO::isPersisted)) {
                context.finish();
            }
            return makeResponse(type, context);
        }
    }

    private GetTypeResponse makeResponse(Type type, IEntityContext context) {
        try (var serContext = SerializeContext.enter()) {
            var typeDTO = type.toDTO();
            serContext.writeDependencies(context);
            return new GetTypeResponse(typeDTO, serContext.getTypesExclude(type));
        }
    }

    public GetTypeResponse getFunctionType(List<Long> parameterTypeIds, Long returnTypeId) {
        try (var context = newContext()) {
            var parameterTypes = NncUtils.map(parameterTypeIds, context::getType);
            var returnType = context.getType(returnTypeId);
            var type = context.getFunctionType(parameterTypes, returnType);
            if (type.tryGetId() == null) {
                if (TransactionSynchronizationManager.isActualTransactionActive())
                    context.finish();
                else
                    return transactionTemplate.execute(s -> getFunctionType(parameterTypeIds, returnTypeId));
            }
            return makeResponse(type, context);
        }
    }

    public GetTypeResponse getUncertainType(Long lowerBoundId, Long upperBoundId) {
        try (var context = newContext()) {
            var lowerBound = context.getType(lowerBoundId);
            var upperBound = context.getType(upperBoundId);
            var type = context.getUncertainType(lowerBound, upperBound);
            if (type.tryGetId() == null) {
                if (TransactionSynchronizationManager.isActualTransactionActive())
                    context.finish();
                else
                    return transactionTemplate.execute(s -> getUncertainType(lowerBoundId, upperBoundId));
            }
            try (var serContext = SerializeContext.enter()) {
                var typeDTO = type.toDTO();
                return new GetTypeResponse(typeDTO, serContext.getTypesExclude(type));
            }
        }
    }

    public GetTypesResponse getDescendants(long id) {
        return getByRange(new GetByRangeRequest(
                StandardTypes.getNothingType().getId(),
                id,
                false,
                false,
                true,
                null));
    }

    public GetTypesResponse getByRange(GetByRangeRequest request) {
        try (var context = newContext()) {
            var lowerBound = context.getType(request.lowerBoundId());
            var upperBound = context.getType(request.upperBoundId());

            List<ClassType> types;
            if (lowerBound == StandardTypes.getNothingType() && upperBound == StandardTypes.getAnyType()) {
                types = NncUtils.filterByType(query0(
                        new TypeQuery(null, request.categories(), request.isTemplate(),
                                request.includeParameterized(), request.includeBuiltin(), null,
                                List.of(), 1, 20),
                        context
                ).data(), ClassType.class);
            } else {
                Set<TypeCategory> categories = request.categories() != null ?
                        NncUtils.mapUnique(request.categories(), TypeCategory::getByCode) : TypeCategory.pojoCategories();
                boolean downwards = upperBound != StandardTypes.getAnyType();
                Queue<ClassType> queue = new LinkedList<>();
                if (downwards) {
                    if (upperBound instanceof ClassType classType) {
                        queue.offer(classType);
                    } else if (upperBound instanceof UnionType unionType) {
                        for (Type member : unionType.getMembers()) {
                            if (member instanceof ClassType classType) {
                                queue.offer(classType);
                            }
                        }
                    }
                } else {
                    if (lowerBound instanceof ClassType classType) {
                        queue.offer(classType);
                    } else {
                        if (lowerBound instanceof IntersectionType intersection) {
                            for (Type type : intersection.getTypes()) {
                                if (type instanceof ClassType classType) {
                                    queue.offer(classType);
                                }
                            }
                        }
                    }
                }
                LinkedList<ClassType> typeList = new LinkedList<>();
                types = typeList;
                while (!queue.isEmpty()) {
                    var t = queue.poll();
                    if (t.isAssignableFrom(lowerBound)) {
                        if (t.isTemplate() == request.isTemplate()
                                && categories.contains(t.category)
                                && t.isParameterized() == request.includeParameterized()) {
                            if (downwards) {
                                typeList.add(t);
                            } else {
                                typeList.addFirst(t);
                            }
                        }
                        if (downwards) {
                            queue.addAll(t.getSubTypes());
                        } else {
                            queue.addAll(t.getSuperTypes());
                        }
                    }
                }
            }
            var typeRefs = NncUtils.mapUnique(types, Entity::getRef);
            try (var serContext = SerializeContext.enter()) {
                types.forEach(serContext::forceWriteType);
                return new GetTypesResponse(
                        NncUtils.map(types, t -> serContext.getType(t.getId())),
                        serContext.getTypes(t -> !typeRefs.contains(t.getRef()))
                );
            }
        }
    }

    @Transactional
    public long saveEnumConstant(InstanceDTO instanceDTO) {
        try (var context = newContext()) {
            var instanceContext = Objects.requireNonNull(context.getInstanceContext());
            var type = context.getClassType(instanceDTO.typeRef());
            ClassInstance instance;
            if (instanceDTO.id() == null) {
                instanceDTO = setOrdinal(instanceDTO, type.getEnumConstants().size(), type);
                instance = (ClassInstance) instanceManager.create(instanceDTO, instanceContext);
                FieldBuilder.newBuilder(instance.getTitle(), null, type, type)
                        .isStatic(true)
                        .staticValue(instance)
                        .build();
            } else {
                instance = (ClassInstance) instanceContext.get(instanceDTO.parseId());
                var ordinalField = type.findFieldByCode("ordinal");
                int ordinal = instance.getLongField(ordinalField).getValue().intValue();
                instanceDTO = setOrdinal(instanceDTO, ordinal, type);
                var field = type.getStaticFieldByName(instance.getTitle());
                instanceManager.update(instanceDTO, instanceContext);
                field.setName(instance.getTitle());
            }
            context.finish();
            return instance.getPhysicalId();
        }
    }

    private InstanceDTO setOrdinal(InstanceDTO instanceDTO, int ordinal, ClassType type) {
        var ordinalField = type.getFieldByCode("ordinal");
        var param = (ClassInstanceParam) instanceDTO.param();
        return instanceDTO.copyWithParam(
                param.copyWithNewField(
                        new InstanceFieldDTO(
                                ordinalField.tryGetId(),
                                ordinalField.getName(),
                                TypeCategory.LONG.code(),
                                false,
                                new PrimitiveFieldValue(
                                        ordinal + "",
                                        PrimitiveKind.LONG.code(),
                                        ordinal
                                )
                        )
                )
        );
    }

    @Transactional
    public void deleteEnumConstant(String id) {
        try (var context = newContext()) {
            var instanceContext = NncUtils.requireNonNull(context.getInstanceContext());
            var instance = instanceContext.get(Id.parse(id));
            var type = (ClassType) instance.getType();
            var field = type.getStaticFieldByName(instance.getTitle());
            context.remove(field);
            context.finish();
        }
    }

    @Transactional
    public void batchRemove(List<Long> typeIds) {
        try (var context = newContext()) {
            List<Type> types = NncUtils.map(typeIds, context::getType);
            context.batchRemove(types);
            context.finish();
        }
    }

    public ClassType createType(TypeDTO classDTO, IEntityContext context) {
        return createType(classDTO, true, context);
    }

    public ClassType createType(TypeDTO classDTO, boolean withContent, IEntityContext context) {
        NncUtils.requireNonNull(classDTO.name(), "类型名称不能为空");
        ensureClassNameAvailable(classDTO, context);
        var stage = withContent ? ResolutionStage.DECLARATION : ResolutionStage.INIT;
        var batch = SaveTypeBatch.create(context, List.of(classDTO), List.of());
        var type = Types.saveClasType(classDTO, stage, batch);
        createOverridingFlows(type, context);
        return type;
    }

    public ClassType updateType(TypeDTO typeDTO, ClassType type, IEntityContext context) {
        NncUtils.requireNonNull(typeDTO.name(), "类型名称不能为空");
        var batch = SaveTypeBatch.create(context, List.of(typeDTO), List.of());
        Types.saveClasType(typeDTO, ResolutionStage.DECLARATION, batch);
        retransformClassTypeIfRequired(type, context);
        createOverridingFlows(type, context);
        return type;
    }

    private void ensureClassNameAvailable(TypeDTO typeDTO, IEntityContext context) {
        var classWithSameName = context.selectFirstByKey(ClassType.IDX_NAME, typeDTO.name());
        if (classWithSameName != null && !classWithSameName.isAnonymous()) {
            throw BusinessException.invalidType(typeDTO, "类型名称已存在");
        }
    }

    @Transactional
    public void remove(long id) {
        try (var context = newContext()) {
            ClassType type = context.getClassType(id);
            if (type == null)
                return;
            context.remove(type);
            context.finish();
        }
    }

    @Transactional
    public long saveField(FieldDTO fieldDTO) {
        IEntityContext context = newContext();
        Field field = saveField(fieldDTO, context);
        context.finish();
        return NncUtils.getOrElse(field, Entity::tryGetId, 0L);
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
                declaringClass.getAllFields(), f -> f.tryGetId() != null && !fieldIds.contains(f.tryGetId()));
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
        var type = context.getType(fieldDTO.typeRef());
        var field = Types.createFieldAndBind(
                declaringType,
                fieldDTO,
                context
        );
        retransformClassTypeIfRequired(field.getDeclaringType(), context);
        if (fieldDTO.defaultValue() != null || fieldDTO.isChild() && type.isArray()) {
            context.bind(new AddFieldJobGroup(field));
        }
        return field;
    }

    @Transactional
    public void moveField(long id, int ordinal) {
        try (var context = newContext()) {
            var field = context.getField(id);
            field.getDeclaringType().moveField(field, ordinal);
            context.finish();
        }
    }

    private void removeTransformedFieldIfRequired(Field field, IEntityContext context) {
        if (field.getDeclaringType().isTemplate() && context.isPersisted(field.getDeclaringType())) {
            var templateInstances = context.selectByKey(ClassType.TEMPLATE_IDX, field.getDeclaringType());
            for (ClassType templateInstance : templateInstances) {
                templateInstance.removeField(
                        templateInstance.tryGetFieldByName(field.getName())
                );
            }
        }
    }

    private void retransformFieldIfRequired(Field field, IEntityContext context) {
        if (field.getDeclaringType().isTemplate() && context.isPersisted(field.getDeclaringType())) {
            var templateInstances = context.getTemplateInstances(field.getDeclaringType());
            for (ClassType templateInstance : templateInstances) {
                context.getGenericContext().retransformField(field, templateInstance);
            }
        }
    }

    private void retransformClassTypeIfRequired(ClassType classType, IEntityContext context) {
        if (classType.isTemplate() && context.isPersisted(classType)) {
            var templateInstances = context.getTemplateInstances(classType);
            for (ClassType templateInstance : templateInstances) {
                context.getGenericContext().retransformClass(classType, templateInstance);
            }
        }
    }

    private Field updateField(FieldDTO fieldDTO, Field field, IEntityContext context) {
        field.update(fieldDTO);
        if (fieldDTO.defaultValue() != null) {
            field.setDefaultValue(InstanceFactory.resolveValue(fieldDTO.defaultValue(), field.getType(), context));
        } else {
            field.setDefaultValue(Instances.nullInstance());
        }
        retransformFieldIfRequired(field, context);
        return field;
    }

    public GetFieldResponse getField(long fieldId) {
        try (var context = newContext()) {
            Field field = context.getField(fieldId);
            try (var serContext = SerializeContext.enter()) {
                var fieldDTO = NncUtils.get(field, field1 -> field1.toDTO());
                serContext.writeType(field.getType());
                return new GetFieldResponse(fieldDTO, serContext.getTypes());
            }
        }
    }

    @Transactional
    public void removeField(long fieldId) {
        IEntityContext context = newContext();
        Field field = context.getField(fieldId);
        field.getDeclaringType().removeField(field);
        removeTransformedFieldIfRequired(field, context);
        context.finish();
    }

    @Transactional
    public void setFieldAsTitle(long fieldId) {
        try (var context = newContext()) {
            Field field = context.getField(fieldId);
            field.getDeclaringType().setTitleField(field);
            context.finish();
        }
    }

    public Page<ConstraintDTO> listConstraints(long typeId, int page, int pageSize) {
        IEntityContext context = newContext();
        ClassType type = context.getClassType(typeId);
        Page<Constraint> dataPage = entityQueryService.query(
                EntityQueryBuilder.newBuilder(Constraint.class)
                        .addField("declaringType", type)
                        .page(page)
                        .pageSize(pageSize)
                        .build(),
                context
        );
        return new Page<>(
                NncUtils.map(dataPage.data(), constraint -> constraint.toDTO()),
                dataPage.total()
        );
    }

    public ConstraintDTO getConstraint(long id) {
        try (IEntityContext context = newContext()) {
            Constraint constraint = context.getEntity(Constraint.class, id);
            if (constraint == null)
                throw BusinessException.constraintNotFound(id);
            return constraint.toDTO();
        }
    }

    @Transactional
    public long saveConstraint(ConstraintDTO constraintDTO) {
        var context = newContext();
        Constraint constraint;
        constraint = ConstraintFactory.save(constraintDTO, context);
        context.finish();
        return constraint.getId();
    }

    @Transactional
    public void removeConstraint(long id) {
        try (var context = newContext()) {
            Constraint constraint = context.getEntity(Constraint.class, id);
            if (constraint == null)
                throw BusinessException.constraintNotFound(id);
            constraint.getDeclaringType().removeConstraint(constraint);
            context.finish();
        }
    }

    public LoadByPathsResponse loadByPaths(List<String> paths) {
        try (IEntityContext context = newContext()) {
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
                    type = context.getType(Expressions.parseIdFromConstantVar(firstItem));
                } else {
                    type = context.selectFirstByKey(ClassType.IDX_NAME, firstItem);
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
                    Type parent = Objects.requireNonNull(path2type.get(parentPath.toString()));
                    if (parent.isBinaryNullable()) {
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
                    path2typeId.put(path, type.tryGetId());
                    if (!visitedTypeIds.contains(type.tryGetId())) {
                        visitedTypeIds.add(type.tryGetId());
                        typeDTOs.add(type.toDTO());
                    }
                }
            });
            return new LoadByPathsResponse(path2typeId, typeDTOs);
        }
    }

    @Transactional
    public void initCompositeTypes(long id) {
        IEntityContext context = newContext();
        initCompositeTypes(context.getType(id), context);
        context.finish();
    }

    private void initCompositeTypes(Type type, IEntityContext context) {
        context.getUnionType(Set.of(type, StandardTypes.getNullType()));
        var arrayType = context.getArrayType(type, ArrayKind.READ_WRITE);
        context.getUnionType(Set.of(arrayType, StandardTypes.getNullType()));
    }


    @Autowired
    public void setFlowManager(FlowManager flowManager) {
        this.flowManager = flowManager;
    }

    @Autowired
    public void setFlowExecutionService(FlowExecutionService flowExecutionService) {
        this.flowExecutionService = flowExecutionService;
    }

    @Autowired
    public void setVersionManager(VersionManager versionManager) {
        this.versionManager = versionManager;
    }

    @Autowired
    public void setInstanceManager(InstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

    @Autowired
    public void setViewManager(ViewManager viewManager) {
    }
}
