package tech.metavm.autograph;

import tech.metavm.expression.Expression;
import tech.metavm.flow.*;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class VariableTable {

    private VariableMap variableMap = new VariableMap();
//    private final Map<BranchNode, Map<Branch, VariableMap>> condVariableMaps = new HashMap<>();
//    private final Map<BranchNode, VariableMap> condEntry = new HashMap<>();
//    private final LinkedList<Branch> branchStack = new LinkedList<>();

    private final Map<BranchNode, CondSection> condSections = new HashMap<>();

    private final LinkedList<TrySection> trySections = new LinkedList<>();

    Expression get(String name) {
        return variableMap.getVariable(name);
    }

    void set(String name, Expression value) {
        variableMap.setVariable(name, value);
    }

    void enterCondSection(BranchNode sectionId) {
        condSections.put(sectionId, new CondSection(variableMap));
    }

    void newCondBranch(BranchNode sectionId, Branch branch) {
        var section = condSections.get(sectionId);
        section.currentBranch = branch;
        variableMap = section.entryMap.copy();
        section.putBranchMap(branch, variableMap);
    }

    Map<Branch, Map<String, Expression>> exitCondSection(BranchNode sectionId, List<String> outputVars) {
        var section = condSections.remove(sectionId);
        Map<Branch, Map<String, Expression>> result = new HashMap<>();
        for (var entry : section.branchMaps.entrySet()) {
            var branch = entry.getKey();
            var varMap = entry.getValue();
            Map<String, Expression> branchOutputs = new HashMap<>();
            result.put(branch, branchOutputs);
            for (String outputVar : outputVars) {
                branchOutputs.put(outputVar, varMap.getVariable(outputVar));
            }
        }
        variableMap = section.entryMap;
        return result;
    }

    void enterTrySection(TryNode tryNode) {
        trySections.push(new TrySection(tryNode));
    }

    Map<NodeRT<?>, Map<String, Expression>> exitTrySection(TryNode tryNode, List<String> outputVars) {
        var trySection = trySections.pop();
        NncUtils.requireTrue(trySection.tryNode == tryNode);
        Map<NodeRT<?>, Map<String, Expression>> result = new HashMap<>();
        trySection.raiseVariables.forEach((raiseNode, variableMap) -> {
            result.put(
                    raiseNode,
                    NncUtils.toMap(outputVars, Function.identity(), variableMap::getVariable)
            );
        });
        return result;
    }

    void processRaiseNode(RaiseNode raiseNode) {
        if(trySections.isEmpty()) {
            return;
        }
        var trySection = trySections.peek();
        trySection.raiseVariables.put(raiseNode, variableMap.copy());
    }

    private static class VariableMap {

        private final Map<String, Expression> variables = new HashMap<>();

        public VariableMap() {}

        public VariableMap(VariableMap variableMap) {
            variables.putAll(variableMap.variables);
        }

        VariableMap copy() {
            return new VariableMap(this);
        }

        Expression getVariable(String name) {
            return variables.get(name);
        }

        void setVariable(String name, Expression value) {
            variables.put(name, value);
        }

    }

    private static class CondSection {
        Branch currentBranch;
        Branch outputBranch;
        final VariableMap entryMap;
        final Map<Branch, VariableMap> branchMaps = new HashMap<>();

        private CondSection(VariableMap entryMap) {
            this.entryMap = entryMap;
        }

        void putBranchMap(Branch branch, VariableMap map) {
            branchMaps.put(branch, map);
        }

    }

    private static class TrySection {
        private final TryNode tryNode;
        private final Map<NodeRT<?>, VariableMap> raiseVariables = new HashMap<>();

        private TrySection(TryNode tryNode) {
            this.tryNode = tryNode;
        }
    }

}
