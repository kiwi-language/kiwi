package tech.metavm.spoon;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.instance.persistence.InstancePO;

import java.util.Map;

public class MockInstancePO extends InstancePO {

    public MockInstancePO(Long tenantId, Long id, Long typeId, String title, Map<String, @Nullable Object> data, Long version, Long syncVersion) {
        super(tenantId, id, typeId, null, Map.of(), version, syncVersion);
    }
}
