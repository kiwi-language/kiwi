package hashcode;

import org.metavm.api.ValueObject;
import org.metavm.api.ValueType;

@ValueType
public record MapEntry(
        Object key, Object value
) implements ValueObject {
}
