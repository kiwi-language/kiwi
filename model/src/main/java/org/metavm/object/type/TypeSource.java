package org.metavm.object.type;

public interface TypeSource {

    default ClassType getClassType(String name) {
        return (ClassType) getType(name);
    }

    Type getType(String name);

    void addType(Type type);

}
