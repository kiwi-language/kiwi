package tech.metavm.transpile.ir;

public record ValueSwitchLabel(
        IValueSymbol value
) implements SwitchLabel {
}
