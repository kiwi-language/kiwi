package tech.metavm.object.instance;

import tech.metavm.object.instance.persistence.InstanceArrayPO;

import java.util.List;

public interface IInstanceArray extends IInstance {

    Object get(int i);

    void add(Object element);

    void remove(Object element);

    int length();

    List<Object> getElements();

    boolean isElementAsChild();

    InstanceArrayPO toPO(long tenantId);
}
