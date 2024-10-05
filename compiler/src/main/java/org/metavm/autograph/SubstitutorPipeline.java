package org.metavm.autograph;

import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class SubstitutorPipeline {

    public static final Logger logger = LoggerFactory.getLogger(SubstitutorPipeline.class);

    private final PsiSubstitutor substitutor;
    private @Nullable SubstitutorPipeline next;

    public SubstitutorPipeline(PsiSubstitutor substitutor) {
        this.substitutor = substitutor;
    }

    public PsiType substitute(PsiType type) {
        var r = substitutor.substitute(type);
        if(next != null)
            r = next.substitute(r);
        return r;
    }

    public void append(SubstitutorPipeline next) {
        if(this.next == null)
            this.next = next;
        else
            this.next.append(next);
    }

    public int getDepth() {
        return next == null ? 1 : next.getDepth() + 1;
    }

    @Override
    public String toString() {
        return next != null ? substitutor + ", " + next : substitutor.toString();
    }
}
