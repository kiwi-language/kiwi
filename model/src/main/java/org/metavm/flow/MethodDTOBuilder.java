package org.metavm.flow;

import org.metavm.flow.rest.*;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.type.Access;
import org.metavm.object.type.MetadataState;
import org.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MethodDTOBuilder {

    public static MethodDTOBuilder newBuilder(String declaringTypeId, String name) {
        return new MethodDTOBuilder(declaringTypeId, name);
    }

    private final String declaringTypeId;
    private final String name;
    private String code;
    private boolean isConstructor;
    private boolean isAbstract;
    private boolean synthetic;
    private boolean isNative;
    private boolean isStatic;
    private List<NodeDTO> nodes = new ArrayList<>();
    private int access = Access.PUBLIC.code();
    private String returnType;
    private List<ParameterDTO> parameters = new ArrayList<>();
    private List<String> capturedTypeIds = new ArrayList<>();
    private List<String> capturedCompositeTypeIds = new ArrayList<>();
    private List<String> capturedFlowIds = new ArrayList<>();
    private int state = MetadataState.READY.code();
    private String id;
    private Long tmpId;
    private String rootScopeId;
    private boolean skipRootScope;
    private List<String> typeParameterIds = new ArrayList<>();

    public MethodDTOBuilder(String declaringTypeId, String name) {
        this.declaringTypeId = declaringTypeId;
        this.name = name;
    }

    public MethodDTOBuilder code(String code) {
        this.code = code;
        return this;
    }

    public MethodDTOBuilder isConstructor(boolean isConstructor) {
        this.isConstructor = isConstructor;
        return this;
    }

    public MethodDTOBuilder isAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
        return this;
    }

    public MethodDTOBuilder isNative(boolean isNative) {
        this.isNative = isNative;
        return this;
    }

    public MethodDTOBuilder synthetic(boolean synthetic) {
        this.synthetic = synthetic;
        return this;
    }

    public MethodDTOBuilder skipRootScope(boolean skipRootScope) {
        this.skipRootScope = skipRootScope;
        return this;
    }

    public MethodDTOBuilder capturedTypeIds(List<String> capturedTypeIds) {
        this.capturedTypeIds = capturedTypeIds;
        return this;
    }

    public MethodDTOBuilder capturedCompositeTypeIds(List<String> capturedCompositeTypeIds) {
        this.capturedCompositeTypeIds = capturedCompositeTypeIds;
        return this;
    }

    public MethodDTOBuilder capturedFlowIds(List<String> capturedFlowIds) {
        this.capturedFlowIds = capturedFlowIds;
        return this;
    }

    public MethodDTOBuilder nodes(List<NodeDTO> nodes) {
        this.nodes = nodes;
        return this;
    }

    public MethodDTOBuilder addNode(NodeDTO node) {
        if (!this.nodes.isEmpty())
            node = node.copyWithPrevId(this.nodes.get(this.nodes.size() - 1).id());
        this.nodes.add(node);
        return this;
    }

    public MethodDTOBuilder autoCreateInputNode(Long tmpId, String name) {
        return addNode(NodeDTOFactory.createInputNode(
                tmpId,
                name,
                NncUtils.map(parameters, p -> InputFieldDTO.create(p.name(), p.type()))
        ));
    }

    public MethodDTOBuilder access(int access) {
        this.access = access;
        return this;
    }

    public MethodDTOBuilder returnType(String returnType) {
        this.returnType = returnType;
        return this;
    }

    public MethodDTOBuilder parameters(List<ParameterDTO> parameters) {
        this.parameters = parameters;
        return this;
    }

    public List<ParameterDTO> getParameters() {
        return parameters;
    }

    public MethodDTOBuilder addParameter(ParameterDTO parameter) {
        this.parameters.add(parameter);
        return this;
    }

    public MethodDTOBuilder state(int state) {
        this.state = state;
        return this;
    }

    public MethodDTOBuilder isStatic(boolean isStatic) {
        this.isStatic = isStatic;
        return this;
    }

    public MethodDTOBuilder id(String id) {
        this.id = id;
        return this;
    }

    public MethodDTOBuilder rootScopeId(String rootScopeId) {
        this.rootScopeId = rootScopeId;
        return this;
    }


    public MethodDTOBuilder tmpId(long tmpId) {
        this.tmpId = tmpId;
        return this;
    }

    public MethodDTOBuilder typeParameterIds(List<String> typeParameterIds) {
        this.typeParameterIds = typeParameterIds;
        return this;
    }

    public FlowDTO build() {
        if (id == null && tmpId != null)
            id = TmpId.of(tmpId).toString();
        return new FlowDTO(
                id,
                name,
                code,
                isNative,
                synthetic,
                skipRootScope ? null : new ScopeDTO(
                        rootScopeId,
                        nodes,
                        0,
                        0
                ),
                returnType,
                parameters,
                null,
                typeParameterIds,
                null,
                List.of(),
                capturedTypeIds,
                capturedCompositeTypeIds,
                capturedFlowIds,
                List.of(),
                false,
                Map.of(),
                state,
                new MethodParam(
                        isConstructor,
                        isAbstract,
                        isStatic,
                        null,
                        declaringTypeId,
                        null,
                        access
                )
        );
    }

}
