package org.metavm.entity.mocks;

import lombok.Getter;
import lombok.Setter;
import org.metavm.wire.Wire;

import java.util.List;

@Setter
@Getter
@Wire
public class Customer extends User {

    private List<Order> orders;

    public Customer(long id, String name) {
        super(id, name);
    }


}
