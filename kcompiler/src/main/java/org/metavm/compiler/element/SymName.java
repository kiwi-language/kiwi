package org.metavm.compiler.element;

import org.jetbrains.annotations.NotNull;

public interface SymName extends Comparable<SymName> {

    static SymName from(String string) {
        return SymNameTable.instance.get(string);
    }

    static SymName this_() {
        return SymNameTable.instance.this_;
    }

    static SymName super_() {
        return SymNameTable.instance.super_;
    }

    static SymName value() {
        return SymNameTable.instance.value;
    }

    static SymName Component() {
        return SymNameTable.instance.Component;
    }

    static SymName Searchable() {
        return SymNameTable.instance.Searchable;
    }

    static SymName Tag() {
        return SymNameTable.instance.Tag;
    }

    static SymName length() {
        return SymNameTable.instance.length;
    }

    static SymName init() {
        return SymNameTable.instance.init;
    }

    boolean isEmpty();

    SymName concat(String s);

    @Override
    int compareTo(@NotNull SymName o);

}
