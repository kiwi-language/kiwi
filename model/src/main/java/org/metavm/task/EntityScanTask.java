package org.metavm.task;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.ScanResult;
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
    protected ScanResult scan(IInstanceContext context, long cursor, long limit) {
        org.metavm.object.type.Type metaType = ModelDefRegistry.getType(entityType);
        var r = context.scan(cursor, limit);
        return new ScanResult(
                r.instances().stream().filter(i -> metaType.isInstance(i.getReference())).collect(Collectors.toList()),
                r.completed(),
                r.cursor()
        );
    }

    @Override
    protected void process(List<Instance> batch, IEntityContext context, IEntityContext taskContext) {
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
