package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.common.Page;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.NodeExpression;
import org.metavm.flow.rest.*;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.ClassTypeDTOBuilder;
import org.metavm.object.type.rest.dto.FieldDTO;
import org.metavm.object.type.rest.dto.FieldDTOBuilder;
import org.metavm.object.type.rest.dto.KlassDTO;
import org.metavm.util.BusinessException;
import org.metavm.util.DebugEnv;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class FlowManager extends EntityContextFactoryAware {

    private TypeManager typeManager;

    private final TransactionOperations transactionTemplate;

    public FlowManager(EntityContextFactory entityContextFactory, TransactionOperations transactionTemplate) {
        super(entityContextFactory);
        this.transactionTemplate = transactionTemplate;
    }

    @Transactional(readOnly = true)
    public GetFlowResponse get(GetFlowRequest request) {
        try (var context = newContext()) {
            Flow flow = context.getEntity(Flow.class, request.id());
            if (flow == null) {
                return null;
            }
            return makeFlowResponse(flow, request.includeNodes());
        }
    }

    private GetFlowResponse makeFlowResponse(Flow flow, boolean includeNodes) {
        try (var serContext = SerializeContext.enter()) {
            var flowDTO = flow.toDTO(includeNodes, serContext);
            return new GetFlowResponse(flowDTO, List.of());
        }
    }

    @Transactional
    public List<NodeDTO> createTryNode(NodeDTO nodeDTO) {
        try (var context = newContext(); var serContext = SerializeContext.enter()) {
            var scope = context.getScope(nodeDTO.scopeId());
            TryEnterNode tryEnterNode = (TryEnterNode) createNode(nodeDTO, scope, NodeSavingStage.INIT, context);
            TryExitNode tryExitNode = new TryExitNode(null, tryEnterNode.getName() + " end", null,
                    KlassBuilder.newBuilder("TryEndOutput", "TryEndOutput").temporary().build(),
                    tryEnterNode, scope);
            FieldBuilder.newBuilder("exception", "exception",
                            tryExitNode.getType().resolve(), Types.getNullableThrowableType())
                    .build();
            context.bind(tryExitNode);
            afterFlowChange(scope.getFlow(), context);
            context.finish();
            try (var ignored = SerializeContext.enter()) {
                return List.of(tryEnterNode.toDTO(serContext), tryExitNode.toDTO(serContext));
            }
        }
    }

    private Type getReturnType(FlowDTO flowDTO, Klass declaringType, IEntityContext context) {
        if (flowDTO.isConstructor())
            return declaringType.getType();
        else
            return TypeParser.parseType(Objects.requireNonNull(flowDTO.returnType()), context);
    }

    @Transactional
    public Flow save(FlowDTO flowDTO) {
        try (var context = newContext()) {
            var flow = save(flowDTO, context);
            context.finish();
            return flow;
        }
    }

    public Flow getParameterizedFlow(GetParameterizedFlowRequest request) {
        var templateId = Id.parse(request.templateId());
        var typeArgumentIds = NncUtils.map(request.typeArgumentIds(), Id::parse);
        try (var context = newContext()) {
            var template = context.getFlow(templateId);
            var typeArgs = NncUtils.map(typeArgumentIds, context::getType);
            var existing = template.getExistingParameterized(typeArgs);
            if (existing != null) {
                return existing;
            } else {
                return transactionTemplate.execute(s -> createParameterizedFlow(request));
            }
        }
    }

    private Flow createParameterizedFlow(GetParameterizedFlowRequest request) {
        try (var context = newContext()) {
            var templateId = Id.parse(request.templateId());
            var typeArgIds = NncUtils.map(request.typeArgumentIds(), Id::parse);
            var template = context.getFlow(templateId);
            var typeArgs = NncUtils.map(typeArgIds, context::getType);
            var flow = template.getParameterized(typeArgs);
            context.finish();
            return flow;
        }
    }

    public Flow save(FlowDTO flowDTO, IEntityContext context) {
        return save(flowDTO, false, context);
    }

    public Flow save(FlowDTO flowDTO, boolean declarationOnly, IEntityContext context) {
        var parameters = NncUtils.map(flowDTO.parameters(), paramDTO -> saveParameter(paramDTO, context));
        Flow flow;
        if (flowDTO.param() instanceof MethodParam methodParam) {
            Method method = context.getMethod(flowDTO.id());
            Klass declaringType = method != null ? method.getDeclaringType() :
                    context.getKlass(methodParam.declaringTypeId());
            boolean creating = method == null;
            if (method == null) {
                method = MethodBuilder
                        .newBuilder(context.getKlass(methodParam.declaringTypeId()), flowDTO.name(), flowDTO.code())
                        .flowDTO(flowDTO)
                        .access(Access.getByCode(methodParam.access()))
                        .isStatic(methodParam.isStatic())
                        .tmpId(flowDTO.tmpId()).build();
                context.bind(method);
            }
            Type oldFuncType = method.getType();
            Type returnType = getReturnType(flowDTO, declaringType, context);
            method.setName(flowDTO.name());
            method.setCode(flowDTO.code());
            method.setConstructor(flowDTO.isConstructor());
            method.setAbstract(declaringType.isInterface() || methodParam.isAbstract());
            method.setParameters(parameters);
            method.setReturnType(returnType);
            if (method.isAbstract()) {
                if (creating) {
                    createOverridingFlows(method, context);
                } else if (oldFuncType != method.getType()) {
                    recreateOverridingFlows(method, context);
                }
            }
            flow = method;
        } else if (flowDTO.param() instanceof FunctionParam) {
            var function = context.getFunction(flowDTO.id());
            if (function == null) {
                function = FunctionBuilder.newBuilder(flowDTO.name(), flowDTO.code())
                        .tmpId(flowDTO.tmpId())
                        .build();
                context.bind(function);
            }
            var returnType = TypeParser.parseType(flowDTO.returnType(), context);
            function.setParameters(parameters);
            function.setReturnType(returnType);
            function.setNative(flowDTO.isNative());
            flow = function;
        } else {
            throw new InternalException("Invalid flowDTO, unrecognized param: " + flowDTO.param());
        }
        flow.setNative(flowDTO.isNative());
        flow.setTypeParameters(NncUtils.map(flowDTO.typeParameterIds(), context::getTypeVariable));
        flow.setCapturedTypeVariables(NncUtils.map(flowDTO.capturedTypeIds(), context::getCapturedTypeVariable));
//        if (flowDTO.horizontalInstances() != null) {
//            for (FlowDTO templateInstance : flowDTO.horizontalInstances()) {
//                save(templateInstance, declarationOnly, context);
//            }
//        }
        if (!declarationOnly)
            saveContent(flowDTO, flow, context);
        flow.check();
        return flow;
    }

    public Parameter saveParameter(ParameterDTO parameterDTO, IEntityContext context) {
        var parameter = context.getEntity(Parameter.class, parameterDTO.id());
        if (parameter != null) {
            parameter.setName(parameterDTO.name());
            parameter.setCode(parameterDTO.code());
            parameter.setType(TypeParser.parseType(parameterDTO.type(), context));
            return parameter;
        } else {
            return new Parameter(
                    parameterDTO.tmpId(),
                    parameterDTO.name(),
                    parameterDTO.code(),
                    TypeParser.parseType(parameterDTO.type(), context)
            );
        }
    }

    public void createOverridingFlows(Method overridden, IEntityContext context) {
        NncUtils.requireTrue(overridden.isAbstract());
        for (Klass subType : overridden.getDeclaringType().getSubKlasses()) {
            createOverridingFlows(overridden, subType, context);
        }
    }

    public void recreateOverridingFlows(Method method, IEntityContext context) {
        createOverridingFlows(method, context);
    }

    public void createOverridingFlows(Method overridden, Klass type, IEntityContext context) {
        NncUtils.requireTrue(overridden.isAbstract());
        if (type.isEffectiveAbstract()) {
            for (Klass subType : type.getSubKlasses()) {
                createOverridingFlows(overridden, subType, context);
            }
        } else {
            var flow = type.tryResolveNonParameterizedMethod(overridden);
            if (flow == null) {
                var candidate = NncUtils.find(
                        type.getMethods(),
                        f -> f.getParameterTypes().equals(overridden.getParameterTypes())
                                && overridden.getReturnType().isAssignableFrom(f.getReturnType())
                );
                if (candidate == null) {
                    flow = MethodBuilder.newBuilder(type, overridden.getName(), overridden.getCode())
                            .returnType(overridden.getReturnType())
                            .type(overridden.getType())
                            .access(overridden.getAccess())
                            .parameters(NncUtils.map(overridden.getParameters(), Parameter::copy))
                            .typeParameters(NncUtils.map(overridden.getTypeParameters(), TypeVariable::copy))
                            .build();
                    initNodes(flow, context);
                }
            }
        }
    }

    public void initNodes(Flow flow, IEntityContext context) {
        if (flow instanceof Method method && method.isInstanceMethod())
            createSelfNode(method, context);
        NodeRT inputNode = createInputNode(flow, flow.getRootScope().getLastNode());
        createReturnNode(flow, inputNode);
    }

    private void saveNodes(FlowDTO flowDTO, Flow flow, IEntityContext context) {
        if (flowDTO.rootScope() == null) {
            return;
        }
        flow.clearNodes();
        for (NodeDTO nodeDTO : flowDTO.rootScope().nodes()) {
            saveNode(nodeDTO, flow.getRootScope(), NodeSavingStage.INIT, context);
        }
        for (NodeDTO nodeDTO : flowDTO.rootScope().nodes()) {
            saveNode(nodeDTO, flow.getRootScope(), NodeSavingStage.FINALIZE, context);
        }
    }

    private NodeRT saveNode(NodeDTO nodeDTO, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        if (nodeDTO.id() == null || Id.parse(nodeDTO.id()).tryGetTreeId() == null) {
            return createNode(nodeDTO, scope, stage, context);
        } else {
            return updateNode(nodeDTO, stage, context);
        }
    }

    private void removeTransformedFlowsIfRequired(Flow flow, IEntityContext context) {
        if (flow instanceof Method method) {
            if (method.getDeclaringType().isTemplate() && context.isPersisted(method.getDeclaringType())) {
                var templateInstances = context.getTemplateInstances(method.getDeclaringType());
                for (Klass templateInstance : templateInstances) {
                    var flowTi = templateInstance.findMethodByVerticalTemplate(method);
                    templateInstance.removeMethod(flowTi);
                    context.remove(flowTi);
                }
            }
        }
        context.selectByKey(Flow.IDX_HORIZONTAL_TEMPLATE, flow).forEach(context::remove);
    }

    public void saveContent(FlowDTO flowDTO, Flow flow, IEntityContext context) {
//        log.debug("Saving content of flow: {}", flow.getQualifiedName());
        try (var ignored = context.getProfiler().enter("FlowManager.saveContent")) {
            if (DebugEnv.debugging)
                DebugEnv.logger.info("FlowManager.saveContent flow: {}", flow.getQualifiedName());
            if (flow.isNative() || (flow instanceof Method method && method.isAbstract()))
                return;
            if (flow.getNodes().isEmpty() && flowDTO.rootScope() == null) {
                if (context.isNewEntity(flow))
                    initNodes(flow, context);
            } else
                saveNodes(flowDTO, flow, context);
            afterFlowChange(flow, context);
//            Flows.retransformFlowIfRequired(flow, context);
//        if (flowDTO.horizontalInstances() != null) {
//            for (FlowDTO inst : flowDTO.horizontalInstances()) {
//                saveContent(inst, context.getFlow(inst.getRef()), context);
//            }
//        }
        }
    }

    private SelfNode createSelfNode(Method flow, IEntityContext context) {
        NodeDTO selfNodeDTO = NodeDTO.newNode(
                null,
                "self",
                NodeKind.SELF.code(),
                null
        );
        return new SelfNode(selfNodeDTO.tmpId(), selfNodeDTO.name(), null,
                flow.getDeclaringType().getType(), null, flow.getRootScope());
    }

    private NodeRT createInputNode(Flow flow, NodeRT prev) {
        var type = KlassBuilder.newBuilder("Input", null).temporary().build();
        for (Parameter parameter : flow.getParameters()) {
            FieldBuilder.newBuilder(parameter.getName(), parameter.getCode(), type, parameter.getType())
                    .build();
        }
        return new InputNode(null, "input", null, type, prev, flow.getRootScope());
    }

    private void createReturnNode(Flow flow, NodeRT prev) {
        Value value;
        if (Flows.isConstructor(flow)) {
            NncUtils.requireTrue(flow.getRootNode() instanceof SelfNode);
            value = Values.reference(new NodeExpression(flow.getRootNode()));
        } else
            value = null;
        new ReturnNode(null, "return", null, prev, flow.getRootScope(), value);
    }

    @Transactional
    public void remove(String id) {
        try (var context = newContext()) {
            Flow flow = context.getEntity(Flow.class, Id.parse(id));
            remove(flow, context);
            context.finish();
        }
    }

    public void remove(Flow flow, IEntityContext context) {
        removeTransformedFlowsIfRequired(flow, context);
        if (flow instanceof Method method) {
            method.getDeclaringType().removeMethod(method);
        } else
            context.remove(flow);
    }

    @Transactional
    public NodeDTO saveNode(NodeDTO nodeDTO) {
        if (nodeDTO.id() == null || Id.parse(nodeDTO.id()) instanceof TmpId)
            return createNode(nodeDTO);
        else
            return updateNode(nodeDTO);
    }

    @Transactional
    public NodeDTO createNode(NodeDTO nodeDTO) {
        try (var context = newContext();
             var serContext = SerializeContext.enter()) {
            Flow flow = context.getEntity(Flow.class, Id.parse(nodeDTO.flowId()));
            if (flow == null) {
                throw BusinessException.flowNotFound(nodeDTO.flowId());
            }
            var node = createNode(nodeDTO, context.getScope(Id.parse(nodeDTO.scopeId())), NodeSavingStage.INIT, context);
            afterFlowChange(flow, context);
            context.finish();
            return node.toDTO(serContext);
        }
    }

    private void afterFlowChange(Flow flow, IEntityContext context) {
        try (var ignored1 = context.getProfiler().enter("Flow.check")) {
            flow.analyze();
            flow.check();
        }
    }

    private NodeRT createNode(NodeDTO nodeDTO, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        try (var ignored = context.getProfiler().enter("createNode")) {
            nodeDTO = beforeNodeChange(nodeDTO, null, scope, context);
            try (var ignored1 = context.getProfiler().enter("Flow.analyze", true)) {
                scope.getFlow().analyze();
            }
            var node = NodeFactory.save(nodeDTO, scope, stage, context);
            afterNodeChange(nodeDTO, node, stage, context);
//            Flows.retransformFlowIfRequired(scope.getFlow(), context);
//            try (var ignored1 = context.getProfiler().enter("Flow.check")) {
//                node.getFlow().analyze();
//                node.getFlow().check();
//            }
            return node;
        }
    }

    public NodeDTO getNode(String id) {
        try (var context = newContext(); var serContext = SerializeContext.enter()) {
            NodeRT node = context.getEntity(NodeRT.class, Id.parse(id));
            return NncUtils.get(node, nodeRT -> nodeRT.toDTO(serContext));
        }
    }

    @Transactional
    public NodeDTO updateNode(NodeDTO nodeDTO) {
        try (var context = newContext(); var serContext = SerializeContext.enter()) {
            var node = updateNode(nodeDTO, NodeSavingStage.INIT, context);
            afterFlowChange(node.getFlow(), context);
            context.finish();
            return node.toDTO(serContext);
        }
    }

    NodeRT updateNode(NodeDTO nodeDTO, NodeSavingStage stage, IEntityContext context) {
        nodeDTO.ensureIdSet();
        NodeRT node = context.getEntity(NodeRT.class, Id.parse(nodeDTO.id()));
        if (node == null) {
            throw BusinessException.nodeNotFound(nodeDTO.id());
        }
        var scope = context.getScope(Id.parse(nodeDTO.scopeId()));
        scope.getFlow().analyze();
        nodeDTO = beforeNodeChange(nodeDTO, node, scope, context);
        NodeFactory.save(nodeDTO, scope, stage, context);
        afterNodeChange(nodeDTO, node, stage, context);
//        Flows.retransformFlowIfRequired(scope.getFlow(), context);
//        try (var ignored = context.getProfiler().enter("Flow.check")) {
//            node.getFlow().check();
//        }
        return node;
    }

    private NodeDTO beforeNodeChange(NodeDTO nodeDTO, @Nullable NodeRT node, ScopeRT scope, IEntityContext context) {
        try (var ignored = context.getProfiler().enter("FlowManager.beforeNodeChange")) {
            if (!FlowSavingContext.skipPreprocessing())
                nodeDTO = preprocess(nodeDTO, node, scope, context);
            NodeKind kind = NodeKind.fromCode(nodeDTO.kind());
            if (kind.isOutputTypeAsChild()) {
                typeManager.saveTypeWithContent(nodeDTO.outputKlass(), context);
            }
            return nodeDTO;
        }
    }

    private NodeDTO preprocess(NodeDTO nodeDTO, NodeRT node, ScopeRT scope, IEntityContext context) {
        if (nodeDTO.kind() == NodeKind.INPUT.code())
            return preprocessInputNode(nodeDTO, (InputNode) node);
        if (nodeDTO.kind() == NodeKind.JOIN.code())
            return preprocessJoinNode(nodeDTO, (JoinNode) node);
        if (nodeDTO.kind() == NodeKind.TRY_EXIT.code())
            return preprocessTryEndNode(nodeDTO, (TryExitNode) node);
        return nodeDTO;
    }

    private NodeDTO preprocessInputNode(NodeDTO nodeDTO, @Nullable InputNode node) {
        InputNodeParam inputParam = nodeDTO.getParam();
        var inputFields = initializeFieldRefs(inputParam.fields());
        var currentKlassId = NncUtils.get(node, n -> n.getKlass().getStringId());
        List<FieldDTO> fields = NncUtils.map(inputFields, inputField -> inputField.toFieldDTO(currentKlassId));
        KlassDTO klassDTO = ClassTypeDTOBuilder.newBuilder(
                        NncUtils.getOrElse(node, n -> n.getKlass().getName(), "input" + NncUtils.randomNonNegative())
                )
                .id(currentKlassId)
                .anonymous(true)
                .ephemeral(true)
                .fields(fields)
                .build();
        return nodeDTO.copyWithParamAndType(
                new InputNodeParam(inputFields),
                klassDTO);
    }

    private NodeDTO preprocessJoinNode(NodeDTO nodeDTO, JoinNode node) {
        JoinNodeParam param = nodeDTO.getParam();
        var joinFields = initializeFieldRefs(param.fields());
        List<FieldDTO> fields = NncUtils.map(joinFields, JoinNodeFieldDTO::toFieldDTO);
        var outputType = createNodeTypeDTO("JoinOutput",
                NncUtils.get(node, JoinNode::getKlass), fields);
        return nodeDTO.copyWithParamAndType(new JoinNodeParam(param.sourceIds(), joinFields), outputType);
    }

    private NodeDTO preprocessTryEndNode(NodeDTO nodeDTO, TryExitNode node) {
        TryExitNodeParam param = nodeDTO.getParam();
        var tryEndFields = initializeFieldRefs(param.fields());
        List<FieldDTO> fieldDTOs = NncUtils.map(tryEndFields, TryExitFieldDTO::toFieldDTO);
        FieldDTO excetpionFieldDTO;
        if (node != null) {
            var outputType = node.getType();
            excetpionFieldDTO = outputType.resolve().getFieldByCode("exception").toDTO();
        } else {
            excetpionFieldDTO = FieldDTOBuilder
                    .newBuilder("exception", Types.getNullableThrowableType().toExpression())
                    .readonly(true)
                    .code("exception")
                    .build();
        }
        fieldDTOs = NncUtils.prepend(excetpionFieldDTO, fieldDTOs);
        var outputTypeDTO = createNodeTypeDTO("TryEndOutput",
                NncUtils.get(node, TryExitNode::getKlass), fieldDTOs);
        return nodeDTO.copyWithParamAndType(new TryExitNodeParam(tryEndFields), outputTypeDTO);
    }

    private <T extends FieldReferringDTO<T>> List<T> initializeFieldRefs(List<T> fields) {
        if (NncUtils.isEmpty(fields)) {
            return List.of();
        }
        return NncUtils.map(
                fields,
                field -> field.fieldId() != null ?
                        field : field.copyWithFieldId(TmpId.of(NncUtils.randomNonNegative()).toString())
        );
    }

    private KlassDTO createNodeTypeDTO(String namePrefix, @Nullable Klass currentType, List<FieldDTO> fields) {
        var id = NncUtils.get(currentType, Klass::getStringId);
        String name = NncUtils.get(currentType, Klass::getName);
//        String code = NncUtils.get(currentType, Type::getCode);
        Long tmpId = NncUtils.get(currentType, Klass::getTmpId);
        if (name == null)
            name = namePrefix + "_" + NncUtils.randomNonNegative();
        if (tmpId == null)
            tmpId = NncUtils.randomNonNegative();
        return ClassTypeDTOBuilder.newBuilder(name)
                .id(id)
                .tmpId(tmpId)
                .kind(ClassKind.CLASS.code())
                .ephemeral(true)
                .anonymous(true)
                .fields(fields)
                .build();
    }

    @Transactional
    public GetFlowResponse check(String id) {
        try (var context = newContext()) {
            var flow = context.getFlow(Id.parse(id));
            flow.check();
            context.finish();
            return makeFlowResponse(flow, true);
        }
    }

    private void afterNodeChange(NodeDTO nodeDTO, NodeRT node, NodeSavingStage stage, IEntityContext context) {
        try (var ignored = context.getProfiler().enter("afterNodeChange")) {
        }
    }

    @Transactional
    public void moveMethod(String id, int ordinal) {
        try (var context = newContext()) {
            var flow = context.getMethod(Id.parse(id));
            flow.getDeclaringType().moveMethod(flow, ordinal);
            context.finish();
        }
    }

    @Transactional
    public void deleteNode(String nodeId) {
        try (var context = newContext()) {
            NodeRT node = context.getEntity(NodeRT.class, Id.parse(nodeId));
            if (node == null) {
                return;
            }
            deleteNode(node, context);
            context.finish();
        }
    }

    private void deleteNode(NodeRT node, IEntityContext context) {
        context.remove(node);
    }

    public Page<FlowSummaryDTO> list(String typeId, int page, int pageSize, String searchText) {
        try (var context = newContext()) {
            Klass type = context.getKlass(Id.parse(typeId));
            var methods = type.getAllMethods();
            if (searchText != null) {
                methods = NncUtils.filter(methods, flow -> flow.getName().contains(searchText)
                        || flow.getCode() != null && flow.getCode().contains(searchText));
            }
            int start = Math.min((page - 1) * pageSize, methods.size());
            int end = Math.min(page * pageSize, methods.size());
            var data = methods.subList(start, end);
            Page<Method> flowPage = new Page<>(data, methods.size());
            return new Page<>(
                    NncUtils.map(flowPage.data(), Method::toSummaryDTO),
                    flowPage.total()
            );
        }
    }

    @Autowired
    public void setTypeManager(TypeManager typeManager) {
        this.typeManager = typeManager;
    }
}
