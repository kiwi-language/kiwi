package org.metavm.manufacturing.common;

import org.metavm.manufacturing.user.User;

public class OwnedEntity {

    private final User owner;

    public OwnedEntity(User owner) {
        this.owner = owner;
    }

    public User getOwner() {
        return owner;
    }
}
