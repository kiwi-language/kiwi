package org.manul.object.instance.core;

import junit.framework.TestCase;
import org.junit.Assert;
import org.manul.flow.MethodBuilder;
import org.manul.flow.Nodes;
import org.manul.object.instance.IndexKeyRT;
import org.manul.object.type.FieldBuilder;
import org.manul.object.type.Index;
import org.manul.object.type.Types;
import org.manul.util.Instances;
import org.manul.util.TestUtils;

import java.util.List;
import java.util.Map;

public class InstanceMemoryIndexTest extends TestCase {

    public void test() {
        var memIndex = new InstanceMemoryIndex();

        var fooKlass = TestUtils.newKlassBuilder("Foo", "Foo")
                        .build();
        var nameField = FieldBuilder.newBuilder("name", fooKlass, Types.getStringType())
                        .build();

        var getNameMethod = MethodBuilder.newBuilder(fooKlass, "getName")
                .returnType(Types.getStringType())
                .build();
        {
            var code = getNameMethod.getCode();
            Nodes.this_(code);
            Nodes.getField(nameField.getRef(), code);
            Nodes.ret(code);
            code.emitCode();
        }
        var index = new Index(
                fooKlass.nextChildId(), fooKlass, "idxName", "name must be unique", true,
                Types.getStringType(), getNameMethod
        );

        var name = Instances.stringInstance("foo001");

        var foo = ClassInstanceBuilder.newBuilder(fooKlass.getType(), TmpId.random())
                .data(Map.of(nameField, name))
                .build();
        memIndex.save(foo);
        var result = memIndex.selectUnique(new IndexKeyRT(index, List.of(name)));
        Assert.assertSame(foo, result);
    }

}