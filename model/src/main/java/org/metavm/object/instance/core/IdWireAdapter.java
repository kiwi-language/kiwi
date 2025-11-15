package org.metavm.object.instance.core;

import org.jetbrains.annotations.Nullable;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import org.metavm.wire.*;

public class IdWireAdapter implements WireAdapter<Id> {
    @Override
    public void init(AdapterRegistry registry) {

    }

    @Override
    public Id read(WireInput input, @Nullable Object parent) {
        return Id.readId((MvInput) input);
    }

    @Override
    public void write(Id o, WireOutput output) {
        o.write((MvOutput) output);
    }

    @Override
    public void visit(WireVisitor visitor) {
        var streamVisitor = (StreamVisitor) visitor;
       streamVisitor.visitId();
    }

    @Override
    public List<Class<? extends Id>> getSupportedTypes() {
        return List.of(Id.class);
    }

    @Override
    public int getTag() {
        return -1;
    }
}
