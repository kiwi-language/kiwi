package org.metavm.entity.natives;

import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.ValueReference;
import org.metavm.object.type.Type;
import org.metavm.util.TriConsumer;
import org.metavm.util.TriFunction;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface StatefulNative {

    void forEachReference(Consumer<Reference> action);

    void forEachReference(BiConsumer<Reference, Boolean> action);

    void forEachReference(TriConsumer<Reference, Boolean, Type> action);

    void transformReference(TriFunction<Reference, Boolean, Type, Reference> function);

    default void forEachChild(Consumer<? super Instance> action) {}

    default void forEachMember(Consumer<? super Instance> action) {
        forEachValue(action);
    }

    default void forEachValue(Consumer<? super Instance> action) {
        forEachReference(r -> {
            if (r instanceof ValueReference vr)
                action.accept(vr.get());
        });
    }

    default void onChildRemove(Instance child) {
        throw new UnsupportedOperationException();
    }

}
