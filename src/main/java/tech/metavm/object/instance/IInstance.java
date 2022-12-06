package tech.metavm.object.instance;

import tech.metavm.object.instance.persistence.IndexItemPO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;

import java.util.List;

public interface IInstance {

    Long getId();

//    long getTenantId();

    String getTitle();

//    void update(InstanceDTO instanceDTO);

//    InstanceContext getContext();

    Type getType();

    Instance getInstance(Field field);

    Object get(String fieldName);

    Object get(Field field);

    Object get(long fieldId);

    String getString(Field field);

    default void remove() {}

    InstanceDTO toDTO();

    void set(Field field, Object value);

    Object getResolved(List<Long> fieldPath);

    Object get(List<Long> fieldPath);

    List<IndexItemPO> getUniqueKeys(long tenantId);

    InstanceArray getInstanceArray(Field field);

}
