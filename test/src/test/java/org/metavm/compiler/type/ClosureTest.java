package org.metavm.compiler.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.compiler.element.*;
import org.metavm.compiler.util.List;

public class ClosureTest extends TestCase {

    public void test() {
        var proj = new Project();
        var pkg = proj.getRootPackage();

        var speakClass = new Clazz(ClassTag.INTERFACE, Name.from("Speak"), Access.PUBLIC, pkg);
        var baseClass = new Clazz(ClassTag.CLASS, Name.from("Base"), Access.PUBLIC, pkg);
        baseClass.setInterfaces(List.of(speakClass));

        var comparableClass = new Clazz(ClassTag.INTERFACE, Name.from("Comparable"), Access.PUBLIC, pkg);
        new TypeVar(Name.from("T"), PrimitiveType.ANY, comparableClass);

        var fooClass = new Clazz(ClassTag.CLASS, Name.from("Foo"), Access.PUBLIC, pkg);
        var comparableType = comparableClass.getInst(List.of(fooClass));
        fooClass.setInterfaces(List.of(baseClass, comparableType));

        Assert.assertEquals(0, speakClass.getRank());
        Assert.assertEquals(0, comparableClass.getRank());
        Assert.assertEquals(1, baseClass.getRank());
        Assert.assertEquals(2, fooClass.getRank());

        var cl = fooClass.getClosure();

        Assert.assertEquals(
                List.of(fooClass, baseClass, comparableType, speakClass),
                cl.getTypes()
        );

        Assert.assertEquals(List.of(fooClass), cl.min().getTypes());
        Assert.assertEquals(fooClass, cl.toType());

        Assert.assertTrue(speakClass.isAssignableFrom(baseClass));

        Assert.assertEquals(
                Types.instance.getIntersectionType(List.of(baseClass, comparableType)),
                cl.tail().toType()
        );
    }

}
