package tech.metavm.entity;

public interface IInstanceContextFactory {

    IInstanceContext newContext(long tenantId);

    IInstanceContext newContext(long tenantId, boolean asyncProcessLogs);

    IInstanceContext newRootContext();

}
