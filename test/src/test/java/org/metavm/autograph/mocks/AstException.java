package org.metavm.autograph.mocks;

import org.metavm.entity.EntityType;

@EntityType(compiled = true)
public class AstException extends Exception {

    public AstException() {
    }

    public AstException(String message) {
        super(message);
    }

}
