package tech.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;

public class MvListTest extends TestCase {

    public void test() {
        var l1 = MvList.of(1, 2, 3);
        Assert.assertEquals(MvList.of(0, 1, 2, 3), l1.prepend(0));
        Assert.assertEquals(MvList.of(3, 2, 1), l1.reverse());
        Assert.assertEquals(MvList.of(-1, 0, 1, 2, 3), l1.prependList(MvList.of(-1, 0)));
    }

}