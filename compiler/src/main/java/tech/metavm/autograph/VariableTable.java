package tech.metavm.autograph;

import tech.metavm.expression.Expression;
import tech.metavm.expression.ExpressionTypeMap;
import tech.metavm.expression.ExpressionUtil;
import tech.metavm.expression.TypeNarrower;
import tech.metavm.flow.*;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Function;

public class VariableTable {

    private VariableMap variableMap = new VariableMap();
    private final Map<BranchNode, CondSection> condSections = new HashMap<>();

    private final LinkedList<TrySection> trySections = new LinkedList<>();

    Expression get(String name) {
        return variableMap.getVariable(name);
    }

    void set(String name, Expression value) {
        variableMap.setVariable(name, value);
    }

    void enterCondSection(BranchNode branchNode) {
        condSections.put(branchNode, new CondSection(branchNode, variableMap));
    }

    void addBranchEntry(ExpressionTypeMap entry, BranchNode branchNode) {
        var section = NncUtils.requireNonNull(condSections.get(branchNode));
        section.nextBranchEntries.add(entry);
    }

    ExpressionTypeMap nextBranch(BranchNode sectionId, Branch branch) {
        var section = condSections.get(sectionId);
        section.currentBranch = branch;
        variableMap = section.entryMap.copy();
        section.putBranchMap(branch, variableMap);
        return section.nextBranch(branch);
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
        trySection.raiseVariables.forEach((raiseNode, variableMap) ->
            result.put(
                    raiseNode,
                    NncUtils.toMap(outputVars, Function.identity(), variableMap::getVariable)
            )
        );
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
            return NncUtils.get(variables.get(name), Expression::copy);
        }

        void setVariable(String name, Expression value) {
            variables.put(name, value);
        }

    }

    private static class CondSection {
        final BranchNode branchNode;
        Branch currentBranch;
        Expression nextBranchCond;
        final VariableMap entryMap;
        final Map<Branch, VariableMap> branchMaps = new HashMap<>();

        private final List<ExpressionTypeMap> nextBranchEntries = new ArrayList<>();

        private CondSection(BranchNode branchNode, VariableMap entryMap) {
            this.branchNode = branchNode;
            this.entryMap = entryMap;
        }

        void putBranchMap(Branch branch, VariableMap map) {
            branchMaps.put(branch, map);
        }

        ExpressionTypeMap nextBranch(Branch branch) {
            var narrower = new TypeNarrower(branchNode.getExpressionTypes()::getType);
            ExpressionTypeMap extraExprTypeMap = null;
            if(nextBranchCond != null && !ExpressionUtil.isConstantFalse(nextBranchCond)) {
                extraExprTypeMap = narrower.narrowType(nextBranchCond);
            }
            for (var entry : nextBranchEntries) {
                if(extraExprTypeMap == null) {
                    extraExprTypeMap = entry;
                }
                else {
                    extraExprTypeMap = entry.union(extraExprTypeMap);
                }
            }
            var exprTypeMap = narrower.narrowType(branch.getCondition().getExpression());
            nextBranchEntries.clear();
            nextBranchCond = nextBranchCond == null ?
                    ExpressionUtil.not(branch.getCondition().getExpression()) :
                    ExpressionUtil.and(nextBranchCond, ExpressionUtil.not(branch.getCondition().getExpression()));
            return extraExprTypeMap != null ? exprTypeMap.merge(extraExprTypeMap) : exprTypeMap;
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
