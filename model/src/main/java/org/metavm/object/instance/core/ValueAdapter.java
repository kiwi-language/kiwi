package org.metavm.object.instance.core;

import org.jetbrains.annotations.Nullable;
import org.metavm.wire.*;
import org.metavm.util.*;

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
                org.metavm.object.type.UncertainType.class,
                org.metavm.object.type.ClassType.class,
                org.metavm.object.type.NeverType.class,
                org.metavm.entity.Reference.class,
                org.metavm.object.type.NullType.class,
                org.metavm.flow.LambdaRef.class,
                org.metavm.object.instance.core.DoubleValue.class,
                org.metavm.flow.MethodRef.class,
                org.metavm.object.type.UnionType.class,
                org.metavm.entity.GenericDeclarationRef.class,
                org.metavm.object.instance.core.LambdaValue.class,
                org.metavm.object.type.PropertyRef.class,
                org.metavm.object.instance.core.NumberValue.class,
                org.metavm.object.type.KlassType.class,
                org.metavm.object.instance.core.ShortValue.class,
                org.metavm.object.type.Type.class,
                org.metavm.object.instance.core.FunctionValue.class,
                org.metavm.object.instance.core.TimeValue.class,
                org.metavm.object.type.VariableType.class,
                org.metavm.object.instance.core.BooleanValue.class,
                org.metavm.object.type.AnyType.class,
                org.metavm.object.type.StringType.class,
                org.metavm.flow.ParameterRef.class,
                org.metavm.object.instance.core.FlowValue.class,
                org.metavm.flow.CallableRef.class,
                org.metavm.object.type.IndexRef.class,
                org.metavm.object.type.CapturedType.class,
                org.metavm.object.instance.core.EphemeralValue.class,
                org.metavm.object.type.IntersectionType.class,
                org.metavm.object.type.ArrayType.class,
                org.metavm.object.instance.core.IntValue.class,
                org.metavm.object.instance.core.CharValue.class,
                org.metavm.flow.FunctionRef.class,
                org.metavm.object.instance.core.NativeValue.class,
                org.metavm.object.instance.core.ByteValue.class,
                org.metavm.object.type.FieldRef.class,
                org.metavm.object.instance.core.PrimitiveValue.class,
                org.metavm.object.type.FunctionType.class,
                org.metavm.object.instance.core.LongValue.class,
                org.metavm.object.instance.core.FloatValue.class,
                org.metavm.object.type.CompositeType.class,
                org.metavm.object.instance.core.PasswordValue.class,
                org.metavm.object.instance.core.NullValue.class,
                org.metavm.object.type.PrimitiveType.class,
                org.metavm.flow.FlowRef.class
        );
    }

    @Override
    public int getTag() {
        return -1;
    }
}
