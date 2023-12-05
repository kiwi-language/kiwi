package tech.metavm.object.instance.core;

import tech.metavm.object.type.Type;

// TODO loadByType should be implemented by a dedicated buffer.
//  TODO The caching strategy requires optimization.
public record LoadByTypeRequest(Type type, Instance startExclusive, long limit) {
}
