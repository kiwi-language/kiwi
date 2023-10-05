package tech.metavm.flow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.Page;
import tech.metavm.dto.RefDTO;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.*;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.rest.ExpressionFieldValueDTO;
import tech.metavm.object.instance.rest.FieldValueDTO;
import tech.metavm.object.instance.rest.PrimitiveFieldValueDTO;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.rest.dto.ClassParamDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

@Component
public class FlowManager {

    private final InstanceContextFactory contextFactory;

    private TypeManager typeManager;

    private final EntityQueryService entityQueryService;

    public FlowManager(InstanceContextFactory contextFactory, EntityQueryService entityQueryService) {
        this.contextFactory = contextFactory;
        this.entityQueryService = entityQueryService;
    }

    public FlowDTO get(long flowId) {
        IEntityContext context = newContext();
        Flow flow = context.getEntity(Flow.class, flowId);
        if (flow == null) {
            return null;
        }
        return flow.toDTO(true);
    }

    public Flow saveDeclaration(FlowDTO flowDTO, ClassType declaringType, IEntityContext context) {
        return save(flowDTO, declaringType, true, context);
    }

//    public Flow save(FlowDTO flowDTO, ClassType declaringType, boolean declarationOnly, IEntityContext context) {
//        if (flowDTO.id() == null) {
//            return create(flowDTO, declaringType, declarationOnly, context);
//        } else {
//            return update(flowDTO, context);
//        }
//    }

    @Transactional
    public List<NodeDTO> createTryNode(NodeDTO nodeDTO) {
        IEntityContext context = newContext();
        var scope = context.getScope(nodeDTO.scopeId());
        TryNode tryNode = (TryNode) createNode(nodeDTO, scope, context);
        TryEndNode tryEndNode = new TryEndNode(null, tryNode.getName() + "结束",
                ClassBuilder.newBuilder("守护结束节点输出", "TryEndNodeOutput").temporary().build(),
                tryNode, scope);
        FieldBuilder.newBuilder("异常", "exception",
                        tryEndNode.getType(), TypeUtil.getNullableType(StandardTypes.getThrowableType()))
                .build();
        context.bind(tryEndNode);
        context.finish();
        try (var ignored = SerializeContext.enter()) {
            return List.of(tryNode.toDTO(), tryEndNode.toDTO());
        }
    }

    @Transactional
    public List<NodeDTO> createBranchNode(NodeDTO nodeDTO) {
        IEntityContext context = newContext();
        var scope = context.getScope(nodeDTO.scopeId());
        BranchNode branchNode = (BranchNode) createNode(nodeDTO, scope, context);
        MergeNode mergeNode = new MergeNode(null, branchNode.getName() + "合并",
                branchNode, ClassBuilder.newBuilder("合并节点输出", "MergeNodeOutput").temporary().build(),
                scope);
        context.bind(mergeNode);
        context.finish();
        try (var ignored = SerializeContext.enter()) {
            return List.of(branchNode.toDTO(), mergeNode.toDTO());
        }
    }

    @Transactional
    public long create(FlowDTO flowDTO) {
        IEntityContext context = newContext();
        var flow = save(flowDTO, context.getClassType(flowDTO.declaringTypeRef()), false, context);
        context.finish();
        return flow.getIdRequired();
    }

    public Flow create(FlowDTO flowDTO, ClassType declaringClass, IEntityContext context) {
        return save(flowDTO, declaringClass, false, context);
    }

