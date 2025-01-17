package org.metavm.object.instance;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.NativeApi;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NativeEntity(27)
@Entity
public class ChangeLog extends org.metavm.entity.Entity implements Message {

    public static final Logger logger = LoggerFactory.getLogger(ChangeLog.class);

    public static final IndexDef<ChangeLog> IDX_STATUS = IndexDef.create(ChangeLog.class,
            1, changeLog -> List.of(Instances.intInstance(changeLog.status.code())));

    public static volatile BiConsumer<Long, ChangeLog> saveHook;
    @SuppressWarnings("unused")
    private static Klass __klass__;
    private List<InstanceLog> items = new ArrayList<>();
    private ChangeLogStatus status;

    public ChangeLog(List<InstanceLog> items) {
        this.items.addAll(items);
        this.status = ChangeLogStatus.PENDING;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitList(() -> InstanceLog.visit(visitor));
        visitor.visitByte();
    }

    public List<InstanceLog> getItems() {
        return Collections.unmodifiableList(items);
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

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        items.forEach(arg -> arg.forEachReference(action));
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("items", this.getItems().stream().map(InstanceLog::toJson).toList());
        map.put("status", this.getStatus().name());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_ChangeLog;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.items = input.readList(() -> InstanceLog.read(input));
        this.status = ChangeLogStatus.fromCode(input.read());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeList(items, arg0 -> arg0.write(output));
        output.write(status.code());
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
    }
}
