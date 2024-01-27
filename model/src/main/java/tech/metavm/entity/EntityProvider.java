package tech.metavm.entity;

import tech.metavm.common.RefDTO;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeProvider;
import tech.metavm.object.view.Mapping;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public interface EntityProvider extends MappingProvider, TypeProvider {

    <T> T getEntity(Class<T> entityType, RefDTO ref);

    default <T> T getEntity(Class<T> entityType, long id) {
        return getEntity(entityType, RefDTO.fromId(id));
    }

    default ClassType getClassType(RefDTO ref) {
        return getEntity(ClassType.class, ref);
    }

    default Mapping getMapping(RefDTO ref) {
        return getEntity(Mapping.class, ref);
    }

    default Type getType(long id) {
        return getEntity(Type.class, id);
    }

    default Type getType(RefDTO ref) {
        return getEntity(Type.class, ref);
    }

    <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Object... values);

    default @Nullable <T extends Entity> T selectFirstByKey(IndexDef<T> indexDef, Object... values) {
        return NncUtils.first(selectByKey(indexDef, values));
    }
}