    public Flow save(FlowDTO flowDTO, ClassType declaringClass, boolean declarationOnly, IEntityContext context) {
        Flow overriden = flowDTO.overridenRef() != null ? context.getFlow(flowDTO.overridenRef()) : null;
        Type outputType = context.getType(flowDTO.returnTypeRef());
        Flow flow = context.getFlow(flowDTO.getRef());
        if (flow == null) {
            flow = FlowBuilder
                    .newBuilder(context.getClassType(flowDTO.declaringTypeRef()), flowDTO.name(), flowDTO.code())
                    .flowDTO(flowDTO)
                    .tmpId(flowDTO.tmpId()).build();
            context.bind(flow);
        }
        flow.setConstructor(flowDTO.isConstructor());
        flow.setAbstract(flowDTO.isAbstract());
        flow.setNative(flowDTO.isNative());
        flow.setOverridden(overriden);
        flow.setTypeArguments(NncUtils.map(flowDTO.typeArgumentRefs(), context::getType));
        flow.setTypeParameters(NncUtils.map(flowDTO.typeParameters(),
                typeParam -> context.getTypeVariable(typeParam.getRef())));
        flow.setReturnType(outputType);
        if (overriden == null && flowDTO.parameters() != null) {
            flow.setParameters(NncUtils.map(
                    flowDTO.parameters(),
                    paramDTO -> new Parameter(
                            paramDTO.tmpId(), paramDTO.name(), paramDTO.code(), context.getType(paramDTO.typeRef())
                    )
            ));
        }
        if (flowDTO.templateInstances() != null) {
            for (FlowDTO templateInstance : flowDTO.templateInstances()) {
                save(templateInstance, declaringClass, declarationOnly, context);
            }
        }
        if (!declarationOnly && !flow.isNative() && !flow.isAbstract()) {
            if (flowDTO.rootScope() == null) {
                NodeRT<?> selfNode = createSelfNode(flow);
                NodeRT<?> inputNode = createInputNode(flow, selfNode);
                createReturnNode(flow, inputNode);
            } else {
                for (NodeDTO nodeDTO : flowDTO.rootScope().nodes()) {
                    createNode(nodeDTO, flow.getRootScope(), context);
                }
            }
        }
        return flow;
    }

    private void initNodes(Flow flow) {
        NodeRT<?> selfNode = createSelfNode(flow);
        NodeRT<?> inputNode = createInputNode(flow, selfNode);
        createReturnNode(flow, inputNode);
    }

    private void saveNodes(FlowDTO flowDTO, Flow flow, IEntityContext context) {
        flow.clearNodes();
        for (NodeDTO nodeDTO : flowDTO.rootScope().nodes()) {
            if (nodeDTO.id() == null) {
                createNode(nodeDTO, flow.getRootScope(), context);
            } else {
                updateNode(nodeDTO, context);
            }
        }
    }

    private void saveNode(NodeDTO nodeDTO, ScopeRT scope, IEntityContext context) {
        if (nodeDTO.id() == null) {
            createNode(nodeDTO, scope, context);
        } else {
            updateNode(nodeDTO, context);
        }
    }

    public void saveContent(FlowDTO flowDTO, Flow flow, ClassType declaringType, IEntityContext context) {
        if (flow.isNative() || flow.isAbstract()) {
            return;
        }
        if (flow.getNodes().isEmpty() && flowDTO.rootScope() == null) {
            initNodes(flow);
        } else {
            saveNodes(flowDTO, flow, context);
        }
        if (flowDTO.templateInstances() != null) {
            for (FlowDTO templateInstance : flowDTO.templateInstances()) {
                saveContent(templateInstance, context.getFlow(templateInstance.getRef()), declaringType, context);
            }
        }
    }

    private SelfNode createSelfNode(Flow flow) {
        NodeDTO selfNodeDTO = NodeDTO.newNode(
                0L,
                "当前记录",
                NodeKind.SELF.code(),
                null
        );
        return new SelfNode(selfNodeDTO.tmpId(), selfNodeDTO.name(), null, flow.getRootScope());
    }

    private NodeRT<?> createInputNode(Flow flow, NodeRT<?> prev) {
        NodeDTO inputNodeDTO = NodeDTO.newNode(
                0L,
                "流程输入",
                NodeKind.INPUT.code(),
                null
        );
        var type = ClassBuilder.newBuilder("输入类型", "InputType").temporary().build();
        for (Parameter parameter : flow.getParameters()) {
            FieldBuilder.newBuilder(parameter.getName(), parameter.getCode(), type, parameter.getType())
                    .build();
        }
        return new InputNode(inputNodeDTO, type, prev, flow.getRootScope());
    }

