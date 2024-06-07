package tech.metavm.task;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.util.NncUtils;

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
    protected List<DurableInstance> scan(IInstanceContext context, long cursor, long limit) {
        tech.metavm.object.type.Type metaType = ModelDefRegistry.getType(entityType);
        return context.scan(cursor, limit).stream().filter(metaType::isInstance).collect(Collectors.toList());
    }

    @Override
    protected void process(List<DurableInstance> batch, IEntityContext context) {
        List<T> models = NncUtils.map(
                batch, instance -> context.getEntity(entityType, instance)
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
