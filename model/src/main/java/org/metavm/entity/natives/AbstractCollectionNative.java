package org.metavm.entity.natives;

import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;

public abstract class AbstractCollectionNative implements CollectionNative {

    @Override

    public Value containsAll(Value values, CallContext callContext) {
        var iterableNative = (IterableNative) (values.resolveObject().getNativeObject());
        for (Value value : iterableNative) {
            if (!Instances.toBoolean(contains(value, callContext)))
                return Instances.zero();
        }
        return Instances.one();
    }

    @Override
    public Value retainAll(Value values, CallContext callContext) {
        var collNative = (AbstractCollectionNative) (values.resolveObject().getNativeObject());
        var it = iterator();
        var modified = false;
        while (it.hasNext()) {
            if (!Instances.toBoolean(collNative.contains(it.next(), callContext))) {
                it.remove();
                modified = true;
            }
        }
        return Instances.intInstance(modified);
    }

}
