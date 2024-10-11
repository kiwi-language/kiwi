package org.metavm.util;

import org.metavm.object.instance.core.Value;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarkingInstanceOutput extends InstanceOutput {

    private final List<Integer> valueOffsets = new ArrayList<>();
    private final ByteArrayOutputStream bout;

    public MarkingInstanceOutput() {
        this(new ByteArrayOutputStream());
    }

    private MarkingInstanceOutput(ByteArrayOutputStream bout) {
        super(bout);
        this.bout = bout;
    }

    @Override
    public void writeValue(Value value) {
        recordValueOffset();
        super.writeValue(value);
    }

    @Override
    public void writeInstance(Value value) {
        recordValueOffset();
        super.writeInstance(value);
    }

    private void recordValueOffset() {
        valueOffsets.add(bout.size());
    }

    public byte[] toByteArray() {
        return bout.toByteArray();
    }

    public List<Integer> getValueOffsets() {
        return Collections.unmodifiableList(valueOffsets);
    }
}
