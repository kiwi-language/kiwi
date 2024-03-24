package tech.metavm.object.instance;

import tech.metavm.entity.Tree;
import tech.metavm.object.instance.core.IInstanceContext;

import java.util.Collection;
import java.util.List;

public interface TreeSource {

    void save(List<Tree> trees);

    List<Tree> load(Collection<Long> ids, IInstanceContext context);

    void remove(List<Long> ids);
}
