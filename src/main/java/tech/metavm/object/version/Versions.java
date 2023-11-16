package tech.metavm.object.version;

import tech.metavm.entity.*;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Set;

public class Versions {

    public static Version create(Type type, IEntityContext context) {
        return create(Set.of(type), Set.of(), context);
    }

    public static Version createForRemoval(Long removedTypeId, IEntityContext context) {
        return create(Set.of(), Set.of(removedTypeId), context);
    }

    public static Version create(Set<Type> changedTypes, Set<Long> removedTypeIds, IEntityContext context) {
        NncUtils.requireTrue(!changedTypes.isEmpty() || !removedTypeIds.isEmpty(),
                "Change set is empty");
        if (NncUtils.anyMatch(changedTypes, Entity::isIdNull))
            context.initIds();
        Version lastVersion = NncUtils.getFirst(context.query(new EntityIndexQuery<>(
                Version.IDX_VERSION,
                List.of(new EntityIndexQueryItem("version", Long.MAX_VALUE)),
                IndexQueryOperator.LT,
                true,
                1
        )));
        long nextVersion = lastVersion != null ? lastVersion.getVersion() + 1 : 1;
        var version = new Version(nextVersion,
                NncUtils.mapUnique(changedTypes, Entity::getIdRequired),
                removedTypeIds);
        if (context.getInstanceContext().getBindHook() != null)
            context.getInstanceContext().getBindHook().accept(version);
        else
            context.bind(version);
        return version;
    }

    public static long getLatestVersion(IEntityContext context) {
        var lastVersion = NncUtils.getFirst(context.query(new EntityIndexQuery<>(
                Version.IDX_VERSION,
                List.of(
                        new EntityIndexQueryItem(
                                "version", Long.MAX_VALUE
                        )
                ),
                IndexQueryOperator.LE,
                true,
                1
        )));
        return NncUtils.getOrElse(lastVersion, Version::getVersion, 0L);
    }

}
