package org.manul.entity.mocks;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.manul.wire.Wire;
import org.manul.wire.SubType;

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
