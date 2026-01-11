package org.manul.object.type;

public interface TypeTagStore {

    int getTypeTag(String className);

    void save();

}
