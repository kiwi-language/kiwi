package org.metavm.object.type.rest.dto;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.NncUtils;

@Slf4j
public class CpEntryDTOTest extends TestCase {

    public void testJSON() {
        var fieldCpEntryDTO = new FieldCpEntryDTO(0, new FieldRefDTO(
                "$$1", "1"
        ));
        var json = NncUtils.toPrettyJsonString(fieldCpEntryDTO);
        log.debug("{}", json);
        NncUtils.readJSONString(json, CpEntryDTO.class);
    }

}