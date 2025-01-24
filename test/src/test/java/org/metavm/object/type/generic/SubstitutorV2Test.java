package org.metavm.object.type.generic;

import junit.framework.TestCase;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.NameAndType;
import org.metavm.flow.Nodes;
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
        var voidType = PrimitiveType.voidType;

        var typeVar = new TypeVariable(null, "E", DummyGenericDeclaration.INSTANCE);
        Klass foo = TestUtils.newKlassBuilder("Foo", "Foo")
                .typeParameters(typeVar)
                .build();

        var valueField = FieldBuilder.newBuilder("value", foo, typeVar.getType())
                .build();
        {
            var getValueFlow = MethodBuilder.newBuilder(foo, "getValue")
                    .returnType(typeVar.getType())
                    .build();
            var code = getValueFlow.getCode();
            Nodes.thisField(valueField.getRef(), code);
            Nodes.ret(code);
        }

        {
            var flow = MethodBuilder.newBuilder(foo, "setValue")
                    .returnType(voidType)
                    .parameters(new NameAndType("value", typeVar.getType()))
                    .build();
            var code = flow.getCode();
            Nodes.this_(code);
            Nodes.argument(flow, 0);
            Nodes.setField(valueField.getRef(), code);
            Nodes.voidRet(code);
        }

        var stringType = PrimitiveType.stringType;
    }

}