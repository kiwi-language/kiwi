package org.metavm.ddl;

import org.metavm.api.Entity;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;
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
    MIGRATING(0) {
        @Override
        public void process(Iterable<Instance> instances, Commit commit, IInstanceContext context) {
            Instances.migrate(instances, commit, context);
        }

        @Override
        public boolean isMigrating() {
            return true;
        }

        @Override
        public boolean shouldSkip(Commit commit) {
//            return commit.getNewFieldIds().isEmpty() && commit.getConvertingFieldIds().isEmpty()
//                    && commit.getFromEnumKlassIds().isEmpty() && commit.getToEnumKlassIds().isEmpty()
//                    && commit.getEntityToValueKlassIds().isEmpty() && commit.getValueToEntityKlassIds().isEmpty()
//                    && commit.getToChildFieldIds().isEmpty() && commit.getToNonChildFieldIds().isEmpty() && commit.getRemovedChildFieldIds().isEmpty()
//                    && commit.getChangingSuperKlassIds().isEmpty() && commit.getRunMethodIds().isEmpty()
//                    && commit.getNewIndexIds().isEmpty() && commit.getSearchEnabledKlassIds().isEmpty();
            return false;
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
        public boolean isMigrating() {
            return true;
        }

        @Override
        public Task createTask(Commit commit, IInstanceContext context) {
            return new SimpleDDLTask(context.allocateRootId(), commit, this);
        }

        @Override
        public long getSessionTimeout() {
            return Constants.DDL_SESSION_TIMEOUT;
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
            Commit.dropTmpTableHook.accept(context.getAppId(), commit.getId());
        }

        @Override
        public Task createTask(Commit commit, IInstanceContext context) {
            return new SimpleDDLTask(context.allocateRootId(), commit, this);
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

    public boolean isMigrating() {
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

    public Task createTask(Commit commit, IInstanceContext context) {
        return new DDLTask(context.allocateRootId(), commit, this);
    }

    public void transition(Commit commit, IInstanceContext taskContext) {
        CommitState nextState;
        if(isMigrating() && commit.isCancelled())
            nextState = CommitState.ABORTING;
        else {
            onCompletion(commit);
            nextState = nextState();
            while (!nextState.isTerminal() && nextState.shouldSkip(commit))
                nextState = nextState.nextState();
        }
        commit.setState(nextState);
        if(!nextState.isTerminal()) {
            var nextTask = nextState.createTask(commit, taskContext);
            if(DISABLE_DELAY)
                taskContext.bind(nextTask);
            else
                taskContext.bind(Tasks.delay(nextTask));
        }

    }

}
