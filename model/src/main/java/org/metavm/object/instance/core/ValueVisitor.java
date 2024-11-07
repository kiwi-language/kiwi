package org.metavm.object.instance.core;

public abstract class ValueVisitor<R> {

    public R visit(Value value) {
        return value.accept(this);
    }

    public abstract R visitValue(Value value);

//    public R visitDurableInstance(DurableInstance value) {
//        return visitInstance(value);
//    }

//    public R visitClassInstance(ClassInstance value) {
//        return visitDurableInstance(value);
//    }
//
//    public R visitArrayInstance(ArrayInstance value) {
//        return visitDurableInstance(value);
//    }
//
    public R visitPrimitiveValue(PrimitiveValue value) {
        return visitValue(value);
    }

    public R visitNullValue(NullValue value) {
        return visitPrimitiveValue(value);
    }

    public R visitLongValue(LongValue value) {
        return visitNumberValue(value);
    }

    public R visitCharValue(CharValue value) {
        return visitValue(value);
    }

    public R visitStringValue(StringValue value) {
        return visitPrimitiveValue(value);
    }

    public R visitDoubleValue(DoubleValue value) {
        return visitNumberValue(value);
    }

    public R visitBooleanValue(BooleanValue value) {
        return visitPrimitiveValue(value);
    }

    public R visitPasswordValue(PasswordValue value) {
        return visitPrimitiveValue(value);
    }

    public R visitNumberValue(NumberValue value) {
        return visitPrimitiveValue(value);
    }

    public R visitTimeValue(TimeValue value) {
        return visitPrimitiveValue(value);
    }

    public R visitFunctionValue(FunctionValue value) {
        return visitValue(value);
    }

    public R visitFlowValue(FlowValue value) {
        return visitFunctionValue(value);
    }

    public R visitLambdaValue(LambdaValue value) {
        return visitFunctionValue(value);
    }

    public R visitReference(Reference reference) {
        return visitValue(reference);
    }

}
