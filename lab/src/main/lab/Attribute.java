import org.metavm.api.ValueType;

@ValueType
public record Attribute<T>(
        String name,
        T value
) {
}
