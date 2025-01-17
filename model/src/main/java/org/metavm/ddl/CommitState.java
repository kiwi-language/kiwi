package org.metavm.ddl;

import org.metavm.api.Entity;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.MvInstance;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.object.type.StaticFieldTable;
import org.metavm.task.DDLTask;
import org.metavm.task.SimpleDDLTask;
import org.metavm.task.Task;
import org.metavm.task.Tasks;
import org.metavm.util.Constants;
import org.metavm.util.Instances;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.metavm.task.DDLTask.DISABLE_DELAY;

@Entity
public enum CommitState {
    PREPARING0(0) {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IInstanceContext context) {
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
                    && commit.getNewIndexIds().isEmpty() && commit.getSearchEnabledKlassIds().isEmpty();
        }

        @Override
        public long getSessionTimeout() {
            return Constants.DDL_SESSION_TIMEOUT;
        }
    },
    SUBMITTING(1) {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IInstanceContext context) {
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
    RELOCATING(2) {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IInstanceContext context) {
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
    SETTING_REFERENCE_FLAGS(3) {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IInstanceContext context) {
            var valueToEntityKlasses = Utils.map(commit.getValueToEntityKlassIds(), context::getKlass);
            for (var instance : instances) {
                instance.forEachReference(r -> {
                    if(!r.isResolved())
                        context.buffer(r.getId());
                });
            }
            for (var instance : instances) {
                if (instance instanceof MvInstance mvInst) {
                    mvInst.transformReference((ref, isChild) -> {
                        var referent = ref.resolveDurable();
                        if (referent instanceof MvInstance i && i.tryGetOldId() != null && i.isUseOldId())
                            ref.setForwarded();
                        return ref;
                    });
                    for (Klass klass : valueToEntityKlasses) {
                        instance.forEachReference(ref -> {
                            var referent = ref.get();
                            if (referent instanceof ClassInstance object) {
                                var k = object.getInstanceType().asSuper(klass);
                                if (k != null)
                                    ref.clearEager();
                            }
                        });
                    }
                }
            }
        }

        @Override
        public boolean shouldSkip(Commit commit) {
            return commit.getToChildFieldIds().isEmpty() && commit.getToNonChildFieldIds().isEmpty()
                    && commit.getValueToEntityKlassIds().isEmpty();
        }
    },
    SWITCHING_ID(4) {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IInstanceContext context) {
            for (var instance : instances) {
                if(instance.tryGetOldId() != null && instance instanceof MvInstance mvInst && mvInst.isUseOldId())
                    mvInst.switchId();
            }
        }

        @Override
        public boolean shouldSkip(Commit commit) {
            return commit.getToChildFieldIds().isEmpty() && commit.getToNonChildFieldIds().isEmpty();
        }
    },
    UPDATING_REFERENCE(5) {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IInstanceContext context) {
            for (var instance : instances) {
                instance.forEachReference(r -> {
                    if(r.isForwarded())
                        context.buffer(r.getId());
                });
            }
            for (var instance : instances) {
                if (instance instanceof MvInstance mvInst) {
                    mvInst.transformReference((r, isChild) -> {
                        if (isChild && r.get() instanceof MvInstance i && i.isValue())
                            return i.copy().getReference();
                        if (r.isForwarded())
                            return r.forward();
                        return r;
                    });
                }
            }
        }

        @Override
        public boolean shouldSkip(Commit commit) {
            return commit.getToChildFieldIds().isEmpty() && commit.getToNonChildFieldIds().isEmpty()
                    && commit.getEntityToValueKlassIds().isEmpty();

        }
    },
    CLEANING_UP(6) {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IInstanceContext context) {
            var toEnumKlasses = Utils.map(commit.getToEnumKlassIds(), context::getKlass);
            var fromEnumKlasses = Utils.mapToSet(commit.getFromEnumKlassIds(), context::getKlass);
            var removedChildFields = Utils.map(commit.getRemovedChildFieldIds(), context::getField);
            for (Instance instance : instances) {
                if(instance instanceof MvInstance mvInst && instance.tryGetOldId() != null && !mvInst.isUseOldId())
                    context.buffer(instance.getOldId());
            }
            for (var instance : instances) {
                if(instance instanceof MvInstance mvInst && instance.tryGetOldId() != null && !mvInst.isUseOldId()) {
                    context.loadTree(instance.getOldId().getTreeId());
                    context.removeForwardingPointer(mvInst, true);
                }
                if(instance instanceof ClassInstance object) {
                    for (Klass k : toEnumKlasses) {
                        var staticFieldTable = StaticFieldTable.getInstance(k.getType(), context);
                        if (object.getInstanceKlass() == k && !staticFieldTable.isEnumConstant(object.getReference())) {
                            context.remove(instance);
                        }
                    }
                    if (fromEnumKlasses.contains(object.getInstanceKlass()) && !context.isReferenced(object))
                        context.remove(instance);
                    for (Field removedChildField : removedChildFields) {
                        var k = object.getInstanceType().asSuper(removedChildField.getDeclaringType());
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
    COMPLETED(7) {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IInstanceContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isTerminal() {
            return true;
        }
    },
    ABORTING(8) {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IInstanceContext context) {
            Instances.rollbackDDL(instances, commit, context);
        }

        @Override
        public boolean isRelocationEnabled() {
            return true;
        }
    },
    ABORTED(9) {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IInstanceContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isTerminal() {
            return true;
        }
    }
    ;

    public static final Logger logger = LoggerFactory.getLogger(CommitState.class);

    private final int code;

    CommitState(int code) {
        this.code = code;
    }

    public abstract void process(Iterable<Instance> instances, Commit commit, IInstanceContext context);

    public void onStart(IInstanceContext context, Commit commit) {}

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

    public int code() {
        return code;
    }

    public static CommitState fromCode(int code) {
        return Utils.findRequired(values(), s -> s.code == code);
    }

    public Task createTask(Commit commit) {
        return new DDLTask(commit, this);
    }

    public void transition(Commit commit, IInstanceContext taskContext) {
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
