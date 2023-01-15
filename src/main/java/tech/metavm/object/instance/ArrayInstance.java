package tech.metavm.object.instance;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.NoProxy;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.rest.ArrayFieldValueDTO;
import tech.metavm.object.instance.rest.ArrayParamDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import java.util.*;

public class ArrayInstance extends Instance implements Collection<Instance> {

    public static ArrayInstance allocate(ArrayType type) {
        return new ArrayInstance(type);
    }

    private final List<Instance> elements = new ArrayList<>();
    private boolean elementAsChild;

    protected ArrayInstance(ArrayType type) {
        super(type);
        this.elementAsChild = false;
    }


    public ArrayInstance(ArrayType type, boolean elementAsChild) {
        super(type);
        this.elementAsChild = elementAsChild;
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
        for (Instance element : elements) {
            addInternally(element);
        }
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
        for (Instance element : elements) {
            addInternally(element);
        }
    }

    @NoProxy
    public void initialize(List<Instance> elements) {
        for (Instance element : elements) {
            addInternally(element);
        }
    }

    @Override
    public boolean isChild(Instance instance) {
        return elementAsChild && elements.contains(instance);
    }

    public Set<Instance> getChildren() {
        if(elementAsChild) {
            return NncUtils.filterUnique(elements, Instance::isNotNull);
        }
        else {
            return Set.of();
        }
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

    public void setElementAsChild(boolean elementAsChild) {
        this.elementAsChild = elementAsChild;
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
        return addInternally(element);
    }

    private boolean addInternally(Instance element) {
        new ReferenceRT(this, element, null);
        elements.add(element);
        return true;
    }

    public void setElements(List<Instance> elements) {
        for (Instance element : new ArrayList<>(this.elements)) {
            remove(element);
        }
        this.addAll(elements);
    }

    @Override
    public boolean remove(Object element) {
        return removeInternally(element);
    }

    private boolean removeInternally(Object element) {
        //noinspection SuspiciousMethodCalls
        if(elements.remove(element)) {
            Instance removed = (Instance) element;
            getOutgoingReference(removed, null).clear();
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        //noinspection SlowListContainsAll
        return elements.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Instance> c) {
        c.forEach(this::add);
        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean anyChange = false;
        for (Object o : c) {
            if(remove(o)) {
                anyChange = true;
            }
        }
        return anyChange;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        List<Object> toRemove = new ArrayList<>();
        for (Object o : c) {
            if(!contains(o)) {
                toRemove.add(o);
            }
        }
        if(toRemove.isEmpty()) {
            return false;
        }
        for (Object o : toRemove) {
            remove(o);
        }
        return true;
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
    @NoProxy
    public Object toColumnValue(long tenantId, IdentitySet<Instance> visited) {
        return toIdentityPO();
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    protected ArrayParamDTO getParam() {
        return new ArrayParamDTO(
                NncUtils.map(elements, Instance::toFieldValueDTO)
        );
    }

    private static Object elementToPO(long tenantId, Instance element, IdentitySet<Instance> visited) {
        if(element.isNull()) {
            return null;
        }
        if(element instanceof PrimitiveInstance primitiveInstance) {
            return primitiveInstance.getValue();
        }
        else {
            if(element.isValue()) {
                return element.toPO(tenantId, visited);
            }
            else {
                return element.toIdentityPO();
            }
        }
    }

    @Override
    @NoProxy
    public ArrayType getType() {
        return (ArrayType) super.getType();
    }

    @Override
    public ArrayFieldValueDTO toFieldValueDTO() {
        return new ArrayFieldValueDTO(
                getId(),
                NncUtils.map(elements, Instance::toFieldValueDTO)
        );
    }

    public void clear() {
        for (Instance instance : new ArrayList<>(elements)) {
            remove(instance);
        }
    }

    @Override
    public String toString() {
        return "InstanceArray{" +
                "elements=" + elements +
                ", elementAsChild=" + elementAsChild +
                '}';
    }

}
