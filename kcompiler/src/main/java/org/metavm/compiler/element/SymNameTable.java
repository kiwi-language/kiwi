package org.metavm.compiler.element;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SymNameTable {

    public static final SymNameTable instance = new SymNameTable();

    private final Map<String, SymNameImpl> map = new HashMap<>();

    public final SymName empty = get("");
    public final SymName this_ = get("this");
    public final SymName super_ = get("super");
    public final SymName init = get("init");
    public final SymName cinit = get("__cinit__");
    public final SymName value = get("value");
    public final SymName Component = get("Component");
    public final SymName Searchable = get("Searchable");
    public final SymName Tag = get("Tag");
    public final SymName length = get("length");
    public final SymName values = get("values");
    public final SymName valueOf = get("valueOf");
    public final SymName name = get("name");

    private SymNameTable() {
    }

    public SymName get(String name) {
        return map.computeIfAbsent(name, SymNameImpl::new);
    }

    private class SymNameImpl implements SymName {
        private final String string;

        public SymNameImpl(String string) {
            this.string = string;
        }

        @Override
        public boolean isEmpty() {
            return string.isEmpty();
        }

        @Override
        public SymName concat(String s) {
            return get(string + s);
        }

        @Override
        public int compareTo(@NotNull SymName o) {
            return string.compareTo(o.toString());
        }

        @Override
        public String toString() {
            return string;
        }

    }
}
