package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeProvider;
import tech.metavm.object.view.Mapping;
import tech.metavm.object.view.MappingProvider;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public interface EntityProvider extends MappingProvider, TypeProvider {

    <T> T getEntity(Class<T> entityType, Id id);

    TypeRegistry getTypeRegistry();

    default ClassType getClassType(Id id) {
        return getEntity(ClassType.class, id);
    }

    default Mapping getMapping(Id id) {
        return getEntity(Mapping.class, id);
    }

    default Type getType(Id id) {
        return getEntity(Type.class, id);
    }

    <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Object... values);

    default @Nullable <T extends Entity> T selectFirstByKey(IndexDef<T> indexDef, Object... values) {
        return NncUtils.first(selectByKey(indexDef, values));
    }
}
