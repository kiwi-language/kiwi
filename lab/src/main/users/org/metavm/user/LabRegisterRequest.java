package org.metavm.user;

import org.metavm.api.Entity;

@Entity
public record LabRegisterRequest(
        String loginName,
        String name,
        String password,
        String verificationCode
) {
}
