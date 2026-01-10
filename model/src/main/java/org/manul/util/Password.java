package org.manul.util;

import org.manul.object.instance.core.PasswordValue;

public class Password {

    private String password;

    public Password(String password) {
        this(password, true);
    }

    public Password(PasswordValue passwordInstance) {
        this(passwordInstance.getValue(), false);
    }

    public Password(String password, boolean doEncoding) {
        this.password = doEncoding ? EncodingUtils.md5(password) : password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = EncodingUtils.md5(password);
    }
}