    private void createReturnNode(Flow flow, NodeRT<?> prev) {
        NodeDTO returnNodeDTO = NodeDTO.newNode(
                0L,
                "结束",
                NodeKind.RETURN.code(),
                null
        );
        new ReturnNode(returnNodeDTO, prev, flow.getRootScope());
    }

    @Transactional
    public void update(FlowDTO flowDTO) {
        flowDTO.requiredId();
        IEntityContext context = newContext();
        update(flowDTO, context);
        context.finish();
    }

    public Flow update(FlowDTO flowDTO, IEntityContext context) {
        Flow flow = context.getFlow(flowDTO.getRef());
        if (flow == null) {
            throw BusinessException.flowNotFound(flowDTO.id());
        }
        if (flowDTO.returnTypeRef() != null) {
            var outputType = context.getType(flowDTO.returnTypeRef());
            flow.setReturnType(outputType);
            var returnNodes = NncUtils.filterByType(flow.getNodes(), ReturnNode.class);
            for (ReturnNode returnNode : returnNodes) {
                returnNode.setOutputType(outputType);
            }
        }
        flow.update(flowDTO);
//        flow.getInputType().setName(getInputTypeName(flow.getName()));
        return flow;
    }

    @Transactional
    public void delete(long id) {
        IEntityContext context = newContext();
        Flow flow = context.getEntity(Flow.class, id);
        delete(flow, context);
        context.finish();
    }

    public void delete(Flow flow, IEntityContext context) {
        context.remove(flow);
    }

    @Transactional
    public NodeDTO createNode(NodeDTO nodeDTO) {
        IEntityContext context = newContext();
        Flow flow = context.getEntity(Flow.class, nodeDTO.flowId());
        if (flow == null) {
            throw BusinessException.flowNotFound(nodeDTO.flowId());
        }
        var node = createNode(nodeDTO, context.getScope(nodeDTO.scopeId()), context);
        context.finish();
        return node.toDTO();
    }

    private NodeRT<?> createNode(NodeDTO nodeDTO, ScopeRT scope, IEntityContext context) {
        nodeDTO = beforeNodeChange(nodeDTO, null, scope, context);
        new FlowAnalyzer().visitFlow(scope.getFlow());
        var node = NodeFactory.create(nodeDTO, scope, context);
        afterNodeChange(nodeDTO, node, context);
        return node;
    }

    private void saveBranchNodeContent(NodeDTO nodeDTO, BranchNode branchNode, IEntityContext context) {
        BranchParamDTO param = nodeDTO.getParam();
        if (param.branches() != null) {
            for (BranchDTO branchDTO : param.branches()) {
                var branch = branchNode.getBranchByIndex(branchDTO.index());
                if (branchDTO.scope() != null && branchDTO.scope().nodes() != null) {
                    Set<Long> nodeIds = new HashSet<>();
                    for (NodeDTO n : branchDTO.scope().nodes()) {
                        saveNode(n, branch.getScope(), context);
                        if (n.id() != null) {
                            nodeIds.add(n.id());
                        }
                    }
                    for (NodeRT<?> node : branch.getScope().getNodes()) {
                        if (node.getId() != null && !nodeIds.contains(node.getId())) {
                            deleteNode(node, context);
                        }
                    }
                }
            }
        }
    }

    public NodeDTO getNode(long id) {
        IEntityContext context = newContext();
        NodeRT<?> node = context.getEntity(NodeRT.class, id);
        return NncUtils.get(node, NodeRT::toDTO);
    }

    @Transactional
    public NodeDTO updateNode(NodeDTO nodeDTO) {
        IEntityContext context = newContext();
        var node = updateNode(nodeDTO, context);
        context.finish();
        return node.toDTO();
    }

    NodeRT<?> updateNode(NodeDTO nodeDTO, IEntityContext context) {
        nodeDTO.ensureIdSet();
        NodeRT<?> node = context.getEntity(NodeRT.class, nodeDTO.id());
        if (node == null) {
            throw BusinessException.nodeNotFound(nodeDTO.id());
        }
        var scope = context.getScope(nodeDTO.scopeId());
        nodeDTO = beforeNodeChange(nodeDTO, node, scope, context);
        node.update(nodeDTO, context);
        afterNodeChange(nodeDTO, node, context);
        return node;
    }

