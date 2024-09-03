package org.metavm.autograph;

import org.junit.Assert;
import org.metavm.util.TestUtils;

import java.util.List;
import java.util.Map;

public class LabCompilingTest extends CompilerTestBase {

    public static final String LAB_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab";

    public void test() {
        compile(LAB_SOURCE_ROOT);
        submit(() -> {
            var foo1Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("ComparableFoo", Map.of("seq", 1)));
            var foo2Id = TestUtils.doInTransaction(() -> apiClient.saveInstance("ComparableFoo", Map.of("seq", 2)));
            var cmp = TestUtils.doInTransaction(() -> apiClient.callMethod(foo1Id, "compareTo", List.of(foo2Id)));
            Assert.assertEquals(-1L, cmp);
            var labId = TestUtils.doInTransaction(() -> apiClient.saveInstance("SortLab", Map.of(
                    "foos", List.of(foo2Id, foo1Id)
            )));
            var foos = apiClient.getObject(labId).getRaw("foos");
            Assert.assertEquals(List.of(foo1Id, foo2Id), foos);
        });
    }

}
