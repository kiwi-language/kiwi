package org.metavm.util;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ObjectPool<T> {

    // Concurrent queue to hold pooled objects
    private final ConcurrentLinkedQueue<T> pool;
    private final int maxSize;
    private final AtomicInteger currentSize;
    private final Supplier<T> creator;

    public ObjectPool(int maxSize, Supplier<T> creator) {
        this.maxSize = maxSize;
        this.pool = new ConcurrentLinkedQueue<>();
        this.currentSize = new AtomicInteger(0);
        this.creator = creator;
    }

    // Borrow an object from the pool
    public T borrowObject() {
        // Try to take an object from the pool (queue)
        T connection = pool.poll();

        // If the pool is empty, create a new object (if maxSize is not reached)
        if (connection == null && currentSize.get() < maxSize) {
            currentSize.incrementAndGet();
            connection = creator.get();
        }

        return connection;
    }

    // Return an object to the pool
    public void returnObject(T connection) {
        // Add the object back to the pool (queue)
        pool.offer(connection);
    }

    // Size of the pool
    public int getSize() {
        return currentSize.get();
    }

}
