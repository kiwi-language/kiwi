package tech.metavm.object.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.common.Page;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.flow.FlowExecutionService;
import tech.metavm.flow.FlowManager;
import tech.metavm.flow.FlowSavingContext;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.object.version.VersionManager;
import tech.metavm.object.version.Versions;
import tech.metavm.task.AddFieldJobGroup;
import tech.metavm.task.TaskManager;
import tech.metavm.util.BusinessException;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import java.util.*;

@Component
public class TypeManager extends EntityContextFactoryBean {

    public static final Logger logger = LoggerFactory.getLogger(TypeManager.class);

    private final EntityQueryService entityQueryService;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final TaskManager jobManager;

    private FlowExecutionService flowExecutionService;

    private FlowManager flowManager;

    private InstanceManager instanceManager;

    private VersionManager versionManager;

    public TypeManager(EntityContextFactory entityContextFactory,
                       EntityQueryService entityQueryService,
                       TaskManager jobManager) {
        super(entityContextFactory);
        this.entityQueryService = entityQueryService;
        this.jobManager = jobManager;
    }

    public Map<Integer, String> getPrimitiveMap() {
        Map<Integer, String> primitiveTypes = new HashMap<>();
        primitiveTypes.put(PrimitiveKind.LONG.code(), "long");
        primitiveTypes.put(PrimitiveKind.DOUBLE.code(), "double");
        primitiveTypes.put(PrimitiveKind.STRING.code(), "string");
        primitiveTypes.put(PrimitiveKind.BOOLEAN.code(), "boolean");
        primitiveTypes.put(PrimitiveKind.TIME.code(), "time");
        primitiveTypes.put(PrimitiveKind.PASSWORD.code(), "password");
        primitiveTypes.put(PrimitiveKind.NULL.code(), "null");
        primitiveTypes.put(PrimitiveKind.VOID.code(), "void");
        return primitiveTypes;
    }

    public TreeResponse queryTrees(TypeTreeQuery query) {
        try (var context = newContext()) {
            List<?> entities;
            List<Long> removedIds;
            long version;
            if (query.version() == -1L) {
                entities = versionManager.getAllTypes(context);
                removedIds = List.of();
                version = Versions.getLatestVersion(context);
            } else {
                var patch = versionManager.pullInternal(query.version(), context);
                entities = NncUtils.merge(
                        NncUtils.map(patch.changedTypeDefIds(), context::getTypeDef),
                        NncUtils.map(patch.changedFunctionIds(), context::getFunction)
                );
                var removedInstanceIds = NncUtils.merge(
                        patch.removedTypeDefIds(),
                        patch.removedFunctionIds()
                );
                removedIds = new ArrayList<>();
                for (String removedInstanceId : removedInstanceIds) {
                    var id = Id.parse(removedInstanceId);
                    if (id instanceof PhysicalId physicalId && physicalId.getNodeId() == 0L)
                        removedIds.add(id.getTreeId());
                }
                version = patch.version();
            }
            return new TreeResponse(
                    version,
                    NncUtils.filterAndMap(entities,
                            t -> context.getInstance(t).isRoot(),
                            t -> getTypeTree(t, context)),
                    removedIds
            );
        }
    }

    private TreeDTO getTypeTree(Object entity, IEntityContext context) {
        var typeInstance = context.getInstance(entity);
        return typeInstance.toTree().toDTO();
    }

    public Page<TypeDTO> query(TypeQuery request) {
        try (IEntityContext context = newContext()) {
            return query(request, context);
        }
    }

    public GetTypeResponse getTypeByCode(String code) {
        try (IEntityContext context = newContext()) {
            var type = context.selectFirstByKey(Klass.UNIQUE_CODE, code);
            if (type == null) {
                throw BusinessException.typeNotFound(code);
            }
            try (var serContext = SerializeContext.enter()) {
                return new GetTypeResponse(type.toDTO(serContext), List.of());
            }
        }
    }

    private Page<TypeDTO> query(TypeQuery query,
                                IEntityContext context) {
        var typePage = query0(query, context);
        return new Page<>(
                NncUtils.map(typePage.data(), Klass::toDTO),
                typePage.total()
        );
    }

