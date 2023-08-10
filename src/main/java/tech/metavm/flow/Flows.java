package tech.metavm.flow;

import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.util.KeyValue;
import tech.metavm.util.LinkedList;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Flows {

    private static Flows INSTANCE;

    public static Flows getInstance() {
        return INSTANCE;
    }

    private int nextId;

    private Flows() {}

    private IEntityContext entityContext;
    private final LinkedList<ScopeRT> scopeStack = new LinkedList<>();

    public BranchNode cond(List<KeyValue<Value, Runnable>> branches) {
        BranchNode branchNode = new BranchNode(createNodeDTO(NodeKind.BRANCH), currentScope());
        for (KeyValue<Value, Runnable> pair : branches) {
            var branch = branchNode.addBranch(pair.key());
            enterScope(branch.getScope());
            pair.value().run();
            exitScope();
        }
        return branchNode;
    }

    public AddObjectNode createObject(Map<Long, Value> fieldValues) {
        AddObjectNode node = AddObjectNode.create(createNodeDTO(NodeKind.ADD_OBJECT), entityContext);
        for (Map.Entry<Long, Value> entry : fieldValues.entrySet()) {
            var field = entityContext.getField(entry.getKey());
            Value value = entry.getValue();
            node.setField(field.getIdRequired(), value);
        }
        return node;
    }

    public UpdateObjectNode updateObject(Map<Long, UpdateOpAndValue> fieldValues) {
        UpdateObjectNode node = UpdateObjectNode.create(createNodeDTO(NodeKind.UPDATE_OBJECT), entityContext);
        for (Map.Entry<Long, UpdateOpAndValue> entry : fieldValues.entrySet()) {
            node.setField(entry.getKey(), entry.getValue());
        }
        return node;
    }

    private ScopeRT currentScope() {
        return Objects.requireNonNull(scopeStack.peek());
    }

    private void enterScope(ScopeRT scope) {
        scopeStack.push(scope);
    }

    private void exitScope() {
        scopeStack.pop();
    }

    private String nextId() {
        return (++nextId) + "";
    }

    private NodeDTO createNodeDTO(NodeKind kind) {
        return new NodeDTO(null, 0, nextId(),
                kind.code(), null, null, null, null, 0);
    }

}
