package tech.metavm.object.type;

import tech.metavm.common.RefDTO;

public interface IndexProvider {

    Index getIndex(RefDTO ref);

    default Index getIndex(long id) {
        return getIndex(RefDTO.fromId(id));
    }

}
