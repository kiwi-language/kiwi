package org.metavm.object.instance.core;

import org.jetbrains.annotations.Nullable;
import org.metavm.wire.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;

public class ReferenceAdapter implements WireAdapter<Reference> {
    @Override
    public void init(AdapterRegistry registry) {

    }

    @Override
    public Reference read(WireInput input, @Nullable Object parent) {
        var mvInput = (MvInput) input;
        return (Reference) mvInput.readValue();
    }

    @Override
    public void write(Reference o, WireOutput output) {
        var mvOutput = (MvOutput) output;
        mvOutput.writeValue(o);
    }

    @Override
    public void visit(WireVisitor visitor) {
        var mvVisitor = (StreamVisitor) visitor;
        mvVisitor.visitValue();
    }

    @Override
    public List<Class<? extends Reference>> getSupportedTypes() {
        return List.of(Reference.class, EntityReference.class, StringReference.class, ValueReference.class);
    }

    @Override
    public int getTag() {
        return -1;
    }
}
