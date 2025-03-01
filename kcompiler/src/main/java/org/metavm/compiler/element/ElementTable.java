package org.metavm.compiler.element;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.util.MultiMap;
import org.metavm.compiler.util.Traces;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.Predicate;

@Slf4j
public class ElementTable {

    public static final ElementTable empty = new ElementTable();

    static {
        empty.freeze();
    }

    private final MultiMap<SymName, Element> map = new MultiMap<>();
    private boolean frozen;

    public void add(Element element) {
        ensureNotFrozen();
        if (Traces.traceAttr)
            log.trace("Entering element {}", element);
        map.put(element.getName(), element);
    }

    public void remove(Element element) {
        ensureNotFrozen();
        map.remove(element.getName(), element);
    }

    public void addAll(ElementTable that) {
        ensureNotFrozen();
        if (Traces.traceAttr) {
            var elements = new ArrayList<Element>();
            that.map.forEach((k, e) -> {
                elements.add(e);
            });
            log.error("Entering all elements {}", elements);
        }
        map.putAll(that.map);
    }

    public void removeAll(ElementTable that) {
        ensureNotFrozen();
        map.removeAll(that.map);
    }

    public @Nullable Element lookupFirst(String name) {
        return lookupFirst(SymNameTable.instance.get(name));
    }

    public @Nullable Element lookupFirst(SymName name) {
        return map.getFirst(name, e -> true);
    }

    public @Nullable Element lookupFirst(SymName name, Predicate<Element> filter) {
        return map.getFirst(name, filter);
    }

    public Iterable<Element> lookupAll(SymName name) {
        return map.get(name, e -> true);
    }

    public Iterable<Element> lookupAll(SymName name, Predicate<Element> filter) {
        return map.get(name, filter);
    }

    public void freeze() {
        frozen = true;
    }

    private void ensureNotFrozen() {
        if (frozen)
            throw new IllegalStateException("Element table is frozen");
    }

    @Override
    public String toString() {
        return map.toString();
    }

}
