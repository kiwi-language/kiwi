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
        var comparableTypeVar = new TypeVar("T", PrimitiveType.ANY, comparableClazz);
        var compareMethod = new Method("compare", Access.PUBLIC, false, true, false, comparableClazz);
        compareMethod.setParams(List.of(new Param("o", comparableTypeVar, compareMethod)));
        compareMethod.setRetType(PrimitiveType.INT);

        var baseClazz = new Clazz(ClassTag.CLASS, "Base", Access.PUBLIC, pkg);
        var versionField = new Field("version", PrimitiveType.LONG, Access.PUBLIC, false, baseClazz);

        var fooClazz = new Clazz(ClassTag.CLASS, "Foo", Access.PUBLIC, pkg);
        var typeVar = new TypeVar("T", PrimitiveType.ANY, fooClazz);
        fooClazz.setInterfaces(List.of(baseClazz, comparableClazz.getInst(List.of(fooClazz))));
        var nameField = new Field("name", Types.instance.getStringType(), Access.PRIVATE, false, fooClazz);
        var valueField = new Field("value", typeVar, Access.PRIVATE, false, fooClazz);
        var getValueMethod = new Method("getValue", Access.PUBLIC, false, false, false, fooClazz);
        getValueMethod.setRetType(typeVar);

        var innerClazz = new Clazz(ClassTag.CLASS, "Inner", Access.PUBLIC, fooClazz);

        var fooOfIntType = fooClazz.getInst(List.of(PrimitiveType.INT));

        var table = fooOfIntType.getTable();
        var nameFieldRef = (FieldInst) table.lookupFirst("name");
        Assert.assertNotNull(nameFieldRef);
        Assert.assertSame(nameField, nameFieldRef.field());
        var valueFieldRef = (FieldInst) table.lookupFirst("value");
        Assert.assertNotNull(valueFieldRef);
        Assert.assertSame(valueField, valueFieldRef.field());
        Assert.assertSame(PrimitiveType.INT, valueFieldRef.type());

        var getValueMethodRef = (PartialMethodInst) table.lookupFirst("getValue");
        Assert.assertNotNull(getValueMethodRef);
        Assert.assertSame(getValueMethod, getValueMethodRef.getFunc());
        Assert.assertSame(PrimitiveType.INT, getValueMethodRef.getRetType());

        Assert.assertSame(innerClazz.getInst(fooOfIntType, List.of()), table.lookupFirst("Inner"));

        var versionFieldRef = (FieldRef) table.lookupFirst("version");
        Assert.assertNotNull(versionFieldRef);
        Assert.assertSame(versionField, versionFieldRef);

        var compareMethodRef = (PartialMethodInst) table.lookupFirst("compare");
        Assert.assertNotNull(compareMethodRef);
        Assert.assertSame(compareMethod, compareMethodRef.getFunc());
        Assert.assertSame(fooOfIntType, compareMethodRef.getParamTypes().getFirst());
    }
    
}
