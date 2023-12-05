package tech.metavm.object.version;

import org.springframework.stereotype.Component;
import tech.metavm.common.MetaPatch;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.SerializeContext;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class VersionManager {

    private final InstanceContextFactory instanceContextFactory;

    public VersionManager(InstanceContextFactory instanceContextFactory) {
        this.instanceContextFactory = instanceContextFactory;
    }

    public InternalMetaPatch pullInternal(long baseVersion, IEntityContext context) {
        List<Version> versions = context.query(Version.IDX_VERSION.newQueryBuilder()
                .addGtItem("version", baseVersion)
                .limit(100)
                .build()
        );
        if (versions.isEmpty()) {
            return new InternalMetaPatch(baseVersion, baseVersion, List.of(), List.of());
        }
        Set<Long> typeIds = NncUtils.flatMapUnique(versions, Version::getChangeTypeIds);
        Set<Long> removedTypeIds = NncUtils.flatMapUnique(versions, Version::getRemovedTypeIds);
        typeIds = NncUtils.diffSet(typeIds, removedTypeIds);
        return new InternalMetaPatch(baseVersion, versions.get(versions.size() - 1).getVersion(),
                new ArrayList<>(typeIds), new ArrayList<>(removedTypeIds)
        );
    }

    public MetaPatch pull(long baseVersion) {
        try (var context = newContext()) {
            var internalPatch = pullInternal(baseVersion, context);
            var types = NncUtils.map(internalPatch.changedTypeIds(), context::getType);
            try (var serContext = SerializeContext.enter()) {
                for (Type type : types) {
                    serContext.writeType(type);
                }
                var typeDTOs = serContext.getTypes();
                return new MetaPatch(
                        baseVersion, internalPatch.version(), typeDTOs, internalPatch.removedTypeIds()
                );
            }
        }
    }

    private IEntityContext newContext() {
        return instanceContextFactory.newEntityContext(true);
    }

}
