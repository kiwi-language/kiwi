package tech.metavm.spoon;

import java.util.Objects;

public class EqualsLab {

    public boolean eq(Object a, Object b) {
        return a.equals(b);
    }

    public boolean ne(Object a, Object b) {
        return !a.equals(b);
    }

    public boolean oeq(Object a, Object b) {
        return Objects.equals(a, b);
    }

    public boolean one(Object a, Object b) {
        return !Objects.equals(a, b);
    }

}
