package org.metavm.autograph;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLoopStatement;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiSwitchBlock;
import lombok.extern.slf4j.Slf4j;
import org.metavm.flow.GotoNode;
import org.metavm.flow.Node;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class Section {

    private final @Nullable Section parent;
    private final @Nullable String label;
    private final @Nullable PsiElement element;
    private final List<GotoNode> breaks = new ArrayList<>();
    private final List<GotoNode> continues = new ArrayList<>();
    private final boolean isLoop;
    private final boolean isSwitch;

    public Section(@Nullable Section parent, @Nullable PsiElement element) {
        this.parent = parent;
        this.element = element;
        this.label = element instanceof PsiStatement stmt ? TranspileUtils.getLabel(stmt) : null;
        isLoop = element instanceof PsiLoopStatement;
        isSwitch = element instanceof PsiSwitchBlock;
    }

    @Nullable
    public Section getParent() {
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

    boolean isBreakTarget(@Nullable String label) {
        return label != null ? label.equals(this.label) : isLoop || isSwitch;
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

    public void connectBreaks(Node exit) {
        breaks.forEach(b -> b.setTarget(exit));
    }

    public void printBlocks() {
        log.debug("{} {}", element != null ? element.getClass().getSimpleName() : null, label);
        if(parent != null)
            parent.printBlocks();
    }

    public boolean isContinueTarget(String label) {
        return label != null ? label.equals(this.label) : isLoop;
    }
}
