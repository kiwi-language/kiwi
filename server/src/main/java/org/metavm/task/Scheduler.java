package org.metavm.task;

import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.EntityIndexKey;
import org.metavm.entity.EntityIndexQuery;
import org.metavm.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class Scheduler extends EntityContextFactoryAware {

    public static final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    public static final long timeout = 30000;

    private final TransactionOperations transactionOperations;

    public volatile boolean active;
    public volatile String nextWorkerIP;

    public Scheduler(EntityContextFactory entityContextFactory, TransactionOperations transactionOperations) {
        super(entityContextFactory);
        this.transactionOperations = transactionOperations;
    }

    @Scheduled(fixedDelay = 100)
    public void schedule() {
        if (active) {
            transactionOperations.executeWithoutResult(s -> schedule0());
        }
    }

    private void schedule0() {
        try (var context = newPlatformContext()) {
            var registry = SchedulerRegistry.getInstance(context);
            if (!NetworkUtils.localIP.equals(registry.getIp())) {
                active = false;
                return;
            }
            var nulls = new ArrayList<>();
            nulls.add(null);
            var tasks = context.query(new EntityIndexQuery<>(
                    ShadowTask.IDX_EXECUTOR_IP,
                    new EntityIndexKey(nulls),
                    new EntityIndexKey(nulls),
                    false,
                    200
            ));
            if (tasks.isEmpty())
                return;
            var executors = context.query(new EntityIndexQuery<>(
                    ExecutorData.IDX_AVAIlABLE,
                    new EntityIndexKey(List.of(true)),
                    new EntityIndexKey(List.of(true)),
                    false,
                    200
            ));
            var now = System.currentTimeMillis();
            var it = executors.listIterator();
            while (it.hasNext()) {
                var executor = it.next();
                if (now - executor.getLastHeartbeat() > timeout) {
                    executor.setAvailable(false);
                    it.remove();
                }
            }
            if (executors.isEmpty()) {
                logger.warn("No executor available");
                return;
            }
            executors = executors.stream().sorted(Comparator.comparing(ExecutorData::getIp)).toList();
            var i = 0;
            if (nextWorkerIP != null) {
                while (i < executors.size() && executors.get(i).getIp().compareTo(nextWorkerIP) < 0)
                    i++;
            }
            for (ShadowTask task : tasks) {
                task.setExecutorIP(executors.get((i++) % executors.size()).getIp());
            }
            nextWorkerIP = executors.get(i % executors.size()).getIp();
            context.finish();
        }
    }

    @Scheduled(fixedDelay = 50000)
    public void processTimeoutTasks() {
        if (active) {
            transactionOperations.executeWithoutResult(s -> processTimeoutTasks0());
        }
    }

    private void processTimeoutTasks0() {
        try (var context = newPlatformContext()) {
            var registry = SchedulerRegistry.getInstance(context);
            if (!NetworkUtils.localIP.equals(registry.getIp())) {
                active = false;
                return;
            }
            var now = System.currentTimeMillis();
            var timeoutTasks = context.query(
                    new EntityIndexQuery<>(
                            ShadowTask.IDX_RUN_AT,
                            new EntityIndexKey(List.of(1)),
                            new EntityIndexKey(List.of(now - timeout)),
                            false,
                            500
                    )
            );
            for (ShadowTask task : timeoutTasks) {
                task.setExecutorIP(null);
            }
            context.finish();
        }
    }

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void sendHeartbeat() {
        transactionOperations.executeWithoutResult(s -> {
            try (var context = newPlatformContext()) {
                var registry = SchedulerRegistry.getInstance(context);
                var now = System.currentTimeMillis();
                if (Objects.equals(NetworkUtils.localIP, registry.getIp())) {
                    registry.setLastHeartbeat(now);
                    active = true;
                } else if (registry.getIp() == null || now - registry.getLastHeartbeat() > timeout) {
                    registry.setIp(NetworkUtils.localIP);
                    registry.setLastHeartbeat(now);
                    active = true;
                } else
                    active = false;
                context.finish();
            }
        });
    }

    public boolean isActive() {
        return active;
    }
}
