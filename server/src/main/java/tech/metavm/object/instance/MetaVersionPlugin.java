package tech.metavm.object.instance;

import tech.metavm.entity.EntityChange;
import tech.metavm.entity.TypeRegistry;
import tech.metavm.flow.Function;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.type.Type;
import tech.metavm.object.version.VersionRepository;
import tech.metavm.object.version.Versions;
import tech.metavm.object.view.Mapping;

import java.util.HashSet;

public class MetaVersionPlugin implements ContextPlugin {

    private final TypeRegistry typeRegistry;
    private VersionRepository versionRepository;

    public MetaVersionPlugin(TypeRegistry typeRegistry, VersionRepository versionRepository) {
        this.typeRegistry = typeRegistry;
        this.versionRepository = versionRepository;
    }

    @Override
    public boolean beforeSaving(EntityChange<InstancePO> change, IInstanceContext context) {
//        if (!context.getEntityContext().isBindSupported() && context.getBindHook() == null)
//            return false;
//        var entityContext = context.getEntityContext();
//        var changedEntities = new ArrayList<>();
        var typeType = typeRegistry.getType(Type.class);
        var mappingType = typeRegistry.getType(Mapping.class);
        var functionType = typeRegistry.getType(Function.class);
        var changedTypeIds = new HashSet<String>();
        var changedMappingIds = new HashSet<String>();
        var changedFunctionIds = new HashSet<String>();
        change.forEachInsertOrUpdate(i -> {
            var id = i.getInstanceId().toString();
            var instance = context.get(i.getInstanceId());
            if (typeType.isInstance(instance))
                changedTypeIds.add(id);
            else if (mappingType.isInstance(instance))
                changedMappingIds.add(id);
            else if (functionType.isInstance(instance))
                changedFunctionIds.add(id);
        }, true);
        var removedTypeIds = new HashSet<String>();
        var removedMappingIds = new HashSet<String>();
        var removedFunctionIds = new HashSet<String>();
        change.deletes().forEach(i -> {
            var id = i.getInstanceId().toString();
            var instance = context.getRemoved(i.getInstanceId());
            if(typeType.isInstance(instance))
                removedTypeIds.add(id);
            else if(mappingType.isInstance(instance))
                removedMappingIds.add(id);
            else if(functionType.isInstance(instance))
                removedFunctionIds.add(id);
        });
        if (!changedTypeIds.isEmpty() || !removedTypeIds.isEmpty()) {
            Versions.create(
                    changedTypeIds,
                    removedTypeIds,
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
    public void afterSaving(EntityChange<InstancePO> change, IInstanceContext context) {
    }

    public void setVersionRepository(VersionRepository versionRepository) {
        this.versionRepository = versionRepository;
    }
}