    private Page<Klass> query0(TypeQuery query, IEntityContext context) {
        List<ClassKind> kinds = query.kinds() != null ?
                NncUtils.map(query.kinds(), ClassKind::fromCode)
                : List.of(ClassKind.CLASS, ClassKind.VALUE);
        if (kinds.isEmpty())
            return new Page<>(List.of(), 0);
        return entityQueryService.query(
                EntityQueryBuilder.newBuilder(Klass.class)
                        .searchText(query.searchText())
                        .searchFields(List.of("code"))
                        .addField("kind", kinds)
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

    public List<CreatingFieldDTO> getCreatingFields(String typeId) {
        try (var context = newContext()) {
            var creatingFields = context.selectByKey(FieldData.IDX_DECLARING_TYPE, context.getKlass(typeId));
            return NncUtils.map(creatingFields, FieldData::toDTO);
        }
    }

    public GetTypeResponse getType(GetTypeRequest request) {
        try (var context = newContext()) {
            var type = context.getKlass(Id.parse(request.getId()));
            try (var serContext = SerializeContext.enter()) {
                serContext.forceWriteKlass(type);
                serContext.writeDependencies(context);
                return new GetTypeResponse(
                        NncUtils.findRequired(serContext.getTypes(), t -> Objects.equals(t.id(), request.getId())),
                        NncUtils.filter(serContext.getTypes(), t -> !Objects.equals(t.id(), type.getStringId()))
                );
            }
        }
    }

    public GetKlassesResponse batchGetTypes(GetTypesRequest request) {
        try (var context = newContext()) {
            var idSet = new HashSet<>(request.ids());
            try (var serContext = SerializeContext.enter()) {
                for (var id : request.ids()) {
                    serContext.forceWriteKlass(context.getKlass(id));
                }
                serContext.writeDependencies(context);
                return new GetKlassesResponse(
                        NncUtils.map(request.ids(), id -> serContext.getType(Id.parse(id))),
                        NncUtils.filter(serContext.getTypes(), t -> !idSet.contains(t.id()))
                );
            }
        }
    }

    @Transactional
    public TypeDTO saveType(TypeDTO typeDTO) {
        try (IEntityContext context = newContext()) {
            Klass type = saveTypeWithContent(typeDTO, context);
            context.finish();
            try (var serContext = SerializeContext.enter()) {
                return type.toDTO(serContext);
            }
        }
    }

    public Klass saveType(TypeDTO typeDTO, IEntityContext context) {
        var type = context.getKlass(typeDTO.id());
        if (type == null)
            return createType(typeDTO, context);
        else
            return updateType(typeDTO, type, context);
    }

    public Klass saveTypeWithContent(TypeDTO typeDTO, IEntityContext context) {
        Klass type = context.getEntity(Klass.class, typeDTO.id());
        return saveTypeWithContent(typeDTO, type, context);
    }

    public Klass saveTypeWithContent(TypeDTO typeDTO, Klass type, IEntityContext context) {
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

    private void initClass(Klass classType, IEntityContext context) {
        var classInit = classType.findMethodByCode("<cinit>");
        if (classInit != null) {
            flowExecutionService.executeInternal(
                    classInit, null,
                    List.of(),
                    context
            );
        }
    }

    private void saveFlows(Klass type, List<FlowDTO> flows, IEntityContext context) {
        Set<String> flowIds = NncUtils.mapNonNullUnique(flows, FlowDTO::id);
        for (Flow flow : new ArrayList<>(type.getMethods())) {
            if (flow.getStringId() != null && !flowIds.contains(flow.getStringId())) {
                flowManager.remove(flow, context);
            }
        }
        for (FlowDTO flowDTO : flows) {
            flowManager.save(flowDTO, context);
        }
    }

    @Transactional
    public List<String> batchSave(BatchSaveRequest request) {
        var typeDefDTOs = request.typeDefs();
        FlowSavingContext.skipPreprocessing(request.skipFlowPreprocess());
        try (var context = newContext()) {
            batchSave(typeDefDTOs, request.functions(), context);
            List<Klass> newClasses = NncUtils.filterAndMap(
                    typeDefDTOs, t -> t instanceof TypeDTO && !Id.isPersistedId(t.id()),
                    t -> context.getKlass(t.id())
            );
            for (Klass newClass : newClasses) {
                if (!newClass.isInterface()) {
//                    context.initIds();
                    initClass(newClass, context);
                }
            }
            context.finish();
            return NncUtils.map(typeDefDTOs, t -> Objects.requireNonNull(context.getTypeDef(t.id()),
                    "Type '" + t.id() + "' not saved").getStringId());
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public List<TypeDef> batchSave(List<? extends TypeDefDTO> typeDefDTOs,
                                   List<FlowDTO> functions,
                                   IEntityContext context) {
        var batch = SaveTypeBatch.create(context, typeDefDTOs, functions);
        for (TypeDTO typeDTO : batch.getTypeDTOs()) {
            ClassTypeParam param = typeDTO.getClassParam();
            if (param.flows() != null) {
                for (FlowDTO flowDTO : param.flows()) {
                    var flow = context.getMethod(Id.parse(flowDTO.id()));
                    if (!flow.isSynthetic())
                        flowManager.saveContent(flowDTO, context.getMethod(Id.parse(flowDTO.id())), context);
                }
            }
        }
        for (FlowDTO function : functions) {
            flowManager.saveContent(function, context.getFunction(Id.parse(function.id())), context);
        }
        for (TypeDTO typeDTO : batch.getTypeDTOs()) {
            var klass = context.getKlass(Id.parse(typeDTO.id()));
            createOverridingFlows(klass, context);
        }
        return batch.getTypes();
    }

    private void createOverridingFlows(Klass type, IEntityContext context) {
        if (type.isParameterized())
            return;
        for (var it : type.getInterfaces()) {
            var methods = it.resolve().getAllMethods();
            for (var overridden : methods) {
                if (overridden.isAbstract())
                    flowManager.createOverridingFlows(overridden, type, context);
            }
        }
    }

    public GetTypesResponse getDescendants(String id) {
        return getByRange(new GetByRangeRequest(
                "never",
                id,
                false,
                false,
                true,
                null));
    }

    public GetTypesResponse getByRange(GetByRangeRequest request) {
        try (var context = newContext()) {
            var lowerBound = TypeParser.parseType(request.lowerBoundId(), context);
            var upperBound = TypeParser.parseType(request.upperBoundId(), context);

            List<Klass> types;
            if (lowerBound.equals(StandardTypes.getNeverType()) && upperBound.equals(StandardTypes.getAnyType())) {
                types = NncUtils.filterByType(query0(
                        new TypeQuery(null, request.categories(), request.isTemplate(),
                                request.includeParameterized(), request.includeBuiltin(), null,
                                List.of(), 1, 20),
                        context
                ).data(), Klass.class);
            } else {
                Set<TypeCategory> categories = request.categories() != null ?
                        NncUtils.mapUnique(request.categories(), TypeCategory::fromCode) : TypeCategory.pojoCategories();
                boolean downwards = !upperBound.equals(StandardTypes.getAnyType());
                Queue<Klass> queue = new LinkedList<>();
                if (downwards) {
                    if (upperBound instanceof ClassType classType) {
                        queue.offer(classType.resolve());
                    } else if (upperBound instanceof UnionType unionType) {
                        for (Type member : unionType.getMembers()) {
                            if (member instanceof ClassType classType) {
                                queue.offer(classType.resolve());
                            }
                        }
                    }
                } else {
                    if (lowerBound instanceof ClassType classType) {
                        queue.offer(classType.resolve());
                    } else {
                        if (lowerBound instanceof IntersectionType intersection) {
                            for (Type type : intersection.getTypes()) {
                                if (type instanceof ClassType classType) {
                                    queue.offer(classType.resolve());
                                }
                            }
                        }
                    }
                }
                LinkedList<Klass> typeList = new LinkedList<>();
                types = typeList;
                while (!queue.isEmpty()) {
                    var t = queue.poll();
                    if (t.getType().isAssignableFrom(lowerBound)) {
                        if (t.isTemplate() == request.isTemplate()
                                && categories.contains(t.getType().getCategory())
                                && t.isParameterized() == request.includeParameterized()) {
                            if (downwards) {
                                typeList.add(t);
                            } else {
                                typeList.addFirst(t);
                            }
                        }
                        if (downwards) {
                            queue.addAll(t.getSubTypes());
                        } else
                            t.forEachSuper(queue::add);
                    }
                }
            }
            try (var serContext = SerializeContext.enter()) {
                return new GetTypesResponse(NncUtils.map(types, t -> t.getType().toExpression(serContext)));
            }
        }
    }

    @Transactional
    public String saveEnumConstant(InstanceDTO instanceDTO) {
        try (var context = newContext()) {
            var instanceContext = Objects.requireNonNull(context.getInstanceContext());
            var klass = TypeParser.parseClassType(instanceDTO.type(), context).resolve();
            ClassInstance instance;
            if (instanceDTO.isNew()) {
                instanceDTO = setOrdinal(instanceDTO, klass.getEnumConstants().size(), klass);
                instance = (ClassInstance) instanceManager.create(instanceDTO, instanceContext);
                FieldBuilder.newBuilder(instance.getTitle(), null, klass, klass.getType())
                        .isStatic(true)
                        .staticValue(instance)
                        .build();
            } else {
                instance = (ClassInstance) instanceContext.get(instanceDTO.parseId());
                var ordinalField = klass.findFieldByCode("ordinal");
                int ordinal = instance.getLongField(ordinalField).getValue().intValue();
                instanceDTO = setOrdinal(instanceDTO, ordinal, klass);
                var field = klass.getStaticFieldByName(instance.getTitle());
                instanceManager.update(instanceDTO, instanceContext);
                field.setName(instance.getTitle());
            }
            context.finish();
            return instance.getStringId();
        }
    }

    private InstanceDTO setOrdinal(InstanceDTO instanceDTO, int ordinal, Klass klass) {
        var ordinalField = klass.getFieldByCode("ordinal");
        var param = (ClassInstanceParam) instanceDTO.param();
        return instanceDTO.copyWithParam(
                param.copyWithNewField(
                        new InstanceFieldDTO(
                                ordinalField.getTag().toString(),
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
            var type = ((ClassInstance) instance).getKlass();
            var field = type.getStaticFieldByName(instance.getTitle());
            context.remove(field);
            context.finish();
        }
    }

    @Transactional
    public void batchRemove(List<String> typeIds) {
        try (var context = newContext()) {
            List<Type> types = NncUtils.map(typeIds, context::getType);
            context.batchRemove(types);
            context.finish();
        }
    }

    public Klass createType(TypeDTO classDTO, IEntityContext context) {
        return createType(classDTO, true, context);
    }

    public Klass createType(TypeDTO classDTO, boolean withContent, IEntityContext context) {
        NncUtils.requireNonNull(classDTO.name(), "类型名称不能为空");
        ensureClassNameAvailable(classDTO, context);
        var stage = withContent ? ResolutionStage.DECLARATION : ResolutionStage.INIT;
        var batch = SaveTypeBatch.create(context, List.of(classDTO), List.of());
        var type = Types.saveClasType(classDTO, stage, batch);
        createOverridingFlows(type, context);
        return type;
    }

    public Klass updateType(TypeDTO typeDTO, Klass type, IEntityContext context) {
        NncUtils.requireNonNull(typeDTO.name(), "类型名称不能为空");
        var batch = SaveTypeBatch.create(context, List.of(typeDTO), List.of());
        Types.saveClasType(typeDTO, ResolutionStage.DECLARATION, batch);
        createOverridingFlows(type, context);
        return type;
    }

    private void ensureClassNameAvailable(TypeDTO typeDTO, IEntityContext context) {
        if (!typeDTO.anonymous()) {
            var classWithSameName = context.selectFirstByKey(Klass.IDX_NAME, typeDTO.name());
            if (classWithSameName != null && !classWithSameName.isAnonymous()) {
                throw BusinessException.invalidType(typeDTO, "类型名称已存在");
            }
        }
    }

    @Transactional
    public void remove(String id) {
        try (var context = newContext()) {
            Klass type = context.getKlass(Id.parse(id));
            if (type == null)
                return;
            context.remove(type);
            context.finish();
        }
    }

    @Transactional
    public String saveField(FieldDTO fieldDTO) {
        IEntityContext context = newContext();
        Field field = saveField(fieldDTO, context);
        context.finish();
        return NncUtils.getOrElse(field, Entity::getStringId, null);
    }

    @SuppressWarnings("unused")
    public void saveFields(List<FieldDTO> fieldDTOs, Klass declaringClass, IEntityContext context) {
        Set<Id> fieldIds = new HashSet<>();
        for (FieldDTO fieldDTO : fieldDTOs) {
            var fieldId = Id.parse(fieldDTO.id());
            var field = context.getField(fieldId);
            if (field != null) {
                fieldIds.add(fieldId);
                updateField(fieldDTO, field, context);
            } else {
                createField(fieldDTO, declaringClass, context);
            }
        }
        List<Field> toRemove = NncUtils.filter(
                declaringClass.getAllFields(), f -> f.getEntityId() != null && !fieldIds.contains(f.getEntityId()));
        toRemove.forEach(declaringClass::removeField);

    }

    public Field saveField(FieldDTO fieldDTO, IEntityContext context) {
        return saveField(fieldDTO, context.getKlass(Id.parse(fieldDTO.declaringTypeId())), context);
    }

    private Field saveField(FieldDTO fieldDTO, Klass declaringType, IEntityContext context) {
        Field field = context.getField(fieldDTO.id());
        if (field == null) {
            return createField(fieldDTO, declaringType, context);
        } else {
            return updateField(fieldDTO, field, context);
        }
    }

    private Field createField(FieldDTO fieldDTO, Klass declaringType, IEntityContext context) {
        var type = TypeParser.parseType(fieldDTO.type(), context);
        var field = Types.createFieldAndBind(
                declaringType,
                fieldDTO,
                context
        );
        if (fieldDTO.defaultValue() != null || fieldDTO.isChild() && type.isArray()) {
            context.bind(new AddFieldJobGroup(field));
        }
        return field;
    }

    @Transactional
    public void moveField(String id, int ordinal) {
        try (var context = newContext()) {
            var field = context.getField(id);
            field.getDeclaringType().moveField(field, ordinal);
            context.finish();
        }
    }

    private void removeTransformedFieldIfRequired(Field field, IEntityContext context) {
        if (field.getDeclaringType().isTemplate() && context.isPersisted(field.getDeclaringType())) {
            var templateInstances = context.selectByKey(Klass.TEMPLATE_IDX, field.getDeclaringType());
            for (Klass templateInstance : templateInstances) {
                templateInstance.removeField(
                        templateInstance.tryGetFieldByName(field.getName())
                );
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
        return field;
    }

    public GetFieldResponse getField(String fieldId) {
        try (var context = newContext()) {
            Field field = context.getField(fieldId);
            try (var serContext = SerializeContext.enter()) {
                var fieldDTO = NncUtils.get(field, Field::toDTO);
                return new GetFieldResponse(fieldDTO, serContext.getTypes());
            }
        }
    }

    @Transactional
    public void removeField(String fieldId) {
        IEntityContext context = newContext();
        Field field = context.getField(fieldId);
        field.getDeclaringType().removeField(field);
        removeTransformedFieldIfRequired(field, context);
        context.finish();
    }

    @Transactional
    public void setFieldAsTitle(String fieldId) {
        try (var context = newContext()) {
            Field field = context.getField(fieldId);
            field.getDeclaringType().setTitleField(field);
            context.finish();
        }
    }

    public Page<ConstraintDTO> listConstraints(String typeId, int page, int pageSize) {
        IEntityContext context = newContext();
        Klass type = context.getKlass(typeId);
        Page<Constraint> dataPage = entityQueryService.query(
                EntityQueryBuilder.newBuilder(Constraint.class)
                        .addField("declaringType", type)
                        .page(page)
                        .pageSize(pageSize)
                        .build(),
                context
        );
        return new Page<>(
                NncUtils.map(dataPage.data(), Constraint::toDTO),
                dataPage.total()
        );
    }

    public ConstraintDTO getConstraint(String id) {
        try (IEntityContext context = newContext()) {
            Constraint constraint = context.getEntity(Constraint.class, id);
            if (constraint == null)
                throw BusinessException.constraintNotFound(id);
            return constraint.toDTO();
        }
    }

    @Transactional
    public String saveConstraint(ConstraintDTO constraintDTO) {
        var context = newContext();
        Constraint constraint;
        constraint = ConstraintFactory.save(constraintDTO, context);
        context.finish();
        return constraint.getStringId();
    }

    @Transactional
    public void removeConstraint(String id) {
        try (var context = newContext()) {
            Constraint constraint = context.getEntity(Constraint.class, id);
            if (constraint == null)
                throw BusinessException.constraintNotFound(id);
            constraint.getDeclaringType().removeConstraint(constraint);
            context.finish();
        }
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

}
