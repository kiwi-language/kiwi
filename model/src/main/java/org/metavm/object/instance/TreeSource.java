package org.metavm.object.instance;

import org.metavm.entity.Tree;
import org.metavm.object.instance.core.IInstanceContext;

import java.util.Collection;
import java.util.List;

public interface TreeSource {

    void save(List<Tree> trees);

    List<Tree> load(Collection<Long> ids, IInstanceContext context);

    void remove(List<Long> ids);
}
