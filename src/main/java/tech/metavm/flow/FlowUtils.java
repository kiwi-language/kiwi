package tech.metavm.flow;

import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import java.util.*;

public class FlowUtils {

    private static NodeRT<?> getBranchCommonPredecessor(List<Branch> branches) {
        Set<NodeRT<?>> lastNodes = NncUtils.mapUnique(
                branches, branch -> branch.isEmpty() ? branch.getOwner() : branch.getScope().getLastNode()
        );
        return getCommonPredecessor(lastNodes);
    }

    private static NodeRT<?> getCommonPredecessor(Set<NodeRT<?>> nodes) {
        var paths = getPaths(nodes).values();

    }

    private static NodeRT<?> getCommonAncestor(List<NodeRT<?>> nodes) {
        
    }

    private static Map<NodeRT<?>, List<NodeRT<?>>> getPaths(Set<NodeRT<?>> nodes) {
        if(nodes.isEmpty()) {
            return Map.of();
        }
        var flow = nodes.iterator().next().getFlow();
        var entry = flow.getRootScope().getFirstNode();
        Map<NodeRT<?>, List<NodeRT<?>>> result = new HashMap<>();
        dfs(entry, nodes, new IdentitySet<>(), new LinkedList<>(), result);
        return result;
    }

    private static void dfs(NodeRT<?> node, Set<NodeRT<?>> nodes,
                            Set<NodeRT<?>> visited,
                            LinkedList<NodeRT<?>> path, Map<NodeRT<?>, List<NodeRT<?>>> result) {
        if(visited.contains(node)) {
            return;
        }
        visited.add(node);
        path.addLast(node);
        if(nodes.contains(node)) {
            result.put(node, new ArrayList<>(path));
        }
        for (NodeRT<?> successor : getSuccessors(node)) {
            dfs(successor, nodes, visited, path, result);
        }
        path.removeLast();
    }

    public static List<NodeRT<?>> getSuccessors(NodeRT<?> node) {
        if(node instanceof BranchNode branchNode) {
            return NncUtils.mapAndFilter(branchNode.getBranches(),
                    branch -> branch.isEmpty() ? branch.getOwner().getGlobalSuccessor()
                            : branch.getScope().getFirstNode(), Objects::nonNull);
        }
        else {
            return node.getGlobalSuccessor() != null ? List.of(node.getGlobalSuccessor()) : List.of();
        }
    }


}
