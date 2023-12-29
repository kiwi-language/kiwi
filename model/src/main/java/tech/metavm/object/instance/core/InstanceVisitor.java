package tech.metavm.object.instance.core;

public abstract class InstanceVisitor<R> {

    public R visit(Instance instance) {
        return instance.accept(this);
    }

    public abstract R visitInstance(Instance instance);

    public R visitDurableInstance(DurableInstance instance) {
        return visitInstance(instance);
    }

    public R visitClassInstance(ClassInstance instance) {
        return visitDurableInstance(instance);
    }

    public R visitArrayInstance(ArrayInstance instance) {
        return visitDurableInstance(instance);
    }

    public R visitPrimitiveInstance(PrimitiveInstance instance) {
        return visitInstance(instance);
    }

    public R visitNullInstance(NullInstance instance) {
        return visitPrimitiveInstance(instance);
    }

    public R visitLongInstance(LongInstance instance) {
        return visitNumberInstance(instance);
    }

    public R visitStringInstance(StringInstance instance) {
        return visitPrimitiveInstance(instance);
    }

    public R visitDoubleInstance(DoubleInstance instance) {
        return visitNumberInstance(instance);
    }

    public R visitBooleanInstance(BooleanInstance instance) {
        return visitPrimitiveInstance(instance);
    }

    public R visitPasswordInstance(PasswordInstance instance) {
        return visitPrimitiveInstance(instance);
    }

    public R visitNumberInstance(NumberInstance instance) {
        return visitPrimitiveInstance(instance);
    }

    public R visitTimeInstance(TimeInstance instance) {
        return visitPrimitiveInstance(instance);
    }

    public R visitFunctionInstance(FunctionInstance instance) {
        return visitInstance(instance);
    }

    public R visitFlowInstance(FlowInstance instance) {
        return visitFunctionInstance(instance);
    }

    public R visitLambdaInstance(LambdaInstance instance) {
        return visitFunctionInstance(instance);
    }
}
