package org.metavm.compiler.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.compiler.element.*;
import org.metavm.compiler.util.List;

public class ClosureTest extends TestCase {

    public void test() {
        var proj = new Project();
        var pkg = proj.getRootPackage();

        var speakClass = new Clazz(ClassTag.INTERFACE, SymName.from("Speak"), Access.PUBLIC, pkg);
        var baseClass = new Clazz(ClassTag.CLASS, SymName.from("Base"), Access.PUBLIC, pkg);
        baseClass.setInterfaces(List.of(speakClass.getType()));

        var comparableClass = new Clazz(ClassTag.INTERFACE, SymName.from("Comparable"), Access.PUBLIC, pkg);
        new TypeVariable(SymName.from("T"), PrimitiveType.ANY, comparableClass);

        var fooClass = new Clazz(ClassTag.CLASS, SymName.from("Foo"), Access.PUBLIC, pkg);
        var comparableType = comparableClass.getType(List.of(fooClass.getType()));
        fooClass.setInterfaces(List.of(baseClass.getType(), comparableType));

        Assert.assertEquals(0, speakClass.getRank());
        Assert.assertEquals(0, comparableClass.getRank());
        Assert.assertEquals(1, baseClass.getRank());
        Assert.assertEquals(2, fooClass.getRank());

        var cl = fooClass.getType().getClosure();

        Assert.assertEquals(
                List.of(fooClass.getType(), baseClass.getType(), comparableType, speakClass.getType()),
                cl.getTypes()
        );

        Assert.assertEquals(List.of(fooClass.getType()), cl.min().getTypes());
        Assert.assertEquals(fooClass.getType(), cl.toType());

        Assert.assertTrue(speakClass.getType().isAssignableFrom(baseClass.getType()));

        Assert.assertEquals(
                Types.instance.getIntersectionType(List.of(baseClass.getType(), comparableType)),
                cl.tail().toType()
        );
    }

}
