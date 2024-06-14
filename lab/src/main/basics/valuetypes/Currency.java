package valuetypes;

import org.metavm.entity.ValueType;

@ValueType
public record Currency(double quantity, CurrencyKind kind) {
}
