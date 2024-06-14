package org.metavm.asm;

import org.metavm.object.type.rest.dto.TypeDTO;

import java.util.List;

public interface TypeDTORepository {

    TypeDTO getClassByCode(String code);

    void batchSave(List<TypeDTO> types);

}
