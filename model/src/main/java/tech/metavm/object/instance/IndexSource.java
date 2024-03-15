package tech.metavm.object.instance;

import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Id;

import java.util.List;

public interface IndexSource {

    List<Id> query(InstanceIndexQuery query, IInstanceContext context);

    long count(InstanceIndexQuery query, IInstanceContext context);

    long count(IndexKeyRT from, IndexKeyRT to, IInstanceContext context);

    List<Id> scan(IndexKeyRT from, IndexKeyRT to, IInstanceContext context);

}
