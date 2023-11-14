package tech.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;

public class InstaListTest extends TestCase {

    public void test() {
        var l1 = InstaList.of(1, 2, 3);
        Assert.assertEquals(InstaList.of(0, 1, 2, 3), l1.prepend(0));
        Assert.assertEquals(InstaList.of(3, 2, 1), l1.reverse());
        Assert.assertEquals(InstaList.of(-1, 0, 1, 2, 3), l1.prependList(InstaList.of(-1, 0)));
    }

}