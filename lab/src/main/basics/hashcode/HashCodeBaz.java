package hashcode;

import org.metavm.api.ValueObject;
import org.metavm.api.Value;

@Value
public record HashCodeBaz(
        String name,
        Object extra
) implements ValueObject {
}
