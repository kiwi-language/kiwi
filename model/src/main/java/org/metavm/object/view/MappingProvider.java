package org.metavm.object.view;

import org.metavm.object.instance.core.Id;

public interface MappingProvider {

    Mapping getMapping(Id id);

    default ObjectMapping getObjectMapping(Id id) {
        return (ObjectMapping) getMapping(id);
    }

}
