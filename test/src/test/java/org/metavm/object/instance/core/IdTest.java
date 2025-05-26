package org.metavm.object.instance.core;

import junit.framework.TestCase;
import org.metavm.compiler.util.List;

public class IdTest extends TestCase {

    public void test() {
        Id id = PhysicalId.of(1L, 0L);
        assertEquals(id, Id.parse(id.toString()));
        id = new TmpId(1L);
        assertEquals(id, Id.parse(id.toString()));
    }

    public void testSorting() {
        var ids = List.of(
                PhysicalId.of(2, 0),
                PhysicalId.of(1, 1),
                PhysicalId.of(1, 0),
                new MockId(1),
                new NullId(),
                new TmpId(1L),
                new TmpId(2)
        ).stream().sorted().toList();

        assertEquals(
                List.of(
                        new NullId(),
                        new TmpId(1L),
                        new TmpId(2),
                        PhysicalId.of(1, 0),
                        PhysicalId.of(1, 1),
                        PhysicalId.of(2, 0),
                        new MockId(1)
                ),
                ids
        );

    }

}