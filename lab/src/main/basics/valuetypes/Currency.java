package valuetypes;

import tech.metavm.entity.ValueType;

@ValueType
public record Currency(double quantity, CurrencyKind kind) {
}
