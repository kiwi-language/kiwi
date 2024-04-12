package tech.metavm.asm;

import tech.metavm.object.type.rest.dto.TypeDTO;

import java.util.List;

public interface TypeDTORepository {

    TypeDTO getClassByCode(String code);

    void batchSave(List<TypeDTO> types);

}
