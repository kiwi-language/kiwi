package org.manul.entity.mocks;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.manul.wire.Wire;
import org.manul.wire.SubType;

@Data
@AllArgsConstructor
@Wire(subTypes = {
        @SubType(value = 1, type = Bus.class),
        @SubType(value = 2, type = SchoolBus.class)
})
public class Bus {
    private int capacity;
}
