package org.metavm.wire;

import javax.annotation.Nullable;
import java.util.List;

public interface WireAdapter<T> {

    void init(AdapterRegistry registry);

    T read(WireInput input, @Nullable Object parent);

    void write(T o, WireOutput output);

    void visit(WireVisitor visitor);

    List<Class<? extends T>> getSupportedTypes();

    int getTag();

}
