package tech.metavm.util;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityUtils;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.IdConstants;

import java.util.List;

public class TestUtils {

    public static void initEntityIds(Object root) {
        var ref = new Object() {
            long nextId = 10000L;
        };

        EntityUtils.visitGraph(List.of(root), o -> {
            if (o instanceof Entity entity && entity.isIdNull()) {
                entity.initId(ref.nextId++);
            }
        });
    }

    public static void initInstanceIds(DurableInstance instance) {
        initInstanceIds(List.of(instance));
    }

    public static void initInstanceIds(List<DurableInstance> instances) {
        long offset = 1000000L;
        var ref = new Object() {
            long nextObjectId = IdConstants.CLASS_REGION_BASE + offset;
            long nextEnumId = IdConstants.ENUM_REGION_BASE + offset;
            long nextReadWriteArrayId = IdConstants.READ_WRITE_ARRAY_REGION_BASE + offset;
            long nextChildArrayId = IdConstants.CHILD_ARRAY_REGION_BASE + offset;
            long nextReadonlyArrayId = IdConstants.READ_ONLY_ARRAY_REGION_BASE + offset;
        };
        var visitor = new GraphVisitor() {
            @Override
            public Void visitDurableInstance(DurableInstance instance) {
                if (!instance.isIdInitialized()) {
                    long id;
                    if (instance instanceof ArrayInstance arrayInstance) {
                        id = switch (arrayInstance.getType().getKind()) {
                            case READ_WRITE -> ref.nextReadWriteArrayId++;
                            case CHILD -> ref.nextChildArrayId++;
                            case READ_ONLY -> ref.nextReadonlyArrayId++;
                        };
                    } else if (instance instanceof ClassInstance classInstance) {
                        var type = classInstance.getType();
                        if (type.isEnum())
                            id = ref.nextEnumId++;
                        else
                            id = ref.nextObjectId++;
                    } else
                        throw new InternalException("Invalid instance: " + instance);
                    instance.initId(PhysicalId.of(id));
                }
                return super.visitDurableInstance(instance);
            }
        };
        instances.forEach(visitor::visit);
    }

}
