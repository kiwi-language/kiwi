package org.metavm.autograph;

import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import org.metavm.util.InternalException;

import java.util.*;

public class GraphBuilder {

    private final String title;
    private CfgNode head;
    private final Map<PsiElement, CfgNode> nodeIndex = new HashMap<>();
    // enclosing block statements
    private final Map<CfgNode, Set<PsiElement>> owners = new HashMap<>();
    private final Set<PsiElement> activeBlockStatements = new HashSet<>();
    private final Set<CfgNode> leaves = new HashSet<>();
    private final Map<PsiElement, Set<CfgNode>> condEntry = new HashMap<>();
    private final Map<PsiElement, List<Set<CfgNode>>> condLeaves = new HashMap<>();
    private final Map<PsiElement, CfgNode> sectionEntry = new HashMap<>();
    private final Map<PsiElement, Set<CfgNode>> continues = new HashMap<>();
    private final Set<PsiElement> pendingFinallySections = new HashSet<>();
    private final Map<PsiElement, FinallySection> finallySectionSubGraphs = new HashMap<>();
    private final Map<CfgNode, List<PsiElement>> finallySections = new HashMap<>();
    private final Map<PsiElement, List<CfgNode>> exits = new HashMap<>();
    private final Map<PsiElement, List<CfgNode>> throwNodes = new HashMap<>();
    private final Map<CfgNode, CfgNode> forwardEdges = new HashMap<>();
    private final Set<PsiElement> errors = new HashSet<>();
    private final Set<PsiElement> defaultCaseFlags = new HashSet<>();

    public GraphBuilder(String title) {
        this.title = title;
    }

    CfgNode addOrdinaryNode(PsiElement astNode) {
        var node = addNewNode(astNode);
        setLeaves(List.of(node));
        return node;
    }

    private CfgNode addJumpNode(PsiElement astNode, List<PsiCodeBlock> guards) {
        var node = addNewNode(astNode);
        setLeaves(List.of());
        finallySections.put(node, new ArrayList<>(guards));
        return node;
    }

    void connectThrowNode(CfgNode node, List<PsiCodeBlock> exceptGuards) {
        for (PsiElement guard : exceptGuards) {
            throwNodes.computeIfAbsent(guard, k -> new ArrayList<>()).add(node);
        }
    }

    void enterSection(PsiElement sectionId) {
        exits.put(sectionId, new ArrayList<>());
    }

    void exitSection(PsiElement sectionId) {
        for (CfgNode exit : exits.get(sectionId)) {
            leaves.addAll(connectJumpToFinnallySections(exit));
        }
        exits.remove(sectionId);
    }

    private void setLeaves(Collection<CfgNode> leaves) {
        this.leaves.clear();
        this.leaves.addAll(leaves);
    }

    private CfgNode addNewNode(PsiElement astNode) {
        if (nodeIndex.containsKey(astNode)) {
            throw new InternalException(astNode + " is added twice");
        }
        var node = new CfgNode(astNode);
        nodeIndex.put(astNode, node);
        owners.put(node, new HashSet<>(activeBlockStatements));
        if (head == null) head = node;
        else connectNodes(leaves, node);
        for (PsiElement sectionId : pendingFinallySections) {
            finallySectionSubGraphs.get(sectionId).begin = node;
        }
        pendingFinallySections.clear();
        return node;
    }

    void setDefaultCaseFlag(PsiElement sectionId) {
        defaultCaseFlags.add(sectionId);
    }

    boolean getDefaultCaseFlag(PsiElement sectionId) {
        return defaultCaseFlags.contains(sectionId);
    }

    CfgNode addExitNode(PsiElement astNode, PsiElement sectionId, List<PsiCodeBlock> guards) {
        var node = addJumpNode(astNode, guards);
        exits.get(sectionId).add(node);
        return node;
    }

    CfgNode addContinueNode(PsiElement astNode, PsiElement sectionId, List<PsiCodeBlock> guards) {
        var node = addJumpNode(astNode, guards);
        continues.get(sectionId).add(node);
        return node;
    }

    void beginBlockStatement(PsiElement statement) {
        activeBlockStatements.add(statement);
    }

    void endBlockStatement(PsiElement statement) {
        activeBlockStatements.remove(statement);
    }

    private Set<CfgNode> connectJumpToFinnallySections(CfgNode node) {
        Set<CfgNode> cursor = new HashSet<>(List.of(node));
        if (!finallySections.containsKey(node)) return cursor;
        for (PsiElement guardSectionId : finallySections.get(node)) {
            var guard = finallySectionSubGraphs.get(guardSectionId);
            connectNodes(cursor, guard.begin);
            cursor = guard.ends;
        }
        finallySections.remove(node);
        return cursor;
    }

