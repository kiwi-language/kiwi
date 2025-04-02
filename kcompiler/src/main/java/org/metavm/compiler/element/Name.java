package org.metavm.compiler.element;

import org.jetbrains.annotations.NotNull;

public interface Name extends Comparable<Name> {

    static Name from(String string) {
        return NameTable.instance.get(string);
    }

    static Name this_() {
        return NameTable.instance.this_;
    }

    static Name super_() {
        return NameTable.instance.super_;
    }

    static Name value() {
        return NameTable.instance.value;
    }

    static Name Component() {
        return NameTable.instance.Component;
    }

    static Name Searchable() {
        return NameTable.instance.Searchable;
    }

    static Name Tag() {
        return NameTable.instance.Tag;
    }

    static Name length() {
        return NameTable.instance.length;
    }

    static Name init() {
        return NameTable.instance.init;
    }

    static Name array() {
        return NameTable.instance.array;
    }

    boolean isEmpty();

    Name concat(String s);

    @Override
    int compareTo(@NotNull Name o);

}
