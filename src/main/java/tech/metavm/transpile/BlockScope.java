package tech.metavm.transpile;

import tech.metavm.transpile.ir.CodeBlock;
import tech.metavm.util.InternalException;

import java.lang.reflect.Type;
import java.util.*;

public class BlockScope extends ScopeBase {

    private final String name;
    private final CodeBlock block;
    private final Map<SymbolIdentifier, Symbol> variables = new LinkedHashMap<>();

    public BlockScope(Scope parent, CodeBlock block) {
        this(parent, null, block);
    }

    public BlockScope(Scope parent, String name, CodeBlock block) {
        super(parent);
        this.name = name;
        this.block = block;
    }

    private void addVariable(SymbolIdentifier identifier, Type type) {
        if(variables.containsKey(identifier)) {
            throw new InternalException("Variable '" + name + "' is already defined in the current block");
        }
        this.variables.put(identifier, new Symbol(identifier, null, identifier.name(), new ReflectSourceType(type)));
    }

    private void defineLocal(SymbolKind kind, String name, Type type) {
        SymbolIdentifier identifier = new SymbolIdentifier(kind, name, null);
        if(isLocalVariableDefined(identifier)) {
            throw new InternalException("Local variable '" + name + "' is already defined in the current scope");
        }
        addVariable(identifier, type);
    }

    public CodeBlock getBlock() {
        return block;
    }

    public void defineType(String name) {
        defineLocal(SymbolKind.TYPE, name, null);
    }

    public void defineVariable(String name, Type type) {
        defineLocal(SymbolKind.VARIABLE, name, type);
    }

    private boolean isLocalVariableDefined(SymbolIdentifier identifier) {
        if(variables.containsKey(identifier)) {
            return true;
        }
        if(parent instanceof BlockScope parentBlock) {
            return parentBlock.isLocalVariableDefined(identifier);
        }
        return false;
    }

    public void defineManually(SymbolIdentifier identifier, Symbol symbol) {
        variables.put(identifier, symbol);
    }

    public Collection<Symbol> getVariables() {
        return Collections.unmodifiableCollection(variables.values());
    }

    @Override
    protected Symbol resolveSelf(SymbolIdentifier identifier) {
        return variables.get(identifier);
    }
}
