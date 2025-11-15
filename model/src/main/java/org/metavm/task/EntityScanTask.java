package org.metavm.task;

import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.entity.StdKlassRegistry;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;
import org.metavm.util.Utils;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Wire(75)
@Entity
public abstract class EntityScanTask extends ScanTask {

    private final ClassType type;

    protected EntityScanTask(Id id, String title, Class<?> entityType) {
        super(id, title);
       this.type = (ClassType) StdKlassRegistry.instance.getType(entityType);
    }

    @Override
    protected ScanResult scan(IInstanceContext context, long cursor, long limit) {
        var r = context.scan(cursor, limit);
        return new ScanResult(
                r.instances().stream().filter(i -> type.isInstance(i.getReference())).collect(Collectors.toList()),
                r.completed(),
                r.cursor()
        );
    }

    @Override
    protected void process(List<Instance> batch, IInstanceContext context, IInstanceContext taskContext) {
        List<Object> models = Utils.map(
                batch, instance -> context.getEntity(Object.class, instance.getId())
        );
        processModels(context, models);
    }

    protected abstract void processModels(IInstanceContext context, List<Object> models);

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        type.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
