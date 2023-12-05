package tech.metavm.util;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Objects;

public class MvList<E> implements Iterable<E> {

    private static final MvList<?> EMPTY = new MvList<>(null, null);

    public static <E> MvList<E> empty() {
        //noinspection unchecked
        return (MvList<E>) EMPTY;
    }

    public static <E> MvList<E> of(E e) {
        return new MvList<>(e, empty());
    }

    public static <E> MvList<E> of(E e1, E e2) {
        return new MvList<>(e1, of(e2));
    }

    public static <E> MvList<E> of(E e1, E e2, E e3) {
        return new MvList<>(e1, of(e2, e3));
    }

    public static <E> MvList<E> of(E e1, E e2, E e3, E e4) {
        return new MvList<>(e1, of(e2, e3, e4));
    }

    public static <E> MvList<E> of(E e1, E e2, E e3, E e4, E e5) {
        return new MvList<>(e1, of(e2, e3, e4, e5));
    }

    private final E head;
    // tail == null implies an empty list
    @Nullable
    private final MvList<E> tail;

    private MvList(E head, @Nullable MvList<E> tail) {
        this.head = head;
        this.tail = tail;
    }

    public E getHead() {
        return head;
    }

    public @Nullable MvList<E> getTail() {
        return tail;
    }

    public boolean isEmpty() {
        return tail == null;
    }

    public boolean isNotEmpty() {
        return tail != null;
    }

    public MvList<E> prependList(@NotNull MvList<? extends E> e) {
        if (e.tail == null)
            return this;
        else
            return new MvList<>(e.head, prependList(e.tail));
    }

    public MvList<E> reverse() {
        if (tail == null)
            return this;
        var cur = of(head);
        MvList<E> t = tail;
        while (t.tail != null) {
            cur = cur.prepend(t.head);
            t = t.tail;
        }
        return cur;
    }

    @Override
    public int hashCode() {
        var t = this;
        int h = 1;
        while (t.tail != null) {
            h = h * 31 + t.head.hashCode();
            t = t.tail;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MvList<?> that) {
            if(tail == null)
                return that.tail == null;
            return Objects.equals(head, that.head) && tail.equals(that.tail);
        }
        else
            return false;
    }

    public MvList<E> prepend(E e) {
        return new MvList<>(e, this);
    }

    public MvList<E> append(E e) {
        if (tail == null)
            return of(e);
        else
            return new MvList<>(head, tail.append(e));
    }

    public MvList<E> remove(E e) {
        if (tail == null)
            return this;
        else if (Objects.equals(head, e))
            return tail;
        else
            return new MvList<>(head, tail.remove(e));
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {

            private MvList<E> current = MvList.this;

            @Override
            public boolean hasNext() {
                return current.isNotEmpty();
            }

            @Override
            public E next() {
                var t = current;
                if(t.tail == null)
                    throw new IndexOutOfBoundsException();
                current = t.tail;
                return t.head;
            }
        };
    }

    @Override
    public String toString() {
        if(tail == null)
            return "[]";
        StringBuilder builder = new StringBuilder("[").append(head);
        var t = tail;
        while (t.tail != null) {
            builder.append(",").append(t.head);
            t = t.tail;
        }
        builder.append(']');
        return builder.toString();
    }
}

