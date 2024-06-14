package org.metavm.autograph.mocks;

import javax.annotation.Nullable;
import java.util.Collection;

public class AstQueue<T> {

    @Nullable
    private Node<T> head;

    public void offer(T value) {
        if (head == null) {
            head = new Node<>(value);
        } else {
            head.insertBefore(new Node<>(value));
        }
    }

    public @Nullable T poll() {
        if (head == null) {
            return null;
        }
        var last = head.getPrev();
        if ((last == head)) {
            head = null;
        } else {
            last.unlink();
        }
        return last.getValue();
    }

    public void offerAll(Collection<? extends T> coll) {
        for (T t : coll) {
            offer(t);
        }
    }

    public boolean isEmpty() {
        return head == null;
    }

    private static class Node<T> {

        private final T value;
        private Node<T> prev = this;
        private Node<T> next = this;

        public Node(T value) {
            this.value = value;
        }

        public Node<T> getPrev() {
            return prev;
        }

        public void unlink() {
            prev.next = next;
            next.prev = prev;
            this.prev = this;
            this.next = this;
        }

        void insertBefore(Node<T> that) {
            prev.next = that;
            that.prev = prev;
            that.next = this;
            this.prev = that;
        }

        public T getValue() {
            return value;
        }
    }


}
