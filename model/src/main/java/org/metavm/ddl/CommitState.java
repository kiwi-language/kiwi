package org.metavm.ddl;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.StaticFieldTable;
import org.metavm.task.DDLTask;
import org.metavm.task.SimpleDDLTask;
import org.metavm.task.Task;
import org.metavm.task.Tasks;
import org.metavm.util.Constants;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.metavm.task.DDLTask.DISABLE_DELAY;

@EntityType
public enum CommitState {
    PREPARING0 {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IEntityContext context) {
            Instances.applyDDL(instances, commit, context);
        }

        @Override
        public boolean isPreparing() {
            return true;
        }

        @Override
        public boolean shouldSkip(Commit commit) {
            return commit.getNewFieldIds().isEmpty() && commit.getConvertingFieldIds().isEmpty()
                    && commit.getFromEnumKlassIds().isEmpty() && commit.getToEnumKlassIds().isEmpty()
                    && commit.getEntityToValueKlassIds().isEmpty() && commit.getValueToEntityKlassIds().isEmpty()
                    && commit.getToChildFieldIds().isEmpty() && commit.getToNonChildFieldIds().isEmpty() && commit.getRemovedChildFieldIds().isEmpty()
                    && commit.getChangingSuperKlassIds().isEmpty() && commit.getRunMethodIds().isEmpty()
                    && commit.getNewIndexIds().isEmpty();
        }

        @Override
        public long getSessionTimeout() {
            return Constants.DDL_SESSION_TIMEOUT;
        }
    },
    SUBMITTING {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IEntityContext context) {
        }

        @Override
        public void onCompletion(Commit commit) {
            if(!commit.isCancelled())
                commit.submit();
        }

        @Override
        public boolean isPreparing() {
            return true;
        }

        @Override
        public Task createTask(Commit commit) {
            return new SimpleDDLTask(commit, this);
        }

        @Override
        public long getSessionTimeout() {
            return Constants.DDL_SESSION_TIMEOUT;
        }
    },
    RELOCATING {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IEntityContext context) {
        }

        @Override
        public boolean shouldSkip(Commit commit) {
            return commit.getValueToEntityKlassIds().isEmpty() && commit.getEntityToValueKlassIds().isEmpty()
                    && commit.getToChildFieldIds().isEmpty() && commit.getToNonChildFieldIds().isEmpty();
        }

        @Override
        public boolean isRelocationEnabled() {
            return true;
        }
    },
    SETTING_REFERENCE_FLAGS {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IEntityContext context) {
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
                    if(referent.tryGetOldId() != null && referent.isUseOldId())
                        ref.setForwarded();
                    return ref;
                });
                for (Klass klass : valueToEntityKlasses) {
                    instance.forEachReference(ref -> {
                        var referent = ref.resolve();
                        if (referent instanceof ClassInstance object) {
                            var k = object.getType().findAncestorByKlass(klass);
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
                    && commit.getValueToEntityKlassIds().isEmpty();
        }
    },
    SWITCHING_ID {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IEntityContext context) {
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
    UPDATING_REFERENCE {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IEntityContext context) {
            for (var instance : instances) {
                instance.forEachReference(r -> {
                    if(r.isForwarded())
                        context.getInstanceContext().buffer(r.getId());
                });
            }
            for (var instance : instances) {
                instance.transformReference((r, isChild) -> {
                    if(isChild && r.resolve().isValue())
                        return r.resolve().copy().getReference();
                    if(r.isForwarded())
                        return r.forward();
                    return r;
                });
            }
        }

        @Override
        public boolean shouldSkip(Commit commit) {
            return commit.getToChildFieldIds().isEmpty() && commit.getToNonChildFieldIds().isEmpty()
                    && commit.getEntityToValueKlassIds().isEmpty();

        }
    },
    CLEANING_UP {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IEntityContext context) {
            var instCtx = context.getInstanceContext();
            var toEnumKlasses = NncUtils.map(commit.getToEnumKlassIds(), context::getKlass);
            var fromEnumKlasses = NncUtils.mapUnique(commit.getFromEnumKlassIds(), context::getKlass);
            var removedChildFields = NncUtils.map(commit.getRemovedChildFieldIds(), context::getField);
            for (Instance instance : instances) {
                if(instance.tryGetOldId() != null && !instance.isUseOldId())
                    instCtx.buffer(instance.getOldId());
            }
            for (var instance : instances) {
                if(instance.tryGetOldId() != null && !instance.isUseOldId()) {
                    instCtx.loadTree(instance.getOldId().getTreeId());
                    instCtx.removeForwardingPointer(instance, true);
                }
                if(instance instanceof ClassInstance object) {
                    for (Klass k : toEnumKlasses) {
                        var staticFieldTable = StaticFieldTable.getInstance(k.getType(), context);
                        if (object.getKlass() == k && !staticFieldTable.isEnumConstant(object.getReference())) {
                            instCtx.remove(instance);
                        }
                    }
                    if (fromEnumKlasses.contains(object.getKlass()) && !instCtx.isReferenced(object))
                        instCtx.remove(instance);
                    for (Field removedChildField : removedChildFields) {
                        var k = object.getType().findAncestorByKlass(removedChildField.getDeclaringType());
                        if(k != null) {
                            var f = k.getField(removedChildField);
                            object.setField(f.getRawField(), Instances.nullInstance());
                        }
                    }
                }
            }
        }

        @Override
        public boolean shouldSkip(Commit commit) {
            return commit.getToChildFieldIds().isEmpty() && commit.getToNonChildFieldIds().isEmpty()
                    && commit.getToEnumKlassIds().isEmpty() && commit.getFromEnumKlassIds().isEmpty()
                    && commit.getRemovedChildFieldIds().isEmpty();
        }
    },
    COMPLETED {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IEntityContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isTerminal() {
            return true;
        }
    },
    ABORTING {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IEntityContext context) {
            Instances.rollbackDDL(instances, commit, context);
        }

        @Override
        public boolean isRelocationEnabled() {
            return true;
        }
    },
    ABORTED {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IEntityContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isTerminal() {
            return true;
        }
    }
    ;

    public static final Logger logger = LoggerFactory.getLogger(CommitState.class);

    public abstract void process(Iterable<Instance> instances, Commit commit, IEntityContext context);

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

    public boolean isRelocationEnabled() {
        return false;
    }

    public boolean isTerminal() {
        return false;
    }

    public Task createTask(Commit commit) {
        return new DDLTask(commit, this);
    }

    public void transition(Commit commit, IEntityContext taskContext) {
        CommitState nextState;
        if(isPreparing() && commit.isCancelled())
            nextState = CommitState.ABORTING;
        else {
            onCompletion(commit);
            nextState = nextState();
            while (!nextState.isTerminal() && nextState.shouldSkip(commit))
                nextState = nextState.nextState();
        }
        commit.setState(nextState);
        if(!nextState.isTerminal()) {
            var nextTask = nextState.createTask(commit);
            if(DISABLE_DELAY)
                taskContext.bind(nextTask);
            else
                taskContext.bind(Tasks.delay(nextTask));
        }

    }

}
