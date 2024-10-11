package org.metavm.autograph;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;

import java.util.List;
import java.util.Map;

@Slf4j
public class LabCompilingTest extends CompilerTestBase {

    public static final String LAB_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab";

    public void test() {
        compile(LAB_SOURCE_ROOT);
        submit(this::processCustomObjectIO);
    }

    private void processCustomObjectIO() {
//        DebugEnv.flag = true;
        var id = saveInstance("objectio.CustomObjectIOFoo",
                Map.of("id", "001", "elements", List.of(1, 2, 3)));
        var foo = getObject(id);
        Assert.assertEquals("001", foo.getString("id"));
        var elements = foo.getArray("elements");
        Assert.assertEquals(List.of(1L,2L,3L), elements.toList());
        var modCount = foo.get("modCount");
        Assert.assertEquals(0L, modCount);
    }

}
