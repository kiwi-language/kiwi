package tech.metavm.object.type.websocket;

public interface MetaChangeQueue {
    void sendMetaChange(long tenantId, long version);
}
