package tech.metavm.object.instance;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.NoProxy;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import java.util.*;

public class ArrayInstance extends Instance implements Collection<Instance> {

    public static ArrayInstance allocate(ArrayType type) {
        return new ArrayInstance(type);
    }

    private final List<Instance> elements;
    private final boolean elementAsChild;

    protected ArrayInstance(ArrayType type) {
        super(type);
        elements = new ArrayList<>();
        this.elementAsChild = false;
    }

    public ArrayInstance(ArrayType type,
                         List<Instance> elements) {
        this(type, elements, false);
    }

    public ArrayInstance(ArrayType type,
                         List<Instance> elements,
                         boolean elementAsChild
                         ) {
        super(type);
        this.elementAsChild = elementAsChild;
        this.elements = new ArrayList<>(elements);
    }

    public ArrayInstance(Long id,
                         ClassType type,
                         long version,
                         long syncVersion,
                         List<Instance> elements,
                         boolean elementAsChild
    ) {
        super(id, type, version, syncVersion);
        this.elementAsChild = elementAsChild;
        this.elements = new ArrayList<>(elements);
    }

    @NoProxy
    public void initialize(List<Instance> elements) {
        this.elements.addAll(elements);
    }

    public Instance get(int i) {
        return elements.get(i);
    }

    public Instance getInstance(int i) {
        return elements.get(i);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return elements.contains(o);
    }

    public BooleanInstance instanceContains(Instance instance) {
        return InstanceUtils.createBoolean(contains(instance));
    }

    @NotNull
    @Override
    public Iterator<Instance> iterator() {
        return elements.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return elements.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return elements.toArray(a);
    }

    @Override
    public boolean add(Instance element) {
        elements.add(element);
        return true;
    }

    public void setElements(List<Instance> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
    }

    @Override
    public boolean remove(Object element) {
        return elements.remove(element);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        //noinspection SlowListContainsAll
        return elements.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Instance> c) {
        return elements.addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return elements.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return elements.retainAll(c);
    }

    public int length() {
        return elements.size();
    }

    public List<Instance> getElements() {
        return elements;
    }

    public boolean isElementAsChild() {
        return elementAsChild;
    }

    @Override
    public Set<Instance> getRefInstances() {
        return new IdentitySet<>(
                NncUtils.filter(elements, Instance::isReference)
        );
    }

    @Override
    @NoProxy
    public boolean isReference() {
        return true;
    }

    @Override
    public InstanceArrayPO toPO(long tenantId) {
        return toPO(tenantId, new IdentitySet<>());
    }

    @Override
    InstanceArrayPO toPO(long tenantId, IdentitySet<Instance> visited) {
        return new InstanceArrayPO(
                getId(),
                getType().getId(),
                tenantId,
                elements.size(),
                NncUtils.map(elements, e -> elementToPO(tenantId, e, visited)),
                elementAsChild,
                getVersion(),
                getSyncVersion()
        );
    }

    @Override
    public Object toColumnValue(long tenantId, IdentitySet<Instance> visited) {
        return toReferencePO();
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    protected Object getParam() {
        return null;
    }

    private static Object elementToPO(long tenantId, Object element, IdentitySet<Instance> visited) {
        if(element == null) {
            return null;
        }
        if(element instanceof Instance instance) {
            if(instance.isValue()) {
                return instance.toPO(tenantId, visited);
            }
            else {
                return instance.toReferencePO();
            }
        }
        else {
            return element;
        }
    }

    public void clear() {
        elements.clear();
    }

    @Override
    public String toString() {
        return "InstanceArray{" +
                "elements=" + elements +
                ", elementAsChild=" + elementAsChild +
                '}';
    }

}
