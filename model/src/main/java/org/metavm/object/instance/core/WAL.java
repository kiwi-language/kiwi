package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ContextFinishWare;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.LoadAware;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.persistence.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NativeEntity(70)
@Entity
public class WAL extends org.metavm.entity.Entity implements LoadAware, ContextFinishWare, Message {

    private static Consumer<WAL> commitHook;

    private static BiConsumer<Long, List<InstanceLog>> postProcessHook;
    @SuppressWarnings("unused")
    private static Klass __klass__;

    public static void setCommitHook(Consumer<WAL> commitHook) {
        WAL.commitHook = commitHook;
    }

    public static void setPostProcessHook(BiConsumer<Long, List<InstanceLog>> postProcessHook) {
        WAL.postProcessHook = postProcessHook;
    }

    private long appId;
    private String data = EncodingUtils.encodeBase64(new byte[]{});

    private transient Map<Long, InstancePO> inserts = new HashMap<>();
    private transient Map<Long, InstancePO> updates = new HashMap<>();
    private transient Map<Long, InstancePO> deletes = new HashMap<>();
    private transient IndexEntryList newIndexEntries = new IndexEntryList();
    private transient IndexEntryList removedIndexEntries = new IndexEntryList();
    private transient List<ReferencePO> newReferences = new ArrayList<>();
    private transient List<ReferencePO> removedReferences = new ArrayList<>();
    private transient List<InstanceLog> instanceLogs = new ArrayList<>();

    public WAL(long appId) {
        this.appId = appId;
    }

    public WAL(long appId, String data) {
        this.appId = appId;
        setData(data);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitLong();
        visitor.visitUTF();
    }

    @Override
    public void onLoad() {
        var input = new InstanceInput(new ByteArrayInputStream(EncodingUtils.decodeBase64(data)));
        inserts = new HashMap<>();
        updates = new HashMap<>();
        deletes = new HashMap<>();
        input.readList(() -> input.readInstancePO(appId)).forEach(inst -> inserts.put(inst.getId(), inst));
        input.readList(() -> input.readInstancePO(appId)).forEach(inst -> updates.put(inst.getId(), inst));
        input.readList(() -> input.readInstancePO(appId)).forEach(inst -> deletes.put(inst.getId(), inst));
        newReferences = new ArrayList<>(input.readList(() -> input.readReferencePO(appId)));
        removedReferences = new ArrayList<>(input.readList(() -> input.readReferencePO(appId)));
        newIndexEntries = new IndexEntryList(input.readList(() -> input.readIndexEntryPO(appId)));
        removedIndexEntries = new IndexEntryList(input.readList(() -> input.readIndexEntryPO(appId)));
        instanceLogs = input.readList(() -> InstanceLog.read(input));
    }

    @Override
    public void onContextFinish(IInstanceContext context) {
        buildData();
    }

    public void buildData() {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.writeList(inserts.values(), output::writeInstancePO);
        output.writeList(updates.values(), output::writeInstancePO);
        output.writeList(deletes.values(), output::writeInstancePO);
        output.writeList(newReferences, output::writeReferencePO);
        output.writeList(removedReferences, output::writeReferencePO);
        output.writeList(newIndexEntries.values, output::writeIndexEntryPO);
        output.writeList(removedIndexEntries.values, output::writeIndexEntryPO);
        output.writeList(instanceLogs, log -> log.write(output));
        data = EncodingUtils.encodeBase64(bout.toByteArray());
    }

    public String getData() {
        return data;
    }

    public void saveInstances(ChangeList<InstancePO> changeList) {
        changeList.inserts().forEach(inst -> inserts.put(inst.getId(), inst));
        changeList.updates().forEach(inst -> updates.put(inst.getId(), inst));
        changeList.deletes().forEach(inst -> deletes.put(inst.getId(), inst));
    }

    public void saveReferences(ChangeList<ReferencePO> changeList) {
        newReferences.addAll(changeList.inserts());
        removedReferences.addAll(changeList.deletes());
    }

    public void saveIndexEntries(ChangeList<IndexEntryPO> changeList) {
        newIndexEntries.addAll(changeList.inserts());
        removedIndexEntries.addAll(changeList.deletes());
    }

