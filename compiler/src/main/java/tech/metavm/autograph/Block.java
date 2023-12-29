package tech.metavm.autograph;

import com.intellij.psi.PsiCodeBlock;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class Block {
    final @Nullable Block parent;
    final PsiCodeBlock element;
    final Set<String> declaredNames = new HashSet<>();

    Block(@Nullable Block parent, PsiCodeBlock element) {
        this.parent = parent;
        this.element = element;
    }

    PsiCodeBlock block() {
        return element;
    }

    public String nextName(String prefix) {
        String name = prefix;
        int num = 1;
        while (containsNameDecl(name)) {
            name = prefix + "_" + num++;
        }
        declName(name);
        return name;
    }

    public boolean containsNameDecl(String name) {
        return declaredNames.contains(name) || parent != null && parent.containsNameDecl(name);
    }

    public void declName(String name) {
        declaredNames.add(name);
        if (parent != null) {
            parent.declName(name);
        }
    }

}
