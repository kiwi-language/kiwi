package tech.metavm.object.instance;

import tech.metavm.object.instance.persistence.InstanceArrayPO;

import java.util.List;

public interface IInstanceArray extends IInstance {

    Instance get(int i);

    void add(Instance element);

    void remove(Instance element);

    int length();

    List<Instance> getElements();

    boolean isElementAsChild();

    InstanceArrayPO toPO();
}
