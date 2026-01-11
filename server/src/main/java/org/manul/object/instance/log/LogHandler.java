package org.manul.object.instance.log;

import org.manul.entity.Entity;
import org.manul.entity.EntityContextFactory;
import org.manul.object.instance.core.IInstanceContext;

import javax.annotation.Nullable;
import java.util.List;

public interface LogHandler<T extends Entity> {

    Class<T> getEntityClass();

    void process(List<T> created, @Nullable String clientId, IInstanceContext context, EntityContextFactory entityContextFactory);

}
