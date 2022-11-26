package tech.metavm.object.instance;

import tech.metavm.entity.InstanceContext;
import tech.metavm.object.instance.persistence.IndexItemPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;

import javax.annotation.Nullable;
import java.util.List;

public interface IInstance {

    Long getId();

//    long getTenantId();

    String getTitle();

    void update(InstanceDTO instanceDTO);

//    InstanceContext getContext();

    Type getType();

    IInstance getInstance(Field field);

    Object getRaw(String fieldName);

    Object getRaw(Field field);

    Object getRaw(long fieldId);

    String getString(Field field);

    default void remove() {}

    InstanceDTO toDTO();

    void set(Field field, Object value);

    Object getResolved(List<Long> fieldPath);

    Object getRaw(List<Long> fieldPath);

    List<IndexItemPO> getUniqueKeys();

    @Nullable Class<?> getEntityType();

    InstanceArray getInstanceArray(Field field);

}
