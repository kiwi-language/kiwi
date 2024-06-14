package org.metavm.util;

import java.util.LinkedList;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockSupport {

    private static ThreadLocal<Queue<Lock>> ACQUIRED_LOCKS_TL = new ThreadLocal<>() ;
    private static final Map<Long, ReadWriteLock> LOCK_MAP = new ConcurrentHashMap<>();

    public static void lockForRead(Collection<Long> ids) {
        List<Long> idList = new ArrayList<>(ids);
        idList.sort(Long::compare);
        idList.forEach(LockSupport::lockForRead);
    }

    public static void lockForWrite(Collection<Long> ids) {
        List<Long> idList = new ArrayList<>(ids);
        idList.sort(Long::compare);
        idList.forEach(LockSupport::lockForWrite);
    }

    public static void unlockAll() {
        Queue<Lock> acquiredLocks = ACQUIRED_LOCKS_TL.get();
        if(NncUtils.isNotEmpty(acquiredLocks)) {
            for (Lock acquiredLock : acquiredLocks) {
                acquiredLock.unlock();
            }
        }
    }

    private static void lockForRead(long id) {
        ReadWriteLock lock = LOCK_MAP.computeIfAbsent(id, k -> new ReentrantReadWriteLock());
        doLock(id, lock.readLock());
    }

    private static void lockForWrite(long id) {
        ReadWriteLock lock = LOCK_MAP.computeIfAbsent(id, k -> new ReentrantReadWriteLock());
        doLock(id, lock.writeLock());
    }

    private static void doLock(long id, Lock lock) {
        try {
            boolean locked = lock.tryLock(1000L, TimeUnit.MILLISECONDS);
            if(!locked) {
                throw new InternalException("Timeout while trying to lock instance " + id);
            }
            Queue<Lock> acquiredLocks = ACQUIRED_LOCKS_TL.get();
            if(acquiredLocks == null) {
                acquiredLocks = new LinkedList<>();
                ACQUIRED_LOCKS_TL.set(acquiredLocks);
            }
            acquiredLocks.offer(lock);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
