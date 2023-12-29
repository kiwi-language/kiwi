package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassBuilder;
import tech.metavm.object.view.ArrayMapping;
import tech.metavm.object.view.DefaultObjectMapping;

import java.util.List;

public class EntityMemoryIndexTest extends TestCase {

    public void test() {
        var fooType = ClassBuilder.newBuilder("Foo", "Foo").build();
        var fooViewType = ClassBuilder.newBuilder("FooView", "FooView").ephemeral(true).build();
        var fooArrayType = new ArrayType(null, fooType, ArrayKind.READ_WRITE);
        var fooViewArrayType = new ArrayType(null, fooViewType, ArrayKind.CHILD);
        var fooMapping = new DefaultObjectMapping(
                null, "FooBuiltinMapping", "FooBuiltinMapping", fooType,
                true, fooViewType, List.of()
        );
        var arrayMapping = new ArrayMapping(null, fooArrayType, fooViewArrayType, fooMapping);
        var index = new EntityMemoryIndex();
        index.save(arrayMapping);
        var result = index.selectByUniqueKey(ArrayMapping.IDX, List.of(fooArrayType, fooViewArrayType, fooMapping));
        Assert.assertSame(arrayMapping, result);
    }

}