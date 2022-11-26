package tech.metavm.entity;

public interface EntityLoadStage {

    void addPending(EntityKey key);

    boolean isPending(EntityKey key);

    void proceed();

}
