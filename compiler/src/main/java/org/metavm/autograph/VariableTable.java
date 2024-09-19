package org.metavm.autograph;

import lombok.extern.slf4j.Slf4j;
import org.metavm.expression.*;
import org.metavm.flow.*;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@Slf4j
public class VariableTable {

    private final LinkedList<BranchNode> branchNodes = new LinkedList<>();
    private VariableMap variableMap = new VariableMap();
    private final Map<BranchNode, CondSection> condSections = new HashMap<>();

    private final LinkedList<TrySection> trySections = new LinkedList<>();

    Expression get(String name) {
        return variableMap.getVariable(name);
    }

    void set(String name, Expression value) {
        variableMap.setVariable(name, value);
    }

    void logVariables() {
        variableMap.logVariables();
    }

    void enterCondSection(BranchNode branchNode) {
        branchNodes.push(branchNode);
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

    void setYield(Expression yield) {
        var branchNode = requireNonNull(branchNodes.peek(), "Not in a branch");
        condSections.get(branchNode).setYield(yield);
    }

    Map<Branch, BranchInfo> exitCondSection(BranchNode sectionId) {
        var section = condSections.remove(sectionId);
        Map<Branch, BranchInfo> result = new HashMap<>();
        var outputVars = new HashSet<String>();
        section.branchMaps.values().forEach(varMap -> outputVars.addAll(varMap.getVisibleModified()));
        for (var entry : section.branchMaps.entrySet()) {
            var branch = entry.getKey();
            var varMap = entry.getValue();
            Map<String, Expression> branchOutputs = new HashMap<>();
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

    void enterTrySection(TryNode tryNode) {
        trySections.push(new TrySection(tryNode));
    }

    Map<NodeRT, Map<String, Expression>> exitTrySection(TryNode tryNode, List<String> outputVars) {
        var trySection = trySections.pop();
        NncUtils.requireTrue(trySection.tryNode == tryNode);
        Map<NodeRT, Map<String, Expression>> result = new HashMap<>();
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

    public void define(String name) {
        variableMap.define(name);
    }


    private static class VariableMap {

        private final Map<String, Expression> variables = new HashMap<>();
        private final Set<String> modified = new HashSet<>();
        private final Set<String> defined = new HashSet<>();

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

        Map<String, Expression> toMap() {
            return Collections.unmodifiableMap(variables);
        }

        public void logVariables() {
            var map = variables;
            log.debug("VariableMap@{} contains {} variables", System.identityHashCode(this), map.size());
            map.forEach((name, expr) -> log.debug("{}: {}", name, expr.build(VarType.NAME)));
        }

        public void define(String name) {
            defined.add(name);
        }
    }

    private static class CondSection {
        final BranchNode branchNode;
        Branch currentBranch;
        Expression nextBranchCond;
        final VariableMap entryMap;
        final Map<Branch, VariableMap> branchMaps = new HashMap<>();
        final Map<Branch, Expression> yields = new HashMap<>();

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
            if(nextBranchCond != null && !Expressions.isConstantFalse(nextBranchCond)) {
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
                    Expressions.not(branch.getCondition().getExpression()) :
                    Expressions.and(nextBranchCond, Expressions.not(branch.getCondition().getExpression()));
            return extraExprTypeMap != null ? exprTypeMap.merge(extraExprTypeMap) : exprTypeMap;
        }

        void setYield(Expression yield) {
            requireNonNull(currentBranch, "Not in a branch");
            yields.put(currentBranch, yield);
        }

        Map<Branch, Expression> getYields() {
            return Collections.unmodifiableMap(yields);
        }

    }

    private static class TrySection {
        private final TryNode tryNode;
        private final Map<NodeRT, VariableMap> raiseVariables = new HashMap<>();

        private TrySection(TryNode tryNode) {
            this.tryNode = tryNode;
        }
    }

}
