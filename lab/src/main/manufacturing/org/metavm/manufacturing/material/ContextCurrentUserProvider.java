package org.metavm.manufacturing.material;

import org.metavm.api.Component;
import org.metavm.api.lang.Lang;
import org.metavm.manufacturing.user.User;
import org.metavm.manufacturing.utils.ContextKeys;

import java.util.Objects;

@Component
public class ContextCurrentUserProvider implements CurrentUserProvider {
    @Override
    public User get() {
        return (User) Objects.requireNonNull(
                Lang.getContext(ContextKeys.USER),
                "Not logged in"
        );
    }
}
