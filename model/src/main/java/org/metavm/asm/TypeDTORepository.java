package org.metavm.asm;

import org.metavm.object.type.rest.dto.KlassDTO;

import java.util.List;

public interface TypeDTORepository {

    KlassDTO getClassByCode(String code);

    void batchSave(List<KlassDTO> types);

}
