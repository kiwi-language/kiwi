package org.metavm;

import org.metavm.util.Base;

import java.io.IOException;
import java.sql.SQLException;

public class Lab {

    static class Foo extends Base {

        Foo(int num) {
            super(num);
        }

        @Override
        public int getInc() {
            return 0;
        }

        Base __Base__() {
            return new Base(10) {
                @Override
                public int getInc() {
                    return Foo.this.getInc();
                }
            };
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
    }


}
