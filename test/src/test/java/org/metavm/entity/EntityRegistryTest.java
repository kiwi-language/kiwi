package org.metavm.entity;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.object.type.Klass;

@Slf4j
public class EntityRegistryTest extends TestCase {

    public void test() {
        var klass = Klass.class;
        var treeTag = klass.getAnnotation(NativeEntity.class).value();
        Assert.assertSame(klass, EntityRegistry.getEntityClass(treeTag));
    }

}