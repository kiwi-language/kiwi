package tech.metavm.object.version;

import org.springframework.stereotype.Component;
import tech.metavm.common.MetaPatch;
import tech.metavm.entity.*;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class VersionManager extends EntityContextFactoryBean {

    public VersionManager(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
    }

    public InternalMetaPatch pullInternal(long baseVersion, IEntityContext context) {
        List<Version> versions = context.query(Version.IDX_VERSION.newQueryBuilder()
                .addGtItem("version", baseVersion)
                .limit(100)
                .build()
        );
        if (versions.isEmpty()) {
            return new InternalMetaPatch(baseVersion, baseVersion,
                    List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        }
        var typeIds = NncUtils.flatMapUnique(versions, Version::getChangedTypeIds);
        var removedTypeIds = NncUtils.flatMapUnique(versions, Version::getRemovedTypeIds);
        var mappingIds = NncUtils.flatMapUnique(versions, Version::getChangedMappingIds);
        var removedMappingIds = NncUtils.flatMapUnique(versions, Version::getRemovedMappingIds);
        var functionIds = NncUtils.flatMapUnique(versions, Version::getChangedFunctionIds);
        var removedFunctionIds = NncUtils.flatMapUnique(versions, Version::getRemovedFunctionIds);

        typeIds = NncUtils.diffSet(typeIds, removedTypeIds);
        mappingIds = NncUtils.diffSet(mappingIds, removedMappingIds);
        functionIds = NncUtils.diffSet(functionIds, removedFunctionIds);

        return new InternalMetaPatch(baseVersion, versions.get(versions.size() - 1).getVersion(),
                new ArrayList<>(typeIds),
                new ArrayList<>(removedTypeIds),
                new ArrayList<>(mappingIds),
                new ArrayList<>(removedMappingIds),
                new ArrayList<>(functionIds),
                new ArrayList<>(removedFunctionIds)
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
                var mappingDTOs = NncUtils.map(
                        internalPatch.changedMappingIds(),
                        id -> context.getMapping(id).toDTO(serContext)
                );
                var functionDTOs = NncUtils.map(
                        internalPatch.changedFunctionIds(),
                        id -> context.getFunction(id).toDTO(false, serContext)
                );
                return new MetaPatch(
                        baseVersion,
                        internalPatch.version(),
                        typeDTOs,
                        internalPatch.removedTypeIds(),
                        mappingDTOs,
                        internalPatch.removedMappingIds(),
                        functionDTOs,
                        internalPatch.removedFunctionIds()
                );
            }
        }
    }

}
