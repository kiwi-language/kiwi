package org.metavm.application.rest.dto;

public record ApplicationCreateRequest(
        String name,
        String adminLoginName,
        String adminPassword,
        String creatorId
) {

    public static ApplicationCreateRequest fromNewUser(String name, String adminName, String adminPassword) {
        return new ApplicationCreateRequest(name, adminName, adminPassword, null);
    }

    public static ApplicationCreateRequest byExistingUser(String name, String creatorId) {
        return new ApplicationCreateRequest(name, null, null, creatorId);
    }

}
