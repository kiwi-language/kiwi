package tech.metavm.object.type;

public interface TypeTagStore {

    int getTypeTag(Class<?> javaClass);

    void save();


}
