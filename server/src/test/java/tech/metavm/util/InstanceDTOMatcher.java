package tech.metavm.util;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import tech.metavm.object.instance.rest.InstanceDTO;

public class InstanceDTOMatcher extends BaseMatcher<InstanceDTO> {

    public static InstanceDTOMatcher of(InstanceDTO value) {
        return new InstanceDTOMatcher(value);
    }

    private final InstanceDTO value;

    public InstanceDTOMatcher(InstanceDTO value) {
        this.value = value;
    }

    @Override
    public boolean matches(Object actual) {
        if(actual instanceof InstanceDTO thatValue)
            return value.valueEquals(thatValue);
        else
            return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(value.toString());
    }
}
