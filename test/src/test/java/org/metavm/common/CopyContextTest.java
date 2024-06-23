package org.metavm.common;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.core.TmpId;

import java.util.Map;

public class CopyContextTest extends TestCase {

    public void testMapType() {
        var format = "$$%s<String,$$%s<Object>>|null";
        var mapTmpId = TmpId.randomString();
        var listTmpId = TmpId.randomString();
        var expr = String.format(format, mapTmpId, listTmpId);
        var mapId = new PhysicalId(false, 1, 0L).toString();
        var listId = new PhysicalId(false, 2, 0L).toString();
        var copyContext = CopyContext.create(Map.of(mapTmpId, mapId, listTmpId, listId));
        var transformed = copyContext.mapType(expr);
        var transformedExpr = String.format(format, mapId, listId);
        Assert.assertEquals(transformedExpr, transformed);
    }

    public void testMapExpression() {
        var format = "this.$%s";
        var fieldTmpId = TmpId.randomString();
        var fieldId = new PhysicalId(false, 1L, 1L).toString();
        var expr = String.format(format, fieldTmpId);
        var copyContext = CopyContext.create(Map.of(fieldTmpId, fieldId));
        var transformedExpr = copyContext.mapExpression(expr);
        Assert.assertEquals(String.format(format, fieldId), transformedExpr);
    }

}