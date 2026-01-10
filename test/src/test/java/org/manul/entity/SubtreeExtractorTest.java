package org.manul.entity;

import junit.framework.TestCase;
import org.manul.flow.MethodBuilder;
import org.manul.flow.NameAndType;
import org.manul.object.instance.core.PhysicalId;
import org.manul.object.type.FieldBuilder;
import org.manul.object.type.KlassBuilder;
import org.manul.object.type.Types;
import org.manul.util.InstanceOutput;
import org.manul.util.Utils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SubtreeExtractorTest extends TestCase {

    public void test() {
        var id = PhysicalId.of(1000, 0);
        var klass = KlassBuilder.newBuilder(id, "Foo", "Foo").build();
        var field = FieldBuilder.newBuilder("name", klass, Types.getStringType()).build();
        var method = MethodBuilder.newBuilder(klass, "bar")
                .isAbstract(true)
                .parameters(new NameAndType("v", Types.getIntType()))
                .build();
        var param = method.getParameters().getFirst();

        var ids = Set.of(
                klass.getId(),
                field.getId(),
                method.getId(),
                param.getId()
        );
        var subtrees = getSubtrees(klass);
        assertEquals(4, subtrees.size());
        assertEquals(ids, Utils.mapToSet(subtrees, Subtree::id));

        param.setName("v1");
        var subtrees1 = getSubtrees(klass);
        var differingTrees = new ArrayList<Subtree>();
        Utils.forEachPair(subtrees, subtrees1, (t1, t2) -> {
            if (!t1.equals(t2)) {
                differingTrees.add(t1);
            }
        });
        assertEquals(1, differingTrees.size());
        assertEquals(param.getId(), differingTrees.getFirst().id());
    }

    private List<Subtree> getSubtrees(Entity klass) {
        var bytes = InstanceOutput.toBytes(klass);
        var subtrees = new ArrayList<Subtree>();
        var visitor = new SubtreeExtractor(new ByteArrayInputStream(bytes), subtrees::add);
        visitor.visitGrove();
        return subtrees;
    }

}