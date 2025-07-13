package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.object.type.Klass;
import org.metavm.util.BootstrapUtils;
import org.metavm.util.Instances;
import org.metavm.util.TestUtils;

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