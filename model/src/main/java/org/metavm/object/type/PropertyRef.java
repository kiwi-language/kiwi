package org.metavm.object.type;

import org.metavm.entity.Reference;

public interface PropertyRef extends Reference {

    Property resolve();

}
