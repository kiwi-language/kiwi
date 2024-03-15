package tech.metavm.object.instance;

import tech.metavm.entity.Tree;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Id;

import java.util.Collection;
import java.util.List;

public interface TreeSource {

    void save(List<Tree> trees);

    List<Tree> load(Collection<Id> ids, IInstanceContext context);

    void remove(List<Id> ids);
}
