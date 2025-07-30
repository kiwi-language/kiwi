package org.metavm.compiler.element;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class NameTable {

    public static final NameTable instance = new NameTable();

    private final Map<String, NameImpl> nameMap = new HashMap<>();

    public Name error = get("<error>");
    public final Name empty = get("");
    public final Name E = get("e");
    public final Name this_ = get("this");
    public final Name super_ = get("super");
    public final Name init = get("init");
    public final Name array = get("Array");
    public final Name hasNext = get("hasNext");
    public final Name iterator = get("iterator");
    public final Name next = get("next");
    public final Name append = get("append");
    public final Name contains = get("contains");
    public final Name remove = get("remove");
    public final Name T = get("T");
    public final Name t = get("t");
    public final Name cinit = get("__cinit__");
    public final Name value = get("value");
    public final Name Component = get("Component");
    public final Name Searchable = get("Searchable");
    public final Name Tag = get("Tag");
    public final Name Label = get("Label");
    public final Name Summary = get("Summary");
    public final Name Bean = get("Bean");
    public final Name Configuration = get("Configuration");
    public final Name length = get("length");
    public final Name values = get("values");
    public final Name valueOf = get("valueOf");
    public final Name name = get("name");
    public final Name builtin = get("builtin");
    public final Name string = get("string");
    public final Name qualString = get("java.lang.String");
    public final Name concat = get("concat");
    public final Name enumName = get("enum$name");
    public final Name enumOrdinal = get("enum$ordinal");
    public final Name java = get("java");
    public final Name lang = get("lang");
    public final Name util = get("util");
    public final Name index = get("Index");
    public final Name qualIndex = get("org.metavm.api.Index");
    public final Name forEach = get("forEach");
    public final Name action = get("action");
    public final Name comparator = get("comparator");
    public final Name a = get("a");
    public final Name mapper = get("mapper");
    public final Name R = get("R");
    public final Name map = get("map");
    public final Name sumInt = get("sumInt");
    public final Name sumLong = get("sumLong");
    public final Name sumFloat = get("sumFloat");
    public final Name sumDouble = get("sumDouble");
    public final Name sum = get("sum");
    public final Name intArray = get("IntArray");
    public final Name longArray = get("LongArray");
    public final Name floatArray = get("FloatArray");
    public final Name doubleArray = get("DoubleArray");
    public final Name toString = get("toString");
    public final Name out = get("out");
    public final Name in = get("in");
    public final Name time = get("time");
    public final Name password = get("password");
    public final Name parent = get("parent");
    public final Name children = get("children");
    public final Name id = get("id");
    public final Name File = get("File");
    public final Name sort = get("sort");
    public final Name reverse = get("reverse");

    private NameTable() {
    }

    public Name get(String name) {
        return nameMap.computeIfAbsent(name, NameImpl::new);
    }

    private class NameImpl implements Name {
        private final String string;

        public NameImpl(String string) {
            this.string = string;
        }

        @Override
        public boolean isEmpty() {
            return string.isEmpty();
        }

        @Override
        public Name concat(String s) {
            return get(string + s);
        }

        @Override
        public int compareTo(@NotNull Name o) {
            return string.compareTo(o.toString());
        }

        @Override
        public String toString() {
            return string;
        }

    }
}
