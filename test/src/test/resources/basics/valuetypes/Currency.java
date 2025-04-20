package valuetypes;

import org.metavm.api.Value;

@Value
public record Currency(double quantity, CurrencyKind kind) {
}
