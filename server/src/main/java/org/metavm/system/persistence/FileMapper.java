package org.metavm.system.persistence;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper {

    FilePO selectByName(String name);

    void insert(FilePO file);

    void update(FilePO file);

    void delete(String name);

    boolean exists(String name);

    default void save(FilePO file) {
        if(exists(file.getName()))
            update(file);
        else
            insert(file);
    }

}
