package org.metavm;


public class Lab {

    int value;

    class Inner {

        int getValue() {
            return value;
        }

    }

}

class Base {
    int value;

    public Base() {
        this.value = this.__init_value__();
    }

    private int __init_value__() {
        return 0;
    }
}

class SuperclassFieldFoo extends Base {

    public int getValue() {

        class $1 implements java.util.Iterator<java.lang.String> {
            private int v;

            $1() {
                this.v = this.__init_v__();
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public String next() {
                return null;
            }

            private int __init_v__() {
                return SuperclassFieldFoo.this.value;
            }
        }
        return new $1().v;
    }

    public static int test() {
        return new SuperclassFieldFoo().getValue();
    }

    public SuperclassFieldFoo() {
        super();
    }
}