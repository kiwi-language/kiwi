package tech.metavm.object.instance;

import tech.metavm.entity.NoProxy;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InstanceArray extends Instance implements IInstanceArray {

    public static InstanceArray allocate(Type type) {
        return new InstanceArray(type);
    }

    private final List<Object> elements;
    private final boolean elementAsChild;

    protected InstanceArray(Type type) {
        super(Map.of(), type);
        elements = new ArrayList<>();
        this.elementAsChild = false;
    }

    public InstanceArray(Type type,
                         List<Object> elements) {
        this(type, elements, false);
    }

    public InstanceArray(Type type,
                         List<Object> elements,
                         boolean elementAsChild
                         ) {
        super(Map.of(), type);
        this.elementAsChild = elementAsChild;
        this.elements = new ArrayList<>(elements);
    }

    public InstanceArray(Long id,
                         Type type,
                         long version,
                         long syncVersion,
                         List<Object> elements,
                         boolean elementAsChild
    ) {
        super(id, Map.of(), type, version, syncVersion);
        this.elementAsChild = elementAsChild;
        this.elements = new ArrayList<>(elements);
    }

    @NoProxy
    public void initialize(List<Object> elements) {
        this.elements.addAll(elements);
    }

    public Object get(int i) {
        return elements.get(i);
    }

    public Instance getInstance(int i) {
        return (Instance) elements.get(i);
    }

    @Override
    public void add(Object element) {
        elements.add(element);
    }

    public void setElements(List<Object> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
    }

    @Override
    public void remove(Object element) {
        elements.remove(element);
    }

    @Override
    public int length() {
        return elements.size();
    }

    @Override
    public List<Object> getElements() {
        return elements;
    }

    @Override
    public boolean isElementAsChild() {
        return elementAsChild;
    }

    @Override
    public Set<Instance> getRefInstances() {
        return new IdentitySet<>(
                NncUtils.filterAndMap(
                        elements,
                        Instance.class::isInstance,
                        Instance.class::cast
                )
        );
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
