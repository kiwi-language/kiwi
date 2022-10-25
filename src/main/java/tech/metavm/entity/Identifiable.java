package tech.metavm.entity;

public interface Identifiable {

    default EntityKey key() {
        return isIdNull() ? null : new EntityKey(getClass(), getId());
    }

    default void setPersisted(boolean persisted) {

    }

    Identifiable copy();

    default boolean isIdNull() {
        return getId() == null;
    }

    Long getId();
    
    void initId(long id);
    
}
