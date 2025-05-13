package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.WireTypes;

public class StringReference extends ValueReference implements  Comparable<StringReference> {

    private final String value;

    public StringReference(StringInstance instance, String value) {
        super(instance);
        this.value = value;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.STRING);
        output.writeUTF(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StringReference that && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public String getValue() {
        return value;
    }

    @Override
    public StringInstance get() {
        return (StringInstance) super.get();
    }

    public int compareTo(@NotNull StringReference s2) {
        return value.compareTo(s2.value);
    }

    @Override
    public String getTitle() {
        return value;
    }

    @Override
    public void writeTree(TreeWriter treeWriter) {
        treeWriter.write("\"" + Utils.escape(value) + "\"");
    }

    @Override
    public String stringValue() {
        return value;
    }

    @Override
    public Object toSearchConditionValue() {
        return value;
    }
}
