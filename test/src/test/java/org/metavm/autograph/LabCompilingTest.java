package org.metavm.autograph;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;

import java.util.List;

@Slf4j
public class LabCompilingTest extends CompilerTestBase {

    public static final String LAB_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/lab";

    public void test() {
        compile(LAB_SOURCE_ROOT);
        submit(() -> {
            processPrimitiveCompare();
            processCharSequence();
        });
    }

    private void processPrimitiveCompare() {
        var className = "primitives.PrimitiveCompareFoo";
        Assert.assertEquals(1, callMethod(className, "compareString", List.of("b", "a")));
        Assert.assertEquals(1, callMethod(className, "compareChar", List.of('b', 'a')));
    }

    private void processCharSequence() {
        var className = "primitives.CharSequenceFoo";
        Assert.assertEquals(6, callMethod(className, "length", List.of("MetaVM")));
        Assert.assertEquals("VM", callMethod(className, "subSequence", List.of("MetaVM", 4, 6)));
        Assert.assertEquals('M', callMethod(className, "charAt", List.of("MetaVM", 0)));
    }

}
