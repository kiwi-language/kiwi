package tech.metavm.user;

public record Token(
        long tenantId,
        String token
) {

}
