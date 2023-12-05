package tech.metavm.user.rest.dto;

import tech.metavm.common.RefDTO;

import java.util.List;

public record UserDTO (
        Long id,
        String loginName,
        String name,
        String password,
        List<RefDTO> roleRefs
) {

    public static UserDTO create(Long id, String loginName, String name, String password, long roleId) {
        return new UserDTO(id, loginName, name, password, List.of(RefDTO.fromId(roleId)));
    }

}
