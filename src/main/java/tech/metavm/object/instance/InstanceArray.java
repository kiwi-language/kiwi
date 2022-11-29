package tech.metavm.object.instance;

import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Map;

import static tech.metavm.util.ContextUtil.getTenantId;

public class InstanceArray extends Instance implements IInstanceArray {

    private final List<Instance> elements;
    private final boolean elementAsChild;

    public InstanceArray(Type type,
                         Class<?> entityType,
                         List<Instance> elements,
                         boolean elementAsChild
                         ) {
        super(Map.of(), type, entityType);
        this.elementAsChild = elementAsChild;
        this.elements = elements;
    }

    public InstanceArray(Long id,
                         Type type,
                         long version,
                         long syncVersion,
                         Class<?> entityType,
                         List<Instance> elements,
                         boolean elementAsChild
    ) {
        super(id, Map.of(), type, version, syncVersion, entityType);
        this.elementAsChild = elementAsChild;
        this.elements = elements;
    }

    @Override
    public Instance get(int i) {
        return elements.get(i);
    }

    @Override
    public void add(Instance element) {
        elements.add(element);
    }

    @Override
    public void remove(Instance element) {
        elements.remove(element);
    }

    @Override
    public int length() {
        return elements.size();
    }

    @Override
    public List<Instance> getElements() {
        return elements;
    }

    @Override
    public boolean isElementAsChild() {
        return elementAsChild;
    }

    @Override
    public InstanceArrayPO toPO() {
        return new InstanceArrayPO(
                getId(),
                getType().getId(),
                getTenantId(),
                elements.size(),
                NncUtils.map(elements, Instance::getId),
                elementAsChild,
                getVersion(),
                getSyncVersion()
        );
    }

    public void clear() {
        elements.clear();
    }

}
