package org.metavm.autograph;

import lombok.extern.slf4j.Slf4j;
import org.metavm.expression.*;
import org.metavm.flow.NodeRT;
import org.metavm.flow.TryEnterNode;
import org.metavm.flow.Value;
import org.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@Slf4j
public class VariableTable {

    private final LinkedList<NodeRT> branchNodes = new LinkedList<>();
    private VariableMap variableMap = new VariableMap();
    private final Map<NodeRT, CondSection> condSections = new HashMap<>();

    private final LinkedList<TrySection> trySections = new LinkedList<>();

    void enterCondSection(NodeRT branchNode) {
        branchNodes.push(branchNode);
        condSections.put(branchNode, new CondSection(branchNode, variableMap));
    }

    void nextBranch(NodeRT sectionId, long branchIndex) {
        var section = condSections.get(sectionId);
        section.currentBranch = branchIndex;
        variableMap = section.entryMap.copy();
        section.putBranchMap(branchIndex, variableMap);
    }

    void setYield(Value yield) {
        var branchNode = requireNonNull(branchNodes.peek(), "Not in a branch");
        condSections.get(branchNode).setYield(yield);
    }

    Map<Long, BranchInfo> exitCondSection(NodeRT sectionId) {
        var section = condSections.remove(sectionId);
        Map<Long, BranchInfo> result = new HashMap<>();
        var outputVars = new HashSet<String>();
        section.branchMaps.values().forEach(varMap -> outputVars.addAll(varMap.getVisibleModified()));
        for (var entry : section.branchMaps.entrySet()) {
            var branch = entry.getKey();
            var varMap = entry.getValue();
            Map<String, Value> branchOutputs = new HashMap<>();
            for (String outputVar : outputVars) {
                branchOutputs.put(outputVar, varMap.getVariable(outputVar));
            }
            result.put(branch, new BranchInfo(branchOutputs, section.yields.get(branch)));
        }
        variableMap = section.entryMap;
        branchNodes.pop();
        return result;
    }

    boolean isInsideBranch() {
        return !branchNodes.isEmpty();
    }

    void enterTrySection(TryEnterNode tryEnterNode) {
        trySections.push(new TrySection(tryEnterNode));
    }

    Map<NodeRT, Map<String, Value>> exitTrySection(TryEnterNode tryEnterNode, List<String> outputVars) {
        var trySection = trySections.pop();
        NncUtils.requireTrue(trySection.tryEnterNode == tryEnterNode);
        Map<NodeRT, Map<String, Value>> result = new HashMap<>();
        trySection.raiseVariables.forEach((raiseNode, variableMap) ->
            result.put(
                    raiseNode,
                    NncUtils.toMap(outputVars, Function.identity(), variableMap::getVariable)
            )
        );
        return result;
    }

    void processRaiseNode(NodeRT raiseNode) {
        if(trySections.isEmpty()) {
            return;
        }
        var trySection = trySections.peek();
        trySection.raiseVariables.put(raiseNode, variableMap.copy());
    }

    public void define(String name) {
        variableMap.define(name);
    }


    private static class VariableMap {

        private final Map<String, Value> variables = new HashMap<>();
        private final Set<String> modified = new HashSet<>();
        private final Set<String> defined = new HashSet<>();

        public VariableMap() {}

        public VariableMap(VariableMap variableMap) {
            variables.putAll(variableMap.variables);
        }

        VariableMap copy() {
            return new VariableMap(this);
        }

        Value getVariable(String name) {
            return variables.get(name);
        }

        void setVariable(String name, Value value) {
            modified.add(name);
            variables.put(name, value);
        }

        Set<String> getModified() {
            return Collections.unmodifiableSet(modified);
        }

        Set<String> getDefined() {
            return Collections.unmodifiableSet(defined);
        }

        Set<String> getVisibleModified() {
            return NncUtils.diffSet(modified, defined);
        }

        Map<String, Value> toMap() {
            return Collections.unmodifiableMap(variables);
        }

        public void logVariables() {
            var map = variables;
            log.debug("VariableMap@{} contains {} variables", System.identityHashCode(this), map.size());
            map.forEach((name, expr) -> log.debug("{}: {}", name, expr.getText()));
        }

        public void define(String name) {
            defined.add(name);
        }
    }

    private static class CondSection {
        final NodeRT branchNode;
        Long currentBranch;
        final VariableMap entryMap;
        final Map<Long, VariableMap> branchMaps = new HashMap<>();
        final Map<Long, Value> yields = new HashMap<>();

        private CondSection(NodeRT branchNode, VariableMap entryMap) {
            this.branchNode = branchNode;
            this.entryMap = entryMap;
        }

        void putBranchMap(long branchIndex, VariableMap map) {
            branchMaps.put(branchIndex, map);
        }

        void nextBranch() {
        }

        void setYield(Value yield) {
            requireNonNull(currentBranch, "Not in a branch");
            yields.put(currentBranch, yield);
        }

        Map<Long, Value> getYields() {
            return Collections.unmodifiableMap(yields);
        }

    }

    private static class TrySection {
        private final TryEnterNode tryEnterNode;
        private final Map<NodeRT, VariableMap> raiseVariables = new HashMap<>();

        private TrySection(TryEnterNode tryEnterNode) {
            this.tryEnterNode = tryEnterNode;
        }
    }

}
