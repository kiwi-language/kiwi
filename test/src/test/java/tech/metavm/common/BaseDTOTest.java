package tech.metavm.common;

import junit.framework.TestCase;
import tech.metavm.util.NncUtils;

public class BaseDTOTest extends TestCase {

    public record FooDTO(String id, Long tmpId) implements BaseDTO {
    }

    public void testDeserialize() {
        String json = "{\"id\": 1}";
        var foo = NncUtils.readJSONString(json, FooDTO.class);
        System.out.println(foo);
    }

}