    private NodeDTO beforeNodeChange(NodeDTO nodeDTO, @Nullable NodeRT<?> node, ScopeRT scope, IEntityContext context) {
        if (!FlowSavingContext.skipPreprocessing()) {
            nodeDTO = preprocess(nodeDTO, node, scope, context);
        }
        NodeKind kind = NodeKind.getByCodeRequired(nodeDTO.kind());
        if (kind.isOutputTypeAsChild()) {
            typeManager.saveTypeWithContent(nodeDTO.outputType(), context);
        }
        return nodeDTO;
    }

    private NodeDTO preprocess(NodeDTO nodeDTO, NodeRT<?> node, ScopeRT scope, IEntityContext context) {
        if (nodeDTO.kind() == NodeKind.INPUT.code()) {
            return preprocessInputNode(nodeDTO, (InputNode) node);
        }
        if (nodeDTO.kind() == NodeKind.FOREACH.code()) {
            return preprocessForeachNode(nodeDTO, node, scope, context);
        }
        if (nodeDTO.kind() == NodeKind.BRANCH.code()) {
            return preprocessBranchNode(nodeDTO, node);
        }
        if (nodeDTO.kind() == NodeKind.WHILE.code()) {
            return preprocessWhileNode(nodeDTO, node);
        }
        if (nodeDTO.kind() == NodeKind.MERGE.code()) {
            return preprocessMergeNode(nodeDTO, node);
        }
        if (nodeDTO.kind() == NodeKind.TRY_END.code()) {
            return preprocessTryEndNode(nodeDTO, (TryEndNode) node);
        }
        return nodeDTO;
    }

    private NodeDTO preprocessBranchNode(NodeDTO nodeDTO, NodeRT<?> node) {
        BranchParamDTO param = nodeDTO.getParam();
        if (node == null && NncUtils.isEmpty(param.branches())) {
            List<BranchDTO> branches = List.of(
                    new BranchDTO(
                            null, NncUtils.random(), 1L, null,
                            new ValueDTO(
                                    ValueKind.CONSTANT.code(),
                                    new PrimitiveFieldValueDTO(
                                            null,
                                            true
                                    ),
                                    null
                            ),
                            null, false
                    ),
                    new BranchDTO(
                            null, NncUtils.random(), 10000L, null,
                            new ValueDTO(
                                    ValueKind.CONSTANT.code(),
                                    new PrimitiveFieldValueDTO(
                                            null,
                                            true
                                    ),
                                    null
                            ),
                            null, true
                    )
            );
            return nodeDTO.copyWithParam(new BranchParamDTO(param.inclusive(), branches));
        } else {
            return nodeDTO;
        }
    }

    private NodeDTO preprocessInputNode(NodeDTO nodeDTO, @Nullable InputNode node) {
        InputParamDTO inputParam = nodeDTO.getParam();
        var inputFields = initializeFieldRefs(inputParam.fields());
        List<FieldDTO> fields = NncUtils.map(inputFields, inputField -> convertToFieldDTO(inputField, node));
        TypeDTO typeDTO = TypeDTO.createClass(
                NncUtils.get(node, n -> n.getType().getId()),
                NncUtils.get(nodeDTO.outputTypeRef(), RefDTO::tmpId),
                node != null ? node.getType().getName() : "输入类型" + NncUtils.random(),
                null,
                true,
                true,
                fields,
                List.of(),
                null
        );
        return nodeDTO.copyWithParamAndType(
                new InputParamDTO(NncUtils.get(node, n -> n.getType().getId()), inputFields),
                typeDTO);
    }

    private NodeDTO preprocessMergeNode(NodeDTO nodeDTO, NodeRT<?> node) {
        MergeParamDTO param = nodeDTO.getParam();
        var mergeFields = initializeFieldRefs(param.fields());
        List<FieldDTO> fields = NncUtils.map(mergeFields, MergeFieldDTO::toFieldDTO);
        var outputType = createNodeTypeDTO("MergeOutput",
                NncUtils.get(node, NodeRT::getType), fields);
        return nodeDTO.copyWithParamAndType(new MergeParamDTO(mergeFields), outputType);
    }

