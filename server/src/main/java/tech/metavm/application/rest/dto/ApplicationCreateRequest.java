package tech.metavm.application.rest.dto;

public record ApplicationCreateRequest(
        String name,
        String adminLoginName,
        String adminPassword,
        Long creatorId
) {

    public static ApplicationCreateRequest fromNewUser(String name, String adminName, String adminPassword) {
        return new ApplicationCreateRequest(name, adminName, adminPassword, null);
    }

    public static ApplicationCreateRequest byExistingUser(String name, Long creatorId) {
        return new ApplicationCreateRequest(name, null, null, creatorId);
    }

}
