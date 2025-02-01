package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.object.instance.rest.StringFieldValue;
import org.metavm.object.instance.rest.StringInstanceParam;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public class StringReference extends Reference implements Comparable<StringReference> {

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
    public FieldValue toFieldValueDTO() {
        return new StringFieldValue(value);
    }

    @Override
    public String getTitle() {
        return value;
    }

    @Override
    public InstanceParam getParam() {
        return new StringInstanceParam(value);
    }

    @Override
    public String stringValue() {
        return value;
    }
}
