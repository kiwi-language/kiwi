package org.metavm.autograph.mocks;

import org.metavm.api.Entity;

import javax.annotation.Nullable;

@Entity(compiled = true)
public class AstExceptionFoo extends org.metavm.entity.Entity {

    private int amount;

    @Nullable
    public String errorMessage;

    public int executionCount;

    public void test(int dec) {
        try {
            if (dec <= 0) {
                throw new RuntimeException("Illegal arguments");
            }
            if (dec > amount) {
                throw new AstException();
            }
            amount -= dec;
            errorMessage = null;
        } catch (AstException e) {
            var message = e.getMessage();
            if (message != null) {
                errorMessage = message;
            } else {
                errorMessage = "Execution failed";
            }
        } finally {
            executionCount++;
        }
    }

}
