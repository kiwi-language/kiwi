package org.metavm.autograph;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLoopStatement;
import com.intellij.psi.PsiStatement;
import lombok.extern.slf4j.Slf4j;
import org.metavm.flow.GotoNode;
import org.metavm.flow.Node;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class BlockInfo {

    private final @Nullable BlockInfo parent;
    private final @Nullable String label;
    private final PsiElement element;
    private final List<GotoNode> breaks = new ArrayList<>();
    private final List<GotoNode> continues = new ArrayList<>();
    private final boolean isLoop;

    public BlockInfo(@Nullable BlockInfo parent, PsiElement element) {
        this.parent = parent;
        this.element = element;
        this.label = element instanceof PsiStatement stmt ? TranspileUtils.getLabel(stmt) : null;
        isLoop = element instanceof PsiLoopStatement;
    }

    @Nullable
    public BlockInfo getParent() {
        return parent;
    }

    @Nullable
    public String getLabel() {
        return label;
    }

    public void addBreak(GotoNode node, @Nullable String target) {
        if(target == null || target.equals(label))
            breaks.add(node);
        else if(parent != null)
            parent.addBreak(node, target);
        else
            throw new IllegalStateException("Can not find target loop for \"break " + target + "\"");
    }

    public void addContinue(GotoNode node, @Nullable String target) {
        if(isLoop && (target == null || target.equals(label)))
            continues.add(node);
        else if(parent != null)
            parent.addContinue(node, target);
        else
            throw new IllegalStateException("Can not find target loop for \"continue " + target + "\"");
    }

    public List<GotoNode> getBreaks() {
        return Collections.unmodifiableList(breaks);
    }

    public List<GotoNode> getContinues() {
        return Collections.unmodifiableList(continues);
    }

    public boolean hasBreaks() {
        return !breaks.isEmpty();
    }

    public void connect(Node continueTarget, Node breakTarget) {
        for (GotoNode c : continues) {
            c.setTarget(continueTarget);
        }
        connectBreaks(breakTarget);
    }

    public void connectBreaks(Node target) {
        for (GotoNode b : breaks) {
            b.setTarget(target);
        }
    }

    public void printBlocks() {
        log.debug("{} {}", element.getClass().getSimpleName(), label);
        if(parent != null)
            parent.printBlocks();
    }

}
