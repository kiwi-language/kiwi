package org.metavm.compiler.util;

import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.syntax.PrefixExpr;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class List<E> extends AbstractCollection<E> implements java.util.List<E> {

    private final E head;
    // Immutable except in Builder
    private List<E> tail;

    public List(E head, List<E> tail) {
        this.head = head;
        this.tail = tail;
    }

    public List<E> prepend(E e) {
        return new List<>(e, this);
    }

    public List<E> append(E e) {
        var builder = builder(this);
        builder.append(e);
        return builder.build();
    }

    public List<E> concat(List<? extends E> that) {
        var b = builder(this);
        b.concat(into(that));
        return b.build();
    }

    @Override
    public int size() {
        int len = 0;
        var t = tail;
        while (t != null) {
            len++;
            t = t.tail;
        }
        return len;
    }

    public E head() {
        return head;
    }

    public List<E> tail() {
        return tail;
    }

    @Override
    public boolean isEmpty() {
        return tail == null;
    }

    public boolean nonEmpty() {
        return tail != null;
    }

    @Override
    public boolean contains(Object o) {
        var l = this;
        while (l.tail != null) {
            if (Objects.equals(l.head, o))
                return true;
            l = l.tail;
        }
        return false;
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {

            private List<E> l = List.this;

            @Override
            public boolean hasNext() {
                return l.tail != null;
            }

            @Override
            public E next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                var e = l.head;
                l = l.tail;
                return e;
            }
        };
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return toArray(new Object[size()]);
    }

    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        var l = this;
        var i = 0;
        while (l.tail != null && i < a.length) {
            //noinspection unchecked
            a[i++] = (T) l.head;
            l = l.tail;
        }
        if (l.tail == null) {
            return a;
        }
        //noinspection unchecked
        var newArray = (T[]) Array.newInstance(a.getClass().getComponentType(), size());
        return toArray(newArray);
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E get(int index) {
        var l = this;
        var i = 0;
        while (i < index) {
            l = l.tail;
            i++;
            if (l.tail == null)
                throw new IndexOutOfBoundsException();
        }
        return l.head;
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        var i = 0;
        var l = this;
        while (l.tail != null) {
            if (l.head == null) {
                if (o == null) return i;
            }
            else if (l.head.equals(o))
                return i;
            i++;
            l = l.tail;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        var idx = -1;
        var i = 0;
        var l = this;
        while (l.tail != null) {
            if (l.head == null) {
                if (o == null) idx = i;
            }
            else if (l.head.equals(o))
                idx = i;
            i++;
            l = l.tail;
        }
        return idx;
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        var len = size();
        if (fromIndex < 0 || toIndex > len || fromIndex > toIndex)
            throw new IllegalArgumentException();
        if (toIndex == len) {
            var l = this;
            for (int i = 0; i < fromIndex; i++) {
                l = l.tail;
            }
            return l;
        } else {
            var a = new Object[toIndex - fromIndex];
            var l = this;
            for (int i = 0; i < toIndex; i++) {
                if (i >= fromIndex)
                    a[i - fromIndex] = l.head;
                l = l.tail;
                assert l.tail != null;
            }
            List<E> l1 = nil();
            for (int i = a.length - 1; i >= 0 ; i--) {
                //noinspection unchecked
                l1 = new List<>((E) a[i], l1);
            }
            return l1;
        }
    }

    public @Nullable E find(Predicate<? super E> filter) {
        return find0(filter).head;
    }

    public Iterator<E> findAll(Predicate<? super E> filter) {
        var first = find0(filter);
        return new Iterator<>() {

            private List<E> l = first;

            @Override
            public boolean hasNext() {
                return l.tail != null;
            }

            @Override
            public E next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                var e = l.head;
                l = l.tail.find0(filter);
                return e;
            }
        };
    }

    private List<E> find0(Predicate<? super E> filter) {
        if (tail == null)
            return this;
        if (filter.test(head))
            return this;
        return tail.find0(filter);
    }

    public <R> List<R> map(Function<? super E, ? extends R> mapper) {
        Builder<R> b = builder();
        var l = this;
        while (l.tail != null) {
            b.append(mapper.apply(l.head));
            l = l.tail;
        }
        return b.build();
    }

    public String join(String delimiter) {
        if (tail == null)
            return "";
        var sb = new StringBuilder(Objects.toString(head));
        var l = tail;
        while (l.tail != null) {
            sb.append(delimiter);
            sb.append(l.head);
            l = l.tail;
        }
        return sb.toString();
    }

    public List<E> delete(E e) {
       if (tail == null)
           return this;
       if (Objects.equals(head, e))
           return tail;
       else {
           var t = tail.delete(e);
           if (t != tail)
               return new List<>(head, t);
           else
               return this;
       }
    }

    public List<E> filter(Predicate<E> filter) {
        if (tail == null)
            return this;
        var t = tail.filter(filter);
        if (filter.test(head)) {
            if (t == tail)
                return this;
            else
                return new List<>(head, t);
        }
        else
            return t;
    }

    @Override
    public List<E> reversed() {
        if (tail == null || tail.tail == null)
            return this;
        var l = new List<>(head, nil());
        var t = tail;
        do {
            l = new List<>(t.head, l);
            t = t.tail;
        } while (t.tail != null);
        return l;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof java.util.List<?> that) {
            var it = that.iterator();
            var l = this;
            while (l.tail != null && it.hasNext()) {
                if (!Objects.equals(l.head, it.next()))
                    return false;
                l = l.tail;
            }
            return l.tail == null && !it.hasNext();
        }
        else
            return false;
    }

    public boolean matches(List<E> that, BiPredicate<E, E> filter) {
        var l1 = this;
        var l2 = that;
        while (l1.tail != null && l2.tail != null) {
           if (!filter.test(l1.head, l2.head))
               return false;
           l1 = l1.tail;
           l2 = l2.tail;
        }
        return l1.tail == null && l2.tail == null;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        var l = this;
        while (l.tail != null) {
            hashCode = 31 * hashCode + (l.head == null ? 0 : l.head.hashCode());
            l = l.tail;
        }
        return hashCode;
    }

    public boolean anyMatch(Predicate<? super E> filter) {
        for (var l = this; l.nonEmpty(); l = l.tail) {
            if (filter.test(l.head))
                return true;
        }
        return false;
    }

    public boolean allMatch(Predicate<? super  E> filter) {
        for (var l = this; l.nonEmpty(); l = l.tail) {
            if (!filter.test(l.head))
                return false;
        }
        return true;
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        if (tail == null)
            return;
        action.accept(head);
        tail.forEach(action);
    }

    public void forEachBackwards(Consumer<? super E> action) {
        if (tail == null)
            return;
        tail.forEachBackwards(action);
        action.accept(head);
    }

    // Statics

    private static final List<?> nil = new List<>(null, null);

    public static <E> List<E> nil() {
        //noinspection unchecked
        return (List<E>) nil;
    }

    public static <E> List<E> of() {
        return nil();
    }
    public static <E> List<E> of(E e) {
        return new List<>(e, nil());
    }

    public static <E> List<E> of(E e1, E e2) {
        return new List<>(e1, new List<>(e2, nil()));
    }

    public static <E> List<E> of(E e1, E e2, E e3) {
        return new List<>(e1, new List<>(e2, new List<>(e3, nil())));
    }

    @SafeVarargs
    public static <E> List<E> of(E e1, E e2, E e3, E...rest) {
        return new List<>(e1, new List<>(e2, new List<>(e3, from(rest))));
    }

    public static <E> List<E> of(E[] array) {
        var builder = List.<E>builder();
        for (E e : array) {
            builder.append(e);
        }
        return builder.build();
    }

    public static <E> List<E> from(E[] a) {
        List<E> l = nil();
        for (int i = a.length - 1; i >= 0; i--) {
            l = new List<>(a[i], l);
        }
        return l;
    }

    public static <E> List<E> from(Collection<? extends E> c) {
        var a = c.toArray();
        List<E> list = nil();
        for (int i = a.length - 1; i >= 0; i--) {
            //noinspection unchecked
            list = list.prepend((E) a[i]);
        }
        return list;
    }

    public static <E> List<E> from(java.util.List<? extends E> list) {
        Builder<E> builder = builder();
        for (E e : list) {
            builder.append(e);
        }
        return builder.build();
    }

    public static <T, E> List<E> from(java.util.List<T> list, Function<T, E> mapper) {
        Builder<E> builder = builder();
        for (T t : list) {
            builder.append(mapper.apply(t));
        }
        return builder.build();
    }

    public static <E extends R, R> List<R> into(List<E> list) {
        //noinspection unchecked
        return (List<R>) list;
    }

    public static <E extends Comparable<E>> int compare(List<E> list1, List<E> list2) {
        return compare(list1, list2, Comparable::compareTo);
    }

    public static <E> int compare(List<E> list1, List<E> list2, Comparator<E> comparator) {
        var l1 = list1;
        var l2 = list2;
        while (l1.tail != null && l2.tail != null) {
            var r = comparator.compare(l1.head, l2.head);
            if (r != 0)
                return r;
            l1 = l1.tail;
            l2 = l2.tail;
        }
        if (l1.tail != null)
            return 1;
        if (l2.tail != null)
            return -1;
        return 0;
    }

    public static <E extends Comparable<E>> boolean isSorted(List<E> list) {
        return isSorted(list, Comparable::compareTo);
    }

    public static <E> boolean isSorted(List<E> list, Comparator<E> comparator) {
        if (list.tail == null)
            return true;
        var h = list.head;
        var l = list.tail;
        while (l.tail != null) {
            if (comparator.compare(h, l.head) > 0)
                return false;
            h = l.head;
            l = l.tail;
        }
        return true;
    }

    public static <E> Builder<E> builder() {
        return new Builder<>();
    }

    public static <E> Builder<E> builder(List<? extends E> list) {
        var b = new Builder<E>();
        List<E> l = into(list);
        while (l.tail != null) {
            b.append(l.head);
            l = l.tail;
        }
        return b;
    }

    public static class Builder<E> {
        private List<E> list = List.nil();
        private List<E> last = List.nil();
        private boolean built;

        private Builder() {
        }

        public void append(E e) {
            Utils.require(!built);
            if (list.isEmpty())
                list = last = List.of(e);
            else
                last = last.tail = List.of(e);
        }

        public  List<E> build() {
            Utils.require(!built);
            built = true;
            return list;
        }

        public void concat(List<E> that) {
            if (that.isEmpty())
                return;
            Utils.require(!built);
            last.tail = that;
            var l = that;
            while (l.tail.nonEmpty())
                l = l.tail;
            last = l;
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }

        public boolean nonEmpty(){
            return list.nonEmpty();
        }

        public void forEach(Consumer<? super E> action) {
            list.forEach(action);
        }

    }

}
