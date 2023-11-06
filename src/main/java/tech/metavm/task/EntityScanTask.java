package tech.metavm.task;

import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.util.NncUtils;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

@EntityType("实体扫描任务")
public abstract class EntityScanTask<T> extends ScanTask {

    private final Class<T> entityType;

    protected EntityScanTask(String title, Class<T> entityType) {
        super(title);
        this.entityType = entityType;
    }

    @Override
    protected List<Instance> scan(IInstanceContext context, Instance cursor, long limit) {
        tech.metavm.object.meta.Type metaType = ModelDefRegistry.getType(entityType);
        return context.getByType(metaType, cursor, limit);
    }

    @Override
    protected void process(List<Instance> batch, IInstanceContext context) {
        List<T> models = NncUtils.map(
                batch, instance -> context.getEntityContext().getModel(entityType, instance)
        );
        processModels(context, models);
    }
    protected abstract void processModels(IInstanceContext context, List<T> models);

    public Map<TypeVariable<?>, Type> getTypeVariableMap() {
        return Map.of(
                EntityScanTask.class.getTypeParameters()[0],
                entityType
        );
    }

}
