package org.metavm.compiler.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.compiler.element.*;
import org.metavm.compiler.util.List;

public class TypeSubstitutorTest extends TestCase {

    public void test() {
        var project = new Project();
        var clazz = new Clazz(ClassTag.CLASS, SymNameTable.instance.get( "Foo"), Access.PUBLIC, project.getRootPackage());
        var typeVar = new TypeVariable(SymNameTable.instance.get("T"), PrimitiveType.ANY, clazz);

        var subst = new TypeSubstitutor(List.of(typeVar), List.of(PrimitiveType.STRING));
        var type = clazz.getType();
        var pType = type.accept(subst);
        Assert.assertSame(
                clazz.getType(null, List.of(PrimitiveType.STRING)),
                pType
        );
    }

}
