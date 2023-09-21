package tech.metavm.autograph;

import com.intellij.psi.PsiElement;
import tech.metavm.expression.Expression;
import tech.metavm.flow.Branch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VariableTable {

    private VariableMap variableMap = new VariableMap();
    private final Map<PsiElement, Map<Branch, VariableMap>> condVariableMaps = new HashMap<>();
    private final Map<PsiElement, VariableMap> condEntry = new HashMap<>();
    private final LinkedList<Branch> branchStack = new LinkedList<>();

    Expression get(String name) {
        return variableMap.getVariable(name);
    }

    void set(String name, Expression value) {
        variableMap.setVariable(name, value);
    }

    void enterCondSection(PsiElement sectionId) {
        condVariableMaps.put(sectionId, new HashMap<>());
    }

    void newCondBranch(PsiElement sectionId, Branch branch) {
        if(!condEntry.containsKey(sectionId)) { // first branch
            condEntry.put(sectionId, variableMap.copy());
        }
        else {
            condVariableMaps.get(sectionId).put(branchStack.pop(), variableMap);
            variableMap = condEntry.get(sectionId).copy();
        }
        branchStack.push(branch);
    }

    Map<Branch, Map<String, Expression>> exitCondSection(PsiElement sectionId, List<String> outputVars) {
        var varMaps = condVariableMaps.remove(sectionId);
        Map<Branch, Map<String, Expression>> result = new HashMap<>();
        for (var entry : varMaps.entrySet()) {
            var branch = entry.getKey();
            var varMap = entry.getValue();
            Map<String, Expression> branchOutputs = new HashMap<>();
            result.put(branch, branchOutputs);
            for (String outputVar : outputVars) {
                branchOutputs.put(outputVar, varMap.getVariable(outputVar));
            }
        }
        Map<String, Expression> lastBranchOutputs = new HashMap<>();
        var lastBranch = branchStack.pop();
        result.put(lastBranch, lastBranchOutputs);
        for (String outputVar : outputVars) {
            lastBranchOutputs.put(outputVar, variableMap.getVariable(outputVar));
        }
        return result;
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

}
