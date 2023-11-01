package tech.metavm.object.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.dto.BaseDTO;
import tech.metavm.dto.Page;
import tech.metavm.dto.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.expression.Var;
import tech.metavm.flow.*;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.instance.ArrayKind;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.query.Path;
import tech.metavm.object.instance.rest.ClassInstanceParam;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.meta.rest.dto.*;
import tech.metavm.task.AddFieldJobGroup;
import tech.metavm.task.FieldData;
import tech.metavm.task.TaskManager;
import tech.metavm.util.*;

import java.util.LinkedList;
import java.util.*;
import java.util.function.BiFunction;

@Component
public class TypeManager {

    public static final Logger LOGGER = LoggerFactory.getLogger(TypeManager.class);

    private final InstanceContextFactory instanceContextFactory;

    private final EntityQueryService entityQueryService;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final TaskManager jobManager;

    private final TransactionOperations transactionTemplate;

    private FlowExecutionService flowExecutionService;

    private FlowManager flowManager;

    private InstanceManager instanceManager;

    public TypeManager(InstanceContextFactory instanceContextFactory,
                       EntityQueryService entityQueryService,
                       TaskManager jobManager,
                       TransactionOperations transactionTemplate) {
        this.instanceContextFactory = instanceContextFactory;
        this.entityQueryService = entityQueryService;
        this.jobManager = jobManager;
        this.transactionTemplate = transactionTemplate;
    }

    public Map<String, TypeDTO> getPrimitiveTypes() {
        try (var ignored = SerializeContext.enter()) {
            Map<String, TypeDTO> primitiveTypes = new HashMap<>();
            primitiveTypes.put("long", StandardTypes.getLongType().toDTO());
            primitiveTypes.put("double", StandardTypes.getDoubleType().toDTO());
            primitiveTypes.put("boolean", StandardTypes.getBoolType().toDTO());
            primitiveTypes.put("string", StandardTypes.getStringType().toDTO());
            primitiveTypes.put("time", StandardTypes.getTimeType().toDTO());
            primitiveTypes.put("password", StandardTypes.getPasswordType().toDTO());
            primitiveTypes.put("null", StandardTypes.getNullType().toDTO());
            primitiveTypes.put("void", StandardTypes.getVoidType().toDTO());
            primitiveTypes.put("object", StandardTypes.getObjectType().toDTO());
            primitiveTypes.put("array", StandardTypes.getObjectArrayType().toDTO());
            primitiveTypes.put("childArray", StandardTypes.getObjectChildArrayType().toDTO());
            primitiveTypes.put("throwable", StandardTypes.getThrowableType().toDTO());
            primitiveTypes.put("nothing", StandardTypes.getNothingType().toDTO());
            primitiveTypes.put("string|null", StandardTypes.getType(BiUnion.createNullableType(String.class)).toDTO());
            return primitiveTypes;
        }
    }

    public Map<Integer, Long> getPrimitiveMap() {
        Map<Integer, Long> primitiveTypes = new HashMap<>();
        primitiveTypes.put(PrimitiveKind.LONG.getCode(), StandardTypes.getLongType().getId());
        primitiveTypes.put(PrimitiveKind.DOUBLE.getCode(), StandardTypes.getDoubleType().getId());
        primitiveTypes.put(PrimitiveKind.STRING.getCode(), StandardTypes.getStringType().getId());
        primitiveTypes.put(PrimitiveKind.BOOLEAN.getCode(), StandardTypes.getBoolType().getId());
        primitiveTypes.put(PrimitiveKind.TIME.getCode(), StandardTypes.getTimeType().getId());
        primitiveTypes.put(PrimitiveKind.PASSWORD.getCode(), StandardTypes.getPasswordType().getId());
        primitiveTypes.put(PrimitiveKind.NULL.getCode(), StandardTypes.getNullType().getId());
        primitiveTypes.put(PrimitiveKind.VOID.getCode(), StandardTypes.getVoidType().getId());
        return primitiveTypes;
    }

    public Page<TypeDTO> query(QueryTypeRequest request) {
        IEntityContext context = newContext();
        return query(request, context);
    }

