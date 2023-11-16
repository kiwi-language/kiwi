package tech.metavm.object.version;

import org.springframework.stereotype.Component;
import tech.metavm.common.MetaPatch;
import tech.metavm.entity.*;
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

    public MetaPatch pull(long baseVersion) {
        try (var context = newContext()) {
            List<Version> versions = context.query(new EntityIndexQuery<>(
                    Version.IDX_VERSION,
                    List.of(new EntityIndexQueryItem("version", baseVersion)),
                    IndexQueryOperator.GT,
                    false,
                    100
            ));
            if (versions.isEmpty()) {
                return new MetaPatch(baseVersion, baseVersion, List.of(), List.of());
            }
            Set<Long> typeIds = NncUtils.flatMapUnique(versions, Version::getChangeTypeIds);
            Set<Long> removedTypeIds = NncUtils.flatMapUnique(versions, Version::getRemovedTypeIds);
            typeIds = NncUtils.diffSet(typeIds, removedTypeIds);
            var types = NncUtils.map(typeIds, context::getType);
            try (var serContext = SerializeContext.enter()) {
                for (Type type : types) {
                    serContext.writeType(type);
                }
                return new MetaPatch(baseVersion, versions.get(versions.size() - 1).getVersion(),
                        serContext.getTypes(), new ArrayList<>(removedTypeIds)
                );
            }
        }
    }

    private IEntityContext newContext() {
        return instanceContextFactory.newEntityContext(true);
    }

}