    private NodeDTO preprocessTryEndNode(NodeDTO nodeDTO, TryEndNode node) {
        TryEndParamDTO param = nodeDTO.getParam();
        var tryEndFields = initializeFieldRefs(param.fields());
        List<FieldDTO> fieldDTOs = NncUtils.map(tryEndFields, TryEndFieldDTO::toFieldDTO);
        FieldDTO excetpionFieldDTO;
        if (node != null) {
            var outputType = node.getType();
            excetpionFieldDTO = outputType.getFieldByCodeRequired("exception").toDTO();
        } else {
            excetpionFieldDTO = new FieldDTO(
                    null, null, "异常", "exception", Access.GLOBAL.code(),
                    null, false, false, null,
                    StandardTypes.getNullableThrowableType().getRef(), null, false, false
            );
        }
        fieldDTOs = NncUtils.prepend(excetpionFieldDTO, fieldDTOs);
        var outputTypeDTO = createNodeTypeDTO("TryEndOutput",
                NncUtils.get(node, NodeRT::getType), fieldDTOs);
        return nodeDTO.copyWithParamAndType(new TryEndParamDTO(tryEndFields), outputTypeDTO);
    }

    private NodeDTO preprocessWhileNode(NodeDTO nodeDTO, NodeRT<?> node) {
        WhileParamDTO param = nodeDTO.getParam();
        var loopFields = initializeFieldRefs(param.getFields());
        List<FieldDTO> fields = new ArrayList<>(NncUtils.map(loopFields, LoopFieldDTO::toFieldDTO));
        var outputType = createNodeTypeDTO("WhileOutput",
                NncUtils.get(node, NodeRT::getType), fields);
        return nodeDTO.copyWithParamAndType(
                new WhileParamDTO(param.getCondition(), param.getBodyScope(), loopFields),
                outputType);
    }

    private <T extends FieldReferringDTO<T>> List<T> initializeFieldRefs(List<T> fields) {
        if (NncUtils.isEmpty(fields)) {
            return List.of();
        }
        return NncUtils.map(
                fields,
                field -> field.fieldRef() != null && field.fieldRef().isNotEmpty() ?
                        field : field.copyWithFieldRef(RefDTO.ofTmpId(NncUtils.random()))
        );
    }

    private TypeDTO createNodeTypeDTO(String namePrefix, @Nullable Type currentType, List<FieldDTO> fields) {
        Long id = NncUtils.get(currentType, Type::getId);
        String name = NncUtils.get(currentType, Type::getName);
        String code = NncUtils.get(currentType, Type::getCode);
        Long tmpId = NncUtils.get(currentType, Type::getTmpId);
        if (name == null || code == null) {
            name = code = namePrefix + "_" + NncUtils.random();
        }
        if (tmpId == null) {
            tmpId = NncUtils.random();
        }
        return new TypeDTO(
                id,
                tmpId,
                name,
                code,
                TypeCategory.CLASS.code(),
                true,
                true,
                null,
                null,
                new ClassParamDTO(
                        null,
                        null,
                        List.of(),
                        List.of(),
                        ClassSource.RUNTIME.code(),
                        fields,
                        List.of(),
                        List.of(),
                        List.of(),
                        null,
                        null,
                        null,
                        List.of(),
                        List.of(),
                        null,
                        List.of(),
                        List.of()
                )
        );
    }

