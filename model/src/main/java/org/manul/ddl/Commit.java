package org.manul.ddl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;
import org.manul.api.Entity;
import org.manul.entity.IndexDef;
import org.manul.entity.StdKlassRegistry;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Message;
import org.manul.object.instance.core.Reference;
import org.manul.object.type.ClassType;
import org.manul.object.type.Klass;
import org.manul.object.type.RedirectStatus;
import org.manul.util.Instances;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;
import org.manul.wire.AdapterRegistry;
import org.manul.wire.Wire;
import org.manul.wire.WireAdapter;
import org.manul.wire.WireInput;
import org.manul.wire.WireOutput;
import org.manul.wire.WireVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Wire(value = 13, adapter = Commit.__WireAdapter__.class)
public class Commit extends org.manul.entity.Entity implements RedirectStatus, Message {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(Commit.class);
    public static final IndexDef<Commit> IDX_RUNNING = IndexDef.create(Commit.class, 1, (commit) -> {
        return List.of(Instances.booleanInstance(commit.running));
    });
    public static BiConsumer<Long, Boolean> META_CONTEXT_INVALIDATE_HOOK;
    public static BiConsumer<Long, Id> tableSwitchHook;
    public static BiConsumer<Long, Id> dropTmpTableHook;
    public static Consumer<Long> cleanupRemovingClassesHook;
    private final Date time;
    private final long appId;
    private final List<String> newFieldIds;
    private final List<String> convertingFieldIds;
    private final List<String> toChildFieldIds;
    private final List<String> toNonChildFieldIds;
    private final List<String> removedChildFieldIds;
    private final List<String> changingSuperKlassIds;
    private final List<String> entityToValueKlassIds;
    private final List<String> valueToEntityKlassIds;
    private final List<FieldChange> fieldChanges;
    private final List<String> newEnumConstantIds;
    private final List<String> changedEnumConstantIds;
    private final List<String> toEnumKlassIds;
    private final List<String> fromEnumKlassIds;
    private final List<String> runMethodIds;
    private final List<String> newIndexIds;
    private final List<String> searchEnabledKlassIds;
    private CommitState state;
    private boolean running;
    private boolean cancelled;
    private final boolean noBackup;
    private boolean submitted;
    public static final Klass __klass__;

    public Commit(@NotNull Id id, long appId, boolean noBackup, List<String> newFieldIds, List<String> convertingFieldIds, List<String> toChildFieldIds, List<String> toNonChildFieldIds, List<String> removedChildFieldIds, List<String> changingSuperKlassIds, List<String> entityToValueKlassIds, List<String> valueToEntityKlassIds, List<String> toEnumKlassIds, List<String> fromEnumKlassIds, List<String> runMethodIds, List<String> newIndexIds, List<String> searchEnabledKlassIds, List<String> changedEnumConstantIds, List<FieldChange> fieldChanges) {
        super(id);
        this.time = new Date();
        this.newFieldIds = new ArrayList();
        this.convertingFieldIds = new ArrayList();
        this.toChildFieldIds = new ArrayList();
        this.toNonChildFieldIds = new ArrayList();
        this.removedChildFieldIds = new ArrayList();
        this.changingSuperKlassIds = new ArrayList();
        this.entityToValueKlassIds = new ArrayList();
        this.valueToEntityKlassIds = new ArrayList();
        this.fieldChanges = new ArrayList();
        this.newEnumConstantIds = new ArrayList();
        this.changedEnumConstantIds = new ArrayList();
        this.toEnumKlassIds = new ArrayList();
        this.fromEnumKlassIds = new ArrayList();
        this.runMethodIds = new ArrayList();
        this.newIndexIds = new ArrayList();
        this.searchEnabledKlassIds = new ArrayList();
        this.state = CommitState.MIGRATING;
        this.running = true;
        this.cancelled = false;
        this.appId = appId;
        this.noBackup = noBackup;
        this.newFieldIds.addAll(newFieldIds);
        this.convertingFieldIds.addAll(convertingFieldIds);
        this.toChildFieldIds.addAll(toChildFieldIds);
        this.toNonChildFieldIds.addAll(toNonChildFieldIds);
        this.removedChildFieldIds.addAll(removedChildFieldIds);
        this.changingSuperKlassIds.addAll(changingSuperKlassIds);
        this.entityToValueKlassIds.addAll(entityToValueKlassIds);
        this.valueToEntityKlassIds.addAll(valueToEntityKlassIds);
        this.toEnumKlassIds.addAll(toEnumKlassIds);
        this.fromEnumKlassIds.addAll(fromEnumKlassIds);
        this.runMethodIds.addAll(runMethodIds);
        this.newIndexIds.addAll(newIndexIds);
        this.searchEnabledKlassIds.addAll(searchEnabledKlassIds);
        this.changedEnumConstantIds.addAll(changedEnumConstantIds);
        this.fieldChanges.addAll(fieldChanges);
    }

