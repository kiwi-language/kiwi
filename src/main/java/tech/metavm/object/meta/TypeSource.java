package tech.metavm.object.meta;

public interface TypeSource {

    default ClassType getClassType(String name) {
        return (ClassType) getType(name);
    }

    Type getType(String name);

    void addType(Type type);

}
