package org.metavm.flow.rest;

import javax.annotation.Nullable;

public record LambdaNodeParam(
        String lambdaId,
        @Nullable String functionalInterface
) {

}
