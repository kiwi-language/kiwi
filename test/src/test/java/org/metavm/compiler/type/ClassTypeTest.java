package org.metavm.compiler.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.compiler.element.*;
import org.metavm.compiler.util.List;

public class ClassTypeTest extends TestCase {
    
    public void test() {
        var proj = new Project();
        var pkg = proj.getRootPackage();

        var comparableClazz = new Clazz(ClassTag.INTERFACE, "Comparable", Access.PUBLIC, pkg);
        var comparableTypeVar = new TypeVariable("T", PrimitiveType.ANY, comparableClazz);
        var compareMethod = new Method("compare", Access.PUBLIC, false, true, false, comparableClazz);
        compareMethod.setParameters(List.of(new Parameter("o", comparableTypeVar, compareMethod)));
        compareMethod.setReturnType(PrimitiveType.INT);

        var baseClazz = new Clazz(ClassTag.CLASS, "Base", Access.PUBLIC, pkg);
        var versionField = new Field("version", PrimitiveType.LONG, Access.PUBLIC, false, baseClazz);

        var fooClazz = new Clazz(ClassTag.CLASS, "Foo", Access.PUBLIC, pkg);
        var typeVar = new TypeVariable("T", PrimitiveType.ANY, fooClazz);
        fooClazz.setInterfaces(List.of(baseClazz.getType(), comparableClazz.getType(List.of(fooClazz.getType()))));
        var nameField = new Field("name", PrimitiveType.STRING, Access.PRIVATE, false, fooClazz);
        var valueField = new Field("value", typeVar, Access.PRIVATE, false, fooClazz);
        var getValueMethod = new Method("getValue", Access.PUBLIC, false, false, false, fooClazz);
        getValueMethod.setReturnType(typeVar);

        var innerClazz = new Clazz(ClassTag.CLASS, "Inner", Access.PUBLIC, fooClazz);

        var pType = fooClazz.getType(List.of(PrimitiveType.INT));

        var table = pType.getTable();
        var nameFieldRef = (FieldInst) table.lookupFirst("name");
        Assert.assertNotNull(nameFieldRef);
        Assert.assertSame(nameField, nameFieldRef.field());
        var valueFieldRef = (FieldInst) table.lookupFirst("value");
        Assert.assertNotNull(valueFieldRef);
        Assert.assertSame(valueField, valueFieldRef.field());
        Assert.assertSame(PrimitiveType.INT, valueFieldRef.type());

        var getValueMethodRef = (MethodInst) table.lookupFirst("getValue");
        Assert.assertNotNull(getValueMethodRef);
        Assert.assertSame(getValueMethod, getValueMethodRef.getFunction());
        Assert.assertSame(PrimitiveType.INT, getValueMethodRef.getReturnType());

        Assert.assertSame(innerClazz, table.lookupFirst("Inner"));

        var versionFieldRef = (FieldInst) table.lookupFirst("version");
        Assert.assertNotNull(versionFieldRef);
        Assert.assertSame(versionField, versionFieldRef.field());

        var compareMethodRef = (MethodInst) table.lookupFirst("compare");
        Assert.assertNotNull(compareMethodRef);
        Assert.assertSame(compareMethod, compareMethodRef.getFunction());
        Assert.assertSame(pType, compareMethodRef.getParameterTypes().getFirst());
    }
    
}
