package tech.metavm.object.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import tech.metavm.autograph.FlowBuilder;
import tech.metavm.dto.BaseDTO;
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
        primitiveTypes.put("throwable", ModelDefRegistry.getType(Throwable.class).toDTO());
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
        if (typeDTO.param() instanceof ClassParamDTO param) {
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
        List<ClassType> newClasses = new ArrayList<>();
        var newDTOs = sortByTopology(typeDTOs);
        for (BaseDTO baseDTO : newDTOs) {
            if (baseDTO instanceof TypeDTO typeDTO) {
                if (TypeCategory.getByCode(typeDTO.category()).isPojo()) {
                    newClasses.add(TypeUtil.createClassType(typeDTO, context));
                } else if (typeDTO.category() == TypeCategory.VARIABLE.code()) {
                    var param = typeDTO.getTypeVariableParam();
                    var typeVar = new TypeVariable(
                            typeDTO.tmpId(), typeDTO.name(), typeDTO.code(),
                            context.getEntity(GenericDeclaration.class, param.genericDeclarationRef())
                    );
                    context.bind(typeVar);
                } else if (typeDTO.category() == TypeCategory.ARRAY.code()) {
                    var param = typeDTO.getArrayTypeParam();
                    var elementType = context.getType(param.elementTypeRef());
                    var arrayType = new ArrayType(typeDTO.tmpId(), elementType);
                    elementType.setArrayType(arrayType);
                    context.bind(arrayType);
                } else if (typeDTO.category() == TypeCategory.UNION.code()) {
                    var param = typeDTO.getUnionParam();
                    var unionType = new UnionType(
                            typeDTO.tmpId(),
                            NncUtils.mapUnique(param.members(), member -> context.getType(member.getRef()))
                    );
                    if (unionType.isNullable()) {
                        unionType.getUnderlyingType().setNullableType(unionType);
                    }
                    context.bind(unionType);
                }
            } else if (baseDTO instanceof FlowDTO flowDTO) {
                var flow = FlowBuilder
                        .newBuilder(
                                context.getClassType(flowDTO.declaringTypeRef()),
                                flowDTO.name(), flowDTO.code()
                        )
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
            ClassParamDTO param = typeDTO.getClassParam();
            ClassType klass = context.getClassType(typeDTO.getRef());
            saveFields(param.fields(), klass, context);
            saveFields(param.staticFields(), klass, context);
            Set<RefDTO> flowRefs = new HashSet<>();
            for (FlowDTO flowDTO : param.flows()) {
                flowRefs.add(flowDTO.getRef());
                flowManager.saveDeclaration(flowDTO, klass, context);
            }
            List<Flow> flowsToRemove = NncUtils.filter(klass.getFlows(),
                    flow -> !flowRefs.contains(flow.getRef()));
            flowsToRemove.forEach(klass::removeFlow);
        }
        List<TypeDTO> typeVariableDTOs = NncUtils.filter(typeDTOs,
                typeDTO -> typeDTO.category() == TypeCategory.VARIABLE.code());
        for (TypeDTO typeDTO : typeVariableDTOs) {
            var typeVariable = context.getEntity(TypeVariable.class, typeDTO.getRef());
            var param = typeDTO.getTypeVariableParam();
            typeVariable.setBounds(NncUtils.map(param.boundRefs(), context::getType));
        }
        for (TypeDTO typeDTO : classDTOs) {
            ClassParamDTO param = typeDTO.getClassParam();
            ClassType klass = context.getClassType(typeDTO.getRef());
            for (FlowDTO flowDTO : param.flows()) {
                flowManager.saveContent(flowDTO, context.getFlow(flowDTO.getRef()), klass, context);
            }
        }
        for (ClassType newClass : newClasses) {
            if(!newClass.isInterface()) {
                context.initIds();
                initClass(newClass, context);
            }
        }
        context.finish();
        return NncUtils.map(typeDTOs, typeDTO -> context.getType(typeDTO.getRef()).getIdRequired());
    }

    private List<BaseDTO> sortByTopology(List<TypeDTO> typeDTOs) {
        return new DTOVisitor(typeDTOs).result;
    }

    private static class DTOVisitor {
        private final IdentitySet<BaseDTO> visited = new IdentitySet<>();
        private final IdentitySet<BaseDTO> visiting = new IdentitySet<>();
        private final List<BaseDTO> result = new ArrayList<>();
        private final Map<RefDTO, BaseDTO> map = new HashMap<>();

        public DTOVisitor(List<TypeDTO> typeDTOs) {
            for (TypeDTO typeDTO : typeDTOs) {
                if (typeDTO.id() == null) {
                    map.put(typeDTO.getRef(), typeDTO);
                }
                if (TypeCategory.getByCode(typeDTO.category()).isPojo()) {
                    var param = typeDTO.getClassParam();
                    for (FlowDTO flow : param.flows()) {
                        if (flow.id() == null) {
                            map.put(flow.getRef(), flow);
                        }
                        for (FlowDTO flowTmpInst : flow.templateInstances()) {
                            if (flowTmpInst.id() == null) {
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
            } else if (category == TypeCategory.VARIABLE) {
                var param = typeDTO.getTypeVariableParam();
                dependencies.add(param.genericDeclarationRef());
            } else if (category == TypeCategory.ARRAY) {
                var param = typeDTO.getArrayTypeParam();
                dependencies.add(param.elementTypeRef());
            } else if (category == TypeCategory.UNION) {
                var param = typeDTO.getUnionParam();
                dependencies.addAll(NncUtils.map(param.members(), BaseDTO::getRef));
            }
        }

        private void getDependencies(FlowDTO flowDTO, Set<RefDTO> dependencies) {
            dependencies.add(flowDTO.declaringTypeRef());
            dependencies.add(flowDTO.templateRef());
        }

    }

    private void extractTypeDTOsToSave0(List<?> objects, Set<Object> visited, List<TypeDTO> result) {
        List<Object> cascade = new ArrayList<>();
        for (Object object : objects) {
            if (visited.contains(object)) {
                continue;
            }
            visited.add(object);
            if (object instanceof TypeDTO typeDTO) {
                if (typeDTO.id() == null || typeDTO.id() > IdConstants.SYSTEM_RESERVE_PER_REGION) {
                    result.add(typeDTO);
                    cascade.add(typeDTO);
                }
            } else {
                cascade.addAll(getReferences(object));
            }
        }
        if (!cascade.isEmpty()) {
            extractTypeDTOsToSave0(cascade, visited, result);
        }
    }

    private Collection<?> getReferences(Object object) {
        if (object instanceof Collection<?> coll) {
            return coll;
        } else if (object instanceof Map<?, ?> map) {
            return map.values();
        } else {
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
        return TypeUtil.saveClasType(classDTO, withContent, context);
    }

    public ClassType updateType(TypeDTO typeDTO, ClassType type, IEntityContext context) {
        NncUtils.requireNonNull(typeDTO.name(), "类型名称不能为空");
        if (!type.getName().equals(typeDTO.name())) {
            ensureTypeNameAvailable(typeDTO, context);
        }
        if(typeDTO.code() != null) {
            type.setCode(typeDTO.code());
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
            UnionType nullableType = new UnionType(null, Set.of(type, context.getType(Null.class)));
            TypeUtil.getArrayType(nullableType);
            type.setNullableType(nullableType);
            context.bind(nullableType);
        }
        ArrayType arrayType = TypeUtil.getArrayType(type);
        if (!context.containsModel(arrayType)) {
            context.bind(arrayType);
        }
        if (arrayType.getNullableType() == null) {
            arrayType.setNullableType(new UnionType(null, Set.of(arrayType, context.getType(Null.class))));
            context.bind(arrayType.getNullableType());
        }
    }

}
