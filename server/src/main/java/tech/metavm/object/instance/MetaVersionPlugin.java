package tech.metavm.object.instance;

import tech.metavm.entity.EntityChange;
import tech.metavm.entity.TypeRegistry;
import tech.metavm.flow.Function;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.version.VersionRepository;
import tech.metavm.object.version.Versions;
import tech.metavm.object.view.Mapping;

import java.util.HashSet;

public class MetaVersionPlugin implements ContextPlugin {

    private final TypeRegistry typeRegistry;
    private final VersionRepository versionRepository;

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
        var typeType = typeRegistry.getType(java.lang.reflect.Type.class);
        var mappingType = typeRegistry.getType(Mapping.class);
        var functionType = typeRegistry.getType(Function.class);
        var changedTypeIds = new HashSet<Long>();
        var changedMappingIds = new HashSet<Long>();
        var changedFunctionIds = new HashSet<Long>();
        change.forEachInsertOrUpdate(i -> {
            var id = i.getId();
            var instance = context.get(id);
            if (typeType.isInstance(instance))
                changedTypeIds.add(id);
            else if (mappingType.isInstance(instance))
                changedMappingIds.add(id);
            else if (functionType.isInstance(instance))
                changedMappingIds.add(id);
        });
        var removedTypeIds = new HashSet<Long>();
        var removedMappingIds = new HashSet<Long>();
        var removedFunctionIds = new HashSet<Long>();
        change.deletes().forEach(i -> {
            var id = i.getId();
            var instance = context.getRemoved(id);
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
}
