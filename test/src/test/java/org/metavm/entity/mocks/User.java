package org.metavm.entity.mocks;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.metavm.wire.Wire;
import org.metavm.wire.SubType;

@Wire(
        subTypes = {
                @SubType(value = 1, type = Admin.class),
                @SubType(value = 2, type = Customer.class)
        }
)
@AllArgsConstructor
@Data
public abstract class User {
    private long id;
    private String name;
}
