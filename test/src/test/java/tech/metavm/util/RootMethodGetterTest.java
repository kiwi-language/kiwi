package tech.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collection;

public class RootMethodGetterTest extends TestCase {

    public void test() {
        var method = ReflectionUtils.getMethod(ArrayList.class, "size");
        var rootMethod = RootMethodGetter.getRootMethod(method);
        Assert.assertEquals(
                ReflectionUtils.getMethod(Collection.class, "size"),
                rootMethod
        );
    }

}