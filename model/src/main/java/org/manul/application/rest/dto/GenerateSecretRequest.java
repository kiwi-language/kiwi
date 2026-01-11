package org.manul.application.rest.dto;

import org.jsonk.Json;

@Json
public record GenerateSecretRequest(
        String verificationCode
) {
}
