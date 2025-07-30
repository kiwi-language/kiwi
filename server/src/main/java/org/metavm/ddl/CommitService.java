package org.metavm.ddl;

import org.metavm.entity.EntityContextFactory;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.persistence.SchemaManager;
import org.metavm.object.instance.search.InstanceSearchService;
import org.metavm.util.Utils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class CommitService {

    private final SchemaManager schemaManager;
    private final InstanceSearchService instanceSearchService;
    private final EntityContextFactory entityContextFactory;

    public CommitService(SchemaManager schemaManager, InstanceSearchService instanceSearchService, EntityContextFactory entityContextFactory) {
        this.schemaManager = schemaManager;
        this.instanceSearchService = instanceSearchService;
        this.entityContextFactory = entityContextFactory;
        Commit.tableSwitchHook = this::switchTable;
        Commit.dropTmpTableHook = this::dropTmpTables;
    }

    public void switchTable(long appId, Id commitId) {
        Utils.require(TransactionSynchronizationManager.isActualTransactionActive());
        terminateCommit(appId, commitId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void beforeCommit(boolean readOnly) {
                // This terminates the commit in the original table
                terminateCommit(appId, commitId);
                switchTable0(appId, commitId);
            }
        });
    }

    public void dropTmpTables(long appId, Id commitId) {
        Utils.require(TransactionSynchronizationManager.isActualTransactionActive());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void beforeCommit(boolean readOnly) {
                dropTmpTables0(appId, commitId);
            }
        });
    }

    private void dropTmpTables0(long appId, Id commitId) {
        schemaManager.dropTmpTables(appId);
        try (var context = entityContextFactory.newContext(appId, builder -> builder.skipPostProcessing(true))) {
            var commit = context.getEntity(Commit.class, commitId);
            commit.terminate();
            context.finish();
        }
    }

    private void switchTable0(long appId, Id commitId) {
        schemaManager.switchTable(appId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                instanceSearchService.switchAlias(appId);
            }
        });
        terminateCommit(appId, commitId);
    }

    private void terminateCommit(long appId, Id commitId) {
        try (var context = entityContextFactory.newContext(appId, builder -> builder.skipPostProcessing(true))) {
            var commit = context.getEntity(Commit.class, commitId);
            commit.terminate();
            context.finish();
        }
    }

}
