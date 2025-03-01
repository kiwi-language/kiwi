package org.metavm.compiler.util;


import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Slf4j
public class MultiMap<K, V> {

    private static final int INITIAL_SIZE = 16;

    @SuppressWarnings("rawtypes")
    private static final Entry nil = new Entry<>(null, null, null);

    private static  <K, V> Entry<K, V> nil() {
        //noinspection unchecked
        return (Entry<K, V>) nil;
    }

    @SuppressWarnings("unchecked")
    private Entry<K, V>[] table = new Entry[INITIAL_SIZE];
    private int mask = table.length - 1;
    private int size;

    @SuppressWarnings("rawtypes")
    private static final Iterator emptyIterator = new Iterator<>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }
    };

    public @Nullable V getFirst(K key) {
        var i = index(key);
        var e = table[i];
        if (e == null)
            return null;
        else
            return e.value;
    }

    public @Nullable V getFirst(K key, Predicate<V> filter) {
        var i = index(key);
        var e = table[i];
        if (e == null)
            return null;
        else {
            while (e.next != null && !filter.test(e.value))
                e = e.next;
            return e.next != null ? e.value : null;
        }
    }

    public Iterable<V> get(K key, Predicate<V> filter) {
        var i = index(key);
        var e = table[i];
        if (e == null || e.next == null) {
            return List.nil();
        }

        return new Iterable<>() {
            @NotNull
            @Override
            public Iterator<V> iterator() {
                return new Iterator<>() {

                    private Entry<K, V> entry = e;

                    @Override
                    public boolean hasNext() {
                        return entry.next != null;
                    }

                    @Override
                    public V next() {
                        if (!hasNext())
                            throw new NoSuchElementException();
                        var v = entry.value;
                        doNext();
                        return v;
                    }

                    private void doNext() {
                        var e = entry.next;
                        while (e.next != null && !filter.test(e.value))
                            e = e.next;
                        entry = e;
                    }

                };
            }
        };
    }

    public void put(K key, V value) {
        ensureCapacity();
        size++;
        var i = index(key);
        var next = table[i];
        table[i] = new Entry<>(key, value, next != null ? next : nil());
    }

    private void ensureCapacity() {
        if (size * 3 >= table.length * 2)
            grow(table.length * 2);
    }

    public boolean remove(K key, V value) {
        var i = index(key);
        var e = table[i];
        if (e != null) {
            var e1 = e.remove(value);
            if (e1 != e) {
                table[i] = e1;
                size--;
                return true;
            }
        }
        return false;
    }

    public int size() {
        return size;
    }

    private void grow(int capacity) {
        var oldTable = table;
        //noinspection unchecked
        table = new Entry[capacity];
        mask = table.length - 1;
        for (var entry : oldTable) {
            if (entry != null && entry.next != null) {
                table[index(entry.key)] = entry;
            }
        }
    }

    private int index(K key) {
        var h = key.hashCode() & mask;
        var i = h & mask;
        var s = mask - (h + (h >> 16) << 1);
        var d = -1;
        for (;;) {
            var e = table[i];
            if (e == null)
                return d >= 0 ? d : i;
            else if (e.next == null) {
                if (d < 0)
                    d = i;
            }
            else if (e.key.equals(key))
                return i;
            i = (i + s) & mask;
        }
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        for (var e : table) {
            if (e != null) {
                var e1 = e;
                while (e1.next != null) {
                    action.accept(e1.key, e1.value);
                    e1 = e1.next;
                }
            }
        }
    }

    public void putAll(MultiMap<K, V> that) {
        size += that.size;
        var cap = table.length;
        if (size * 3 >= cap * 2) {
            do {
                cap *= 2;
            } while (size * 3 >= cap * 2);
            grow(cap);
        }
        for (var entry : that.table) {
            if (entry != null && entry.next != null) {
                var i = index(entry.key);
                var e = table[i];
                if (e == null)
                    table[i] = entry;
                else
                    table[i] = entry.concat(e);
            }
        }
    }

    public void removeAll(MultiMap<K, V> that) {
        size -= that.size;
        for (var entry : that.table) {
            if (entry != null && entry.next != null) {
                var i = index(entry.key);
                var e = table[i];
                if (e == entry)
                    table[i] = nil();
                else if (e != null && e.next != null) {
                    var e1 = entry;
                    do {
                        e = e.remove(e1.value);
                        e1 = e1.next;
                    } while (e1.next != null && e.next != null);
                    table[i] = e;
                }
            }
        }
    }


    @Override
    public String toString() {
        var sb = new StringBuilder("{");
        for (var e : table) {
            if (e != null && e != nil) {
                if (sb.length() > 1) sb.append(", ");
                sb.append(e.key).append(": [")
                        .append(e.value);
                var e1 = e.next;
                while (e1 != nil) {
                    sb.append(", ").append(e1.value);
                    e1 = e1.next;
                }
                sb.append("]");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private record Entry<K, V>(K key, V value, Entry<K, V> next) {

        Entry<K, V> remove(V value) {
            if (next == null)
                return this;
            if (this.value == value)
                return next;
            else {
                var n = next.remove(value);
                if (n != next)
                    return new Entry<>(key, value, n);
                else
                    return this;
            }
        }

        Entry<K, V> concat(Entry<K, V> that) {
            if (that.next == null)
                return this;
            if (next == null)
                return that;
            else
                return new Entry<>(key, value, next.concat(that));
        }

    }

}
