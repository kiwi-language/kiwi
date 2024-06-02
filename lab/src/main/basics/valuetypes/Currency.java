package valuetypes;

import tech.metavm.entity.ValueType;

@ValueType("Currency")
public record Currency(double quantity, CurrencyKind kind) {
}
