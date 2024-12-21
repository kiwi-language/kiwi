package org.metavm.autograph;

import com.intellij.psi.PsiCodeBlock;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

public class TrySection extends Section {
    private final @Nullable PsiCodeBlock finallyBlock;

    public TrySection(@Nullable PsiCodeBlock finallyBlock, @Nullable Section parent) {
        super(parent, null);
        this.finallyBlock = finallyBlock;
    }

    public void handleExit(MethodGenerator methodGenerator) {
        methodGenerator.createTryExit();
        NncUtils.ifNotNull(finallyBlock, b -> b.accept(methodGenerator.getVisitor()));
    }

    public void handleReturn(MethodGenerator methodGenerator) {
        Section s = this;
        do {
            if (s instanceof TrySection t)
                t.handleExit(methodGenerator);
            s = s.getParent();
        } while (s != null);
    }

}
