package tech.metavm.object.type.websocket;

public interface MetaChangeQueue {

    void notifyTypeChange(long tenantId, long version);

}
