package tech.metavm.user;

public record Token (
        long tenantId,
        long userId,
        long createdAt
) {

    @Override
    public String toString() {
        return tenantId+":"+userId + ":" + createdAt;
    }
}
