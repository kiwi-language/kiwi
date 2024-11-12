package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.Element;
import org.metavm.entity.StructuralVisitor;

import java.util.Objects;

@Slf4j
public class MaxesComputer extends StructuralVisitor<Void> {

    private CallableInfo currentCallable;

    @Override
    public Void defaultValue(Element element) {
        return null;
    }

    @Override
    public Void visitMethod(Method method) {
        if(!method.isRootScopePresent())
            return null;
//        if(method.getQualifiedName().equals("MyList.fromView")) {
//            log.debug("Visiting method: {}", method.getQualifiedSignature());
//            DebugEnv.flag = true;
//        }
        var c = enterCallable(method);
        c.setMaxLocals(method.isStatic() ? method.getParameters().size() : method.getParameters().size() + 1);
        super.visitMethod(method);
//        DebugEnv.flag = false;
        exitCallable();
        method.getScope().setMaxLocals(c.maxLocals);
        method.getScope().setMaxStack(c.maxStacks);
        return null;
    }

    @Override
    public Void visitFunction(Function function) {
        if(!function.isRootScopePresent())
            return null;
        var c = enterCallable(function);
        c.setMaxLocals(function.getParameters().size());
        super.visitFunction(function);
        exitCallable();
        function.getScope().setMaxLocals(c.maxLocals);
        function.getScope().setMaxStack(c.maxStacks);
        return null;
    }

    @Override
    public Void visitLambda(Lambda lambda) {
        var c = enterCallable(lambda);
        c.setMaxLocals(lambda.getParameters().size());
        super.visitLambda(lambda);
        exitCallable();
        lambda.getScope().setMaxLocals(c.maxLocals);
        lambda.getScope().setMaxStack(c.maxStacks);
        return null;
    }

    @Override
    public Void visitNode(NodeRT node) {
        currentCallable().changeStack(node.getStackChange());
//        if(DebugEnv.flag)
//            log.debug("Stack size at " + node.getName() + ": {}, {}",
//                    currentCallable().currentStacks, currentCallable().maxStacks);
        return null;
    }

    @Override
    public Void visitVariableAccessNode(VariableAccessNode node) {
        super.visitVariableAccessNode(node);
        currentCallable().setMaxLocals(node.getIndex() + 1);
        return null;
    }

    private CallableInfo enterCallable(Callable callable) {
        return currentCallable = new CallableInfo(callable, currentCallable);
    }

    private void exitCallable() {
        currentCallable = currentCallable.parent;
    }

    private CallableInfo currentCallable() {
        return Objects.requireNonNull(currentCallable);
    }

    private static class CallableInfo {
        final CallableInfo parent;
        final Callable callable;
        private int maxLocals;
        private int maxStacks;
        private int currentStacks;

        private CallableInfo(Callable callable, CallableInfo parent) {
            this.callable = callable;
            this.parent = parent;
        }

        public void setMaxLocals(int maxLocals) {
            this.maxLocals = Math.max(this.maxLocals, maxLocals);
        }

        public void changeStack(int change) {
            currentStacks += change;
            maxStacks = Math.max(maxStacks, currentStacks);
        }

    }

}
