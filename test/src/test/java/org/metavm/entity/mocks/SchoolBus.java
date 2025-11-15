package org.metavm.entity.mocks;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.metavm.wire.Wire;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@Wire
public class SchoolBus extends Bus {
    private int schoolId;

    public SchoolBus(int capacity, int schoolId) {
        super(capacity);
        this.schoolId = schoolId;
    }
}
