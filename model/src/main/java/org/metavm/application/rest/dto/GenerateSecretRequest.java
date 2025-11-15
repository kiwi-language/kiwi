package org.metavm.application.rest.dto;

import org.jsonk.Json;

@Json
public record GenerateSecretRequest(
        String verificationCode
) {
}
