package org.metavm.object.instance.log;

import org.metavm.entity.Entity;
import org.metavm.entity.EntityContextFactory;
import org.metavm.object.instance.core.IInstanceContext;

import javax.annotation.Nullable;
import java.util.List;

public interface LogHandler<T extends Entity> {

    Class<T> getEntityClass();

    void process(List<T> created, @Nullable String clientId, IInstanceContext context, EntityContextFactory entityContextFactory);

}
