package org.metavm.entity;

import java.util.HashMap;
import java.util.Map;

class MockIdAllocator {

    private final Map<Object, Long> code2id = new HashMap<>();
    private long nextId = 1L;

    long getId(Object obj) {
        return code2id.computeIfAbsent(obj, id -> nextId++);
    }
}