    private NodeDTO preprocessForeachNode(NodeDTO nodeDTO, @Nullable NodeRT<?> node, ScopeRT scope, IEntityContext context) {
        ForEachParamDTO param = nodeDTO.getParam();
        @Nullable ClassType currentType = (ClassType) NncUtils.get(node, NodeRT::getType);
        var loopFields = initializeFieldRefs(param.getFields());
        List<FieldDTO> fields = new ArrayList<>(NncUtils.map(loopFields, LoopFieldDTO::toFieldDTO));
        NodeRT<?> prev = NncUtils.get(nodeDTO.prevRef(), context::getNode);
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var arrayValue = ValueFactory.create(param.getArray(), parsingContext);
        Field arrayField, indexField;
        if (currentType != null && (arrayField = currentType.getFieldByCode("array")) != null) {
            fields.add(arrayField.toDTO());
        } else {
            fields.add(new FieldDTO(
                    NncUtils.random(),
                    null,
                    "数组",
                    "array",
                    Access.GLOBAL.code(),
                    null,
                    false,
                    false,
                    null,
                    arrayValue.getType().getRef(),
                    null,
                    false,
                    false
            ));
        }
        if (currentType != null && (indexField = currentType.getFieldByCode("index")) != null) {
            fields.add(indexField.toDTO());
        } else {
            fields.add(new FieldDTO(
                    NncUtils.random(),
                    null,
                    "索引",
                    "index",
                    Access.GLOBAL.code(),
                    null,
                    false,
                    false,
                    null,
                    ModelDefRegistry.getType(Long.class).getRef(),
                    null,
                    false,
                    false
            ));
        }
        TypeDTO type = createNodeTypeDTO("ForeachOutput", currentType, fields);
        ScopeDTO loopScope;
        if (param.getBodyScope() == null) {
            var elementType = ((ArrayType) context.getType(arrayValue.getType().getRef())).getElementType();
            NodeDTO elementNode = new NodeDTO(
                    NncUtils.random(),
                    null,
                    null,
                    "element" + NncUtils.random(),
                    NodeKind.VALUE.code(),
                    null,
                    elementType.getRef(),
                    new ValueParamDTO(
                            new ValueDTO(
                                    ValueKind.EXPRESSION.code(),
                                    new ExpressionFieldValueDTO(
                                            nodeDTO.name() + ".array[" + nodeDTO.name() + ".index]"
                                    ),
                                    null
                            )
                    ),
                    null,
                    null
            );
            loopScope = new ScopeDTO(NncUtils.random(), null, List.of(elementNode));
        } else {
            loopScope = param.getBodyScope();
        }

        return nodeDTO.copyWithParamAndType(
                new ForEachParamDTO(param.getArray(), param.getCondition(), loopFields, loopScope),
                type
        );
    }

    private void afterNodeChange(NodeDTO nodeDTO, NodeRT<?> node, IEntityContext context) {
        if(node instanceof InputNode inputNode) {
            updateFlowParameters(inputNode);
        }
        if (node instanceof BranchNode branchNode) {
            saveBranchNodeContent(nodeDTO, branchNode, context);
        }
        if (node instanceof ScopeNode<?> scopeNode) {
            saveScopeNodeContent(nodeDTO, scopeNode, context);
        }
        if (node instanceof LoopNode<?> loopNode) {
            updateLoopFields(nodeDTO, loopNode, context);
        }
    }

    private void updateFlowParameters(InputNode inputNode) {
        var flow = inputNode.getFlow();
        List<Parameter> parameters = new ArrayList<>();
        for (var field : inputNode.getType().getFields()) {
            var parameter = flow.getParameterByName(field.getName());
            if(parameter == null) {
                parameters.add(new Parameter(null, field.getName(), field.getCode(), field.getType()));
            }
            else {
                parameters.add(parameter);
                parameter.setName(field.getName());
                parameter.setType(field.getType());
            }
        }
        flow.setParameters(parameters);
    }

    private void saveScopeNodeContent(NodeDTO nodeDTO, ScopeNode<?> tryNode, IEntityContext context) {
        ScopeNodeParamDTO param = nodeDTO.getParam();
        if (param.getBodyScope() != null && param.getBodyScope().nodes() != null) {
            Set<RefDTO> refs = new HashSet<>();
            for (NodeDTO node : param.getBodyScope().nodes()) {
                if (!node.getRef().isEmpty()) {
                    refs.add(node.getRef());
                }
            }
            List<NodeRT<?>> toRemove = new ArrayList<>();
            for (NodeRT<?> node : tryNode.getBodyScope().getNodes()) {
                if (!refs.contains(node.getRef())) {
                    toRemove.add(node);
                }
            }
            for (NodeRT<?> node : toRemove) {
                tryNode.getBodyScope().removeNode(node);
            }
            for (NodeDTO node : param.getBodyScope().nodes()) {
                saveNode(node, tryNode.getBodyScope(), context);
            }
        }
    }

