package tech.metavm.util;

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class RootMethodGetterTest extends TestCase {

    public void test() {
        var method = ReflectUtils.getMethod(ArrayList.class, "size");
        var rootMethod = RootMethodGetter.getRootMethod(method);
        Assert.assertEquals(
                ReflectUtils.getMethod(List.class, "size"),
                rootMethod
        );
    }

}