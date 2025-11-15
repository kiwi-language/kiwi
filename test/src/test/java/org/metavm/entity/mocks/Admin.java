package org.metavm.entity.mocks;


import org.metavm.wire.Wire;

@Wire
public class Admin extends User {
    public Admin(long id, String name) {
        super(id, name);
    }
}
