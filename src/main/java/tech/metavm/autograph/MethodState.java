package tech.metavm.autograph;

import com.intellij.psi.PsiMethod;

public class MethodState extends State{

    private PsiMethod method;

    public MethodState(State parent, StateStack stack) {
        super(parent, stack);
    }

    public void setMethod(PsiMethod method) {
        this.method = method;
    }

    public PsiMethod getMethod() {
        return method;
    }
}
