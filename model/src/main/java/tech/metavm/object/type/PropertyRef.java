package tech.metavm.object.type;

import tech.metavm.entity.Reference;

public interface PropertyRef extends Reference {

    Property resolve();

}
