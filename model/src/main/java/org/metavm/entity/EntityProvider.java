package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.metavm.object.type.*;
import org.metavm.object.view.Mapping;
import org.metavm.object.view.MappingProvider;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public interface EntityProvider extends MappingProvider, TypeDefProvider, RedirectStatusProvider {

    <T> T getEntity(Class<T> entityType, Id id);

    TypeRegistry getTypeRegistry();

    default Klass getKlass(Id id) {
        return getEntity(Klass.class, id);
    }

    default Mapping getMapping(Id id) {
        return getEntity(Mapping.class, id);
    }

    default RedirectStatus getRedirectStatus(Id id) {
        return getEntity(RedirectStatus.class, id);
    }

    default Type getType(Id id) {
        return getEntity(Type.class, id);
    }

    default TypeDef getTypeDef(Id id) {
        return getEntity(TypeDef.class, id);
    }

    <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Object... values);

    default @Nullable <T extends Entity> T selectFirstByKey(IndexDef<T> indexDef, Object... values) {
        return NncUtils.first(selectByKey(indexDef, values));
    }
}
