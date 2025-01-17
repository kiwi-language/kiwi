package org.metavm.entity.natives;

import org.metavm.entity.StdKlass;
import org.metavm.entity.StdMethod;
import org.metavm.flow.Flows;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.MvClassInstance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;

import java.util.List;
import java.util.Objects;

public abstract class SetNative extends IterableNative {

    public Value equals(Value o, CallContext callContext) {
        if(o instanceof Reference ref) {
            if(ref.get() instanceof MvClassInstance that
                    && Objects.equals(that.getInstanceType().asSuper(StdKlass.set.get()), getInstance().getInstanceType().asSuper(StdKlass.set.get()))) {
                var thatNat = (SetNative) NativeMethods.getNativeObject(that);
                if(size() == thatNat.size()) {
                    for (Value value : thatNat) {
                        if(!thatNat.contains0(value, callContext))
                            return Instances.zero();
                    }
                    return Instances.one();
                }
            }
        }
        return Instances.zero();
    }

    public Value hashCode(CallContext callContext) {
        int h = 0;
        for (Value value : this) {
            h = h + Instances.hashCode(value, callContext);
        }
        return Instances.intInstance(h);
    }

    public Value addAll(Value values, CallContext callContext) {
        var ref = new Object() {
            boolean changed;
        };
        Instances.forEach(values, e -> {
            if(Instances.toBoolean(add(e, callContext)))
                ref.changed = true;
        });
        return Instances.intInstance(ref.changed);
    }

    public Value containsAll(Value values, CallContext callContext) {
        var ref = new Object() {
            boolean containsAll = true;
        };
        Instances.forEach(values, e -> {
            if(!Instances.toBoolean(contains(e, callContext)))
                ref.containsAll = false;
        });
        return Instances.intInstance(ref.containsAll);
    }

    public Value retainAll(Value value, CallContext callContext) {
        var coll = value.resolveObject();
        var containsMethod = coll.getInstanceType().getMethod(StdMethod.collectionContains.get());
        var it = iterator();
        boolean changed = false;
        while (it.hasNext()) {
            var e = it.next();
            if(!Instances.toBoolean(Flows.invokeVirtual(containsMethod, coll, List.of(e), callContext))) {
                it.remove();
                changed = true;
            }
        }
        return Instances.intInstance(changed);
    }

    public abstract Value add(Value value, CallContext callContext);

    public Value contains(Value value, CallContext callContext) {
        return Instances.intInstance(contains0(value, callContext));
    }

    public Value size(CallContext callContext) {
        return Instances.intInstance(size());
    }

    public abstract int size();

    public abstract boolean contains0(Value value, CallContext callContext);

    public abstract ClassInstance getInstance();

}
