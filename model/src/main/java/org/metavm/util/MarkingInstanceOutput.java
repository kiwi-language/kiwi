package org.metavm.util;

import org.metavm.object.instance.core.Value;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarkingInstanceOutput extends InstanceOutput {

    private final List<Integer> skips = new ArrayList<>();
    private int lastValueEnd;
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
        recordSkip();
        super.writeValue(value);
        lastValueEnd = size();
    }

    @Override
    public void writeInstance(Value value) {
        recordSkip();
        super.writeInstance(value);
        lastValueEnd = size();
    }

    private void recordSkip() {
        skips.add(size() - lastValueEnd);
    }

    public byte[] toByteArray() {
        return bout.toByteArray();
    }

    public List<Integer> getSkips() {
        return Collections.unmodifiableList(skips);
    }

    public int size() {
        return bout.size();
    }

    public int getLastSkip() {
        return size() - lastValueEnd;
    }

}
