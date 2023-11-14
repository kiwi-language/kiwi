package tech.metavm.object.instance.core;

public abstract class InstanceVisitor {

    public void visitInstance(Instance instance) {
    }

    public void visitClassInstance(ClassInstance instance) {
        visitInstance(instance);
    }

    public void visitArrayInstance(ArrayInstance instance) {
        visitInstance(instance);
    }

    public void visitPrimitiveInstance(PrimitiveInstance instance) {
        visitInstance(instance);
    }

    public void visitNullInstance(NullInstance instance) {
        visitPrimitiveInstance(instance);
    }

    public void visitLongInstance(LongInstance instance) {
        visitNumberInstance(instance);
    }

    public void visitStringInstance(StringInstance instance) {
        visitPrimitiveInstance(instance);
    }

    public void visitDoubleInstance(DoubleInstance instance) {
        visitNumberInstance(instance);
    }

    public void visitBooleanInstance(BooleanInstance instance) {
        visitPrimitiveInstance(instance);
    }

    public void visitPasswordInstance(PasswordInstance instance) {
        visitPrimitiveInstance(instance);
    }

    public void visitNumberInstance(NumberInstance instance) {
        visitPrimitiveInstance(instance);
    }

    public void visitTimeInstance(TimeInstance instance) {
        visitPrimitiveInstance(instance);
    }

    public void visitFunctionInstance(FunctionInstance instance) {
        visitInstance(instance);
    }

    public void visitFlowInstance(FlowInstance instance) {
        visitFunctionInstance(instance);
    }

    public void visitLambdaInstance(LambdaInstance instance) {
        visitFunctionInstance(instance);
    }
}
