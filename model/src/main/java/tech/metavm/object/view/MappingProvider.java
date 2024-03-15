package tech.metavm.object.view;

import tech.metavm.object.instance.core.Id;

public interface MappingProvider {

    Mapping getMapping(Id id);

    default ObjectMapping getObjectMapping(Id id) {
        return (ObjectMapping) getMapping(id);
    }

}
