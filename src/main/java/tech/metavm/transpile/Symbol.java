package tech.metavm.transpile;

public record Symbol(SymbolIdentifier identifier, String prefix, String name, SourceType type) {

    public String build() {
        return prefix != null ? prefix + "." + name : name;
    }

    public String toString() {
        return build();
    }

    public SymbolIdentifier identifier() {
        return this.identifier;
    }

}
