package org.manul.object.instance;

import org.manul.entity.Tree;
import org.manul.object.instance.core.IInstanceContext;

import java.util.Collection;
import java.util.List;

public interface TreeSource {

    void save(List<Tree> trees);

    List<Tree> load(Collection<Long> ids, IInstanceContext context);

    void remove(List<Long> ids);
}
