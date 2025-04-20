package org.metavm.autograph.mocks;

public class AstLambdaFoo {

    private int value;

    private int valueCopy;

    public FuncType<? super AstLambdaFoo, Integer> test() {
        return s -> s.value;
    }

}
