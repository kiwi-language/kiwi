package org.metavm.wire.adapters;

import org.metavm.wire.*;

import javax.annotation.Nullable;
import java.util.List;

public class ObjectAdapter implements WireAdapter<Object> {

    private AdapterRegistry registry;

    @Override
    public void init(AdapterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Object read(WireInput input, @Nullable Object parent) {
        var tag = input.read();
        return registry.getAdapter(tag).read(input, parent);
    }

    @Override
    public void write(Object o, WireOutput output) {
        //noinspection rawtypes
        var adapter = (WireAdapter) registry.getAdapter(o.getClass());
        output.write(adapter.getTag());
        //noinspection unchecked
        adapter.write(o, output);
    }

    @Override
    public void visit(WireVisitor visitor) {
        var tag = visitor.visitByte();
        var adapter = registry.getAdapter(tag);
        adapter.visit(visitor);
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return List.of(Object.class);
    }

    @Override
    public int getTag() {
        return -1;
    }
}
