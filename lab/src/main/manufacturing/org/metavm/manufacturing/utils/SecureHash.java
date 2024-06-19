package org.metavm.manufacturing.utils;

import org.metavm.api.lang.Lang;

public record SecureHash(
        String salt,
        String hash
) {

    public static SecureHash create(String value) {
        var s = Lang.secureRandom(16);
        var h = Lang.secureHash(value, s);
        return new SecureHash(s, h);
    }

    public boolean verify(String value) {
        return hash.equals(Lang.secureHash(value, salt));
    }
}
