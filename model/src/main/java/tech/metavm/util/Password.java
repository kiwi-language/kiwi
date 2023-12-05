package tech.metavm.util;

import tech.metavm.object.instance.core.PasswordInstance;

public class Password {

    private String password;

    public Password(String password) {
        this(password, true);
    }

    public Password(PasswordInstance passwordInstance) {
        this(passwordInstance.getValue(), false);
    }

    private Password(String password, boolean doEncoding) {
        this.password = doEncoding ? EncodingUtils.md5(password) : password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = EncodingUtils.md5(password);
    }
}