    public void submit() {
        if (this.submitted) {
            throw new IllegalStateException("Commit is already submitted");
        } else {
            this.submitted = true;
            tableSwitchHook.accept(this.appId, this.getId());
        }
    }

    public boolean hasCleanUpWorks() {
        return !this.valueToEntityKlassIds.isEmpty();
    }

    public void finish() {
        if (this.state == CommitState.COMPLETED) {
            throw new IllegalStateException("Commit is already finished");
        } else {
            this.state = CommitState.COMPLETED;
        }
    }

    public void setState(CommitState state) {
        if (state.ordinal() <= this.state.ordinal()) {
            String var10002 = String.valueOf(this.state);
            throw new IllegalStateException("Invalid state transition from " + var10002 + " to " + String.valueOf(state) + ". Commit ID: " + String.valueOf(this.getId()));
        } else {
            this.state = state;
        }
    }

    public void terminate() {
        this.running = false;
        if (META_CONTEXT_INVALIDATE_HOOK != null) {
            META_CONTEXT_INVALIDATE_HOOK.accept(this.appId, false);
            META_CONTEXT_INVALIDATE_HOOK.accept(this.appId, true);
        }

    }

    public Date getTime() {
        return this.time;
    }

    public CommitState getState() {
        return this.state;
    }

    public List<String> getNewFieldIds() {
        return Collections.unmodifiableList(this.newFieldIds);
    }

    public List<String> getConvertingFieldIds() {
        return Collections.unmodifiableList(this.convertingFieldIds);
    }

    public List<String> getToNonChildFieldIds() {
        return Collections.unmodifiableList(this.toNonChildFieldIds);
    }

    public List<String> getRemovedChildFieldIds() {
        return Collections.unmodifiableList(this.removedChildFieldIds);
    }

    public List<String> getToChildFieldIds() {
        return Collections.unmodifiableList(this.toChildFieldIds);
    }

    public List<String> getChangingSuperKlassIds() {
        return Collections.unmodifiableList(this.changingSuperKlassIds);
    }

    public List<String> getEntityToValueKlassIds() {
        return Collections.unmodifiableList(this.entityToValueKlassIds);
    }

    public List<String> getValueToEntityKlassIds() {
        return Collections.unmodifiableList(this.valueToEntityKlassIds);
    }

    public List<FieldChange> getFieldChanges() {
        return Collections.unmodifiableList(this.fieldChanges);
    }

    public List<String> getNewEnumConstantIds() {
        return Collections.unmodifiableList(this.newEnumConstantIds);
    }

    public List<String> getChangedEnumConstantIds() {
        return Collections.unmodifiableList(this.changedEnumConstantIds);
    }

    public List<String> getToEnumKlassIds() {
        return Collections.unmodifiableList(this.toEnumKlassIds);
    }

    public List<String> getFromEnumKlassIds() {
        return Collections.unmodifiableList(this.fromEnumKlassIds);
    }

    public List<String> getRunMethodIds() {
        return Collections.unmodifiableList(this.runMethodIds);
    }

    public List<String> getNewIndexIds() {
        return Collections.unmodifiableList(this.newIndexIds);
    }

    public List<String> getSearchEnabledKlassIds() {
        return Collections.unmodifiableList(this.searchEnabledKlassIds);
    }

