package tech.metavm.entity;

import tech.metavm.entity.natives.IteratorImplNative;
import tech.metavm.entity.natives.ListNative;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.object.instance.BooleanInstance;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.PrimitiveType;
import tech.metavm.util.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListDef<E> extends ModelDef<List<E>, ClassInstance> {

    private final ClassType type;
    private final ModelDef<E, Instance> elementDef;
    private final PrimitiveType booleanType;

    protected ListDef(TypeReference<List<E>> typeReference, ClassType type,
                      ModelDef<E,Instance> elementDef, PrimitiveType booleanType) {
        super(typeReference, ClassInstance.class);
        this.elementDef = elementDef;
        this.type = type;
        this.booleanType = booleanType;
    }

    @Override
    public ClassType getType() {
        return type;
    }

    @Override
    protected List<E> allocateModel() {
        return new ArrayList<>();
    }

    @Override
    public void initModel(List<E> model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        var listNative = (ListNative) NativeInvoker.getNativeObject(instance);
        var iteratorNative = (IteratorImplNative) NativeInvoker.getNativeObject(listNative.iterator());
        while (iteratorNative.hasNext().isTrue()) {
            model.add(modelInstanceMap.getModel(elementDef.getJavaClass(), iteratorNative.next()));
        }
    }

    @Override
    public void updateModel(List<E> model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        model.clear();
        var listNative = (ListNative) NativeInvoker.getNativeObject(instance);
        var iteratorNative = (IteratorImplNative) NativeInvoker.getNativeObject(listNative.iterator());
        while(iteratorNative.hasNext().isTrue()) {
            model.add(modelInstanceMap.getModel(elementDef.getJavaClass(), iteratorNative.next()));
        }
    }

    @Override
    public void initInstance(ClassInstance instance, List<E> model, ModelInstanceMap instanceMap) {
        var listNative = (ListNative) NativeInvoker.getNativeObject(instance);
        listNative.List(new BooleanInstance(false, booleanType));
        for (E e : model) {
            listNative.add(instanceMap.getInstance(e));
        }
    }

    @Override
    public void updateInstance(ClassInstance instance, List<E> model, ModelInstanceMap instanceMap) {
        var listNative = (ListNative) NativeInvoker.getNativeObject(instance);
        listNative.clear();
        for (E e : model) {
            listNative.add(instanceMap.getInstance(e));
        }
    }

    @Override
    public Map<Object, Identifiable> getEntityMapping() {
        return null;
    }
}
