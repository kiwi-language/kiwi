package tech.metavm.object.instance;

import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.object.instance.core.IInstanceContext;

import java.util.List;

public interface IndexSource {

    List<Long> query(InstanceIndexQuery query, IInstanceContext context);

    long count(InstanceIndexQuery query, IInstanceContext context);

    long count(IndexKeyRT from, IndexKeyRT to, IInstanceContext context);

    List<Long> scan(IndexKeyRT from, IndexKeyRT to, IInstanceContext context);

    List<Long> queryByType(long typeId, long startId, long limit, IInstanceContext context);

}
