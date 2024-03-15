package tech.metavm.object.version;

import tech.metavm.entity.*;
import tech.metavm.util.NncUtils;

import java.util.Set;

public class Versions {

    public static Version create(Set<String> changedTypeIds,
                                 Set<String> removedTypeIds,
                                 Set<String> changedMappingIds,
                                 Set<String> removedMappingIds,
                                 Set<String> changedFunctionIds,
                                 Set<String> removedFunctionIds,
                                 VersionRepository versionRepository) {
        NncUtils.requireTrue(!changedTypeIds.isEmpty() || !removedTypeIds.isEmpty(),
                "Change set is empty");
        Version lastVersion = versionRepository.getLastVersion();
        long nextVersion = lastVersion != null ? lastVersion.getVersion() + 1 : 1;
        var version = new Version(nextVersion,
                changedTypeIds,
                removedTypeIds,
                changedMappingIds,
                removedMappingIds,
                changedFunctionIds,
                removedFunctionIds
        );
//        if (versionRepository.getInstanceContext().getBindHook() != null)
//            versionRepository.getInstanceContext().getBindHook().accept(version);
//        else
//            versionRepository.bind(version);
        versionRepository.save(version);
        return version;
    }

    public static long getLatestVersion(IEntityContext context) {
        var lastVersion = NncUtils.first(
                context.query(Version.IDX_VERSION.newQueryBuilder().limit(1).desc(true).build())
        );
        return NncUtils.getOrElse(lastVersion, Version::getVersion, 0L);
    }

}
