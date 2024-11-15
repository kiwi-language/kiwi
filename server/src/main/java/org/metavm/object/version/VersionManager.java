package org.metavm.object.version;

import org.metavm.common.MetaPatch;
import org.metavm.entity.*;
import org.metavm.flow.Function;
import org.metavm.object.type.TypeDef;
import org.metavm.object.type.rest.dto.LoadAllMetadataResponse;
import org.metavm.util.NncUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VersionManager extends EntityContextFactoryAware {

    public VersionManager(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
    }

    public InternalMetaPatch pullInternal(long baseVersion, IEntityContext context) {
        List<Version> versions = context.query(Version.IDX_VERSION.newQueryBuilder()
                .from(new EntityIndexKey(List.of(baseVersion + 1)))
                .limit(100)
                .build()
        );
        if (versions.isEmpty()) {
            return new InternalMetaPatch(baseVersion, baseVersion,
                    List.of(), List.of(), List.of(), List.of());
        }
        var typeIds = NncUtils.flatMapUnique(versions, Version::getChangedTypeIds);
        var removedTypeIds = NncUtils.flatMapUnique(versions, Version::getRemovedTypeIds);
        var functionIds = NncUtils.flatMapUnique(versions, Version::getChangedFunctionIds);
        var removedFunctionIds = NncUtils.flatMapUnique(versions, Version::getRemovedFunctionIds);

        typeIds = NncUtils.diffSet(typeIds, removedTypeIds);
        functionIds = NncUtils.diffSet(functionIds, removedFunctionIds);

        return new InternalMetaPatch(baseVersion, versions.get(versions.size() - 1).getVersion(),
                new ArrayList<>(typeIds),
                new ArrayList<>(removedTypeIds),
                new ArrayList<>(functionIds),
                new ArrayList<>(removedFunctionIds)
        );
    }

    public MetaPatch pull(long baseVersion) {
        try (var context = newContext()) {
            var latestVersion = Versions.getLatestVersion(context);
            // if the base version lags too far behind, a reset is performed
            if(latestVersion - baseVersion > 100) {
                var allMetadata = loadAllMetadata();
                return new MetaPatch(
                      0L,
                      latestVersion,
                      true,
                      allMetadata.typeDefs(),
                      List.of(),
                      allMetadata.functions(),
                      List.of()
                );
            }
            var internalPatch = pullInternal(baseVersion, context);
            var types = NncUtils.map(internalPatch.changedTypeDefIds(), context::getTypeDef);
            try (var serContext = SerializeContext.enter()) {
                for (var type : types) {
                    serContext.writeTypeDef(type);
                }
                var typeDefDTOs = serContext.getTypeDefs();
                var functionDTOs = NncUtils.map(
                        internalPatch.changedFunctionIds(),
                        id -> context.getFunction(id).toDTO(false, serContext)
                );
                return new MetaPatch(
                        baseVersion,
                        internalPatch.version(),
                        false,
                        typeDefDTOs,
                        internalPatch.removedTypeDefIds(),
                        functionDTOs,
                        internalPatch.removedFunctionIds()
                );
            }
        }
    }

    public List<TypeDef> getAllTypes(IEntityContext context) {
        var defContext = context.getDefContext();
        var typeDefs = new ArrayList<>(
                NncUtils.exclude(defContext.getAllBufferedEntities(TypeDef.class), Entity::isEphemeralEntity)
        );
        typeDefs.addAll(context.selectByKey(TypeDef.IDX_ALL_FLAG, true));
        return typeDefs;
    }

    private List<Function> getAllFunctions(IEntityContext context) {
        var defContext = context.getDefContext();
        var functions = new ArrayList<>(defContext.getAllBufferedEntities(Function.class));
        functions.addAll(context.selectByKey(Function.IDX_ALL_FLAG, true));
        return functions;
    }

    public LoadAllMetadataResponse loadAllMetadata() {
        try (var context = newContext();
             var serContext = SerializeContext.enter()) {
            var types = getAllTypes(context);
            var functions = getAllFunctions(context);
            return new LoadAllMetadataResponse(
                    Versions.getLatestVersion(context),
                    SerializeContext.forceWriteTypeDefs(types),
                    NncUtils.map(functions, f -> f.toDTO(false, serContext))
            );
        }
    }


}
