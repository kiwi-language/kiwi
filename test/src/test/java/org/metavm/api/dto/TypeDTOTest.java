package org.metavm.api.dto;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.jsonk.Jsonk;
import org.jsonk.Option;
import org.metavm.compiler.util.List;

@Slf4j
public class TypeDTOTest extends TestCase {

    public void testJson() {
        var type = new UnionTypeDTO(
                List.of(
                        new ArrayTypeDTO(
                                new ClassTypeDTO("User")
                        ),
                        new PrimitiveTypeDTO("null")
                )
        );
        var json = Jsonk.toJson(type, Option.create().indent());
        log.info("\n{}", json);
        var type1 = Jsonk.fromJson(json, TypeDTO.class);
        assertEquals(type, type1);
    }

}