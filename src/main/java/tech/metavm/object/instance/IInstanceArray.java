package tech.metavm.object.instance;

import tech.metavm.object.instance.persistence.InstanceArrayPO;

import java.util.List;

public interface IInstanceArray extends IInstance {

    IInstance get(int i);

    void add(IInstance element);

    void remove(IInstance element);

    int length();

    List<IInstance> getElements();

    boolean isElementAsChild();

    InstanceArrayPO toPO();
}
