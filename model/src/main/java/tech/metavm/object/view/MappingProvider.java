package tech.metavm.object.view;

import tech.metavm.common.RefDTO;

public interface MappingProvider {

    Mapping getMapping(RefDTO ref);

    default ObjectMapping getObjectMapping(RefDTO ref) {
        return (ObjectMapping) getMapping(ref);
    }

    default Mapping getMapping(long id) {
        return getMapping(RefDTO.fromId(id));
    }

}
