package org.metavm.entity.natives;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;
import org.metavm.util.MvObjectOutputStream;

import java.util.function.Consumer;

public class ChildListNative extends ArrayListNative implements StatefulNative {

    public ChildListNative(ClassInstance instance) {
        super(instance);
    }

    public Value ChildList(CallContext callContext) {
        return List(callContext);
    }

    public Value ChildList(Value c, CallContext callContext) {
        return List(c, callContext);
    }

    @Override
    protected void add0(Value v) {
        v.resolveDurable().setParent(instance, null);
        super.add0(v);
    }

    public Value writeObject(Value o, CallContext callContext) {
        var out = ((MvObjectOutputStream) o.resolveObject()).getOut();
        out.writeInt(list.size());
        for (Value value : list) {
            out.writeInstance(value);
        }
        return Instances.nullInstance();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        for (Value value : list) {
            action.accept(value.resolveDurable());
        }
    }

    @Override
    public void forEachValue(Consumer<? super Instance> action) {
    }

    @Override
    public void onChildRemove(Instance child) {
        assert child.getParent() == instance;
        list.remove(child.getReference());
    }

    @Override
    public boolean isChildList() {
        return true;
    }

}
