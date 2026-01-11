package org.manul.util;

import junit.framework.TestCase;
import org.manul.object.instance.core.TmpId;
import org.manul.object.type.Klass;

public class EntityFactoryTest extends TestCase {

    public void test() {
        var id = TmpId.random();
        System.out.println(Klass.class.getName());
//        var k = new Klass(id);
//        var klass = EntityFactory.instance.create(Klass.class, id);
//        MatcherAssert.assertThat(klass, CoreMatchers.instanceOf(Klass.class));
//        assertEquals(id, klass.getId());
    }

}