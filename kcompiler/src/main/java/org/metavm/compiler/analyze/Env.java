package org.metavm.compiler.analyze;

import org.metavm.compiler.element.Package;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.TypeNode;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.Types;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

public class Env {

    private final Types types = Types.instance;
    private final ElementTable table = new ElementTable();
    private Scope currentScope;

    Scope enterScope() {
        return currentScope = new Scope(table, this, currentScope);
    }

    void setCurrentScope(Scope currentScope) {
        this.currentScope = currentScope;
    }

    Scope currentScope() {
        return Objects.requireNonNull(currentScope);
    }

    @Nullable Element lookupFirst(SymName name) {
        return table.lookupFirst(name);
    }

    public @Nullable Element lookupFirst(SymName name, Predicate<Element> filter) {
        return table.lookupFirst(name ,filter);
    }

    public @Nullable Element lookupFirst(SymName name, EnumSet<ResolveKind> kinds) {
        if (kinds.contains(ResolveKind.VAR)) {
            var found = table.lookupFirst(name, e -> e instanceof Variable);
            if (found != null)
                return found;
        }
        if (kinds.contains(ResolveKind.TYPE)) {
            var found = table.lookupFirst(name, e -> e instanceof Clazz || e instanceof TypeVariable);
            if (found != null)
                return found;
        }
        if (kinds.contains(ResolveKind.PACKAGE)) {
            return table.lookupFirst(name, e -> e instanceof Package);
        }
        return null;
    }

    Iterable<Element> lookupAll(SymName name, Predicate<Element> filter) {
        return table.lookupAll(name, filter);
    }

    <E extends Element> Iterator<E> lookupAll(SymName name, Class<E> clazz) {
        //noinspection unchecked
        return (Iterator<E>) table.lookupAll(name, clazz::isInstance);
    }

    public Type resolveType(TypeNode typeNode) {
        return typeNode.resolve(this);
    }

    public Types types() {
        return types;
    }

    public ElementTable getTable() {
        return table;
    }
}
