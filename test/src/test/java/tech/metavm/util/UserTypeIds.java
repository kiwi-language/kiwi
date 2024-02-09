package tech.metavm.util;

public record UserTypeIds(
        long platformUserTypeId,
        long applicationTypeId,
        long applicationNameFieldId,
        long applicationOwnerFieldId,
        long platformUserLoginNameFieldId,
        long platformUserPasswordFieldId
) {
}
