package org.metavm.object.instance;

import org.metavm.object.version.Versions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.entity.EntityChange;
import org.metavm.entity.TypeRegistry;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.persistence.VersionRT;
import org.metavm.object.version.VersionRepository;

import java.util.HashSet;

public class MetaVersionPlugin implements ContextPlugin {

    public static final Logger logger = LoggerFactory.getLogger(MetaVersionPlugin.class);

    private final TypeRegistry typeRegistry;
    private VersionRepository versionRepository;

    public MetaVersionPlugin(TypeRegistry typeRegistry, VersionRepository versionRepository) {
        this.typeRegistry = typeRegistry;
        this.versionRepository = versionRepository;
    }

    @Override
    public boolean beforeSaving(EntityChange<VersionRT> change, IInstanceContext context) {
//        if (!context.getEntityContext().isBindSupported() && context.getBindHook() == null)
//            return false;
//        var entityContext = context.getEntityContext();
//        var changedEntities = new ArrayList<>();
        var changedTypeIds = new HashSet<String>();
        var changedMappingIds = new HashSet<String>();
        var changedFunctionIds = new HashSet<String>();
        change.forEachInsertOrUpdate(v -> {
            var instance = context.get(v.id());
            //noinspection DuplicatedCode
            if(instance instanceof ClassInstance clsInst) {
                var type = clsInst.getType();
                if (typeRegistry.isTypeDefType(type))
                    changedTypeIds.add(v.id().toString());
                else if (typeRegistry.isMappingType(type))
                    changedMappingIds.add(v.id().toString());
                else if (typeRegistry.isFunctionType(type))
                    changedFunctionIds.add(v.id().toString());
            }
        });
        var removedTypeDefIds = new HashSet<String>();
        var removedMappingIds = new HashSet<String>();
        var removedFunctionIds = new HashSet<String>();
        change.deletes().forEach(v -> {
            var instance = context.getRemoved(v.id());
            //noinspection DuplicatedCode
            if(instance instanceof ClassInstance clsInst) {
                var type = clsInst.getType();
                if (typeRegistry.isTypeDefType(type))
                    removedTypeDefIds.add(v.id().toString());
                else if (typeRegistry.isMappingType(type))
                    removedMappingIds.add(v.id().toString());
                else if (typeRegistry.isFunctionType(type))
                    removedFunctionIds.add(v.id().toString());
            }
        });
        if (!changedTypeIds.isEmpty() || !removedTypeDefIds.isEmpty()) {
            Versions.create(
                    changedTypeIds,
                    removedTypeDefIds,
                    changedMappingIds,
                    removedMappingIds,
                    changedFunctionIds,
                    removedFunctionIds,
                    versionRepository);
            return true;
        } else
            return false;
    }

    @Override
    public void afterSaving(EntityChange<VersionRT> change, IInstanceContext context) {
    }

    public void setVersionRepository(VersionRepository versionRepository) {
        this.versionRepository = versionRepository;
    }
}
