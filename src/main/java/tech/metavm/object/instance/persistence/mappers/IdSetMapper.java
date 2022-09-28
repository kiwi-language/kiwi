package tech.metavm.object.instance.persistence.mappers;

import tech.metavm.object.instance.persistence.IdSetPO;

import java.util.List;

public interface IdSetMapper {

    int batchDelete(List<Long> id);

    int batchInsert(List<IdSetPO> record);

    List<IdSetPO> selectByIds(List<Long> ids);

    int batchUpdate(List<IdSetPO> records);

}