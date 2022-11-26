//package tech.metavm.object.instance;
//
//import tech.metavm.entity.EntityContext;
//import tech.metavm.object.instance.log.InstanceLog;
//import tech.metavm.object.instance.persistence.VersionPO;
//import tech.metavm.object.meta.Type;
//import tech.metavm.object.meta.Field;
//import tech.metavm.util.ChangeList;
//import tech.metavm.util.NncUtils;
//import tech.metavm.util.Pair;
//
//import java.util.*;
//
//public class ContextDifference {
//
//    private final InstanceContext context;
//    private final List<Instance> insertingList = new ArrayList<>();
//
//    private final List<Instance> instancesToInsert = new ArrayList<>();
//    private final List<Instance> instancesToUpdate = new ArrayList<>();
//    private final List<Instance> instanceIdsToRemove = new ArrayList<>();
//
//    private final List<InstanceRelation> relationsToInsert = new ArrayList<>();
//    private final List<InstanceRelation> relationsToRemove = new ArrayList<>();
//
//    private final Map<String, Object> attributes = new HashMap<>();
//
//    public ContextDifference(InstanceContext context) {
//        this.context = context;
//    }
//
//    public void diff(Collection<Instance> beforeList, Collection<Instance> afterList) {
//        List<Pair<Instance>> pairs = NncUtils.buildPairs(beforeList, afterList, AbsInstance::getId);
//        for (Pair<Instance> pair : pairs) {
//            diff(pair.first(), pair.second());
//        }
//    }
//
//    private void diff(Instance before, Instance after) {
//        if(before == null && after == null) {
//            return;
//        }
//        if(before == null) {
//            insertingList.add(after);
//            instancesToInsert.add(after);
//            relationsToInsert.addAll(after.getRelations());
//        }
//        else if(after == null) {
//            instanceIdsToRemove.add(before);
//            relationsToRemove.addAll(before.getRelations());
//        }
//        else {
//            if(isDifferent(before, after)) {
//                after.incVersion();
//                instancesToUpdate.add(after);
//                ChangeList<InstanceRelation> relationChange = ChangeList.build(
//                        before.getRelations(), after.getRelations(), InstanceRelation::getDestKey
//                );
//                relationsToInsert.addAll(relationChange.inserts());
//                relationsToRemove.addAll(relationChange.deletes());
//            }
//        }
//    }
//
//    private boolean isDifferent(Instance before, Instance after) {
//        Type type = before.getType();
//        for (Field field : type.getFields()) {
//            if(!Objects.equals(before.getRaw(field), after.getRaw(field))) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public List<Instance> getInstancesAfter() {
//        return NncUtils.merge(inserts(), updates());
//    }
//
//    public List<Instance> getInstancesBefore() {
//        return NncUtils.merge(updates(), deletes());
//    }
//
//    public List<Instance> inserts() {
//        return instancesToInsert;
//    }
//
//    public List<Instance> updates() {
//        return instancesToUpdate;
//    }
//
//    public List<VersionPO> deleteVersions() {
//        return NncUtils.map(instanceIdsToRemove, Instance::nextVersion);
//    }
//
//    public List<Instance> deletes() {
//        return instanceIdsToRemove;
//    }
//
//    public List<InstanceRelation> relationsToInsert() {
//        return relationsToInsert;
//    }
//
//    public List<InstanceRelation> relationsToRemove() {
//        return relationsToRemove;
//    }
//
//    public void setInsertIds(List<Long> insertIds) {
//        NncUtils.biForEach(insertingList, insertIds, Instance::initId);
//    }
//
//    public long tenantId() {
//        return context.getTenantId();
//    }
//
//    public Object getAttribute(String key) {
//        return attributes.get(key);
//    }
//
//    public void setAttribute(String key, Object value) {
//        attributes.put(key, value);
//    }
//
//    public InstanceContext getContext() {
//        return context;
//    }
//
//    public EntityContext getEntityContext() {
//        return context.getEntityContext();
//    }
//
//    public List<InstanceLog> buildLogs() {
//        List<InstanceLog> logs = new ArrayList<>();
//        for (Instance instance : inserts()) {
//            logs.add(InstanceLog.insert(instance));
//        }
//        for (Instance instance : updates()) {
//            logs.add(InstanceLog.update(instance));
//        }
//        for (VersionPO version : deleteVersions()) {
//            logs.add(InstanceLog.delete(version));
//        }
//        return logs;
//    }
//
//}