    public Page<TypeDTO> query(QueryTypeRequest request,
                               IEntityContext context) {
        List<TypeCategory> categories = request.categories() != null ?
                NncUtils.map(request.categories(), TypeCategory::getByCode)
                : List.of(TypeCategory.CLASS, TypeCategory.VALUE);
        if (categories.isEmpty()) {
            return new Page<>(List.of(), 0);
        }
        var fields = new ArrayList<>(List.of(
                new EntityQueryField("category", categories),
                new EntityQueryField("anonymous", categories.equals(List.of(TypeCategory.FUNCTION)))
        ));
        fields.add(new EntityQueryField("isParameterized", request.isParameterized()));
        if (request.isTemplate() != null) {
            fields.add(new EntityQueryField("isTemplate", request.isTemplate()));
        }
        Page<? extends Type> typePage = entityQueryService.query(
                new EntityQuery<>(
                        Type.class,
                        request.searchText(),
                        List.of("code"),
                        request.includeBuiltin(),
                        request.page(),
                        request.pageSize(),
                        fields
                ),
                context
        );

        return new Page<>(
                NncUtils.map(typePage.data(), Type::toDTO),
                typePage.total()
        );
    }

    public List<CreatingFieldDTO> getCreatingFields(long typeId) {
        try(var context = newContext()) {
            var creatingFields = context.selectByKey(FieldData.IDX_DECLARING_TYPE, context.getClassType(typeId));
            return NncUtils.map(creatingFields, FieldData::toDTO);
        }
    }

    public GetTypeResponse getType(GetTypeRequest request) {
        try(var context = newContext()) {
            Type type = context.getType(request.id());

            try (var serContext = SerializeContext.enter()) {
                serContext.forceWriteType(type);
                serContext.writeDependencies();
                return new GetTypeResponse(
                        NncUtils.findRequired(serContext.getTypes(), t -> t.id() == request.id()),
                        NncUtils.filter(serContext.getTypes(), t -> !Objects.equals(t.id(), type.getId()))
                );
            }
        }
    }

