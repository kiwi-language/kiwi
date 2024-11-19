package org.metavm.object.type.generic;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Nodes;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.*;
import org.metavm.util.TestUtils;

import java.util.List;

public class SubstitutorV2Test extends TestCase {

    public static final String JSON_FILE_PATH = "/Users/leen/workspace/object/test.json";

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void test() {
        var nullType = PrimitiveType.nullType;
        var voidType = PrimitiveType.voidType;

        var typeVar = new TypeVariable(null, "E", DummyGenericDeclaration.INSTANCE);
        Klass foo = TestUtils.newKlassBuilder("Foo", "Foo")
                .typeParameters(typeVar)
                .build();

        var valueField = FieldBuilder.newBuilder("value", foo, typeVar.getType())
                .build();
        {
            var getValueFlow = MethodBuilder.newBuilder(foo, "getValue")
                    .type(new FunctionType(List.of(), typeVar.getType()))
                    .staticType(new FunctionType(List.of(foo.getType()), typeVar.getType()))
                    .returnType(typeVar.getType())
                    .build();
            var code = getValueFlow.getCode();
            Nodes.thisProperty(valueField, code);
            Nodes.ret(code);
        }

        {
            var flow = MethodBuilder.newBuilder(foo, "setValue")
                    .type(new FunctionType(List.of(typeVar.getType()), voidType))
                    .staticType(new FunctionType(List.of(foo.getType(), typeVar.getType()), voidType))
                    .returnType(voidType)
                    .parameters(new Parameter(null, "value", typeVar.getType()))
                    .build();
            var code = flow.getCode();
            Nodes.this_(code);
            Nodes.argument(flow, 0);
            Nodes.setField(valueField, code);
            Nodes.voidRet(code);
        }

        var stringType = PrimitiveType.stringType;
        stringType.initId(PhysicalId.of(1L, 0L, TestUtils.mockClassType()));
    }

    public void testFlow() {
        var fooType = TestUtils.newKlassBuilder("Foo", "Foo").build();
        var barMethod = MethodBuilder.newBuilder(fooType, "bar")
                .typeParameters(List.of(
                        new TypeVariable(null, "T", DummyGenericDeclaration.INSTANCE)
                ))
                .build();
        var bar1 = barMethod.getParameterized(List.of(Types.getStringType()));
        var bar2 = barMethod.getParameterized(List.of(Types.getStringType()));
        Assert.assertSame(bar1, bar2);
    }



}