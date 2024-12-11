package org.metavm.entity;

import org.metavm.api.Entity;
import org.metavm.api.ValueObject;
import org.metavm.util.EncodingUtils;

@Entity
public record HashedValue(
        String salt,
        String hashedValue
) implements ValueObject {

    public boolean verify(String value) {
        return EncodingUtils.verifySecureHash(value, salt, hashedValue);
    }

}
