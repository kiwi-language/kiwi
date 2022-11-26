package tech.metavm.object.instance;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.instance.persistence.IndexItemPO;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.Table;

import java.util.List;
import java.util.function.Supplier;

public class InstanceArrayRef implements IInstanceArray {
    private final Long id;
    private InstanceArray realInstanceArray;
    private final Supplier<InstanceArray> loader;

    public InstanceArrayRef(Long id, Supplier<InstanceArray> loader) {
        this.id = id;
        this.loader = loader;
    }

    private void ensureLoaded() {
        if(realInstanceArray == null) {
            realInstanceArray = loader.get();
        }
    }

    @Override
    public IInstance get(int i) {
        ensureLoaded();
        return realInstanceArray.get(i);
    }

    @Override
    public void add(IInstance element) {
        ensureLoaded();
        realInstanceArray.add(element);
    }

    @Override
    public void remove(IInstance element) {
        ensureLoaded();
        realInstanceArray.remove(element);
    }

    @Override
    public int length() {
        ensureLoaded();
        return realInstanceArray.length();
    }

    @Override
    public List<IInstance> getElements() {
        ensureLoaded();
        return realInstanceArray.getElements();
    }

    @Override
    public boolean isElementAsChild() {
        ensureLoaded();
        return realInstanceArray.isElementAsChild();
    }

    @Override
    public InstanceArrayPO toPO() {
        ensureLoaded();
        return realInstanceArray.toPO();
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void update(InstanceDTO instanceDTO) {

    }

    @Override
    public Type getType() {
        ensureLoaded();
        return realInstanceArray.getType();
    }

    @Override
    public IInstance getInstance(Field field) {
        return null;
    }

    @Override
    public Object getRaw(String fieldName) {
        return null;
    }

    @Override
    public Object getRaw(Field field) {
        return null;
    }

    @Override
    public Object getRaw(long fieldId) {
        return null;
    }

    @Override
    public String getString(Field field) {
        return null;
    }

    @Override
    public InstanceDTO toDTO() {
        ensureLoaded();
        return realInstanceArray.toDTO();
    }

    @Override
    public void set(Field field, Object value) {

    }

    @Override
    public Object getResolved(List<Long> fieldPath) {
        return null;
    }

    @Override
    public Object getRaw(List<Long> fieldPath) {
        return null;
    }

    @Override
    public List<IndexItemPO> getUniqueKeys() {
        return null;
    }

    @Nullable
    @Override
    public Class<?> getEntityType() {
        return Table.class;
    }

    @Override
    public InstanceArray getInstanceArray(Field field) {
        return null;
    }

    public InstanceArray getRealInstanceArray() {
        ensureLoaded();
        return realInstanceArray;
    }
}
