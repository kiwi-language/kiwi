package tech.metavm.user.rest.dto;

public record LoginResponse(
        boolean successful,
        String token
) {


}
