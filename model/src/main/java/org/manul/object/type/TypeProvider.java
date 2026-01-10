package org.manul.object.type;

import org.manul.object.instance.core.Id;

public interface TypeProvider {

    Type getType(Id id);

    default Type getType(String id) {
        return getType(Id.parse(id));
    }

}
