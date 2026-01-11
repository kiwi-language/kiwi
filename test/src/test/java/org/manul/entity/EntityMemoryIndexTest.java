package org.manul.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.manul.object.type.Klass;
import org.manul.util.BootstrapUtils;
import org.manul.util.Instances;
import org.manul.util.TestUtils;

import java.util.List;

public class EntityMemoryIndexTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        BootstrapUtils.bootstrap();
    }

    // Found existing class hashcode.HashCodeBaz for 'hashcode.HashCodeBar'
    public void test() {
        var index = new EntityMemoryIndex();
        var klass = TestUtils.newKlassBuilder("HashCodeBaz", "hashcode.HashCodeBaz").build();
        index.save(klass);
        var found = index.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, List.of(Instances.stringInstance("hashcode.HashCodeBaz")));
        Assert.assertNotNull(found);
    }


}