    private void connectNodes(Collection<CfgNode> first, CfgNode second) {
        connectNodes(first, second, false);
    }

    private void connectNodes(Collection<CfgNode> first, CfgNode second, boolean isBackEdge) {
        first.forEach(node -> connectNodes(node, second, isBackEdge));
    }

    private void connectNodes(CfgNode first, CfgNode second) {
        connectNodes(first, second, false);
    }

    private void connectNodes(CfgNode first, CfgNode second, boolean isBackEdge) {
        first.addNext(second, isBackEdge);
        forwardEdges.put(first, second);
    }

    void enterLoopSection(PsiElement sectionId, PsiElement entryNode) {
        continues.put(sectionId, new HashSet<>());
        sectionEntry.put(sectionId, addOrdinaryNode(entryNode));
    }

    void exitLoopSection(PsiElement sectionId) {
        var entry = sectionEntry.get(sectionId);
        connectNodes(leaves, entry, true);
        for (CfgNode reentry : continues.get(sectionId)) {
            connectNodes(connectJumpToFinnallySections(reentry), entry, true);
        }
        leaves.clear();
        leaves.add(entry);
        sectionEntry.remove(sectionId);
        continues.remove(sectionId);
    }

    void enterCondSection(PsiElement sectionId) {
        condLeaves.put(sectionId, new ArrayList<>());
    }

    void newCondBranch(PsiElement sectionId) {
        newCondBranch(sectionId, false);
    }

    void newCondBranch(PsiElement sectionId, boolean fallThrough) {
        if (!condEntry.containsKey(sectionId)) { // first split
            condEntry.put(sectionId, new HashSet<>(leaves));
        } else { // subsequent splits
            if (fallThrough) leaves.addAll(condEntry.get(sectionId));
            else {
                condLeaves.get(sectionId).add(new HashSet<>(leaves));
                setLeaves(condEntry.get(sectionId));
            }
        }
    }

    void exitCondSection(PsiElement sectionId) {
        for (Set<CfgNode> split : condLeaves.get(sectionId)) {
            leaves.addAll(split);
        }
        condEntry.remove(sectionId);
        condLeaves.remove(sectionId);
        defaultCaseFlags.remove(sectionId);
    }

    void enterExceptSection(PsiElement sectionId) {
        if (throwNodes.containsKey(sectionId)) {
            leaves.addAll(throwNodes.get(sectionId));
        }
    }

    void enterFinallySection(PsiElement sectionId) {
        var section = new FinallySection();
        finallySectionSubGraphs.put(sectionId, section);
        section.hasDirectFlow = !leaves.isEmpty();
        pendingFinallySections.add(sectionId);
    }

    void exitFinallySection(PsiElement sectionId) {
        var section = finallySectionSubGraphs.get(sectionId);
        section.ends = new HashSet<>(leaves);
        if (!section.hasDirectFlow) setLeaves(List.of());
    }

    void addError(PsiElement error) {
        errors.add(error);
    }

    Graph build() {
        Map<PsiElement, Set<CfgNode>> stmtPrev = new HashMap<>();
        Map<PsiElement, Set<CfgNode>> stmtNext = new HashMap<>();
        for (CfgNode node : nodeIndex.values()) {
            for (PsiElement stmt : owners.get(node)) {
                stmtPrev.computeIfAbsent(stmt, k -> new HashSet<>());
                stmtNext.computeIfAbsent(stmt, k -> new HashSet<>());
            }
        }
        for (var _e : forwardEdges.entrySet()) {
            CfgNode first = _e.getKey(), second = _e.getValue();
            Set<PsiElement> firstOwners = owners.get(first), secondOwners = owners.get(second);
            for (PsiElement stmt : firstOwners) {
                if (!secondOwners.contains(stmt)) {
                    stmtNext.get(stmt).add(second);
                }
            }
            for (PsiElement stmt : secondOwners) {
                if (!firstOwners.contains(stmt)) {
                    stmtPrev.get(stmt).add(first);
                }
            }
        }
        return new Graph(
                title,
                head,
                new HashSet<>(leaves),
                new HashSet<>(errors),
                new HashMap<>(nodeIndex),
                new HashMap<>(stmtPrev),
                new HashMap<>(stmtNext)
        );
    }

    private static class FinallySection {
        private CfgNode begin;
        private Set<CfgNode> ends;
        private boolean hasDirectFlow;
    }

}
