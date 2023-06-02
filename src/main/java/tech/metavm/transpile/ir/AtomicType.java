package tech.metavm.transpile.ir;

import org.jetbrains.annotations.Nullable;

public abstract class AtomicType extends IRType {

    public AtomicType(@Nullable String name) {
        super(name);
    }

}
