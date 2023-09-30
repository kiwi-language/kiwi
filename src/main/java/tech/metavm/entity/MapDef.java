package tech.metavm.entity;

import tech.metavm.entity.natives.IteratorImplNative;
import tech.metavm.entity.natives.MapNative;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.entity.natives.SetNative;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.PrimitiveType;
import tech.metavm.util.TypeReference;

import java.util.HashMap;
import java.util.Map;

public class MapDef<K, V> extends ModelDef<Map<K, V>, ClassInstance> {

    private final ClassType type;
    private final ModelDef<K, Instance> keyDef;
    private final ModelDef<V, Instance> valueDef;
    private final PrimitiveType booleanType;

    protected MapDef(TypeReference<Map<K, V>> typeReference, ClassType type,
                     ModelDef<K, Instance> keyDef, ModelDef<V, Instance> valueDef,
                     PrimitiveType booleanType) {
        super(typeReference, ClassInstance.class);
        this.keyDef = keyDef;
        this.valueDef = valueDef;
        this.type = type;
        this.booleanType = booleanType;
    }

    @Override
    public ClassType getType() {
        return type;
    }

    @Override
    protected Map<K, V> allocateModel() {
        return new HashMap<>();
    }

    @Override
    public void initModel(Map<K, V> model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        addModelElements(model, instance, modelInstanceMap, false);
    }

    @Override
    public void updateModel(Map<K, V> model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        addModelElements(model, instance, modelInstanceMap, true);
    }

    public void addModelElements(Map<K,V> model, ClassInstance instance, ModelInstanceMap modelInstanceMap, boolean clear) {
        var mapNative = (MapNative) NativeInvoker.getNativeObject(instance);
        if(clear) {
            mapNative.clear();
        }
        var keySetNative = (SetNative) NativeInvoker.getNativeObject(mapNative.keySet());
        var keyIteratorNative = (IteratorImplNative) NativeInvoker.getNativeObject(keySetNative.iterator());
        while(keyIteratorNative.hasNext().isTrue()) {
            var key = keyIteratorNative.next();
            var value = mapNative.get(key);
            model.put(modelInstanceMap.getModel(keyDef.getJavaClass(), key, keyDef),
                    modelInstanceMap.getModel(valueDef.getJavaClass(), value, valueDef));
        }
    }

    @Override
    public void initInstance(ClassInstance instance, Map<K, V> model, ModelInstanceMap instanceMap) {
        addElements(model, instance, instanceMap, false);
    }

    @Override
    public void updateInstance(ClassInstance instance, Map<K, V> model, ModelInstanceMap instanceMap) {
        addElements(model, instance, instanceMap, true);
    }

    private void addElements(Map<K, V> model, ClassInstance instance, ModelInstanceMap modelInstanceMap, boolean clear) {
        var mapNative = (MapNative) NativeInvoker.getNativeObject(instance);
        if(clear) {
            mapNative.clear();
        }
        model.forEach((k, v) ->
                mapNative.put(modelInstanceMap.getInstance(k), modelInstanceMap.getInstance(v))
        );
    }


    @Override
    public Map<Object, Identifiable> getEntityMapping() {
        return null;
    }
}
