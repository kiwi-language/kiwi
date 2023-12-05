package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.EntityChange;
import tech.metavm.entity.MemInstanceContext;
import tech.metavm.object.instance.log.InstanceLog;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.util.Constants;

import java.util.List;

public class ChangeLogPluginTest extends TestCase {

    public void test() {
        MockInstanceLogService instanceLogService = new MockInstanceLogService();
        ChangeLogPlugin changeLogPlugin = new ChangeLogPlugin(instanceLogService);
        MemInstanceContext context = new MemInstanceContext();

        EntityChange<InstancePO> change = new EntityChange<>(InstancePO.class);

        InstancePO instancePO = new InstancePO(
                Constants.ROOT_APP_ID, 1000L, "", 100L,
                new byte[0],
                -1L, -1L,
                10000L,
                1L, 1L
        );

        change.addToInsert(instancePO);

        changeLogPlugin.beforeSaving(change, context);
        changeLogPlugin.afterSaving(change, context);
        changeLogPlugin.postProcess(context);

        List<InstanceLog> logs = instanceLogService.getLogs();
        Assert.assertEquals(1, logs.size());

        InstanceLog log = logs.get(0);
        Assert.assertEquals(ChangeType.INSERT, log.getChangeType());
        Assert.assertEquals((long) instancePO.getId(), log.getId());
    }

}