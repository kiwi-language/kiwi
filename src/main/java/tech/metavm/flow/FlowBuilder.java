package tech.metavm.flow;

import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.LinkedList;
import tech.metavm.util.NncUtils;

import java.util.Random;
import java.util.UUID;

public class FlowBuilder {

    private final IEntityContext context;
    private FlowRT flow;
    private final LinkedList<ScopeRT> scopeStack = new LinkedList<>();
    private NodeRT<?> prev;

    public FlowBuilder(IEntityContext context) {
        this.context = context;
    }

    public FlowRT createFlow(String name, Class<?> javaType) {
        ClassType type = (ClassType) context.getType(javaType);
        FlowRT flow = new FlowRT(name, createInputType(), createOutputType(), type);
        enterScope(flow.getRootScope());
        return this.flow = flow;
    }

    public BranchNode createBranch(String name) {
        BranchNode branchNode = new BranchNode(createNodeDTO(name, NodeKind.BRANCH), currentScope());
        prev = branchNode;
        return branchNode;
    }

    private BranchNode currentBranchNode() {
        return (BranchNode) prev;
    }

    public Branch enterBranch(Value condition, boolean preselected) {
        var branchNode = currentBranchNode();
        Branch branch = new Branch(branchNode.getBranches().size(), condition, preselected,
                createScope(), branchNode);
        enterScope(branch.getScope());
        return branch;
    }

    public void exitBranch() {
        exitScope();
    }

    private ScopeRT createScope() {
        return new ScopeRT(flow);
    }

    private NodeDTO createNodeDTO(String name, NodeKind kind) {
        return new NodeDTO(
                null, 0, name, kind.code(), null,
                null, null, null, 0
        );
    }

    private void enterScope(ScopeRT scope) {
        scopeStack.push(scope);
    }

    private void exitScope() {
        scopeStack.pop();
    }

    private ScopeRT currentScope() {
        return NncUtils.requireNonNull(scopeStack.peek());
    }

    private String nextClassName(String prefix) {
        Random random = new Random();
        return prefix + "_" + random.nextLong();
    }

    private ClassType createInputType() {
        return new ClassType(
                nextClassName("input"),
                null,
                TypeCategory.CLASS,
                true,
                false,
                null
        );
    }

    private ClassType createOutputType() {
        return new ClassType(
                nextClassName("output"),
                null,
                TypeCategory.CLASS,
                true,
                false,
                null
        );
    }

}
