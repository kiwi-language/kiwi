package org.metavm.user.rest.dto;



import org.jsonk.Json;

import java.util.List;

@Json
public record UserDTO (
        String id,
        String loginName,
        String name,
        String password,
        List<String> roleIds
) {

    public static UserDTO create(String id, String loginName, String name, String password, String roleId) {
        return new UserDTO(id, loginName, name, password, List.of(roleId));
    }

}
