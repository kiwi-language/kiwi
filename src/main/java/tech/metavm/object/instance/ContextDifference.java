package tech.metavm.object.instance;

import tech.metavm.object.instance.log.InstanceLog;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.Field;
import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ContextDifference {

    private final long tenantId;
    private final List<Instance> insertingList = new ArrayList<>();

    private final List<Instance> instancesToInsert = new ArrayList<>();
    private final List<Instance> instancesToUpdate = new ArrayList<>();
    private final List<VersionPO> instanceIdsToRemove = new ArrayList<>();

    private final List<InstanceRelation> relationsToInsert = new ArrayList<>();
    private final List<InstanceRelation> relationsToRemove = new ArrayList<>();

    public ContextDifference(long tenantId) {
        this.tenantId = tenantId;
    }

    public void diff(Collection<Instance> beforeList, Collection<Instance> afterList) {
        List<Pair<Instance>> pairs = NncUtils.buildPairs(beforeList, afterList, AbsInstance::getId);
        for (Pair<Instance> pair : pairs) {
            diff(pair.first(), pair.second());
        }
    }

    private void diff(Instance before, Instance after) {
        if(before == null && after == null) {
            return;
        }
        if(before == null) {
            insertingList.add(after);
            instancesToInsert.add(after);
            relationsToInsert.addAll(after.getRelations());
        }
        else if(after == null) {
            instanceIdsToRemove.add(before.nextVersion());
            relationsToRemove.addAll(before.getRelations());
        }
        else {
            if(isDifferent(before, after)) {
                after.incVersion();
                instancesToUpdate.add(after);
                ChangeList<InstanceRelation> relationChange = ChangeList.build(
                        before.getRelations(), after.getRelations(), InstanceRelation::getDestKey
                );
                relationsToInsert.addAll(relationChange.inserts());
                relationsToRemove.addAll(relationChange.deletes());
            }
        }
    }

    private boolean isDifferent(Instance before, Instance after) {
        Type type = before.getType();
        for (Field field : type.getFields()) {
            if(!Objects.equals(before.getRaw(field), after.getRaw(field))) {
                return true;
            }
        }
        return false;
    }

    public List<Instance> instancesToInsert() {
        return instancesToInsert;
    }

    public List<Instance> instanceToUpdate() {
        return instancesToUpdate;
    }

    public List<VersionPO> instanceIdsToRemove() {
        return instanceIdsToRemove;
    }

    public List<InstanceRelation> relationsToInsert() {
        return relationsToInsert;
    }

    public List<InstanceRelation> relationsToRemove() {
        return relationsToRemove;
    }

    public void setInsertIds(List<Long> insertIds) {
        NncUtils.biForEach(insertingList, insertIds, Instance::initId);
    }

    public long tenantId() {
        return tenantId;
    }

    public List<InstanceLog> buildLogs() {
        List<InstanceLog> logs = new ArrayList<>();
        for (Instance instance : instancesToInsert) {
            logs.add(InstanceLog.insert(instance));
        }
        for (Instance instance : instancesToUpdate) {
            logs.add(InstanceLog.update(instance));
        }
        for (VersionPO version : instanceIdsToRemove) {
            logs.add(InstanceLog.delete(version));
        }
        return logs;
    }

}
