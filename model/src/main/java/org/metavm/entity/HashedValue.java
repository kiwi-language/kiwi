package org.metavm.entity;

import org.metavm.util.EncodingUtils;

@EntityType
public record HashedValue(
        String salt,
        String hashedValue
) implements Value {

    public boolean verify(String value) {
        return EncodingUtils.verifySecureHash(value, salt, hashedValue);
    }

}
