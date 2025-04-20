package org.metavm.manufacturing.user;

import org.metavm.api.Component;
import org.metavm.api.lang.Lang;
import org.metavm.manufacturing.utils.ContextKeys;

import javax.annotation.Nullable;

@Component
public class UserService {

    public User signup(String name, String password) {
        var existing = User.nameIndex.getFirst(name);
        if(existing != null)
            throw new IllegalStateException("User with name " + name + " already exists");
        return new User(name, password);
    }

    public void login(String name, String password) {
        var user = User.nameIndex.getFirst(name);
        if(user == null || !user.getPassword().verify(password))
            throw new IllegalArgumentException("Login failed");
        var session = new Session(user);
        Lang.setContext(ContextKeys.TOKEN, session.getToken());
    }

    public @Nullable User verify(String token) {
        var session = Session.tokenIndex.getFirst(token);
        if(session != null && session.isActive())
            return session.getUser();
        else
            return null;
    }

}
