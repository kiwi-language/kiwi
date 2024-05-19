package tech.metavm.object.type;

public interface TypeTagStore {

    int getTypeTag(String className);

    void save();

}
