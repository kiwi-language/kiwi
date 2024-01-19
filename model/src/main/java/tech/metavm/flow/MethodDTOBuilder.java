package tech.metavm.flow;

import tech.metavm.common.RefDTO;
import tech.metavm.flow.rest.*;
import tech.metavm.object.type.Access;
import tech.metavm.object.type.MetadataState;

import java.util.ArrayList;
import java.util.List;

public class MethodDTOBuilder {

    public static MethodDTOBuilder newBuilder(RefDTO declaringTypeRef, String name) {
        return new MethodDTOBuilder(declaringTypeRef, name);
    }

    private final RefDTO declaringTypeRef;
    private final String name;
    private String code;
    private boolean isConstructor;
    private boolean isAbstract;
    private boolean isNative;
    private boolean isStatic;
    private List<RefDTO> overriddenRefs = new ArrayList<>();
    private List<NodeDTO> nodes = new ArrayList<>();
    private int access = Access.PUBLIC.code();
    private RefDTO returnTypeRef;
    private List<ParameterDTO> parameters = new ArrayList<>();
    private int state = MetadataState.READY.code();
    private Long id;
    private Long tmpId;
    private Long rootScopeId;
    private Long rootScopeTmpId;

    public MethodDTOBuilder(RefDTO declaringTypeRef, String name) {
        this.declaringTypeRef = declaringTypeRef;
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

    public MethodDTOBuilder overriddenRefs(List<RefDTO> overriddenRefs) {
        this.overriddenRefs = overriddenRefs;
        return this;
    }

    public MethodDTOBuilder nodes(List<NodeDTO> nodes) {
        this.nodes = nodes;
        return this;
    }

    public MethodDTOBuilder addNode(NodeDTO node) {
        if(!this.nodes.isEmpty())
            node = node.copyWithPrevRef(this.nodes.get(this.nodes.size() - 1).getRef());
        this.nodes.add(node);
        return this;
    }

    public MethodDTOBuilder access(int access) {
        this.access = access;
        return this;
    }

    public MethodDTOBuilder returnTypeRef(RefDTO returnTypeRef) {
        this.returnTypeRef = returnTypeRef;
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

    public MethodDTOBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public MethodDTOBuilder tmpId(Long tmpId) {
        this.tmpId = tmpId;
        return this;
    }

    public MethodDTOBuilder rootScopeId(Long rootScopeId) {
        this.rootScopeId = rootScopeId;
        return this;
    }

    public MethodDTOBuilder rootScopeTmpId(Long rootScopeTmpId) {
        this.rootScopeTmpId = rootScopeTmpId;
        return this;
    }

    public FlowDTO build() {
        return new FlowDTO(
                id,
                tmpId,
                name,
                code,
                isNative,
                new ScopeDTO(
                        rootScopeId,
                        rootScopeTmpId,
                        nodes
                ),
                returnTypeRef,
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
                        declaringTypeRef,
                        null,
                        overriddenRefs,
                        access
                )
        );
    }

}