    private void createTryEndNode(TryNode tryNode, IEntityContext context) {
        var tryEndNode = new TryEndNode(
                null, "守护结束节点",
                ClassBuilder.newBuilder("守护结束节点类型", "TryEndNodeOutput")
                        .temporary().build(),
                tryNode, tryNode.getScope()
        );
        context.bind(tryEndNode);
    }

    private void updateLoopFields(NodeDTO nodeDTO, LoopNode<?> loopNode, IEntityContext context) {
        LoopParamDTO param = nodeDTO.getParam();
        loopNode.updateFields(param.getFields(), context);
    }

    private FieldDTO convertToFieldDTO(InputFieldDTO inputFieldDTO, @Nullable InputNode node) {
        return convertToFieldDTO(
                inputFieldDTO.fieldRef(),
                node != null ? node.getType().getId() : null,
                inputFieldDTO.name(),
                inputFieldDTO.defaultValue(),
                inputFieldDTO.typeRef()
        );
    }

    private FieldDTO convertToFieldDTO(OutputFieldDTO inputFieldDTO, Flow flow) {
        return convertToFieldDTO(
                inputFieldDTO.fieldRef(),
                flow.getReturnType().getId(),
                inputFieldDTO.name(),
                null,
                inputFieldDTO.typeRef()
        );
    }

    private FieldDTO convertToFieldDTO(RefDTO ref, Long declaringTypeId, String name, FieldValueDTO defaultValue, RefDTO typeRef) {
        return new FieldDTO(
                ref.tmpId(),
                ref.id(),
                name,
                null,
                Access.GLOBAL.code(),
                defaultValue,
                false,
                false,
                declaringTypeId,
                typeRef,
                null,
                false,
                false
        );
    }

    @Transactional
    public void deleteNode(long nodeId) {
        IEntityContext context = newContext();
        NodeRT<?> node = context.getEntity(NodeRT.class, nodeId);
        if (node == null) {
            return;
        }
        deleteNode(node, context);
        context.finish();
    }

    private void deleteNode(NodeRT<?> node, IEntityContext context) {
        context.remove(node);
    }

    private TypeDTO getInputTypeDTO(FlowDTO flowDTO) {
        var inputNode = extractNodeDTO(flowDTO.rootScope(),
                nodeDTO -> nodeDTO.kind() == NodeKind.INPUT.code());
        return NncUtils.get(inputNode, NodeDTO::outputType);
    }

    public TypeDTO getOutputTypeDTO(FlowDTO flowDTO) {
        var returnNode = extractNodeDTO(flowDTO.rootScope(),
                nodeDTO -> nodeDTO.kind() == NodeKind.RETURN.code() && nodeDTO.outputType() != null);
        return NncUtils.get(returnNode, NodeDTO::outputType);
    }

    private NodeDTO extractNodeDTO(ScopeDTO scopeDTO, Predicate<NodeDTO> filter) {
        if (scopeDTO == null || NncUtils.isEmpty(scopeDTO.nodes())) return null;
        for (NodeDTO node : scopeDTO.nodes()) {
            if (filter.test(node)) {
                return node;
            }
            if (node.kind() == NodeKind.BRANCH.code()) {
                BranchParamDTO branchParam = node.getParam();
                for (BranchDTO branch : branchParam.branches()) {
                    var innerNode = extractNodeDTO(branch.scope(), filter);
                    if (innerNode != null) return innerNode;
                }
            }
        }
        return null;
    }

