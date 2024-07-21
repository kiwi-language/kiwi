package org.metavm.task;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ReadWriteArray;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.InstanceReference;
import org.metavm.object.type.Klass;
import org.metavm.util.NncUtils;

import java.util.List;

@EntityType
public class EagerFlagClearer extends ScanTask {
    @ChildEntity
    private final ReadWriteArray<String> valueToEntityKlassIds = addChild(new ReadWriteArray<>(String.class), "valueToEntityKlassIds");

    protected EagerFlagClearer(List<String> valueToEntityKlassIds) {
        super("EagerFlagClearer");
        this.valueToEntityKlassIds.addAll(valueToEntityKlassIds);
    }

    @Override
    protected List<InstanceReference> scan(IInstanceContext context, long cursor, long limit) {
        return context.scan(cursor, limit);
    }

    @Override
    protected void process(List<InstanceReference> batch, IEntityContext context, IEntityContext taskContext) {
        var valueToEntityKlasses = NncUtils.map(valueToEntityKlassIds, context::getKlass);
        for (InstanceReference reference : batch) {
            for (Klass klass : valueToEntityKlasses) {
                var instance = reference.resolve();
                instance.forEachReference(ref -> {
                    if (ref.isResolved()) {
                        var resolved = ref.resolve();
                        if (resolved instanceof ClassInstance clsInst) {
                            var k = clsInst.getKlass().findAncestorByTemplate(klass);
                            if(k != null)
                                ref.clearEager();
                        }
                    }
                });
            }
        }
    }

}
