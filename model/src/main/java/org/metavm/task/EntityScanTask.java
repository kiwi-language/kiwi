package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.InstanceReference;
import org.metavm.util.NncUtils;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EntityType
public abstract class EntityScanTask<T> extends ScanTask {

    private final Class<T> entityType;

    protected EntityScanTask(String title, Class<T> entityType) {
        super(title);
        this.entityType = entityType;
    }

    @Override
    protected List<InstanceReference> scan(IInstanceContext context, long cursor, long limit) {
        org.metavm.object.type.Type metaType = ModelDefRegistry.getType(entityType);
        return context.scan(cursor, limit).stream().filter(metaType::isInstance).collect(Collectors.toList());
    }

    @Override
    protected void process(List<InstanceReference> batch, IEntityContext context, IEntityContext taskContext) {
        List<T> models = NncUtils.map(
                batch, instance -> context.getEntity(entityType, instance.getId())
        );
        processModels(context, models);
    }
    protected abstract void processModels(IEntityContext context, List<T> models);

    public Map<TypeVariable<?>, Type> getTypeVariableMap() {
        return Map.of(
                EntityScanTask.class.getTypeParameters()[0],
                entityType
        );
    }

}
