package hashcode;

import org.metavm.api.ValueObject;
import org.metavm.api.ValueType;

@ValueType
public record HashCodeBaz(
        String name,
        Object extra
) implements ValueObject {
}
