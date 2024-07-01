package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ContextFinishWare;
import org.metavm.entity.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.LoadAware;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.persistence.*;
import org.metavm.util.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Consumer;

@EntityType
public class WAL extends Entity implements LoadAware, ContextFinishWare {

    private static Consumer<WAL> commitHook;
    private static Consumer<List<InstanceLog>> postProcessHook;

    public static void setCommitHook(Consumer<WAL> commitHook) {
        WAL.commitHook = commitHook;
    }

    public static void setPostProcessHook(Consumer<List<InstanceLog>> postProcessHook) {
        WAL.postProcessHook = postProcessHook;
    }

    private final long appId;
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
        instanceLogs = input.readList(() -> input.readInstanceLog(appId));
    }

    @Override
    public void onContextFinish(IEntityContext context) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.writeList(inserts.values(), output::writeInstancePO);
        output.writeList(updates.values(), output::writeInstancePO);
        output.writeList(deletes.values(), output::writeInstancePO);
        output.writeList(newReferences, output::writeReferencePO);
        output.writeList(removedReferences, output::writeReferencePO);
        output.writeList(newIndexEntries.values, output::writeIndexEntryPO);
        output.writeList(removedIndexEntries.values, output::writeIndexEntryPO);
        output.writeList(instanceLogs, output::writeInstanceLog);
        data = EncodingUtils.encodeBase64(bout.toByteArray());
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
                NncUtils.filter(newIndexEntries, e -> query.match(e.getKey())),
                NncUtils.filter(removedIndexEntries, e -> query.match(e.getKey()))
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

    public void commit() {
        commitHook.accept(this);
        postProcessHook.accept(instanceLogs);
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
            return NncUtils.flatMap(keys, k -> key2entries.getOrDefault(k, List.of()));
        }

        public List<IndexEntryPO> getByInstanceIds(Collection<Id> instanceIds) {
            return NncUtils.flatMap(instanceIds, k -> instanceId2entries.getOrDefault(k, List.of()));
        }


        @NotNull
        @Override
        public Iterator<IndexEntryPO> iterator() {
            return values.iterator();
        }
    }

}

