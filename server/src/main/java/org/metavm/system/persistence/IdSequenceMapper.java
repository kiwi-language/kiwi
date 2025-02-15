package org.metavm.system.persistence;

public interface IdSequenceMapper {

    Long selectNextId();

    void incrementNextId(long increment);

    void insert(long initial);

}
