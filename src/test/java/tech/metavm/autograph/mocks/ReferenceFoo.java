package tech.metavm.autograph.mocks;

import java.sql.Ref;

public class ReferenceFoo {

    private String[] args;

//    public String foo() {
//        return args[0];
//    }
//
//    public String bar() {
//        return args[1];
//    }
//
//    void setArgs(String[] args) {
//        this.args = args;
//    }

    public class Inner {

        public String[] test() {
            return ReferenceFoo.this.args;
        }

    }

}
