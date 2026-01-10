package org.manul.entity;

import org.manul.ddl.Commit;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Reference;
import org.manul.object.instance.core.Value;
import org.manul.object.type.*;
import org.manul.util.Instances;
import org.manul.util.Utils;

import javax.annotation.Nullable;
import java.util.List;

public interface EntityProvider extends TypeDefProvider, RedirectStatusProvider, ActiveCommitProvider {

    <T> T getEntity(Class<T> entityType, Id id);

    Reference createReference(Id id);

    default Klass getKlass(Id id) {
        return getEntity(Klass.class, id);
    }

    default RedirectStatus getRedirectStatus(Id id) {
        return getEntity(RedirectStatus.class, id);
    }

    default Type getType(Id id) {
        return getEntity(Type.class, id);
    }

    default ITypeDef getTypeDef(Id id) {
        return getEntity(TypeDef.class, id);
    }

    @Nullable
    @Override
    default Commit getActiveCommit() {
        return selectFirstByKey(Commit.IDX_RUNNING, Instances.trueInstance());
    }

    <T extends Entity> List<T> selectByKey(IndexDef<T> indexDef, Value... values);

    default @Nullable <T extends Entity> T selectFirstByKey(IndexDef<T> indexDef, Value... values) {
        return Utils.first(selectByKey(indexDef, values));
    }
}
