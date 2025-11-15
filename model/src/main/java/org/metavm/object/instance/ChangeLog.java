package org.metavm.object.instance;

import lombok.Getter;
import lombok.Setter;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Message;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.util.Instances;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Wire(27)
@Entity
public class ChangeLog extends org.metavm.entity.Entity implements Message {

    public static final Logger logger = LoggerFactory.getLogger(ChangeLog.class);

    public static final IndexDef<ChangeLog> IDX_STATUS = IndexDef.create(ChangeLog.class,
            1, changeLog -> List.of(Instances.intInstance(changeLog.status.code())));

    public static volatile BiConsumer<Long, ChangeLog> saveHook;
    private final List<InstanceLog> items = new ArrayList<>();
    @Setter
    @Getter
    private ChangeLogStatus status;

    public ChangeLog(Id id, List<InstanceLog> items) {
        super(id);
        this.items.addAll(items);
        this.status = ChangeLogStatus.PENDING;
    }

    public List<InstanceLog> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void save(long appId) {
        if(saveHook == null)
            throw new NullPointerException("Save hook is missing");
        saveHook.accept(appId, this);
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        for (var items_ : items) items_.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