    private ClassType saveInputType(@SuppressWarnings("SameParameterValue") @Nullable ClassType type, List<FieldDTO> fieldDTOs, String typeName, IEntityContext context) {
        TypeDTO typeDTO = TypeDTO.createClass(
                NncUtils.get(type, ClassType::getId),
                NncUtils.random(),
                typeName,
                null,
                true,
                true,
                fieldDTOs,
                List.of(),
                null
        );
        if (type != null) {
            typeManager.saveTypeWithContent(typeDTO, type, context);
            return type;
        } else {
            return typeManager.saveTypeWithContent(typeDTO, context);
        }
    }

    private ClassType saveOutputType(@SuppressWarnings("SameParameterValue") @Nullable ClassType type, List<FieldDTO> fieldDTOs, String flowName, IEntityContext context) {
        TypeDTO typeDTO = TypeDTO.createClass(
                NncUtils.get(type, ClassType::getId),
                NncUtils.random(),
                getOutputTypeName(flowName),
                null,
                true,
                true,
                fieldDTOs,
                List.of(),
                "流程输出"
        );
        if (type != null) {
            typeManager.saveTypeWithContent(typeDTO, type, context);
            return type;
        } else {
            return typeManager.saveTypeWithContent(typeDTO, context);
        }
    }

    private String getInputTypeName(String ignored) {
        return "流程输入" + NncUtils.random();
    }

    private String getOutputTypeName(String ignored) {
        return "流程输出" + NncUtils.random();
    }

    public Page<FlowSummaryDTO> list(long typeId, int page, int pageSize, String searchText) {
        IEntityContext context = newContext();
        ClassType type = context.getClassType(typeId);
        EntityQuery<Flow> query = EntityQuery.create(
                Flow.class,
                searchText,
                page,
                pageSize,
                List.of(
                        new EntityQueryField("declaringType", type)
                )
        );
        Page<Flow> flowPage = entityQueryService.query(query, context);
        return new Page<>(
                NncUtils.map(flowPage.data(), Flow::toSummaryDTO),
                flowPage.total()
        );
    }

    @Transactional
    public BranchDTO createBranch(BranchDTO branchDTO) {
        IEntityContext context = newContext();
        NodeRT<?> nodeRT = context.getEntity(NodeRT.class, branchDTO.ownerId());
        if (nodeRT instanceof BranchNode branchNode) {
            Branch branch = branchNode.addBranch(branchDTO, context);
            context.finish();
            return branch.toDTO(true, false);
        } else {
            throw BusinessException.invalidParams("节点" + branchDTO.ownerId() + "不是分支节点");
        }
    }

    @Transactional
    public BranchDTO updateBranch(BranchDTO branchDTO) {
        NncUtils.requireNonNull(branchDTO.index(), "分支序号必填");
        IEntityContext context = newContext();
        NodeRT<?> owner = context.getEntity(NodeRT.class, branchDTO.ownerId());
        if (owner == null) {
            throw BusinessException.nodeNotFound(branchDTO.ownerId());
        }
        if (owner instanceof BranchNode branchNode) {
            Branch branch = branchNode.getBranchByIndex(branchDTO.index());
            if (branch == null) {
                throw BusinessException.branchNotFound(branchDTO.index());
            }
            branch.update(branchDTO, context);
            context.finish();
            return branch.toDTO(true, false);
        } else {
            throw BusinessException.invalidParams("节点" + branchDTO.ownerId() + "不是分支节点");
        }
    }

    @Transactional
    public void deleteBranch(long ownerId, long branchId) {
        IEntityContext context = newContext();
        NodeRT<?> owner = context.getEntity(NodeRT.class, ownerId);
        if (owner instanceof BranchNode branchNode) {
            Branch branch = branchNode.getBranchByIndex(branchId);
            if (branch == null) {
                throw BusinessException.branchNotFound(branchId);
            }
            context.remove(branch);
            context.finish();
        } else {
            throw BusinessException.invalidParams("节点" + ownerId + "不是分支节点");
        }
    }

    private IEntityContext newContext() {
        return contextFactory.newContext().getEntityContext();
    }

    @Autowired
    public void setTypeManager(TypeManager typeManager) {
        this.typeManager = typeManager;
    }
}
