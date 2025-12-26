package org.metavm.flow.rest;

import org.jsonk.Json;
import org.jsonk.SubType;

import java.util.List;

@Json(
    typeProperty = "kind",
    subTypes = {
            @SubType(value = "method", type = MethodRefKey.class),
            @SubType(value = "function", type = FunctionRefKey.class)
    }
)
public interface FlowRefKey extends CallableRefKey {

    String rawFlowId();

    List<String> typeArguments();

    int getKind();

}
