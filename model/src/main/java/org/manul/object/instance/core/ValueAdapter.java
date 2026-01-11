package org.manul.object.instance.core;

import org.jetbrains.annotations.Nullable;
import org.manul.wire.*;
import org.manul.util.*;

import java.util.List;

public class ValueAdapter implements WireAdapter<Value> {
    @Override
    public void init(AdapterRegistry registry) {

    }

    @Override
    public Value read(WireInput input, @Nullable Object parent) {
        var mvInput = (MvInput) input;
        return mvInput.readValue();
    }

    @Override
    public void write(Value o, WireOutput output) {
        var mvOutput = (MvOutput) output;
        mvOutput.writeValue(o);
    }

    @Override
    public void visit(WireVisitor visitor) {
        var mvVisitor = (StreamVisitor) visitor;
        mvVisitor.visitValue();
    }

    @Override
    public List<Class<? extends Value>> getSupportedTypes() {
        return List.of(
                Value.class,
                org.manul.object.type.UncertainType.class,
                org.manul.object.type.ClassType.class,
                org.manul.object.type.NeverType.class,
                org.manul.entity.Reference.class,
                org.manul.object.type.NullType.class,
                org.manul.flow.LambdaRef.class,
                org.manul.object.instance.core.DoubleValue.class,
                org.manul.flow.MethodRef.class,
                org.manul.object.type.UnionType.class,
                org.manul.entity.GenericDeclarationRef.class,
                org.manul.object.instance.core.LambdaValue.class,
                org.manul.object.type.PropertyRef.class,
                org.manul.object.instance.core.NumberValue.class,
                org.manul.object.type.KlassType.class,
                org.manul.object.instance.core.ShortValue.class,
                org.manul.object.type.Type.class,
                org.manul.object.instance.core.FunctionValue.class,
                org.manul.object.instance.core.TimeValue.class,
                org.manul.object.type.VariableType.class,
                org.manul.object.instance.core.BooleanValue.class,
                org.manul.object.type.AnyType.class,
                org.manul.object.type.StringType.class,
                org.manul.flow.ParameterRef.class,
                org.manul.object.instance.core.FlowValue.class,
                org.manul.flow.CallableRef.class,
                org.manul.object.type.IndexRef.class,
                org.manul.object.type.CapturedType.class,
                org.manul.object.instance.core.EphemeralValue.class,
                org.manul.object.type.IntersectionType.class,
                org.manul.object.type.ArrayType.class,
                org.manul.object.instance.core.IntValue.class,
                org.manul.object.instance.core.CharValue.class,
                org.manul.flow.FunctionRef.class,
                org.manul.object.instance.core.NativeValue.class,
                org.manul.object.instance.core.ByteValue.class,
                org.manul.object.type.FieldRef.class,
                org.manul.object.instance.core.PrimitiveValue.class,
                org.manul.object.type.FunctionType.class,
                org.manul.object.instance.core.LongValue.class,
                org.manul.object.instance.core.FloatValue.class,
                org.manul.object.type.CompositeType.class,
                org.manul.object.instance.core.PasswordValue.class,
                org.manul.object.instance.core.NullValue.class,
                org.manul.object.type.PrimitiveType.class,
                org.manul.flow.FlowRef.class
        );
    }

    @Override
    public int getTag() {
        return -1;
    }
}
