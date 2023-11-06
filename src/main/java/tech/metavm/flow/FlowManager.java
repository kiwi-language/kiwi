package tech.metavm.flow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.dto.ErrorCode;
import tech.metavm.dto.Page;
import tech.metavm.dto.RefDTO;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.entity.SerializeContext;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.NodeExpression;
import tech.metavm.flow.rest.*;
import tech.metavm.object.meta.ArrayType;
import tech.metavm.object.instance.rest.ExpressionFieldValueDTO;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.rest.dto.ClassTypeParam;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FlowManager {

    private final InstanceContextFactory contextFactory;

    private TypeManager typeManager;

    public FlowManager(InstanceContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    public GetFlowResponse get(GetFlowRequest request) {
        try(var context = newContext()) {
            Flow flow = context.getEntity(Flow.class, request.id());
            if (flow == null) {
                return null;
            }
            try (var serContext = SerializeContext.enter()) {
                serContext.setIncludingNodeOutputType(true);
                var flowDTO = flow.toDTO(request.includeNodes());
                serContext.writeDependencies();
                return new GetFlowResponse(flowDTO, serContext.getTypes());
            }
        }
    }

    public Flow saveDeclaration(FlowDTO flowDTO, IEntityContext context) {
        return save(flowDTO, true, context);
    }

    @Transactional
    public List<NodeDTO> createTryNode(NodeDTO nodeDTO) {
        try(var context = newContext(true)) {
            var scope = context.getScope(nodeDTO.scopeId());
            TryNode tryNode = (TryNode) createNode(nodeDTO, scope, context);
            TryEndNode tryEndNode = new TryEndNode(null, tryNode.getName() + "结束",
                    ClassBuilder.newBuilder("守护结束节点输出", "TryEndNodeOutput").temporary().build(),
                    tryNode, scope);
            FieldBuilder.newBuilder("异常", "exception",
                            tryEndNode.getType(), context.getNullableType(StandardTypes.getThrowableType()))
                    .build();
            context.bind(tryEndNode);
            retransformFlowIfRequired(scope.getFlow(), context);
            context.finish();
            try (var ignored = SerializeContext.enter()) {
                return List.of(tryNode.toDTO(), tryEndNode.toDTO());
            }
        }
    }

    @Transactional
    public List<NodeDTO> createBranchNode(NodeDTO nodeDTO) {
        try(var context = newContext(true)) {
            var scope = context.getScope(nodeDTO.scopeId());
            BranchNode branchNode = (BranchNode) createNode(nodeDTO, scope, context);
            MergeNode mergeNode = new MergeNode(null, branchNode.getName() + "合并",
                    branchNode, ClassBuilder.newBuilder("合并节点输出", "MergeNodeOutput").temporary().build(),
                    scope);
            context.bind(mergeNode);
            retransformFlowIfRequired(branchNode.getFlow(), context);
            context.finish();
            try (var ignored = SerializeContext.enter()) {
                return List.of(branchNode.toDTO(), mergeNode.toDTO());
            }
        }
    }

    private Type getReturnType(FlowDTO flowDTO, ClassType declaringType, IEntityContext context) {
        if (flowDTO.isConstructor()) {
            return declaringType.isTemplate() ?
                    context.getParameterizedType(declaringType, declaringType.getTypeParameters()) : declaringType;
        } else {
            return context.getType(NncUtils.requireNonNull(flowDTO.returnTypeRef()));
        }
    }

    public Flow save(FlowDTO flowDTO) {
        try(var context = newContext()) {
            var flow = save(flowDTO, context);
            context.finish();
            return flow;
        }
    }

    public Flow save(FlowDTO flowDTO, IEntityContext context) {
        return save(flowDTO, false, context);
    }

    public Flow save(FlowDTO flowDTO, boolean declarationOnly, IEntityContext context) {
        List<Flow> overriden = NncUtils.map(flowDTO.overriddenRefs(), context::getFlow);
        Flow flow = context.getFlow(flowDTO.getRef());
        ClassType declaringType = flow != null ? flow.getDeclaringType() :
                context.getClassType(flowDTO.declaringTypeRef());
        boolean creating = flow == null;
        if (flow == null) {
            flow = FlowBuilder
                    .newBuilder(context.getClassType(flowDTO.declaringTypeRef()), flowDTO.name(),
                            flowDTO.code(), context.getFunctionTypeContext())
                    .flowDTO(flowDTO)
                    .tmpId(flowDTO.tmpId()).build();
            context.bind(flow);
        }
        Type oldFuncType = flow.getType();
        Type returnType = getReturnType(flowDTO, declaringType, context);
        flow.setConstructor(flowDTO.isConstructor());
        flow.setOverridden(overriden);
        flow.setAbstract(declaringType.isInterface() || flowDTO.isAbstract());
        flow.setNative(flowDTO.isNative());
        flow.setTypeArguments(NncUtils.map(flowDTO.typeArgumentRefs(), context::getType));
        flow.setTypeParameters(NncUtils.map(flowDTO.typeParameterRefs(), context::getTypeVariable));
        flow.setReturnType(returnType);
        flow.setType(context.getFunctionType(flow.getParameterTypes(), returnType));
        flow.setStaticType(context.getFunctionType(NncUtils.prepend(declaringType, flow.getParameterTypes()), returnType));
        flow.setParameters(NncUtils.map(flowDTO.parameters(), paramDTO -> saveParameter(paramDTO, context)));
        if (flowDTO.templateInstances() != null) {
            for (FlowDTO templateInstance : flowDTO.templateInstances()) {
                save(templateInstance, declarationOnly, context);
            }
        }
        if (!declarationOnly) {
            saveContent(flowDTO, flow, context);
        }
        retransformFlowIfRequired(flow, context);
        if (flow.isAbstract()) {
            if (creating) {
                createOverrideFlows(flow, context);
            } else if (oldFuncType != flow.getType()) {
                recreateOverrideFlows(flow, context);
            }
        }
        return flow;
    }

    public Parameter saveParameter(ParameterDTO parameterDTO, IEntityContext context) {
        var parameter = context.getEntity(Parameter.class, parameterDTO.getRef());
        if (parameter != null) {
            parameter.setName(parameterDTO.name());
            parameter.setCode(parameterDTO.code());
            parameter.setType(context.getType(parameterDTO.typeRef()));
            return parameter;
        } else {
            return new Parameter(
                    parameterDTO.tmpId(),
                    parameterDTO.name(),
                    parameterDTO.code(),
                    context.getType(parameterDTO.typeRef()),
                    null
            );
        }
    }

    public void createOverrideFlows(Flow overriden, IEntityContext context) {
        NncUtils.requireTrue(overriden.isAbstract());
        for (ClassType subType : overriden.getDeclaringType().getSubTypes()) {
            createOverrideFlows(overriden, subType, context);
        }
    }

    public void recreateOverrideFlows(Flow flow, IEntityContext context) {
        detachOverrideFlows(flow);
        createOverrideFlows(flow, context);
    }

    public void createOverrideFlows(Flow overriden, ClassType type, IEntityContext context) {
        NncUtils.requireTrue(overriden.isAbstract());
        if (type.isAbstract()) {
            for (ClassType subType : type.getSubTypes()) {
                createOverrideFlows(overriden, subType, context);
            }
        } else {
            var flow = type.resolveFlow(overriden);
            if (flow == null) {
                var candidate = NncUtils.find(
                        type.getFlows(),
                        f -> f.getParameterTypes().equals(overriden.getParameterTypes())
                                && overriden.getReturnType().isAssignableFrom(f.getReturnType())
                );
                if (candidate != null) {
                    candidate.addOverriden(overriden);
                } else {
                    flow = FlowBuilder.newBuilder(type, overriden.getName(), overriden.getCode(), context.getFunctionTypeContext())
                            .returnType(overriden.getReturnType())
                            .type(overriden.getType())
                            .staticType(overriden.getStaticType())
                            .overriden(List.of(overriden))
                            .parameters(NncUtils.map(overriden.getParameters(), Parameter::copy))
                            .typeParameters(NncUtils.map(overriden.getTypeParameters(), TypeVariable::copy))
                            .build();
                    initNodes(flow, context);
                }
            }
        }
    }

    public void initNodes(Flow flow, IEntityContext context) {
        NodeRT<?> selfNode = createSelfNode(flow, context);
        NodeRT<?> inputNode = createInputNode(flow, selfNode);
        createReturnNode(flow, inputNode);
    }

    private void saveNodes(FlowDTO flowDTO, Flow flow, IEntityContext context) {
        if (flowDTO.rootScope() == null) {
            return;
        }
        flow.clearNodes();
        for (NodeDTO nodeDTO : flowDTO.rootScope().nodes()) {
            saveNode(nodeDTO, flow.getRootScope(), context);
        }
    }

    private NodeRT<?> saveNode(NodeDTO nodeDTO, ScopeRT scope, IEntityContext context) {
        if (nodeDTO.id() == null) {
            return createNode(nodeDTO, scope, context);
        } else {
            return updateNode(nodeDTO, context);
        }
    }

    private void retransformFlowIfRequired(Flow flow, IEntityContext context) {
        if (flow.getDeclaringType().isTemplate() && context.isPersisted(flow.getDeclaringType())) {
            new FlowAnalyzer().visitFlow(flow);
            var templateInstances = context.getTemplateInstances(flow.getDeclaringType());
            for (ClassType templateInstance : templateInstances) {
                context.getGenericContext().retransformFlow(flow, templateInstance);
            }
        }
    }

    private void removeTransformedFlowsIfRequired(Flow flow, IEntityContext context) {
        if (flow.getDeclaringType().isTemplate() && context.isPersisted(flow.getDeclaringType())) {
            var templateInstances = context.getTemplateInstances(flow.getDeclaringType());
            for (ClassType templateInstance : templateInstances) {
                var flowTi = templateInstance.getFlowByTemplate(flow);
                templateInstance.removeFlow(flowTi);
                context.remove(flowTi);
            }
        }
    }

    public void saveContent(FlowDTO flowDTO, Flow flow, IEntityContext context) {
        if (flow.isNative() || flow.isAbstract()) {
            return;
        }
        if (flow.getNodes().isEmpty() && flowDTO.rootScope() == null) {
            if (context.isNewEntity(flow)) {
                initNodes(flow, context);
            }
        } else {
            saveNodes(flowDTO, flow, context);
        }
        if (flowDTO.templateInstances() != null) {
            for (FlowDTO templateInstance : flowDTO.templateInstances()) {
                saveContent(templateInstance, context.getFlow(templateInstance.getRef()), context);
            }
        }
    }

    private SelfNode createSelfNode(Flow flow, IEntityContext context) {
        NodeDTO selfNodeDTO = NodeDTO.newNode(
                0L,
                "当前记录",
                NodeKind.SELF.code(),
                null
        );
        return new SelfNode(selfNodeDTO.tmpId(), selfNodeDTO.name(),
                SelfNode.getSelfType(flow, context), null, flow.getRootScope());
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
        var node = new ReturnNode(null, "结束", prev, flow.getRootScope());
        if (flow.isConstructor()) {
            NncUtils.requireTrue(flow.getRootNode() instanceof SelfNode);
            node.setValue(new ReferenceValue(new NodeExpression(flow.getRootNode())));
        }
    }

    @Transactional
    public void delete(long id) {
        try(var context = newContext()) {
            Flow flow = context.getEntity(Flow.class, id);
            delete(flow, context);
            context.finish();
        }
    }

    public void delete(Flow flow, IEntityContext context) {
        removeTransformedFlowsIfRequired(flow, context);
        for (ClassType subType : flow.getDeclaringType().getSubTypes()) {
            detachOverrideFlows(flow, subType);
        }
        context.remove(flow);
    }

    private void detachOverrideFlows(Flow flow) {
        for (ClassType subType : flow.getDeclaringType().getSubTypes()) {
            detachOverrideFlows(flow, subType);
        }
    }

    private void detachOverrideFlows(Flow flow, ClassType type) {
        var override = type.resolveFlow(flow);
        if (override != null) {
            override.removeOverriden(flow);
            override.addOverriden(flow.getOverridden());
        } else {
            for (ClassType subType : type.getSubTypes()) {
                detachOverrideFlows(flow, subType);
            }
        }
    }

    @Transactional
    public NodeDTO createNode(NodeDTO nodeDTO) {
        try(var context = newContext(true)) {
            Flow flow = context.getEntity(Flow.class, nodeDTO.flowId());
            if (flow == null) {
                throw BusinessException.flowNotFound(nodeDTO.flowId());
            }
            var node = createNode(nodeDTO, context.getScope(nodeDTO.scopeId()), context);
            context.finish();
            return node.toDTO();
        }
    }

    private NodeRT<?> createNode(NodeDTO nodeDTO, ScopeRT scope, IEntityContext context) {
        nodeDTO = beforeNodeChange(nodeDTO, null, scope, context);
        new FlowAnalyzer().visitFlow(scope.getFlow());
        var node = NodeFactory.create(nodeDTO, scope, context);
        afterNodeChange(nodeDTO, node, context);
        retransformFlowIfRequired(scope.getFlow(), context);
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

    private void saveAddObjectChildren(NodeDTO nodeDTO, AddObjectNode node, IEntityContext context) {
        AddObjectParam param = nodeDTO.getParam();
        if(param.getBodyScope() != null) {
            for (NodeDTO childNodeDTO : param.getBodyScope().nodes()) {
                NodeKind kind = NodeKind.getByCodeRequired(childNodeDTO.kind());
                NncUtils.assertTrue(NodeKind.CREATING_KINDS.contains(kind),
                    ErrorCode.INVALID_ADD_OBJECT_CHILD, childNodeDTO.name());
            }
            var childrenScope = node.getScope();
            childrenScope.clearNodes();
            NncUtils.map(param.getBodyScope().nodes(),
                    childNodeDTO -> saveNode(childNodeDTO, childrenScope, context));
        }
    }

    public NodeDTO getNode(long id) {
        try(var context = newContext()) {
            NodeRT<?> node = context.getEntity(NodeRT.class, id);
            return NncUtils.get(node, NodeRT::toDTO);
        }
    }

    @Transactional
    public NodeDTO updateNode(NodeDTO nodeDTO) {
        try(var context = newContext(true)) {
            var node = updateNode(nodeDTO, context);
            context.finish();
            return node.toDTO();
        }
    }

    NodeRT<?> updateNode(NodeDTO nodeDTO, IEntityContext context) {
        nodeDTO.ensureIdSet();
        NodeRT<?> node = context.getEntity(NodeRT.class, nodeDTO.id());
        if (node == null) {
            throw BusinessException.nodeNotFound(nodeDTO.id());
        }
        var scope = context.getScope(nodeDTO.scopeId());
        new FlowAnalyzer().visitFlow(scope.getFlow());
        nodeDTO = beforeNodeChange(nodeDTO, node, scope, context);
        node.update(nodeDTO, context);
        afterNodeChange(nodeDTO, node, context);
        retransformFlowIfRequired(scope.getFlow(), context);
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
                                    new PrimitiveFieldValue(
                                            null,
                                            PrimitiveKind.BOOLEAN.getCode(),
                                            true
                                    )
                            ),
                            null, false, false
                    ),
                    new BranchDTO(
                            null, NncUtils.random(), 10000L, null,
                            new ValueDTO(
                                    ValueKind.CONSTANT.code(),
                                    new PrimitiveFieldValue(
                                            null,
                                            PrimitiveKind.BOOLEAN.getCode(),
                                            true
                                    )
                            ),
                            null, true, false
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
                    StandardTypes.getNullableThrowableType().getRef(), false, false
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
                new ClassTypeParam(
                        null,
                        List.of(),
                        ClassSource.RUNTIME.code(),
                        fields,
                        List.of(),
                        List.of(),
                        List.of(),
                        null,
                        null,
                        List.of(),
                        false,
                        List.of(),
                        List.of(),
                        null,
                        List.of(),
                        List.of(),
                        false
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
                                    )
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
        if (node instanceof InputNode inputNode) {
            updateParameters(inputNode, context);
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
        if(node instanceof AddObjectNode addObjectNode) {
            saveAddObjectChildren(nodeDTO, addObjectNode, context);
        }
    }

    private void updateParameters(InputNode inputNode, IEntityContext context) {
        var callable = inputNode.getScope().getOwner() == null ? inputNode.getFlow() :
                (Callable) inputNode.getScope().getOwner();
        List<Parameter> parameters = new ArrayList<>();
        for (var field : inputNode.getType().getFields()) {
            var cond = NncUtils.get(inputNode.getFieldCondition(field), Value::copy);
            var parameter = callable.getParameterByName(field.getName());
            if (parameter == null) {
                parameters.add(
                        new Parameter(null, field.getName(), field.getCode(), field.getType(), cond)
                );
            } else {
                parameters.add(parameter);
                parameter.setName(field.getName());
                parameter.setType(field.getType());
                parameter.setCondition(cond);
            }
        }
        var oldFuncType = callable.getFunctionType();
        callable.setParameters(parameters);
        var funcTypeContext = context.getFunctionTypeContext();
        callable.setFunctionType(funcTypeContext.get(callable.getParameterTypes(), callable.getReturnType()));
        if (callable instanceof Flow flow) {
            flow.setStaticType(funcTypeContext.get(
                    NncUtils.prepend(flow.getDeclaringType(), flow.getParameterTypes()), flow.getReturnType()));
            if (flow.isAbstract() && !oldFuncType.equals(callable.getFunctionType())) {
                recreateOverrideFlows(flow, context);
            }
        }
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

    @Transactional
    public void moveFlow(long id, int ordinal) {
        try(var context = newContext(true)) {
            var flow = context.getFlow(id);
            flow.getDeclaringType().moveFlow(flow, ordinal);
            context.finish();
        }
    }

    private FieldDTO convertToFieldDTO(RefDTO ref, Long declaringTypeId, String name, FieldValue defaultValue, RefDTO typeRef) {
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
                false,
                false
        );
    }

    @Transactional
    public void deleteNode(long nodeId) {
        try(var context = newContext()) {
            NodeRT<?> node = context.getEntity(NodeRT.class, nodeId);
            if (node == null) {
                return;
            }
            deleteNode(node, context);
            context.finish();
        }
    }

    private void deleteNode(NodeRT<?> node, IEntityContext context) {
        context.remove(node);
        retransformFlowIfRequired(node.getFlow(), context);
    }

    public Page<FlowSummaryDTO> list(long typeId, int page, int pageSize, String searchText) {
        try(var context = newContext()) {
            ClassType type = context.getClassType(typeId);
            var flows = type.getAllFlows();
            if (searchText != null) {
                flows = NncUtils.filter(flows, flow -> flow.getName().contains(searchText)
                        || flow.getCode() != null && flow.getCode().contains(searchText));
            }
            int start = Math.min((page - 1) * pageSize, flows.size());
            int end = Math.min(page * pageSize, flows.size());
            List<Flow> data = flows.subList(start, end);
            Page<Flow> flowPage = new Page<>(data, flows.size());
            return new Page<>(
                    NncUtils.map(flowPage.data(), Flow::toSummaryDTO),
                    flowPage.total()
            );
        }
    }

    @Transactional
    public BranchDTO createBranch(BranchDTO branchDTO) {
        try(var context = newContext()) {
            NodeRT<?> nodeRT = context.getEntity(NodeRT.class, branchDTO.ownerId());
            if (nodeRT instanceof BranchNode branchNode) {
                Branch branch = branchNode.addBranch(branchDTO, context);
                retransformFlowIfRequired(branchNode.getFlow(), context);
                context.finish();
                return branch.toDTO(true, false);
            } else {
                throw BusinessException.invalidParams("节点" + branchDTO.ownerId() + "不是分支节点");
            }
        }
    }

    @Transactional
    public BranchDTO updateBranch(BranchDTO branchDTO) {
        NncUtils.requireNonNull(branchDTO.index(), "分支序号必填");
        try(var context = newContext()) {
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
    }

    @Transactional
    public void deleteBranch(long ownerId, long branchId) {
        try(var context = newContext()) {
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
    }

    private IEntityContext newContext() {
        return newContext(false);
    }

    private IEntityContext newContext(boolean asyncPostProcessing) {
        return contextFactory.newEntityContext(asyncPostProcessing);
    }

    @Autowired
    public void setTypeManager(TypeManager typeManager) {
        this.typeManager = typeManager;
    }
}
