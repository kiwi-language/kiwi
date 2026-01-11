package org.manul.object.instance.core;

import org.manul.object.type.Type;

import javax.annotation.Nullable;

// TODO loadByType should be implemented by a dedicated buffer.
//  TODO The caching strategy requires optimization.
public record LoadByTypeRequest(Type type, @Nullable Instance startExclusive, long limit) {
}
