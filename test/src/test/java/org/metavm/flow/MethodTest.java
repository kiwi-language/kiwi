package org.metavm.flow;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.object.type.KlassType;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.Types;
import org.metavm.object.type.UncertainType;
import org.metavm.util.TestUtils;

import java.util.List;

@Slf4j
public class MethodTest extends TestCase {

    public void testGetInternalName() {
        var listKlass = TestUtils.newKlassBuilder("List").build();
        new TypeVariable(null, "T", listKlass);
        var klass = TestUtils.newKlassBuilder("Foo").build();
        var testMethod = MethodBuilder.newBuilder(klass, "test").build();
        var testTypeVar = new TypeVariable(null, "T", testMethod);
        var param = new Parameter(null, "list",
                Types.getNullableType(
                    KlassType.create(
                            listKlass,
                            List.of(new UncertainType(Types.getNeverType(), testTypeVar.getType()))
                    )
                ),
                testMethod
        );
        testMethod.setParameters(List.of(param));
        Assert.assertEquals(testMethod.getInternalName(), testMethod.getInternalName(null));
    }

}