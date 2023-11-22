package tech.metavm.entity;

import tech.metavm.entity.natives.IteratorImplNative;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.entity.natives.SetNative;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.PrimitiveType;
import tech.metavm.util.TypeReference;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SetDef<E> extends ModelDef<Set<E>, ClassInstance> {

    private ClassType type;

    private ModelDef<E, Instance> elementDef;

    private PrimitiveType booleanType;

    protected SetDef() {
        super(new TypeReference<>() {}, ClassInstance.class);
    }

    @Override
    public ClassType getType() {
        return type;
    }


    @Override
    protected Set<E> allocateModel() {
        return new HashSet<>();
    }

    @Override
    public void initModel(Set<E> model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        var setNative = (SetNative) NativeInvoker.getNativeObject(instance);
        var iteratorNative = (IteratorImplNative) NativeInvoker.getNativeObject(setNative.iterator());
        while (iteratorNative.hasNext().isTrue()) {
            model.add(modelInstanceMap.getEntity(elementDef.getJavaClass(), iteratorNative.next()));
        }
    }

    @Override
    public void updateModel(Set<E> model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        model.clear();
        var setNative = (SetNative) NativeInvoker.getNativeObject(instance);
        var iteratorNative = (IteratorImplNative) NativeInvoker.getNativeObject(setNative.iterator());
        while(iteratorNative.hasNext().isTrue()) {
            model.add(modelInstanceMap.getEntity(elementDef.getJavaClass(), iteratorNative.next()));
        }
    }

    @Override
    public void initInstance(ClassInstance instance, Set<E> model, ModelInstanceMap instanceMap) {
        var setNative = (SetNative) NativeInvoker.getNativeObject(instance);
        setNative.Set();
        for (E e : model) {
            setNative.add(instanceMap.getInstance(e));
        }
    }

    @Override
    public void updateInstance(ClassInstance instance, Set<E> model, ModelInstanceMap instanceMap) {
        var setNative = (SetNative) NativeInvoker.getNativeObject(instance);
        setNative.clear();
        for (E e : model) {
            setNative.add(instanceMap.getInstance(e));
        }
    }

    @Override
    public Map<Object, Identifiable> getEntityMapping() {
        return null;
    }
}