    public GetTypesResponse batchGetTypes(GetTypesRequest request) {
        try(var context = newContext()) {
            Set<Long> idSet = new HashSet<>(request.ids());
            try (var serContext = SerializeContext.enter()) {
                for (Long id : request.ids()) {
                    serContext.forceWriteType(context.getType(id));
                }
                serContext.writeDependencies();
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
        IEntityContext context = newContext();
        Type type = context.getType(id);
        Type compositeType = mapper.apply(context, type);
        if (compositeType.getId() != null) {
            return compositeType.toDTO();
        } else {
            return createCompositeType(id, mapper);
        }
    }

    private TypeDTO createCompositeType(long id, BiFunction<IEntityContext, Type, ? extends Type> mapper) {
        return transactionTemplate.execute(status -> {
            IEntityContext context = newContext();
            Type compositeType = mapper.apply(context, context.getType(id));
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
        return type.toDTO();
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
        var classInit = classType.getFlowByCode("<cinit>");
        if (classInit != null) {
            flowExecutionService.executeInternal(
                    classInit, null,
                    List.of(),
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
            flowManager.save(flowDTO, context);
        }
    }

    @Transactional
    public List<Long> batchSave(List<TypeDTO> typeDTOs) {
        FlowSavingContext.skipPreprocessing(true);
        IEntityContext context = newContext();
        batchSave(typeDTOs, context);
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
        return NncUtils.map(typeDTOs, typeDTO -> context.getType(typeDTO.getRef()).getIdRequired());
    }

    public void batchSave(List<TypeDTO> typeDTOs, IEntityContext context) {
        List<ClassType> newClasses = new ArrayList<>();
        var newDTOs = sortByTopology(typeDTOs);
        for (BaseDTO baseDTO : newDTOs) {
            if (baseDTO instanceof TypeDTO typeDTO) {
                var category = TypeCategory.getByCode(typeDTO.category());
                if (category.isPojo()) {
                    newClasses.add(TypeUtil.createClassType(typeDTO, context));
                } else if (typeDTO.category() == TypeCategory.VARIABLE.code()) {
                    var typeVar = new TypeVariable(
                            typeDTO.tmpId(), typeDTO.name(), typeDTO.code(),
                            DummyGenericDeclaration.INSTANCE
                    );
                    context.bind(typeVar);
                } else if (category.isArray()) {
                    var param = typeDTO.getArrayTypeParam();
                    var elementType = context.getType(param.elementTypeRef());
                    var arrayType = new ArrayType(typeDTO.tmpId(), elementType, ArrayKind.getByCode(param.kind()));
                    context.bind(arrayType);
                } else if (typeDTO.category() == TypeCategory.UNION.code()) {
                    var param = typeDTO.getUnionParam();
                    var unionType = new UnionType(
                            typeDTO.tmpId(),
                            NncUtils.mapUnique(param.memberRefs(), context::getType)
                    );
                    context.bind(unionType);
                } else if (typeDTO.category() == TypeCategory.UNCERTAIN.code()) {
                    var param = typeDTO.getUncertainTypeParam();
                    context.bind(
                            new UncertainType(
                                    typeDTO.tmpId(),
                                    context.getType(param.lowerBoundRef()),
                                    context.getType(param.upperBoundRef())
                            )
                    );
                } else if (typeDTO.category() == TypeCategory.FUNCTION.code()) {
                    var param = typeDTO.getFunctionTypeParam();
                    var funcType = new FunctionType(
                            typeDTO.tmpId(),
                            NncUtils.map(param.parameterTypeRefs(), context::getType),
                            context.getType(param.returnTypeRef())
                    );
                    context.bind(funcType);
                } else {
                    throw new InternalException("Invalid type category: " + typeDTO.category());
                }
            } else if (baseDTO instanceof FlowDTO flowDTO) {
                var flow = FlowBuilder
                        .newBuilder(
                                context.getClassType(flowDTO.declaringTypeRef()),
                                flowDTO.name(), flowDTO.code(),
                                context.getFunctionTypeContext()
                        )
                        .typeParameters(NncUtils.map(flowDTO.typeParameterRefs(), context::getTypeVariable))
                        .type(context.getFunctionType(flowDTO.typeRef()))
                        .staticType(context.getFunctionType(flowDTO.staticTypeRef()))
                        .template(flowDTO.templateRef() != null ? context.getFlow(flowDTO.templateRef()) : null)
                        .flowDTO(flowDTO).build();
                context.bind(flow);
            } else {
                throw new InternalException("Unexpected BaseDTO: " + baseDTO);
            }
        }
        List<TypeDTO> classDTOs = NncUtils.filter(typeDTOs,
                typeDTO -> TypeCategory.getByCode(typeDTO.category()).isPojo());
        for (TypeDTO typeDTO : classDTOs) {
            TypeUtil.saveClasType(typeDTO, true, context);
            ClassTypeParam param = typeDTO.getClassParam();
            ClassType klass = context.getClassType(typeDTO.getRef());
            if (param.fields() != null) {
                saveFields(param.fields(), klass, context);
            }
            if (param.staticFields() != null) {
                saveFields(param.staticFields(), klass, context);
            }
            if (param.flows() != null) {
                Set<RefDTO> flowRefs = new HashSet<>();
                for (FlowDTO flowDTO : param.flows()) {
                    flowRefs.add(flowDTO.getRef());
                    flowManager.saveDeclaration(flowDTO, context);
                }
                List<Flow> flowsToRemove = NncUtils.filter(klass.getFlows(),
                        flow -> !flowRefs.contains(flow.getRef()));
                flowsToRemove.forEach(klass::removeFlow);
            }
        }
        List<TypeDTO> typeVariableDTOs = NncUtils.filter(typeDTOs,
                typeDTO -> typeDTO.category() == TypeCategory.VARIABLE.code());
        for (TypeDTO typeDTO : typeVariableDTOs) {
            var typeVariable = context.getEntity(TypeVariable.class, typeDTO.getRef());
            var param = typeDTO.getTypeVariableParam();
            typeVariable.setBounds(NncUtils.map(param.boundRefs(), context::getType));
        }
        for (TypeDTO typeDTO : classDTOs) {
            ClassTypeParam param = typeDTO.getClassParam();
            if (param.flows() != null) {
                for (FlowDTO flowDTO : param.flows()) {
                    flowManager.saveContent(flowDTO, context.getFlow(flowDTO.getRef()), context);
                }
            }
        }
        List<ClassType> templates = new ArrayList<>();
        for (TypeDTO typeDTO : typeDTOs) {
            var type = context.getType(typeDTO.getRef());
            if (type instanceof ClassType classType) {
                if (classType.isTemplate()) {
                    templates.add(classType);
                }
                createOverridingFlows(classType, context);
            }
        }
        for (ClassType updatedTemplate : templates) {
            retransformClassTypeIfRequired(updatedTemplate, context);
        }
    }

    private void createOverridingFlows(ClassType type, IEntityContext context) {
        if (type.isParameterized()) {
            return;
        }
        for (ClassType it : type.getInterfaces()) {
            var flows = it.getAllFlows();
            for (Flow overriden : flows) {
                flowManager.createOverrideFlows(overriden, type, context);
            }
        }
    }

    private List<BaseDTO> sortByTopology(List<TypeDTO> typeDTOs) {
        return new DTOVisitor(typeDTOs).result;
    }

    @Transactional
    public GetTypeResponse getUnionType(List<Long> memberIds) {
        try(var context = newContext()) {
            var members = NncUtils.mapUnique(memberIds, context::getType);
            var type = context.getUnionType(members);
            if (type.getId() == null) {
                context.finish();
            }
            try (var serContext = SerializeContext.enter()) {
                var typeDTO = type.toDTO();
                return new GetTypeResponse(typeDTO, serContext.getTypesExclude(type));
            }
        }
    }

    public GetTypeResponse getArrayType(long elementId, int kind) {
        try(var context = newContext()) {
            var elementType = context.getType(elementId);
            var arrayType = context.getArrayType(elementType, ArrayKind.getByCode(kind));
            if (arrayType.getId() == null) {
                context.finish();
            }
            try (var serContext = SerializeContext.enter()) {
                var typeDTO = arrayType.toDTO();
                return new GetTypeResponse(typeDTO, serContext.getTypesExclude(arrayType));
            }
        }
    }

    public GetTypeResponse getParameterizedType(GetParameterizedTypeRequest request) {
        if (request.templateRef().isPersisted() && NncUtils.allMatch(request.typeArgumentRefs(), RefDTO::isPersisted)) {
            try(var context = newContext()) {
                var template = context.getClassType(request.templateRef());
                var typeArgs = NncUtils.map(request.typeArgumentRefs(), context::getType);
                var existing = context.getGenericContext().getExisting(template, typeArgs);
                if (existing != null) {
                    return generateResponse(existing);
                } else {
                    return transactionTemplate.execute((status) -> createParameterizedType0(request));
                }
            }
        } else {
            return createParameterizedType0(request);
        }
    }

    private GetTypeResponse createParameterizedType0(GetParameterizedTypeRequest request) {
        var context = newContext();
        if (NncUtils.isNotEmpty(request.contextTypes())) {
            batchSave(request.contextTypes(), context);
        }
        var template = context.getClassType(request.templateRef());
        var typeArgs = NncUtils.map(request.typeArgumentRefs(), context::getType);
        var type = context.getParameterizedType(template, typeArgs);
        if (type.getId() == null && request.templateRef().isPersisted()
                && NncUtils.allMatch(request.typeArgumentRefs(), RefDTO::isPersisted)) {
            context.finish();
        }
        return generateResponse(type);
    }

    private GetTypeResponse generateResponse(ClassType type) {
        try (var serContext = SerializeContext.enter()) {
            var typeDTO = type.toDTO();
            serContext.writeDependencies();
            return new GetTypeResponse(typeDTO, serContext.getTypesExclude(type));
        }
    }

    @Transactional
    public GetTypeResponse getFunctionType(List<Long> parameterTypeIds, Long returnTypeId) {
        try(var context = newContext()) {
            var parameterTypes = NncUtils.map(parameterTypeIds, context::getType);
            var returnType = context.getType(returnTypeId);
            var type = context.getFunctionType(parameterTypes, returnType);
            if (type.getId() == null) {
                context.finish();
            }
            try (var serContext = SerializeContext.enter()) {
                var typeDTO = type.toDTO();
                return new GetTypeResponse(typeDTO, serContext.getTypesExclude(type));
            }
        }
    }

    @Transactional
    public GetTypeResponse getUncertainType(Long lowerBoundId, Long upperBoundId) {
        try(var context = newContext()) {
            var lowerBound = context.getType(lowerBoundId);
            var upperBound = context.getType(upperBoundId);
            var type = context.getUncertainType(lowerBound, upperBound);
            if (type.getId() == null) {
                context.finish();
            }
            try (var serContext = SerializeContext.enter()) {
                var typeDTO = type.toDTO();
                return new GetTypeResponse(typeDTO, serContext.getTypesExclude(type));
            }
        }
    }

    public GetTypesResponse getDescendants(long id) {
        try(var context = newContext()) {
            var type = context.getType(id);
            Queue<ClassType> queue = new LinkedList<>();
            if (type instanceof ClassType classType) {
                queue.offer(classType);
            } else if (type instanceof UnionType unionType) {
                for (Type member : unionType.getMembers()) {
                    if (member instanceof ClassType classType) {
                        queue.offer(classType);
                    }
                }
            }
            List<ClassType> types = new ArrayList<>();
            Set<RefDTO> typeRefs = new LinkedHashSet<>();
            while (!queue.isEmpty()) {
                var t = queue.poll();
                types.add(t);
                typeRefs.add(t.getRef());
                queue.addAll(t.getSubTypes());
            }
            try (var serContext = SerializeContext.enter()) {
                types.forEach(serContext::forceWriteType);
                return new GetTypesResponse(
                        NncUtils.map(types, t -> serContext.getType(t.getIdRequired())),
                        serContext.getTypes(t -> !typeRefs.contains(t.getRef()))
                );
            }
        }
    }

    @Transactional
    public long saveEnumConstant(InstanceDTO instanceDTO) {
        try(var context = newContext()) {
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
                instance = (ClassInstance) instanceContext.get(instanceDTO.id());
                var ordinalField = type.getFieldByCode("ordinal");
                int ordinal = instance.getLongField(ordinalField).getValue().intValue();
                instanceDTO = setOrdinal(instanceDTO, ordinal, type);
                var field = type.getStaticFieldByName(instance.getTitle());
                instanceManager.update(instanceDTO, instanceContext);
                field.setName(instance.getTitle());
            }
            context.finish();
            return instance.getIdRequired();
        }
    }

    private InstanceDTO setOrdinal(InstanceDTO instanceDTO, int ordinal, ClassType type) {
        var ordinalField = type.getFieldByCode("ordinal");
        var param = (ClassInstanceParam) instanceDTO.param();
        return instanceDTO.copyWithParam(
                param.copyWithNewField(
                        new InstanceFieldDTO(
                                ordinalField.getIdRequired(),
                                ordinalField.getName(),
                                TypeCategory.LONG.code(),
                                false,
                                new PrimitiveFieldValue(
                                        ordinal + "",
                                        PrimitiveKind.LONG.getCode(),
                                        ordinal
                                )
                        )
                )
        );
    }

    @Transactional
    public void deleteEnumConstant(long id) {
        try(var context = newContext()) {
            var instanceContext = NncUtils.requireNonNull(context.getInstanceContext());
            var instance = instanceContext.get(id);
            var type = (ClassType) instance.getType();
            var field = type.getStaticFieldByName(instance.getTitle());
            context.remove(field);
            context.finish();
        }
    }

    private static class DTOVisitor {
        private final IdentitySet<BaseDTO> visited = new IdentitySet<>();
        private final IdentitySet<BaseDTO> visiting = new IdentitySet<>();
        private final List<BaseDTO> result = new ArrayList<>();
        private final Map<RefDTO, BaseDTO> map = new HashMap<>();

        public DTOVisitor(List<TypeDTO> typeDTOs) {
            for (TypeDTO typeDTO : typeDTOs) {
                map.put(typeDTO.getRef(), typeDTO);
                if (TypeCategory.getByCode(typeDTO.category()).isPojo()) {
                    var param = typeDTO.getClassParam();
                    if (param.flows() != null) {
                        for (FlowDTO flow : param.flows()) {
                            map.put(flow.getRef(), flow);
                            for (FlowDTO flowTmpInst : flow.templateInstances()) {
                                map.put(flowTmpInst.getRef(), flowTmpInst);
                            }
                        }
                    }
                }
            }
            for (BaseDTO baseDTO : map.values()) {
                visit(baseDTO);
            }
        }

        public void visitRef(RefDTO ref) {
            if (ref == null || ref.isEmpty()) {
                return;
            }
            var baseDTO = map.get(ref);
            if (baseDTO != null) {
                visit(baseDTO);
            }
        }

        private void visit(BaseDTO baseDTO) {
            if (baseDTO.getRef().id() != null) {
                return;
            }
            if (visiting.contains(baseDTO)) {
                throw new InternalException("Circular reference");
            }
            if (visited.contains(baseDTO)) {
                return;
            }
            visiting.add(baseDTO);
            getDependencies(baseDTO).forEach(this::visitRef);
            result.add(baseDTO);
            visiting.remove(baseDTO);
            visited.add(baseDTO);
        }

        private Set<RefDTO> getDependencies(BaseDTO baseDTO) {
            Set<RefDTO> dependencies = new HashSet<>();
            switch (baseDTO) {
                case TypeDTO typeDTO -> getDependencies(typeDTO, dependencies);
                case FlowDTO flowDTO -> getDependencies(flowDTO, dependencies);
                default -> throw new IllegalStateException("Unexpected value: " + baseDTO);
            }
            return dependencies;
        }

        private void getDependencies(TypeDTO typeDTO, Set<RefDTO> dependencies) {
            var category = TypeCategory.getByCode(typeDTO.category());
            if (category.isPojo()) {
                var param = typeDTO.getClassParam();
                dependencies.add(param.templateRef());
                if (param.typeParameterRefs() != null) {
                    dependencies.addAll(param.typeParameterRefs());
                }
            } else if (category.isArray()) {
                var param = typeDTO.getArrayTypeParam();
                dependencies.add(param.elementTypeRef());
            } else if (category == TypeCategory.UNION) {
                var param = typeDTO.getUnionParam();
                dependencies.addAll(param.memberRefs());
            } else if (category == TypeCategory.FUNCTION) {
                var param = typeDTO.getFunctionTypeParam();
                dependencies.addAll(param.parameterTypeRefs());
                dependencies.add(param.returnTypeRef());
            } else if (category == TypeCategory.UNCERTAIN) {
                var param = (UncertainTypeParam) typeDTO.param();
                dependencies.add(param.lowerBoundRef());
                dependencies.add(param.upperBoundRef());
            }
        }

        private void getDependencies(FlowDTO flowDTO, Set<RefDTO> dependencies) {
            dependencies.add(flowDTO.declaringTypeRef());
            dependencies.add(flowDTO.templateRef());
            dependencies.add(flowDTO.typeRef());
            dependencies.add(flowDTO.staticTypeRef());
            dependencies.addAll(flowDTO.typeParameterRefs());
        }

    }

    @Transactional
    public void batchRemove(List<Long> typeIds) {
        try(var context = newContext()) {
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
        var type = TypeUtil.saveClasType(classDTO, withContent, context);
        createOverridingFlows(type, context);
        return type;
    }

    public ClassType updateType(TypeDTO typeDTO, ClassType type, IEntityContext context) {
        NncUtils.requireNonNull(typeDTO.name(), "类型名称不能为空");
        TypeUtil.saveClasType(typeDTO, true, context);
        retransformClassTypeIfRequired(type, context);
        createOverridingFlows(type, context);
        return type;
    }

    private void ensureClassNameAvailable(TypeDTO typeDTO, IEntityContext context) {
        var classWithSameName = context.selectByUniqueKey(ClassType.UNIQUE_NAME, typeDTO.name());
        if (classWithSameName != null && !classWithSameName.isAnonymous()) {
            throw BusinessException.invalidType(typeDTO, "对象名称已存在");
        }
    }

    @Transactional
    public void remove(long id) {
        try(var context = newContext()) {
            ClassType type = context.getClassType(id);
            if (type == null) {
                return;
            }
            context.remove(type);
            context.finish();
        }
    }

    @Transactional
    public long saveField(FieldDTO fieldDTO) {
        IEntityContext context = newContext();
        Field field = saveField(fieldDTO, context);
        context.finish();
        return field != null ? field.getIdRequired() : 0L;
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
                declaringClass.getAllFields(), f -> f.getId() != null && !fieldIds.contains(f.getId()));
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
        if (fieldDTO.defaultValue() != null) {
            FieldData fieldData = FieldData.fromFieldDTO(fieldDTO, context);
            context.bind(new AddFieldJobGroup(fieldData));
            return null;
        } else {
            var field = TypeUtil.createFieldAndBind(
                    declaringType,
                    fieldDTO,
                    context
            );
            retransformFieldIfRequired(field, context);
            return field;
        }
    }

    @Transactional
    public void moveField(long id, int ordinal) {
        try(var context = newContext(true)) {
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
            field.setDefaultValue(InstanceUtils.nullInstance());
        }
        retransformFieldIfRequired(field, context);
        return field;
    }

    public GetFieldResponse getField(long fieldId) {
        try(var context = newContext()) {
            Field field = context.getField(fieldId);
            try (var serContext = SerializeContext.enter()) {
                var fieldDTO = NncUtils.get(field, Field::toDTO);
                serContext.writeType(field.getType());
                return new GetFieldResponse(fieldDTO, serContext.getTypes());
            }
        }
    }

    @Transactional
    public void removeField(long fieldId) {
        IEntityContext context = newContext();
        Field field = context.getField(fieldId);
        context.remove(field);
        removeTransformedFieldIfRequired(field, context);
        context.finish();
    }

    @Transactional
    public void setFieldAsTitle(long fieldId) {
        try (var context = newContext()) {
            Field field = context.getField(fieldId);
            if (field.isAsTitle()) {
                return;
            }
            field.setAsTitle(true);
            context.finish();
        }
    }

    private IEntityContext newContext() {
        return newContext(false);
    }


    private IEntityContext newContext(boolean asyncLogProcessing) {
        return instanceContextFactory.newEntityContext(asyncLogProcessing);
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
        try (IEntityContext context = newContext()) {
            Constraint<?> constraint = context.getEntity(Constraint.class, id);
            if (constraint == null) {
                throw BusinessException.constraintNotFound(id);
            }
            return constraint.toDTO();
        }
    }

    @Transactional
    public long saveConstraint(ConstraintDTO constraintDTO) {
        var context = newContext();
        Constraint<?> constraint;
        constraint = ConstraintFactory.save(constraintDTO, context);
        context.finish();
        return constraint.getIdRequired();

    }

    @Transactional
    public void removeConstraint(long id) {
        try(var context = newContext()) {
            Constraint<?> constraint = context.getEntity(Constraint.class, id);
            if (constraint == null) {
                throw BusinessException.constraintNotFound(id);
            }
            context.remove(constraint);
            context.finish();
        }
    }

    public LoadByPathsResponse loadByPaths(List<String> paths) {
        try(IEntityContext context = newContext()) {
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
                    type = context.selectByUniqueKey(ClassType.UNIQUE_NAME, firstItem);
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
        context.getUnionType(Set.of(type, StandardTypes.getNullType()));
        ArrayType arrayType = context.getArrayType(type, ArrayKind.READ_WRITE);
        context.getUnionType(Set.of(arrayType, StandardTypes.getNullType()));
    }

    @Autowired
    public void setInstanceManager(InstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }
}
