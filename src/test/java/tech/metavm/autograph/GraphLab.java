package tech.metavm.autograph;

import tech.metavm.flow.Value;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.util.LinkedList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GraphLab {

    LinkedList<Scope> scopeStack = new LinkedList<>();

    void enterScope() {
        scopeStack.push(new Scope());
    }

    Scope exitScope() {
        return scopeStack.pop();
    }

    private BranchNode cond(Value condition, Runnable trueBlock, @Nullable Runnable elseBlock) {
        BranchNode branchNode = new BranchNode();
        enterScope();
        trueBlock.run();
        branchNode.trueScope = exitScope();
        if(elseBlock != null) {
            enterScope();
            elseBlock.run();
            branchNode.elseScope = exitScope();
        }
        return branchNode;
    }

    private static class Scope {

        private final List<Node> nodes = new ArrayList<>();

        void addNode(Node node) {

        }
    }

    private static class Node {}

    private static class BranchNode extends Node {
        Scope trueScope;
        Scope elseScope;
    }

}
