package org.metavm.object.type.generic;

import junit.framework.TestCase;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.NameAndType;
import org.metavm.flow.Nodes;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.Klass;
import org.metavm.object.type.PrimitiveType;
import org.metavm.object.type.TypeVariable;
import org.metavm.util.TestUtils;

import java.util.List;

public class SubstitutorV2Test extends TestCase {

    public void test() {
        var voidType = PrimitiveType.voidType;
        Klass fooKlass = TestUtils.newKlassBuilder("Foo", "Foo").build();
        var typeVar = new TypeVariable(fooKlass.nextChildId(), "E", fooKlass);
        fooKlass.setTypeParameters(List.of(typeVar));

        var valueField = FieldBuilder.newBuilder("value", fooKlass, typeVar.getType())
                .build();
        {
            var getValueFlow = MethodBuilder.newBuilder(fooKlass, "getValue")
                    .returnType(typeVar.getType())
                    .build();
            var code = getValueFlow.getCode();
            Nodes.thisField(valueField.getRef(), code);
            Nodes.ret(code);
        }

        {
            var flow = MethodBuilder.newBuilder(fooKlass, "setValue")
                    .returnType(voidType)
                    .parameters(new NameAndType("value", typeVar.getType()))
                    .build();
            var code = flow.getCode();
            Nodes.this_(code);
            Nodes.argument(flow, 0);
            Nodes.setField(valueField.getRef(), code);
            Nodes.voidRet(code);
        }
    }

}