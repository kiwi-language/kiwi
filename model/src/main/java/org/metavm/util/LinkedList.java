package org.metavm.util;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.NoProxy;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class LinkedList<T> extends AbstractList<T> implements Deque<T> {

    private int size;
    private final Node<T> head;
    private final Node<T> tail;

    public LinkedList(){
        head = new Node<>();
        tail = new Node<>(head);
    }

    public LinkedList(Collection<T> collection) {
        this();
        addAll(collection);
    }

    @Override
    public int size() {
        beforeAccess();
        return size;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        beforeAccess();
        return listIterator();
    }

    @NotNull
    @Override
    public Iterator<T> descendingIterator() {
        beforeAccess();
        return new DescendingListItr(newIterator(head.next, 0));
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        beforeAccess();
        return newIterator(head.next, 0);
    }

    @Override
    public @NotNull ListIterator<T> listIterator(int index) {
        beforeAccess();
        if(index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }
        return newIterator(getNode(index), index);
    }

    private ListItr newIterator(final Node<T> nextNode, int nextIndex) {
        return new ListItr(nextNode, nextIndex);
    }

    @Override
    public void addFirst(T t) {
        beforeAccess();
        addNode(head, t);
    }

    @Override
    public void addLast(T t) {
        beforeAccess();
        addLast0(t);
    }

    @NoProxy
    void addLast0(T t) {
        addNode(tail.prev, t);
    }

    @Override
    public boolean offerFirst(T t) {
        addFirst(t);
        return true;
    }

    @Override
    public boolean offerLast(T t) {
        addLast(t);
        return true;
    }

    @Override
    public T removeFirst() {
        beforeAccess();
        return removeNode(firstNode());
    }

    @Override
    public T removeLast() {
        beforeAccess();
        return removeNode(lastNode());
    }

    @Override
    public T pollFirst() {
        return isEmpty() ? null : removeFirst();
    }

    @Override
    public T pollLast() {
        return isEmpty() ? null : removeLast();
    }

    @Override
    public T getFirst() {
        return firstNode().value;
    }

    @Override
    public T getLast() {
        return lastNode().value;
    }

    @Override
    public T peekFirst() {
        return isEmpty() ? null : getFirst();
    }

    @Override
    public T peekLast() {
        return isEmpty() ? null : getLast();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        beforeAccess();
        Node<T> node = findNode(v -> Objects.equals(v, o));
        if(node != null) {
            removeNode(node);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        beforeAccess();
        Node<T> node = findNodeBackwards(n -> Objects.equals(n.value, o));
        if(node != null) {
            removeNode(node);
            return true;
        }
        return false;
    }

    public boolean add(T value) {
        addLast(value);
        return true;
    }

    @Override
    public boolean offer(T t) {
        addLast(t);
        return true;
    }

    @Override
    public T remove() {
        return removeFirst();
    }

    @Override
    public T poll() {
        return isEmpty() ? null : removeFirst();
    }

    @Override
    public T element() {
        return getFirst();
    }

    @Override
    public T peek() {
        return isEmpty() ? null : getFirst();
    }

    @Override
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    private boolean removeNodeIf(Node<T> node, Predicate<T> test) {
        if(test.test(node.value)) {
            removeNode(node);
            return true;
        }
        return false;
    }

    protected T removeNode(Node<T> node) {
        onRemove(node);
        node.unlink();
        modCount++;
        size--;
        return node.value;
    }

    private Node<T> firstNode() {
        if(isEmpty()) {
            throw new NoSuchElementException();
        }
        return head.next;
    }

    private Node<T> lastNode() {
        if(isEmpty()) {
            throw new NoSuchElementException();
        }
        return tail.prev;
    }

    protected void beforeAccess() {}

    @NoProxy
    protected void onAdd(@SuppressWarnings("unused") Node<T> node) {}

    protected void onRemove(@SuppressWarnings("unused") Node<T> node) {}

    @Override
    public void push(T t) {
        addFirst(t);
    }

    @Override
    public T pop() {
        return removeFirst();
    }

    @Override
    public void clear() {
        beforeAccess();
        forEachNode(this::removeNode);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        beforeAccess();
        return addAll(tail.prev, c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        beforeAccess();
        return addAll(getNode(index), c);
    }

    private boolean addAll(Node<T> prev, Collection<? extends T> c) {
        Node<T> node = prev;
        for (T t : c) {
            node = addNode(node, t);
        }
        return true;
    }

    protected void forEachNode(Consumer<Node<T>> action) {
        Node<T> node = head.next;
        while(node != tail) {
            Node<T> next = node.next;
            action.accept(node);
            node = next;
        }
    }

    protected Node<T> findNode(Predicate<T> predicate) {
        Node<T> node = head.next;
        while(node != tail) {
            if(predicate.test(node.value)) {
                return node;
            }
            node = node.next;
        }
        return null;
    }

    public List<T> filter(Predicate<T> filter) {
        return filterAndMapNodes(
                n -> filter.test(n.value),
                Node::getValue
        );
    }

    protected List<Node<T>> filterNodes(Predicate<Node<T>> filter) {
        return filterAndMapNodes(filter, Function.identity());
    }

    protected <R> List<R> filterAndMapNodes(Predicate<Node<T>> filter, Function<Node<T>, R> mapper) {
        List<R> result = new LinkedList<>();
        Node<T> node = head.next;
        while(node != tail) {
            if(filter.test(node)) {
                result.add(mapper.apply(node));
            }
            node = node.next;
        }
        return result;
    }

    protected Node<T> findNodeBackwards(Predicate<Node<T>> action) {
        Node<T> node = tail.prev;
        while(node != head) {
            if(action.test(node)) {
                return node;
            }
            node = node.prev;
        }
        return node;
    }

    @SuppressWarnings("unused")
    private <R> List<R> mapNode(Function<Node<T>, R> mapper) {
        List<R> results = new ArrayList<>();
        forEachNode(node -> results.add(mapper.apply(node)));
        return results;
    }

    @Override
    public T get(int index) {
        beforeAccess();
        checkIndex(index);
        return getNode(index).value;
    }

    @Override
    public T set(int index, T element) {
        beforeAccess();
        checkIndex(index);
        return getNode(index).value = element;
    }

    @Override
    public void add(int index, T element) {
        beforeAccess();
        checkIndex(index);
        addNode(getNode(index-1), element);
    }

    @Override
    public T remove(int index) {
        beforeAccess();
        checkIndex(index);
        Node<T> node = getNode(index);
        removeNode(node);
        return node.value;
    }

    private Node<T> addNode(Node<T> prev, T value) {
        modCount++;
        size++;
        Node<T> node = new Node<>(value, prev);
        onAdd(node);
        return node;
    }

    private void checkIndex(int index) {
        if(index >= size || index < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    private Node<T> getNode(int index) {
        if(index > size || index < -1) {
            throw new IndexOutOfBoundsException();
        }
        int i = -1;
        Node<T> node = head;
        while (true) {
            if(i == index) {
                return node;
            }
            if(node == tail) {
                throw new InternalException("Should not reach here");
            }
            i++;
            node = node.next;
        }
    }

    protected <K> Node<T> getNode(IndexMapper<? super T, K> keyMapper, K key) {
        return findNode(n -> Objects.equals(keyMapper.apply(n), key));
    }

    protected Node<T> getNodeByValue(T value) {
        return getNode(t -> t, value);
    }

    public void addAfter(T value, T predecessor) {
        Node<T> node = getNodeByValue(predecessor);
        NncUtils.requireNonNull(node, "Value '" + predecessor + "' not found");
        addNode(node, value);
    }

    public static class Node<T> {
        private T value;
        private Node<T> prev;
        private Node<T> next;

        Node() {}

        Node(Node<T> prev) {
            this(null, prev);
        }

        Node(T value, Node<T> prev) {
            this.value = value;
            prev.insertAfter(this);
        }

        void insertAfter(Node<T> that) {
            if(next != null) {
                that.next = next;
                next.prev = that;
            }
            that.prev = this;
            next = that;
        }

        void unlink() {
            if(prev != null) {
                prev.next = next;
            }
            if(next != null) {
                next.prev = prev;
            }
            prev = next = this;
        }

        public T getValue() {
            return value;
        }
    }

    private class DescendingListItr implements Iterator<T> {

        private final ListItr itr;

        private DescendingListItr(ListItr itr) {
            this.itr = itr;
        }

        @Override
        public boolean hasNext() {
            return itr.hasPrevious();
        }

        @Override
        public T next() {
            return itr.previous();
        }
    }

    private class ListItr implements ListIterator<T> {

        private Node<T> nextNode;
        private int nextIndex;
        private int expectedModCount = modCount;
        private Node<T> lastReturned;

        public ListItr(Node<T> nextNode, int startIndex) {
            this.nextNode = nextNode;
            this.nextIndex = startIndex;
        }

        @Override
        public boolean hasNext() {
            return nextNode != tail;
        }

        @Override
        public boolean hasPrevious() {
            return nextNode.prev != head;
        }

        @Override
        public T next() {
            checkForModCount();
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturned = nextNode;
            nextNode = nextNode.next;
            nextIndex++;
            return lastReturned.value;
        }

        @Override
        public T previous() {
            checkForModCount();
            if(!hasPrevious()) {
                throw new NoSuchElementException();
            }
            nextNode = nextNode.prev;
            lastReturned = nextNode;
            nextIndex--;
            return lastReturned .value;
        }

        @Override
        public int nextIndex() {
            checkForModCount();
            return nextIndex;
        }

        @Override
        public int previousIndex() {
            checkForModCount();
            return nextIndex - 1;
        }

        @Override
        public void remove() {
            checkForModCount();
            if(lastReturned == null) {
                throw new IllegalStateException();
            }
            if(lastReturned == nextNode) {
                nextNode = lastReturned.next;
            }
            else {
                nextIndex--;
            }
            removeNode(lastReturned);
            expectedModCount++;
            lastReturned = null;
        }

        @Override
        public void set(T t) {
            checkForModCount();
            if(lastReturned == null) {
                throw new IllegalStateException();
            }
            lastReturned.value = t;
        }

        @Override
        public void add(T t) {
            checkForModCount();
            if(lastReturned == null) {
                throw new IllegalStateException();
            }
            nextNode = addNode(nextNode.prev, t);
            expectedModCount++;
            nextIndex++;
        }

        private void checkForModCount() {
            if(modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

    }

}
