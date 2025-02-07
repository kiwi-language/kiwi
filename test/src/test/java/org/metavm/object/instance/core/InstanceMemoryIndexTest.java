package org.metavm.object.instance.core;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Nodes;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.Index;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;
import org.metavm.util.TestUtils;

import java.util.List;
import java.util.Map;

public class InstanceMemoryIndexTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        TestUtils.ensureStringKlassInitialized();
        MockStandardTypesInitializer.init();
    }

    public void test() {
        var memIndex = new InstanceMemoryIndex();

        var fooType = TestUtils.newKlassBuilder("Foo", "Foo")
                        .build();
        var nameField = FieldBuilder.newBuilder("name", fooType, Types.getStringType())
                        .build();

        var getNameMethod = MethodBuilder.newBuilder(fooType, "getName")
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
                fooType, "idxName", "name must be unique", true,
                Types.getStringType(), getNameMethod
        );

        TestUtils.initEntityIds(fooType);

        var name = Instances.stringInstance("foo001");

        var foo = ClassInstanceBuilder.newBuilder(fooType.getType())
                .data(Map.of(nameField, name))
                .build();
        memIndex.save(foo);
        var result = memIndex.selectUnique(new IndexKeyRT(index, List.of(name)));
        Assert.assertSame(foo, result);
    }

}