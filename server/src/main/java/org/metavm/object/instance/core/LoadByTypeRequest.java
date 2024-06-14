package org.metavm.object.instance.core;

import org.metavm.object.type.Type;

import javax.annotation.Nullable;

// TODO loadByType should be implemented by a dedicated buffer.
//  TODO The caching strategy requires optimization.
public record LoadByTypeRequest(Type type, @Nullable DurableInstance startExclusive, long limit) {
}
