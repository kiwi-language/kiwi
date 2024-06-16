package org.metavm.entity;

import org.metavm.api.EntityType;
import org.metavm.api.Value;
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