    public Set<IndexEntryPO> getNewIndexEntries() {
        return new HashSet<>(newIndexEntries.values);
    }

    public void saveInstanceLogs(List<InstanceLog> instanceLogs) {
        this.instanceLogs.addAll(instanceLogs);
    }

    public boolean isDeleted(long id) {
        return deletes.containsKey(id);
    }

    public InstancePO get(long id) {
        var inst = inserts.get(id);
        if (inst != null)
            return inst;
        return updates.get(id);
    }

    public WALIndexQueryResult query(IndexQueryPO query) {
        return new WALIndexQueryResult(
                Utils.filter(newIndexEntries, e -> query.match(e.getKey())),
                Utils.filter(removedIndexEntries, e -> query.match(e.getKey()))
        );
    }

    public WALIndexQueryResult getIndexEntriesByKeys(List<IndexKeyPO> keys) {
        return new WALIndexQueryResult(newIndexEntries.getByKeys(keys), removedIndexEntries.getByKeys(keys));
    }

    public WALIndexQueryResult getIndexEntriesByInstanceIds(Collection<Id> instanceIds) {
        return new WALIndexQueryResult(newIndexEntries.getByInstanceIds(instanceIds), removedIndexEntries.getByInstanceIds(instanceIds));
    }

    public ChangeList<InstancePO> getInstanceChanges() {
        return ChangeList.create(inserts.values(), updates.values(), deletes.values());
    }

    public ChangeList<ReferencePO> getReferenceChanges() {
        return ChangeList.create(newReferences, List.of(), removedReferences);
    }

    public ChangeList<IndexEntryPO> getIndexEntryChanges() {
        return ChangeList.create(newIndexEntries.values, List.of(), removedIndexEntries.values);
    }

    public List<InstanceLog> getInstanceLogs() {
        return instanceLogs;
    }

    public void setData(String data) {
        this.data = data;
        onLoad();
    }

    public void commit() {
        commitHook.accept(this);
        postProcessHook.accept(appId, instanceLogs);
    }

    public long getAppId() {
        return appId;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    public WAL copy() {
        return new WAL(appId, data);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("data", this.getData());
        map.put("newIndexEntries", this.getNewIndexEntries());
        map.put("instanceChanges", this.getInstanceChanges());
        map.put("referenceChanges", this.getReferenceChanges());
        map.put("indexEntryChanges", this.getIndexEntryChanges());
        map.put("instanceLogs", this.getInstanceLogs().stream().map(InstanceLog::toJson).toList());
        map.put("appId", this.getAppId());
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
        return EntityRegistry.TAG_WAL;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.appId = input.readLong();
        this.data = input.readUTF();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeLong(appId);
        output.writeUTF(data);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
    }

    private static class IndexEntryList implements Iterable<IndexEntryPO> {
        private final List<IndexEntryPO> values = new ArrayList<>();
        private final Map<IndexKeyPO, List<IndexEntryPO>> key2entries = new HashMap<>();
        private final Map<Id, List<IndexEntryPO>> instanceId2entries = new HashMap<>();

        public IndexEntryList() {
        }

        public IndexEntryList(List<IndexEntryPO> values) {
            addAll(values);
        }

        public void addAll(List<IndexEntryPO> entries) {
            if(entries.size() != new HashSet<>(entries).size())
                throw new IllegalArgumentException("Duplicate entries detected");
            values.addAll(entries);
            entries.forEach(e -> key2entries.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).add(e));
            entries.forEach(e -> instanceId2entries.computeIfAbsent(Id.fromBytes(e.getInstanceId()), k -> new ArrayList<>()).add(e));
        }

        public List<IndexEntryPO> getByKeys(List<IndexKeyPO> keys) {
            return Utils.flatMap(keys, k -> key2entries.getOrDefault(k, List.of()));
        }

        public List<IndexEntryPO> getByInstanceIds(Collection<Id> instanceIds) {
            return Utils.flatMap(instanceIds, k -> instanceId2entries.getOrDefault(k, List.of()));
        }


        @NotNull
        @Override
        public Iterator<IndexEntryPO> iterator() {
            return values.iterator();
        }
    }

}

