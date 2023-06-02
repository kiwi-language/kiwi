package tech.metavm.transpile;

import java.util.List;

public record SymbolIdentifier(
        SymbolKind kind,
        String name,
        List<SourceType> parameterTypes
) {

    public static SymbolIdentifier variable(String name) {
        return new SymbolIdentifier(SymbolKind.VARIABLE, name, null);
    }

    public static SymbolIdentifier method(String name, List<SourceType> parameterTypes) {
        return new SymbolIdentifier(SymbolKind.METHOD, name, parameterTypes);
    }

    public static SymbolIdentifier type(String name) {
        return new SymbolIdentifier(SymbolKind.TYPE, name, null);
    }

}
