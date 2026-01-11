package org.manul.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import org.manul.object.instance.persistence.IndexKeyPO;
import org.manul.util.BytesUtils;
import org.manul.util.Instances;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class IndexKeyRTTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(IndexKeyRTTest.class);

    public void test() {
        var data = IndexKeyRT.toKeyBytes(List.of(Instances.longInstance(1L)));
        Assert.assertTrue(data.length > 1);
        var cols = IndexKeyPO.toColumns(data);
        var value = BytesUtils.readIndexBytes(cols.getFirst());
        Assert.assertEquals(1L, value);
    }

}