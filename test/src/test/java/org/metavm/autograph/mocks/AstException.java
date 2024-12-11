package org.metavm.autograph.mocks;

import org.metavm.api.Entity;

@Entity(compiled = true)
public class AstException extends Exception {

    public AstException() {
    }

    public AstException(String message) {
        super(message);
    }

}
