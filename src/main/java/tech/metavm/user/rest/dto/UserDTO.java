package tech.metavm.user.rest.dto;

import java.util.List;

public record UserDTO (
        Long id,
        String loginName,
        String name,
        String password,
        List<Long> roleIds
) {

    public static UserDTO create(Long id, String loginName, String name, String password, long roleId) {
        return new UserDTO(id, loginName, name, password, List.of(roleId));
    }

}
