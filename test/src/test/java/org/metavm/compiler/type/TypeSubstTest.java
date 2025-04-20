package org.metavm.compiler.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.compiler.element.*;
import org.metavm.compiler.util.List;

public class TypeSubstTest extends TestCase {

    public void test() {
        var project = new Project();
        var clazz = new Clazz(ClassTag.CLASS, NameTable.instance.get( "Foo"), Access.PUBLIC, project.getRootPackage());
        var typeVar = new TypeVar(NameTable.instance.get("T"), PrimitiveType.ANY, clazz);

        var subst = new TypeSubst(List.of(typeVar), List.of(Types.instance.getStringType()));
        var pType = clazz.accept(subst);
        Assert.assertSame(
                clazz.getInst(null, List.of(Types.instance.getStringType())),
                pType
        );
    }

}
