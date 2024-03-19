package tech.metavm.flow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.common.ErrorCode;
import tech.metavm.common.Page;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.NodeExpression;
import tech.metavm.flow.rest.*;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.object.instance.rest.ExpressionFieldValue;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.ClassTypeDTOBuilder;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.object.type.rest.dto.FieldDTOBuilder;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.util.AssertUtils;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FlowManager extends EntityContextFactoryBean {

    private TypeManager typeManager;

    public FlowManager(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
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
            TryNode tryNode = (TryNode) createNode(nodeDTO, scope, context);
            TryEndNode tryEndNode = new TryEndNode(null, tryNode.getName() + "结束", null,
                    ClassTypeBuilder.newBuilder("守护结束节点输出", "TryEndNodeOutput").temporary().build(),
                    tryNode, scope);
            FieldBuilder.newBuilder("异常", "exception",
                            tryEndNode.getType(), context.getNullableType(StandardTypes.getThrowableType()))
                    .build();
            context.bind(tryEndNode);
            Flows.retransformFlowIfRequired(scope.getFlow(), context);
            context.finish();
            try (var ignored = SerializeContext.enter()) {
                return List.of(tryNode.toDTO(serContext), tryEndNode.toDTO(serContext));
            }
        }
    }

    @Transactional
    public List<NodeDTO> createBranchNode(NodeDTO nodeDTO) {
        try (var context = newContext(); var serContext = SerializeContext.enter()) {
            var scope = context.getScope(nodeDTO.scopeId());
            BranchNode branchNode = (BranchNode) createNode(nodeDTO, scope, context);
            MergeNode mergeNode = new MergeNode(null, branchNode.getName() + "合并", null,
                    branchNode, ClassTypeBuilder.newBuilder("合并节点输出", "MergeNodeOutput").temporary().build(),
                    scope);
            context.bind(mergeNode);
            Flows.retransformFlowIfRequired(branchNode.getFlow(), context);
            context.finish();
            try (var ignored = SerializeContext.enter()) {
                return List.of(branchNode.toDTO(serContext), mergeNode.toDTO(serContext));
            }
        }
    }

    private Type getReturnType(FlowDTO flowDTO, ClassType declaringType, IEntityContext context) {
        if (flowDTO.isConstructor()) {
            return declaringType.isTemplate() ?
                    context.getParameterizedType(declaringType, declaringType.getTypeParameters()) : declaringType;
        } else {
            return context.getType(NncUtils.requireNonNull(flowDTO.returnTypeId()));
        }
    }

    @Transactional
    public Flow save(FlowDTO flowDTO) {
        try (var context = newContext()) {
            var flow = save(flowDTO, context);
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
            List<Method> overridden = NncUtils.map(methodParam.overriddenIds(), context::getMethod);
            Method method = context.getMethod(flowDTO.id());
            ClassType declaringType = method != null ? method.getDeclaringType() :
                    context.getClassType(methodParam.declaringTypeId());
            boolean creating = method == null;
            if (method == null) {
                method = MethodBuilder
                        .newBuilder(context.getClassType(methodParam.declaringTypeId()), flowDTO.name(),
                                flowDTO.code(), context.getFunctionTypeContext())
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
            method.update(parameters, returnType, overridden, context.getFunctionTypeContext());
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
                function = FunctionBuilder.newBuilder(flowDTO.name(), flowDTO.code(), context.getFunctionTypeContext())
                        .tmpId(flowDTO.tmpId())
                        .build();
                context.bind(function);
            }
            var returnType = context.getType(flowDTO.returnTypeId());
            function.update(parameters, returnType, context.getFunctionTypeContext());
            function.setNative(flowDTO.isNative());
            flow = function;
        } else {
            throw new InternalException("Invalid flowDTO, unrecognized param: " + flowDTO.param());
        }
        flow.setNative(flowDTO.isNative());
        flow.setTypeParameters(NncUtils.map(flowDTO.typeParameterIds(), context::getTypeVariable));
//        if (flowDTO.horizontalInstances() != null) {
//            for (FlowDTO templateInstance : flowDTO.horizontalInstances()) {
//                save(templateInstance, declarationOnly, context);
//            }
//        }
        if (!declarationOnly)
            saveContent(flowDTO, flow, context);
        Flows.retransformFlowIfRequired(flow, context);
        flow.check();
        return flow;
    }

    public Parameter saveParameter(ParameterDTO parameterDTO, IEntityContext context) {
        var parameter = context.getEntity(Parameter.class, parameterDTO.id());
        if (parameter != null) {
            parameter.setName(parameterDTO.name());
            parameter.setCode(parameterDTO.code());
            parameter.setType(context.getType(parameterDTO.typeId()));
            return parameter;
        } else {
            return new Parameter(
                    parameterDTO.tmpId(),
                    parameterDTO.name(),
                    parameterDTO.code(),
                    context.getType(parameterDTO.typeId())
            );
        }
    }

    public void createOverridingFlows(Method overridden, IEntityContext context) {
        NncUtils.requireTrue(overridden.isAbstract());
        for (ClassType subType : overridden.getDeclaringType().getSubTypes()) {
            createOverridingFlows(overridden, subType, context);
        }
    }

    public void recreateOverridingFlows(Method method, IEntityContext context) {
        detachOverrideMethods(method);
        createOverridingFlows(method, context);
    }

    public void createOverridingFlows(Method overridden, ClassType type, IEntityContext context) {
        NncUtils.requireTrue(overridden.isAbstract());
        if (type.isEffectiveAbstract()) {
            for (ClassType subType : type.getSubTypes()) {
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
                if (candidate != null) {
                    candidate.addOverridden(overridden);
                } else {
                    flow = MethodBuilder.newBuilder(type, overridden.getName(), overridden.getCode(), context.getFunctionTypeContext())
                            .returnType(overridden.getReturnType())
                            .type(overridden.getType())
                            .access(overridden.getAccess())
                            .overridden(List.of(overridden))
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
            saveNode(nodeDTO, flow.getRootScope(), context);
        }
    }

    private NodeRT saveNode(NodeDTO nodeDTO, ScopeRT scope, IEntityContext context) {
        if (nodeDTO.id() == null || Id.parse(nodeDTO.id()).tryGetPhysicalId() == null) {
            return createNode(nodeDTO, scope, context);
        } else {
            return updateNode(nodeDTO, context);
        }
    }

    private void removeTransformedFlowsIfRequired(Flow flow, IEntityContext context) {
        if (flow instanceof Method method) {
            if (method.getDeclaringType().isTemplate() && context.isPersisted(method.getDeclaringType())) {
                var templateInstances = context.getTemplateInstances(method.getDeclaringType());
                for (ClassType templateInstance : templateInstances) {
                    var flowTi = templateInstance.findMethodByVerticalTemplate(method);
                    templateInstance.removeMethod(flowTi);
                    context.remove(flowTi);
                }
            }
        }
    }

    public void saveContent(FlowDTO flowDTO, Flow flow, IEntityContext context) {
        try(var ignored = context.getProfiler().enter("FlowManager.saveContent")) {
            if (flow.isNative() || (flow instanceof Method method && method.isAbstract()))
                return;
            if (flow.getNodes().isEmpty() && flowDTO.rootScope() == null) {
                if (context.isNewEntity(flow))
                    initNodes(flow, context);
            } else
                saveNodes(flowDTO, flow, context);
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
                "当前对象",
                NodeKind.SELF.code(),
                null
        );
        return new SelfNode(selfNodeDTO.tmpId(), selfNodeDTO.name(), null,
                SelfNode.getSelfType(flow, context.getGenericContext()), null, flow.getRootScope());
    }

    private NodeRT createInputNode(Flow flow, NodeRT prev) {
        var type = ClassTypeBuilder.newBuilder("输入类型", null).temporary().build();
        for (Parameter parameter : flow.getParameters()) {
            FieldBuilder.newBuilder(parameter.getName(), parameter.getCode(), type, parameter.getType())
                    .build();
        }
        return new InputNode(null, "流程输入", null, type, prev, flow.getRootScope());
    }

    private void createReturnNode(Flow flow, NodeRT prev) {
        Value value;
        if (Flows.isConstructor(flow)) {
            NncUtils.requireTrue(flow.getRootNode() instanceof SelfNode);
            value = Values.reference(new NodeExpression(flow.getRootNode()));
        } else
            value = null;
        new ReturnNode(null, "结束", null, prev, flow.getRootScope(), value);
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
            for (ClassType subType : method.getDeclaringType().getSubTypes())
                detachOverrideMethods(method, subType);
            method.getDeclaringType().removeMethod(method);
        } else
            context.remove(flow);
    }

    private void detachOverrideMethods(Method method) {
        for (ClassType subType : method.getDeclaringType().getSubTypes()) {
            detachOverrideMethods(method, subType);
        }
    }

    private void detachOverrideMethods(Method method, ClassType type) {
        var override = type.tryResolveNonParameterizedMethod(method);
        if (override != null) {
            override.removeOverridden(method);
            override.addOverridden(method.getOverridden());
        } else {
            for (ClassType subType : type.getSubTypes()) {
                detachOverrideMethods(method, subType);
            }
        }
    }

    @Transactional
    public NodeDTO saveNode(NodeDTO nodeDTO) {
        if(nodeDTO.id() == null || Id.parse(nodeDTO.id()) instanceof TmpId)
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
            var node = createNode(nodeDTO, context.getScope(Id.parse(nodeDTO.scopeId())), context);
            context.finish();
            return node.toDTO(serContext);
        }
    }

    private NodeRT createNode(NodeDTO nodeDTO, ScopeRT scope, IEntityContext context) {
        try (var ignored = context.getProfiler().enter("createNode")) {
            nodeDTO = beforeNodeChange(nodeDTO, null, scope, context);
            try (var ignored1 = context.getProfiler().enter("Flow.analyze", true)) {
                scope.getFlow().analyze();
            }
            var node = NodeFactory.save(nodeDTO, scope, context);
            afterNodeChange(nodeDTO, node, context);
            Flows.retransformFlowIfRequired(scope.getFlow(), context);
            try (var ignored1 = context.getProfiler().enter("Flow.check")) {
                node.getFlow().check();
            }
            return node;
        }
    }

    private void saveBranchNodeContent(NodeDTO nodeDTO, BranchNode branchNode, IEntityContext context) {
        BranchNodeParam param = nodeDTO.getParam();
        if (param.branches() != null) {
            for (BranchDTO branchDTO : param.branches()) {
                var branch = branchNode.getBranchByIndex(branchDTO.index());
                if (branchDTO.scope() != null && branchDTO.scope().nodes() != null) {
                    Set<String> nodeIds = new HashSet<>();
                    for (NodeDTO n : branchDTO.scope().nodes()) {
                        saveNode(n, branch.getScope(), context);
                        if (n.id() != null) {
                            nodeIds.add(n.id());
                        }
                    }
                    for (NodeRT node : branch.getScope().getNodes()) {
                        if (node.tryGetId() != null && !nodeIds.contains(node.getStringId())) {
                            deleteNode(node, context);
                        }
                    }
                }
            }
        }
    }

    private void saveAddObjectChildren(NodeDTO nodeDTO, AddObjectNode node, IEntityContext context) {
        AddObjectNodeParam param = nodeDTO.getParam();
        if (param.getBodyScope() != null) {
            for (NodeDTO childNodeDTO : param.getBodyScope().nodes()) {
                NodeKind kind = NodeKind.getByCodeRequired(childNodeDTO.kind());
                AssertUtils.assertTrue(NodeKind.CREATING_KINDS.contains(kind),
                        ErrorCode.INVALID_ADD_OBJECT_CHILD, childNodeDTO.name());
            }
            var bodyScope = node.getBodyScope();
            bodyScope.clearNodes();
            NncUtils.map(param.getBodyScope().nodes(),
                    childNodeDTO -> saveNode(childNodeDTO, bodyScope, context));
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
            var node = updateNode(nodeDTO, context);
            context.finish();
            return node.toDTO(serContext);
        }
    }

    NodeRT updateNode(NodeDTO nodeDTO, IEntityContext context) {
        nodeDTO.ensureIdSet();
        NodeRT node = context.getEntity(NodeRT.class, Id.parse(nodeDTO.id()));
        if (node == null) {
            throw BusinessException.nodeNotFound(nodeDTO.id());
        }
        var scope = context.getScope(Id.parse(nodeDTO.scopeId()));
        scope.getFlow().analyze();
        nodeDTO = beforeNodeChange(nodeDTO, node, scope, context);
        NodeFactory.save(nodeDTO, scope, context);
        afterNodeChange(nodeDTO, node, context);
        Flows.retransformFlowIfRequired(scope.getFlow(), context);
        try (var ignored = context.getProfiler().enter("Flow.check")) {
            node.getFlow().check();
        }
        return node;
    }

    private NodeDTO beforeNodeChange(NodeDTO nodeDTO, @Nullable NodeRT node, ScopeRT scope, IEntityContext context) {
        try (var ignored = context.getProfiler().enter("FlowManager.beforeNodeChange")) {
            if (!FlowSavingContext.skipPreprocessing())
                nodeDTO = preprocess(nodeDTO, node, scope, context);
            NodeKind kind = NodeKind.getByCodeRequired(nodeDTO.kind());
            if (kind.isOutputTypeAsChild()) {
                typeManager.saveTypeWithContent(nodeDTO.outputType(), context);
            }
            return nodeDTO;
        }
    }

    private NodeDTO preprocess(NodeDTO nodeDTO, NodeRT node, ScopeRT scope, IEntityContext context) {
        if (nodeDTO.kind() == NodeKind.INPUT.code())
            return preprocessInputNode(nodeDTO, (InputNode) node);
        if (nodeDTO.kind() == NodeKind.FOREACH.code())
            return preprocessForeachNode(nodeDTO, node, scope, context);
        if (nodeDTO.kind() == NodeKind.BRANCH.code())
            return preprocessBranchNode(nodeDTO, node);
        if (nodeDTO.kind() == NodeKind.WHILE.code())
            return preprocessWhileNode(nodeDTO, node);
        if (nodeDTO.kind() == NodeKind.MERGE.code())
            return preprocessMergeNode(nodeDTO, node);
        if (nodeDTO.kind() == NodeKind.TRY_END.code())
            return preprocessTryEndNode(nodeDTO, (TryEndNode) node);
        return nodeDTO;
    }

    private NodeDTO preprocessBranchNode(NodeDTO nodeDTO, NodeRT node) {
        BranchNodeParam param = nodeDTO.getParam();
        if (node == null && NncUtils.isEmpty(param.branches())) {
            List<BranchDTO> branches = List.of(
                    new BranchDTO(
                            null, 1L, null,
                            new ValueDTO(
                                    ValueKind.CONSTANT.code(),
                                    new PrimitiveFieldValue(
                                            null,
                                            PrimitiveKind.BOOLEAN.code(),
                                            true
                                    )
                            ),
                            null, false, false
                    ),
                    new BranchDTO(
                            null, 10000L, null,
                            new ValueDTO(
                                    ValueKind.CONSTANT.code(),
                                    new PrimitiveFieldValue(
                                            null,
                                            PrimitiveKind.BOOLEAN.code(),
                                            true
                                    )
                            ),
                            null, true, false
                    )
            );
            return nodeDTO.copyWithParam(new BranchNodeParam(param.inclusive(), branches));
        } else {
            return nodeDTO;
        }
    }

    private NodeDTO preprocessInputNode(NodeDTO nodeDTO, @Nullable InputNode node) {
        InputNodeParam inputParam = nodeDTO.getParam();
        var inputFields = initializeFieldRefs(inputParam.fields());
        List<FieldDTO> fields = NncUtils.map(inputFields,
                inputField -> inputField.toFieldDTO(NncUtils.get(node, n -> n.getType().getStringId())));
        TypeDTO typeDTO = ClassTypeDTOBuilder.newBuilder(
                        node != null ? node.getType().getName() : "输入类型" + NncUtils.randomNonNegative()
                )
                .id(nodeDTO.outputTypeId())
                .anonymous(true)
                .ephemeral(true)
                .fields(fields)
                .build();
        return nodeDTO.copyWithParamAndType(
                new InputNodeParam(inputFields),
                typeDTO);
    }

    private NodeDTO preprocessMergeNode(NodeDTO nodeDTO, NodeRT node) {
        MergeNodeParam param = nodeDTO.getParam();
        var mergeFields = initializeFieldRefs(param.fields());
        List<FieldDTO> fields = NncUtils.map(mergeFields, MergeFieldDTO::toFieldDTO);
        var outputType = createNodeTypeDTO("MergeOutput",
                NncUtils.get(node, NodeRT::getType), fields);
        return nodeDTO.copyWithParamAndType(new MergeNodeParam(mergeFields), outputType);
    }

    private NodeDTO preprocessTryEndNode(NodeDTO nodeDTO, TryEndNode node) {
        TryEndNodeParam param = nodeDTO.getParam();
        var tryEndFields = initializeFieldRefs(param.fields());
        List<FieldDTO> fieldDTOs = NncUtils.map(tryEndFields, TryEndFieldDTO::toFieldDTO);
        FieldDTO excetpionFieldDTO;
        if (node != null) {
            var outputType = node.getType();
            excetpionFieldDTO = outputType.getFieldByCode("exception").toDTO();
        } else {
            excetpionFieldDTO = FieldDTOBuilder
                    .newBuilder("异常", StandardTypes.getNullableThrowableType().getStringId())
                    .readonly(true)
                    .code("exception")
                    .build();
        }
        fieldDTOs = NncUtils.prepend(excetpionFieldDTO, fieldDTOs);
        var outputTypeDTO = createNodeTypeDTO("TryEndOutput",
                NncUtils.get(node, NodeRT::getType), fieldDTOs);
        return nodeDTO.copyWithParamAndType(new TryEndNodeParam(tryEndFields), outputTypeDTO);
    }

    private NodeDTO preprocessWhileNode(NodeDTO nodeDTO, NodeRT node) {
        WhileNodeParam param = nodeDTO.getParam();
        var loopFields = initializeFieldRefs(param.getFields());
        List<FieldDTO> fields = new ArrayList<>(NncUtils.map(loopFields, LoopFieldDTO::toFieldDTO));
        var outputType = createNodeTypeDTO("WhileOutput",
                NncUtils.get(node, NodeRT::getType), fields);
        return nodeDTO.copyWithParamAndType(
                new WhileNodeParam(param.getCondition(), param.getBodyScope(), loopFields),
                outputType);
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

    private TypeDTO createNodeTypeDTO(String namePrefix, @Nullable Type currentType, List<FieldDTO> fields) {
        var id = NncUtils.get(currentType, Type::getStringId);
        String name = NncUtils.get(currentType, Type::getName);
//        String code = NncUtils.get(currentType, Type::getCode);
        Long tmpId = NncUtils.get(currentType, Type::getTmpId);
        if (name == null)
            name = namePrefix + "_" + NncUtils.randomNonNegative();
        if (tmpId == null)
            tmpId = NncUtils.randomNonNegative();
        return ClassTypeDTOBuilder.newBuilder(name)
                .id(id)
                .tmpId(tmpId)
                .category(TypeCategory.CLASS.code())
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

    private NodeDTO preprocessForeachNode(NodeDTO nodeDTO, @Nullable NodeRT node, ScopeRT scope, IEntityContext context) {
        ForeachNodeParam param = nodeDTO.getParam();
        @Nullable ClassType currentType = (ClassType) NncUtils.get(node, NodeRT::getType);
        var loopFields = initializeFieldRefs(param.getFields());
        List<FieldDTO> fields = new ArrayList<>(NncUtils.map(loopFields, LoopFieldDTO::toFieldDTO));
        NodeRT prev = NncUtils.get(nodeDTO.prevId(), id -> context.getNode(Id.parse(id)));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var arrayValue = ValueFactory.create(param.getArray(), parsingContext);
        Field arrayField, indexField;
        if (currentType != null && (arrayField = currentType.findFieldByCode("array")) != null) {
            fields.add(arrayField.toDTO());
        } else {
            fields.add(
                    FieldDTOBuilder.newBuilder("数组", arrayValue.getType().getStringId())
                            .code("array")
                            .readonly(true)
                            .id(TmpId.of(NncUtils.randomNonNegative()).toString())
                            .build()
            );
        }
        if (currentType != null && (indexField = currentType.findFieldByCode("index")) != null) {
            fields.add(indexField.toDTO());
        } else {
            fields.add(
                    FieldDTOBuilder
                            .newBuilder("索引", ModelDefRegistry.getType(Long.class).getStringId())
                            .code("index")
                            .id(TmpId.of(NncUtils.randomNonNegative()).toString())
                            .build()
            );
        }
        TypeDTO type = createNodeTypeDTO("ForeachOutput", currentType, fields);
        ScopeDTO loopScope;
        if (param.getBodyScope() == null) {
            var elementType = ((ArrayType) context.getType(arrayValue.getType().getId())).getElementType();
            NodeDTO elementNode = new NodeDTO(
                    null,
                    null,
                    "element" + NncUtils.randomNonNegative(),
                    null,
                    NodeKind.VALUE.code(),
                    null,
                    elementType.getStringId(),
                    new ValueNodeParam(
                            new ValueDTO(
                                    ValueKind.EXPRESSION.code(),
                                    new ExpressionFieldValue(
                                            nodeDTO.name() + ".array[" + nodeDTO.name() + ".index]"
                                    )
                            )
                    ),
                    null,
                    null,
                    null
            );
            loopScope = new ScopeDTO(TmpId.random().toString(), List.of(elementNode));
        } else {
            loopScope = param.getBodyScope();
        }

        return nodeDTO.copyWithParamAndType(
                new ForeachNodeParam(param.getArray(), param.getCondition(), loopFields, loopScope),
                type
        );
    }

    private void afterNodeChange(NodeDTO nodeDTO, NodeRT node, IEntityContext context) {
        try (var ignored = context.getProfiler().enter("afterNodeChange")) {
            if (node instanceof InputNode inputNode)
                updateParameters(inputNode, context);
            if (node instanceof BranchNode branchNode)
                saveBranchNodeContent(nodeDTO, branchNode, context);
            if (node instanceof ScopeNode scopeNode)
                saveScopeNodeContent(nodeDTO, scopeNode, context);
            if (node instanceof LoopNode loopNode)
                updateLoopFields(nodeDTO, loopNode, context);
            if (node instanceof AddObjectNode addObjectNode)
                saveAddObjectChildren(nodeDTO, addObjectNode, context);
        }
    }

    private void updateParameters(InputNode inputNode, IEntityContext context) {
        var callable = inputNode.getScope().getOwner() == null ? inputNode.getFlow() :
                (Callable) inputNode.getScope().getOwner();
        List<Parameter> parameters = new ArrayList<>();
        for (var field : inputNode.getType().getReadyFields()) {
            var cond = NncUtils.get(inputNode.getFieldCondition(field), Value::copy);
            var parameter = callable.getParameterByName(field.getName());
            if (parameter == null) {
                parameters.add(
                        new Parameter(
                                null,
                                field.getName(),
                                field.getCode(),
                                field.getType(),
                                cond,
                                null,
                                callable
                        )
                );
            } else {
                parameters.add(parameter);
                parameter.setName(field.getName());
                parameter.setType(field.getType());
                parameter.setCondition(cond);
            }
        }
        var funcTypeContext = context.getFunctionTypeContext();
        if (callable instanceof Flow flow) {
            var oldFuncType = flow.getFunctionType();
            flow.update(parameters, flow.getReturnType(), funcTypeContext);
            if (flow instanceof Method method) {
                if (method.isAbstract() && !oldFuncType.equals(flow.getFunctionType()))
                    recreateOverridingFlows(method, context);
            }
        } else {
            var funcType = funcTypeContext.getFunctionType(callable.getParameterTypes(), callable.getReturnType());
            callable.setParameters(parameters);
            callable.setFunctionType(funcType);

        }
    }

    private void saveScopeNodeContent(NodeDTO nodeDTO, ScopeNode scopeNode, IEntityContext context) {
        ScopeNodeParamDTO param = nodeDTO.getParam();
        if (param.getBodyScope() != null && param.getBodyScope().nodes() != null) {
            scopeNode.getBodyScope().setNodes(
                    NncUtils.map(
                            param.getBodyScope().nodes(),
                            node -> saveNode(node, scopeNode.getBodyScope(), context)
                    )
            );
        }
    }

    private void updateLoopFields(NodeDTO nodeDTO, LoopNode loopNode, IEntityContext context) {
        loopNode.setLoopParam(nodeDTO.getParam(), context);
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
        Flows.retransformFlowIfRequired(node.getFlow(), context);
    }

    public Page<FlowSummaryDTO> list(String typeId, int page, int pageSize, String searchText) {
        try (var context = newContext()) {
            ClassType type = context.getClassType(Id.parse(typeId));
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

    @Transactional
    public BranchDTO createBranch(BranchDTO branchDTO) {
        try (var context = newContext(); var serContext = SerializeContext.enter()) {
            NodeRT nodeRT = context.getEntity(NodeRT.class, Id.parse(branchDTO.ownerId()));
            if (nodeRT instanceof BranchNode branchNode) {
                Branch branch = branchNode.addBranch(branchDTO, context);
                Flows.retransformFlowIfRequired(branchNode.getFlow(), context);
                context.finish();
                return branch.toDTO(true, serContext);
            } else {
                throw BusinessException.invalidParams("节点" + branchDTO.ownerId() + "不是分支节点");
            }
        }
    }

    @Transactional
    public BranchDTO updateBranch(BranchDTO branchDTO) {
        NncUtils.requireNonNull(branchDTO.index(), "分支序号必填");
        try (var context = newContext(); var serContext= SerializeContext.enter()) {
            NodeRT owner = context.getEntity(NodeRT.class, branchDTO.ownerId());
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
                return branch.toDTO(true, serContext);
            } else {
                throw BusinessException.invalidParams("节点" + branchDTO.ownerId() + "不是分支节点");
            }
        }
    }

    @Transactional
    public void deleteBranch(String ownerId, long branchId) {
        try (var context = newContext()) {
            NodeRT owner = context.getEntity(NodeRT.class, ownerId);
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

    @Autowired
    public void setTypeManager(TypeManager typeManager) {
        this.typeManager = typeManager;
    }
}