    public void cancel() {
        if (this.cancelled) {
            throw new IllegalStateException("The commit has already been cancelled");
        } else if (!this.state.isPreparing()) {
            throw new IllegalStateException("Cannot cancel a prepared commit");
        } else {
            this.cancelled = true;
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    public boolean isSubmitted() {
        return this.submitted;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public boolean isNoBackup() {
        return this.noBackup;
    }

    public boolean shouldRedirect() {
        return this.state != CommitState.MIGRATING && this.state != CommitState.ABORTING && this.state != CommitState.ABORTED;
    }

    @Nullable
    public org.manul.entity.Entity getParentEntity() {
        return null;
    }

    private void writeIds(MvOutput output, List<String> ids) {
        output.writeInt(ids.size());
        Objects.requireNonNull(output);
        ids.forEach(output::writeUTF);
    }

    private static List<String> readIds(MvInput input) {
        int cnt = input.readInt();
        ArrayList<String> ids = new ArrayList(cnt);

        for(int i = 0; i < cnt; ++i) {
            ids.add(input.readUTF());
        }

        return ids;
    }

    private static List<FieldChange> readFieldChanges(MvInput input) {
        int cnt = input.readInt();
        ArrayList<FieldChange> fieldChanges = new ArrayList();

        for(int i = 0; i < cnt; ++i) {
            fieldChanges.add(FieldChange.read(input));
        }

        return fieldChanges;
    }

    public void forEachReference(Consumer<Reference> action) {
        Iterator var2 = this.fieldChanges.iterator();

        while(var2.hasNext()) {
            FieldChange fieldChanges_ = (FieldChange)var2.next();
            fieldChanges_.forEachReference(action);
        }

    }

    public long getAppId() {
        return this.appId;
    }

    public void forEachChild(Consumer<? super Instance> action) {
    }

    public Klass getInstanceKlass() {
        return __klass__;
    }

    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    protected Commit(WireInput input, WireAdapter<Id> adapter0, WireAdapter<FieldChange> adapter1) {
        super(input, adapter0);
        this.time = input.readDate();
        this.appId = input.readLong();
        this.newFieldIds = input.readList(() -> {
            return input.readString();
        });
        this.convertingFieldIds = input.readList(() -> {
            return input.readString();
        });
        this.toChildFieldIds = input.readList(() -> {
            return input.readString();
        });
        this.toNonChildFieldIds = input.readList(() -> {
            return input.readString();
        });
        this.removedChildFieldIds = input.readList(() -> {
            return input.readString();
        });
        this.changingSuperKlassIds = input.readList(() -> {
            return input.readString();
        });
        this.entityToValueKlassIds = input.readList(() -> {
            return input.readString();
        });
        this.valueToEntityKlassIds = input.readList(() -> {
            return input.readString();
        });
        this.fieldChanges = input.readList(() -> {
            return (FieldChange)input.readEntity(adapter1, this);
        });
        this.newEnumConstantIds = input.readList(() -> {
            return input.readString();
        });
        this.changedEnumConstantIds = input.readList(() -> {
            return input.readString();
        });
        this.toEnumKlassIds = input.readList(() -> {
            return input.readString();
        });
        this.fromEnumKlassIds = input.readList(() -> {
            return input.readString();
        });
        this.runMethodIds = input.readList(() -> {
            return input.readString();
        });
        this.newIndexIds = input.readList(() -> {
            return input.readString();
        });
        this.searchEnabledKlassIds = input.readList(() -> {
            return input.readString();
        });
        this.state = CommitState.fromCode(Math.abs(input.readByte()));
        this.running = input.readBoolean();
        this.cancelled = input.readBoolean();
        this.noBackup = input.readBoolean();
        this.submitted = input.readBoolean();
    }

    protected void __write__(WireOutput output, WireAdapter<Id> adapter0, WireAdapter<FieldChange> adapter1) {
        super.__write__(output, adapter0);
        output.writeDate(this.time);
        output.writeLong(this.appId);
        output.writeList(this.newFieldIds, (var0) -> {
            output.writeString(var0);
        });
        output.writeList(this.convertingFieldIds, (var1) -> {
            output.writeString(var1);
        });
        output.writeList(this.toChildFieldIds, (var2) -> {
            output.writeString(var2);
        });
        output.writeList(this.toNonChildFieldIds, (var3) -> {
            output.writeString(var3);
        });
        output.writeList(this.removedChildFieldIds, (var4) -> {
            output.writeString(var4);
        });
        output.writeList(this.changingSuperKlassIds, (var5) -> {
            output.writeString(var5);
        });
        output.writeList(this.entityToValueKlassIds, (var6) -> {
            output.writeString(var6);
        });
        output.writeList(this.valueToEntityKlassIds, (var7) -> {
            output.writeString(var7);
        });
        output.writeList(this.fieldChanges, (var8) -> {
            output.writeEntity(var8, adapter1);
        });
        output.writeList(this.newEnumConstantIds, (var9) -> {
            output.writeString(var9);
        });
        output.writeList(this.changedEnumConstantIds, (var10) -> {
            output.writeString(var10);
        });
        output.writeList(this.toEnumKlassIds, (var11) -> {
            output.writeString(var11);
        });
        output.writeList(this.fromEnumKlassIds, (var12) -> {
            output.writeString(var12);
        });
        output.writeList(this.runMethodIds, (var13) -> {
            output.writeString(var13);
        });
        output.writeList(this.newIndexIds, (var14) -> {
            output.writeString(var14);
        });
        output.writeList(this.searchEnabledKlassIds, (var15) -> {
            output.writeString(var15);
        });
        output.writeByte((byte)this.state.code());
        output.writeBoolean(this.running);
        output.writeBoolean(this.cancelled);
        output.writeBoolean(this.noBackup);
        output.writeBoolean(this.submitted);
    }

    protected static void __visit__(WireVisitor visitor, WireAdapter<Id> adapter0, WireAdapter<FieldChange> adapter1) {
        org.manul.entity.Entity.__visit__(visitor, adapter0);
        visitor.visitDate();
        visitor.visitLong();
        visitor.visitList(() -> {
            visitor.visitString();
        });
        visitor.visitList(() -> {
            visitor.visitString();
        });
        visitor.visitList(() -> {
            visitor.visitString();
        });
        visitor.visitList(() -> {
            visitor.visitString();
        });
        visitor.visitList(() -> {
            visitor.visitString();
        });
        visitor.visitList(() -> {
            visitor.visitString();
        });
        visitor.visitList(() -> {
            visitor.visitString();
        });
        visitor.visitList(() -> {
            visitor.visitString();
        });
        visitor.visitList(() -> {
            visitor.visitEntity(adapter1);
        });
        visitor.visitList(() -> {
            visitor.visitString();
        });
        visitor.visitList(() -> {
            visitor.visitString();
        });
        visitor.visitList(() -> {
            visitor.visitString();
        });
        visitor.visitList(() -> {
            visitor.visitString();
        });
        visitor.visitList(() -> {
            visitor.visitString();
        });
        visitor.visitList(() -> {
            visitor.visitString();
        });
        visitor.visitList(() -> {
            visitor.visitString();
        });
        visitor.visitByte();
        visitor.visitBoolean();
        visitor.visitBoolean();
        visitor.visitBoolean();
        visitor.visitBoolean();
    }

    static {
        __klass__ = StdKlassRegistry.instance.getKlass(Commit.class);
    }

    public static class __WireAdapter__ implements WireAdapter<Commit> {
        private WireAdapter<Id> adapter0;
        private WireAdapter<FieldChange> adapter1;

        public __WireAdapter__() {
        }

        public void init(AdapterRegistry adapterRegistry) {
            this.adapter0 = adapterRegistry.getAdapter(Id.class);
            this.adapter1 = adapterRegistry.getAdapter(FieldChange.class);
        }

        public void write(Commit o, WireOutput output) {
            o.__write__(output, this.adapter0, this.adapter1);
        }

        public Commit read(WireInput input, Object parent) {
            return new Commit(input, this.adapter0, this.adapter1);
        }

        public void visit(WireVisitor visitor) {
            Commit.__visit__(visitor, this.adapter0, this.adapter1);
        }

        public List<Class<? extends Commit>> getSupportedTypes() {
            return List.of(Commit.class);
        }

        public int getTag() {
            return 13;
        }
    }
}
