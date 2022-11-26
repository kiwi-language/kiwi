//package tech.metavm.entity;
//
//public interface AbsEntity extends Identifiable {
//
//    default InternalKey internalKey() {
//        return isIdNull() ? null : new InternalKey(getClass(), getId());
//    }
//
//    default void setPersisted(boolean persisted) {
//
//    }
//
//    AbsEntity copy();
//
//    default boolean isIdNull() {
//        return getId() == null;
//    }
//
//    void initId(long id);
//
//}
