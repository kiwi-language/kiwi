package hashcode;

import org.metavm.api.ValueObject;
import org.metavm.api.Value;

@Value
public record MapEntry(
        Object key, Object value
) implements ValueObject {
}
