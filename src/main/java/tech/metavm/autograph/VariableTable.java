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
        return variableMap.get(name);
    }

    void set(String name, Expression value) {
        variableMap.put(name, value);
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
                branchOutputs.put(outputVar, varMap.get(outputVar));
            }
        }
        Map<String, Expression> lastBranchOutputs = new HashMap<>();
        var lastBranch = branchStack.pop();
        result.put(lastBranch, lastBranchOutputs);
        for (String outputVar : outputVars) {
            lastBranchOutputs.put(outputVar, variableMap.get(outputVar));
        }
        return result;
    }

    private static class VariableMap extends HashMap<String, Expression> {

        public VariableMap() {}

        public VariableMap(VariableMap variableMap) {
            putAll(variableMap);
        }

        VariableMap copy() {
            return new VariableMap(this);
        }

    }

}
