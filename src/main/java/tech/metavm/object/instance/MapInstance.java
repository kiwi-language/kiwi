package tech.metavm.object.instance;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.rest.FieldValueDTO;
import tech.metavm.object.instance.rest.InstanceParamDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapInstance extends Instance {

    private final Map<Instance, Instance> map = new HashMap<>();

    private boolean valueAsChild;

    public MapInstance(MapType type) {
        super(type);
    }

    public MapInstance(@Nullable Long id, MapType type, long version, long syncVersion) {
        super(id, type, version, syncVersion);
    }

    public @Nullable Instance get(Instance key) {
        checkKey(key);
        return map.get(key);
    }

    public Instance getOrDefault(Instance key, Instance value) {
        checkKey(key);
        checkValue(value);
        return map.getOrDefault(key, value);
    }

    public @Nullable Instance put(Instance key, Instance value) {
        checkKey(key);
        checkValue(value);
        return map.put(key, value);
    }

    public BooleanInstance containsKey(Instance key) {
        checkKey(key);
        return InstanceUtils.booleanInstance(map.containsKey(key));
    }

    public @Nullable Instance remove(Instance key) {
        checkKey(key);
        return map.remove(key);
    }

    public void clear() {
        map.clear();
    }

    public void putAll(MapInstance that) {
        checkMap(that);
        map.putAll(that.map);
    }

    private void checkMap(MapInstance that) {
        NncUtils.requireTrue(getKeyType().isAssignableFrom(that.getKeyType()));
        NncUtils.requireTrue(getValueType().isAssignableFrom(that.getValueType()));
    }

    private void checkKey(Instance key) {
        NncUtils.requireTrue(getKeyType().isInstance(key));
    }

    private void checkValue(Instance value) {
        NncUtils.requireTrue(getValueType().isInstance(value));
    }

    @Override
    public Object toColumnValue(long tenantId, IdentitySet<Instance> visited) {
        return null;
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public Set<Instance> getRefInstances() {
        return null;
    }

    @Override
    public InstancePO toPO(long tenantId) {
        return null;
    }

    @Override
    InstancePO toPO(long tenantId, IdentitySet<Instance> visited) {
        return null;
    }

    @Override
    public FieldValueDTO toFieldValueDTO() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    protected InstanceParamDTO getParam() {
        return null;
    }

    public Type getKeyType() {
        return getType().getKeyType();
    }

    public Type getValueType() {
        return getType().getValueType();
    }

    @Override
    public MapType getType() {
        return (MapType) super.getType();
    }
}
