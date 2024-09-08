package org.metavm.system.persistence;

import org.apache.ibatis.annotations.*;
import org.metavm.util.SecondaryMapper;

@Mapper
public interface IdSequenceMapper extends SecondaryMapper {

    @Select("SELECT next_id FROM id_sequence")
    Long selectNextId();

    @Update("UPDATE id_sequence SET next_id = next_id + #{increment}")
    void incrementNextId(@Param("increment") long increment);

    @Insert("INSERT INTO id_sequence (next_id) VALUES (#{initial})")
    void insert(@Param("initial") long initial);

}
