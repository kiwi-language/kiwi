package valuetypes;

import org.manul.api.Value;

@Value
public record Currency(double quantity, CurrencyKind kind) {
}
