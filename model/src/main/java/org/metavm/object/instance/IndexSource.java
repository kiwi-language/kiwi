package org.metavm.object.instance;

import org.metavm.entity.InstanceIndexQuery;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;

import java.util.List;

public interface IndexSource {

    List<Id> query(InstanceIndexQuery query, IInstanceContext context);

    long count(InstanceIndexQuery query, IInstanceContext context);

    long count(IndexKeyRT from, IndexKeyRT to, IInstanceContext context);

    List<Id> scan(IndexKeyRT from, IndexKeyRT to, IInstanceContext context);

}
