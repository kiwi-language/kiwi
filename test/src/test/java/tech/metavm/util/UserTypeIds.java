package tech.metavm.util;

public record UserTypeIds(
        String platformUserTypeId,
        String applicationTypeId,
        String applicationNameFieldId,
        String applicationOwnerFieldId,
        String platformUserLoginNameFieldId,
        String platformUserPasswordFieldId
) {
}
