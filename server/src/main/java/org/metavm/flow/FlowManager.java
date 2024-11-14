package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.common.Page;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.*;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.ConstantPoolDTO;
import org.metavm.object.type.rest.dto.CpEntryDTO;
import org.metavm.util.BusinessException;
import org.metavm.util.DebugEnv;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class FlowManager extends EntityContextFactoryAware {

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
                        .newBuilder(context.getKlass(methodParam.declaringTypeId()), flowDTO.name())
                        .flowDTO(flowDTO)
                        .access(Access.getByCode(methodParam.access()))
                        .isStatic(methodParam.isStatic())
                        .tmpId(flowDTO.tmpId()).build();
                context.bind(method);
            }
            Type oldFuncType = method.getType();
            Type returnType = getReturnType(flowDTO, declaringType, context);
            method.setName(flowDTO.name());
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
                function = FunctionBuilder.newBuilder(flowDTO.name())
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
            parameter.setType(TypeParser.parseType(parameterDTO.type(), context));
            return parameter;
        } else {
            return new Parameter(
                    parameterDTO.tmpId(),
                    parameterDTO.name(),
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
                    MethodBuilder.newBuilder(type, overridden.getName())
                            .returnType(overridden.getReturnType())
                            .type(overridden.getType())
                            .access(overridden.getAccess())
                            .parameters(NncUtils.map(overridden.getParameters(), Parameter::copy))
                            .typeParameters(NncUtils.map(overridden.getTypeParameters(), TypeVariable::copy))
                            .build();
                }
            }
        }
    }

    private void saveScope(CodeDTO scopeDTO, Code code, IEntityContext context) {
        code.clear();
        code.setMaxStack(scopeDTO.maxStack());
        code.setMaxLocals(scopeDTO.maxLocals());
        code.setCodeBase64(scopeDTO.codeBase64());
    }

    private NodeRT saveNode(NodeDTO nodeDTO, Code code, NodeSavingStage stage, IEntityContext context) {
        if (nodeDTO.id() == null || Id.parse(nodeDTO.id()).tryGetTreeId() == null) {
            return createNode(nodeDTO, code, stage, context);
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
        try (var ignored = context.getProfiler().enter("FlowManager.saveContent")) {
            if (DebugEnv.debugging)
                DebugEnv.logger.info("FlowManager.saveContent flow: {}", flow.getQualifiedName());
            if (flow.isNative() || (flow instanceof Method method && method.isAbstract()))
                return;
            if(flowDTO.constantPool() != null)
                saveConstantPool(flow.getConstantPool(), flowDTO.constantPool(), context);
            if(flowDTO.code() != null)
                saveScope(flowDTO.code(), flow.getCode(), context);
            for (LambdaDTO lambdaDTO : flowDTO.lambdas()) {
                saveLambdaContent(lambdaDTO, context);
            }
        }
    }

    private void saveConstantPool(ConstantPool constantPool, ConstantPoolDTO constantPoolDTO, IEntityContext context) {
        constantPool.clear();
        for (CpEntryDTO entryDTO : constantPoolDTO.entries()) {
            constantPool.addEntry(CpEntry.fromDTO(entryDTO, context));
        }
    }

    private void saveLambdaContent(LambdaDTO lambdaDTO, IEntityContext context) {
        var lambda = context.getEntity(Lambda.class, lambdaDTO.id());
        saveScope(lambdaDTO.scope(), lambda.getCode(), context);
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
            var node = createNode(nodeDTO, context.getCode(Id.parse(nodeDTO.scopeId())), NodeSavingStage.INIT, context);
            afterFlowChange(flow, context);
            context.finish();
            return node.toDTO(serContext);
        }
    }

    private void afterFlowChange(Flow flow, IEntityContext context) {
        try (var ignored1 = context.getProfiler().enter("afterFlowChange")) {
            flow.analyze();
            flow.check();
            flow.computeMaxes();
        }
    }

    private NodeRT createNode(NodeDTO nodeDTO, Code code, NodeSavingStage stage, IEntityContext context) {
        try (var ignored = context.getProfiler().enter("createNode")) {
            try (var ignored1 = context.getProfiler().enter("Flow.analyze", true)) {
                code.getFlow().analyze();
            }
            return NodeFactory.save(nodeDTO, code, stage, context);
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
        var code = context.getCode(Id.parse(nodeDTO.scopeId()));
        code.getFlow().analyze();
        NodeFactory.save(nodeDTO, code, stage, context);
        return node;
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
                methods = NncUtils.filter(methods, flow -> flow.getName().contains(searchText));
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

}
