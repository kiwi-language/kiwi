package valuetypes;

import org.metavm.api.ValueType;

@ValueType
public record Currency(double quantity, CurrencyKind kind) {
}
