package org.metavm.manufacturing.user;

import org.metavm.api.Index;
import org.metavm.manufacturing.utils.SecureHash;

public class User {

    public static final Index<String, User> nameIndex = new Index<>(true, u -> u.name);

    private final String name;
    private SecureHash password;

    public User(String name, String password) {
        this.name = name;
        this.password = SecureHash.create(password);
    }

    public void setPassword(String password) {
        this.password = SecureHash.create(password);
    }

    public String getName() {
        return name;
    }

    public SecureHash getPassword() {
        return password;
    }

}
