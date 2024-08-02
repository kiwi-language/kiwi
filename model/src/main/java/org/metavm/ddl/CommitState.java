package org.metavm.ddl;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.type.Klass;
import org.metavm.util.Constants;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EntityType
public enum CommitState {
    PREPARING0 {
        @Override
        public void process(Iterable<DurableInstance> instances, Commit commit, IEntityContext context) {
            Instances.applyDDL(instances, commit, context);
        }

        @Override
        public void onCompletion(Commit commit) {
            commit.submit();
        }

        @Override
        public boolean isPreparing() {
            return true;
        }

        @Override
        public long getSessionTimeout() {
            return Constants.DDL_SESSION_TIMEOUT;
        }
    },
    MIGRATING {
        @Override
        public void process(Iterable<DurableInstance> instances, Commit commit, IEntityContext context) {
        }

        @Override
        public boolean shouldSkip(Commit commit) {
            return commit.getValueToEntityKlassIds().isEmpty() && commit.getEntityToValueKlassIds().isEmpty()
                    && commit.getToChildFieldIds().isEmpty() && commit.getToNonChildFieldIds().isEmpty();
        }

        @Override
        public boolean isMigrationEnabled() {
            return true;
        }
    },
    FLAGGING_REFERENCE {
        @Override
        public void process(Iterable<DurableInstance> instances, Commit commit, IEntityContext context) {
            var valueToEntityKlasses = NncUtils.map(commit.getValueToEntityKlassIds(), context::getKlass);
            for (var instance : instances) {
                instance.forEachReference(r -> {
                    if(!r.isResolved())
                        context.getInstanceContext().buffer(r.getId());
                });
            }
            for (var instance : instances) {
                instance.transformReference((ref, isChild) -> {
                    var referent = ref.resolve();
                    if(isChild && referent.isValue())
                        return referent.copy().getReference();
                    if(referent.tryGetOldId() != null && referent.isUseOldId())
                        ref.setForwarded();
                    return ref;
                });
                for (Klass klass : valueToEntityKlasses) {
                    instance.forEachReference(ref -> {
                        var referent = ref.resolve();
                        if (referent instanceof ClassInstance object) {
                            var k = object.getKlass().findAncestorByTemplate(klass);
                            if (k != null)
                                ref.clearEager();
                        }
                    });
                }
            }
        }

        @Override
        public boolean shouldSkip(Commit commit) {
            return commit.getToChildFieldIds().isEmpty() && commit.getToNonChildFieldIds().isEmpty()
                    && commit.getValueToEntityKlassIds().isEmpty() && commit.getEntityToValueKlassIds().isEmpty();
        }
    },
    SWITCHING_ID {
        @Override
        public void process(Iterable<DurableInstance> instances, Commit commit, IEntityContext context) {
            for (var instance : instances) {
                if(instance.tryGetOldId() != null && instance.isUseOldId())
                    instance.switchId();
            }
        }

        @Override
        public boolean shouldSkip(Commit commit) {
            return commit.getToChildFieldIds().isEmpty() && commit.getToNonChildFieldIds().isEmpty();
        }
    },
    REDIRECTING_REFERENCE {
        @Override
        public void process(Iterable<DurableInstance> instances, Commit commit, IEntityContext context) {
            for (var instance : instances) {
                instance.forEachReference(r -> {
                    if(r.isForwarded())
                        context.getInstanceContext().buffer(r.getId());
                });
            }
            for (var instance : instances) {
                instance.forEachReference(r -> {
                    if(r.isForwarded())
                        r.forward();
                });
            }
        }

        @Override
        public boolean shouldSkip(Commit commit) {
            return commit.getToChildFieldIds().isEmpty() && commit.getToNonChildFieldIds().isEmpty();

        }
    },
    CLEARING_FPS {
        @Override
        public void process(Iterable<DurableInstance> instances, Commit commit, IEntityContext context) {
            var instCtx = context.getInstanceContext();
            var toEnumKlasses = NncUtils.map(commit.getToEnumKlassIds(), context::getKlass);
            var fromEnumKlasses = NncUtils.mapUnique(commit.getFromEnumKlassIds(), context::getKlass);
            for (DurableInstance instance : instances) {
                if(instance.tryGetOldId() != null && !instance.isUseOldId())
                    instCtx.buffer(instance.getOldId());
            }
            for (var instance : instances) {
                if(instance.tryGetOldId() != null && !instance.isUseOldId()) {
                    instCtx.loadTree(instance.getOldId().getTreeId());
                    instCtx.removeForwardingPointer(instance, true);
                }
                for (Klass k : toEnumKlasses) {
                    if(instance instanceof ClassInstance o && o.getKlass() == k && !k.isEnumConstant(o.getReference())) {
                        instCtx.remove(instance);
                    }
                }
                if(instance instanceof ClassInstance clsInst && fromEnumKlasses.contains(clsInst.getKlass()) && !instCtx.isReferenced(clsInst))
                    instCtx.remove(instance);
            }
        }

        @Override
        public boolean shouldSkip(Commit commit) {
            return commit.getToChildFieldIds().isEmpty() && commit.getToNonChildFieldIds().isEmpty()
                    && commit.getToEnumKlassIds().isEmpty() && commit.getFromEnumKlassIds().isEmpty();
        }
    },
    COMPLETED {
        @Override
        public void process(Iterable<DurableInstance> instances, Commit commit, IEntityContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isTerminal() {
            return true;
        }
    },
    ABORTING {
        @Override
        public void process(Iterable<DurableInstance> instances, Commit commit, IEntityContext context) {
            Instances.rollbackDDL(instances, commit, context);
        }

        @Override
        public boolean isMigrationEnabled() {
            return true;
        }
    },
    ABORTED {
        @Override
        public void process(Iterable<DurableInstance> instances, Commit commit, IEntityContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isTerminal() {
            return true;
        }
    }
    ;

    public static final Logger logger = LoggerFactory.getLogger(CommitState.class);

    public abstract void process(Iterable<DurableInstance> instances, Commit commit, IEntityContext context);

    public void onStart(IEntityContext context, Commit commit) {}

    public void onCompletion(Commit commit) {}

    public boolean shouldSkip(Commit commit) {
        return false;
    }

    public CommitState nextState() {
        return values()[ordinal() + 1];
    }

    public boolean isPreparing() {
        return false;
    }

    public long getSessionTimeout() {
        return Constants.SESSION_TIMEOUT;
    }

    public boolean isMigrationEnabled() {
        return false;
    }

    public boolean isTerminal() {
        return false;
    }

}
