package org.metavm.flow.rest;

import org.jsonk.Json;

@Json(
        typeProperty = "kind",
        subTypes = {
                @org.jsonk.SubType(value = "1", type = MethodRefKey.class),
                @org.jsonk.SubType(value = "2", type = FunctionRefKey.class)
        }
)
public interface CallableRefKey {

    int getKind();

}
