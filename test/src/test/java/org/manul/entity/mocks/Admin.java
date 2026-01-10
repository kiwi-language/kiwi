package org.manul.entity.mocks;


import org.manul.wire.Wire;

@Wire
public class Admin extends User {
    public Admin(long id, String name) {
        super(id, name);
    }
}
