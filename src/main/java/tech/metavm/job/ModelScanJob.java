package tech.metavm.job;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ParameterizedTypeImpl;
import tech.metavm.util.RuntimeGeneric;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

@EntityType("模型扫描任务")
public abstract class ModelScanJob<T> extends InstanceScanJob implements RuntimeGeneric {

    private final Class<T> entityType;

    protected ModelScanJob(String title, Class<T> entityType) {
        super(title);
        this.entityType = entityType;
    }

    @Override
    protected List<Instance> scan(IInstanceContext context, Instance cursor, long limit) {
        tech.metavm.object.meta.Type metaType = ModelDefRegistry.getType(entityType);
        return context.getByType(metaType, cursor, limit);
    }

    @Override
    protected void process(IInstanceContext context, List<Instance> batch) {
        List<T> models = NncUtils.map(
                batch, instance -> context.getEntityContext().getModel(entityType, instance)
        );
        processModels(context, models);
    }
    protected abstract void processModels(IInstanceContext context, List<T> models);

    public Map<TypeVariable<?>, Type> getTypeVariableMap() {
        return Map.of(
                ModelScanJob.class.getTypeParameters()[0],
                entityType
        );
    }

}
