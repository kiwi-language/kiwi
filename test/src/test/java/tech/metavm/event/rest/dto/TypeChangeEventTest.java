package tech.metavm.event.rest.dto;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.util.TestUtils;

import java.util.List;

public class TypeChangeEventTest extends TestCase {


    public void testJson() {
        var event = new TypeChangeEvent("1", 2L, List.of("3"), "abc-cde");
        var jsonStr = TestUtils.toJSONString(event);
        System.out.println(jsonStr);
        var recoveredEvent = TestUtils.parseJson(jsonStr, AppEvent.class);
        Assert.assertEquals(event, recoveredEvent);
    }

}