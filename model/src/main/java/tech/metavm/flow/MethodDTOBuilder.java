package tech.metavm.flow;

import tech.metavm.flow.rest.*;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.object.instance.core.TypeTag;
import tech.metavm.object.type.Access;
import tech.metavm.object.type.MetadataState;

import java.util.ArrayList;
import java.util.List;

public class MethodDTOBuilder {

    public static MethodDTOBuilder newBuilder(String declaringTypeId, String name) {
        return new MethodDTOBuilder(declaringTypeId, name);
    }

    private final String declaringTypeId;
    private final String name;
    private String code;
    private boolean isConstructor;
    private boolean isAbstract;
    private boolean isNative;
    private boolean isStatic;
    private List<String> overriddenIds = new ArrayList<>();
    private List<NodeDTO> nodes = new ArrayList<>();
    private int access = Access.PUBLIC.code();
    private String returnTypeId;
    private List<ParameterDTO> parameters = new ArrayList<>();
    private int state = MetadataState.READY.code();
    private String id;
    private Long tmpId;
    private String rootScopeId;

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

    public MethodDTOBuilder overriddenIds(List<String> overriddenIds) {
        this.overriddenIds = overriddenIds;
        return this;
    }

    public MethodDTOBuilder nodes(List<NodeDTO> nodes) {
        this.nodes = nodes;
        return this;
    }

    public MethodDTOBuilder addNode(NodeDTO node) {
        if(!this.nodes.isEmpty())
            node = node.copyWithPrevId(this.nodes.get(this.nodes.size() - 1).id());
        this.nodes.add(node);
        return this;
    }

    public MethodDTOBuilder access(int access) {
        this.access = access;
        return this;
    }

    public MethodDTOBuilder returnTypeId(String returnTypeId) {
        this.returnTypeId = returnTypeId;
        return this;
    }

    public MethodDTOBuilder parameters(List<ParameterDTO> parameters) {
        this.parameters = parameters;
        return this;
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

    public FlowDTO build() {
        if(id == null && tmpId != null)
            id = TmpId.of(tmpId).toString();
        return new FlowDTO(
                id,
                name,
                code,
                isNative,
                new ScopeDTO(
                        rootScopeId,
                        nodes
                ),
                returnTypeId,
                parameters,
                null,
                List.of(),
                null,
                List.of(),
                false,
                state,
                new MethodParam(
                        isConstructor,
                        isAbstract,
                        isStatic,
                        null,
                        declaringTypeId,
                        null,
                        overriddenIds,
                        access
                )
        );
    }

}
