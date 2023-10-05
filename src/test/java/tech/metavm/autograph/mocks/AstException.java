package tech.metavm.autograph.mocks;

import tech.metavm.entity.EntityType;

@EntityType(value = "Ast异常", compiled = true)
public class AstException extends Exception {

    public AstException() {
    }

    public AstException(String message) {
        super(message);
    }

}
