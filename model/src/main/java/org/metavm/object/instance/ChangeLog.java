package org.metavm.object.instance;

import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.entity.IndexDef;
import org.metavm.entity.ReadWriteArray;
import org.metavm.object.instance.log.InstanceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiConsumer;

@Entity
public class ChangeLog extends org.metavm.entity.Entity {

    public static final Logger logger = LoggerFactory.getLogger(ChangeLog.class);

    public static final IndexDef<ChangeLog> IDX_STATUS = IndexDef.create(ChangeLog.class, "status");

    public static volatile BiConsumer<Long, ChangeLog> saveHook;
    @ChildEntity
    private final ReadWriteArray<InstanceLog> items = addChild(new ReadWriteArray<>(InstanceLog.class), "items");
    private ChangeLogStatus status;

    public ChangeLog(List<InstanceLog> items) {
        this.items.addAll(items);
        this.status = ChangeLogStatus.PENDING;
    }

    public List<InstanceLog> getItems() {
        return items.toList();
    }

    public ChangeLogStatus getStatus() {
        return status;
    }

    public void setStatus(ChangeLogStatus status) {
        this.status = status;
    }

    public void save(long appId) {
        if(saveHook == null)
            throw new NullPointerException("Save hook is missing");
        saveHook.accept(appId, this);
    }

}
