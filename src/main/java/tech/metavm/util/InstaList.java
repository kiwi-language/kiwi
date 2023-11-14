package tech.metavm.util;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Objects;

public class InstaList<E> implements Iterable<E> {

    private static final InstaList<?> EMPTY = new InstaList<>(null, null);

    public static <E> InstaList<E> empty() {
        //noinspection unchecked
        return (InstaList<E>) EMPTY;
    }

    public static <E> InstaList<E> of(E e) {
        return new InstaList<>(e, empty());
    }

    public static <E> InstaList<E> of(E e1, E e2) {
        return new InstaList<>(e1, of(e2));
    }

    public static <E> InstaList<E> of(E e1, E e2, E e3) {
        return new InstaList<>(e1, of(e2, e3));
    }

    public static <E> InstaList<E> of(E e1, E e2, E e3, E e4) {
        return new InstaList<>(e1, of(e2, e3, e4));
    }

    public static <E> InstaList<E> of(E e1, E e2, E e3, E e4, E e5) {
        return new InstaList<>(e1, of(e2, e3, e4, e5));
    }

    private final E head;
    // tail == null implies an empty list
    @Nullable
    private final InstaList<E> tail;

    private InstaList(E head, @Nullable InstaList<E> tail) {
        this.head = head;
        this.tail = tail;
    }

    public E getHead() {
        return head;
    }

    public @Nullable InstaList<E> getTail() {
        return tail;
    }

    public boolean isEmpty() {
        return tail == null;
    }

    public boolean isNotEmpty() {
        return tail != null;
    }

    public InstaList<E> prependList(@NotNull InstaList<? extends E> e) {
        if (e.tail == null)
            return this;
        else
            return new InstaList<>(e.head, prependList(e.tail));
    }

    public InstaList<E> reverse() {
        if (tail == null)
            return this;
        var cur = of(head);
        InstaList<E> t = tail;
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
        if(obj instanceof InstaList<?> that) {
            if(tail == null)
                return that.tail == null;
            return Objects.equals(head, that.head) && tail.equals(that.tail);
        }
        else
            return false;
    }

    public InstaList<E> prepend(E e) {
        return new InstaList<>(e, this);
    }

    public InstaList<E> append(E e) {
        if (tail == null)
            return of(e);
        else
            return new InstaList<>(head, tail.append(e));
    }

    public InstaList<E> remove(E e) {
        if (tail == null)
            return this;
        else if (Objects.equals(head, e))
            return tail;
        else
            return new InstaList<>(head, tail.remove(e));
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {

            private InstaList<E> current = InstaList.this;

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

