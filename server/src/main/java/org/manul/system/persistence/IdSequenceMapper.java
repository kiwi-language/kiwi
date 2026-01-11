package org.manul.system.persistence;

public interface IdSequenceMapper {

    Long selectNextId();

    void incrementNextId(long increment);

    void insert(long initial);

}
