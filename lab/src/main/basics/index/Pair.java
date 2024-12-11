package index;

import org.metavm.api.Value;

@Value
public record Pair<T1, T2>(T1 first, T2 second) {
}
