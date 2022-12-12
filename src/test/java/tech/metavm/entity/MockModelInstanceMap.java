package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.util.NncUtils;

import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

public class MockModelInstanceMap implements ModelInstanceMap {

    private final MockModelInstanceMap parent;

    private final Map<Instance, Object> instance2model = new IdentityHashMap<>();
    private final Map<Object, Instance> model2instance = new IdentityHashMap<>();

    private final DefMap defMap;

    public MockModelInstanceMap(Function<ModelInstanceMap, ? extends DefMap> defCreator) {
        this(defCreator, null);
    }

    public MockModelInstanceMap(Function<ModelInstanceMap, ? extends DefMap> defCreator, MockModelInstanceMap parent) {
        this.defMap = defCreator.apply(this);
        this.parent = parent;
    }

    public MockModelInstanceMap(DefMap defMap) {
        this.defMap = defMap;
        parent = null;
    }

    public DefMap getDefMap() {
        return defMap;
    }

    @Override
    public <T> T getModel(Class<T> klass, Instance instance) {
        if(parent != null && parent.containsInstance(instance)) {
            return parent.getModel(klass, instance);
        }
        Object existing;
        if((existing = instance2model.get(instance)) != null) {
            return klass.cast(existing);
        }
        else {
            Object ref = createRef(instance);
            addMapping(ref, instance);
            return klass.cast(ref);
        }
    }

    private void addMapping(Object model, Instance instance) {
        model2instance.put(model, instance);
        instance2model.put(instance, model);
    }

    private Object createRef(Instance instance) {
        Class<?> entityType = defMap.getDef(instance.getType()).getJavaClass();
        NncUtils.requireNonNull(entityType);
        if(Modifier.isFinal(entityType.getModifiers())) {
            return defMap.getDef(instance.getType()).createModelHelper(instance, this);
        }
        else {
            return createRefHelper(instance, entityType);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createRefHelper(Instance instance, Class<T> entityType) {
        ModelDef<T, ?> def = (ModelDef<T, ?>) defMap.getDef(instance.getType());
        return EntityProxyFactory.getProxy(
                entityType,
                instance.getId(),
                model -> initModel(model, instance),
                k -> def.createModelProxy(k)
        );
    }

    private void initModel(Object model, Instance instance) {
        defMap.getDef(instance.getType()).initModelHelper(model, instance, this);
    }

    @Override
    public Instance getInstance(Object model) {
        if(parent != null && parent.containsModel(model)) {
            return parent.getInstance(model);
        }
        Instance instance = model2instance.get(model);
        if(instance == null) {
            instance = createInstance(model);
        }
        return instance;
    }

    private Instance createInstance(Object model) {
        ModelDef<?,?> def = defMap.getDefByModel(model);
        if(def.isProxySupported()) {
            Instance instance = InstanceFactory.allocate(def.getInstanceType(), def.getType());
            if((model instanceof Identifiable identifiable) && identifiable.getId() != null) {
                instance.initId(identifiable.getId());
            }
            addMapping(model, instance);
            def.initInstanceHelper(instance, model, this);
            return instance;
        }
        else {
            Instance instance = def.createInstanceHelper(model, this);
            addMapping(model, instance);
            return instance;
        }
    }

    public boolean containsInstance(Instance instance) {
        return instance2model.containsKey(instance);
    }

    public boolean containsModel(Object model) {
        return model2instance.containsKey(model);
    }

    public void clear() {
        instance2model.clear();
        model2instance.clear();
    }
}
