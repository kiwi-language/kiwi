package tech.metavm.entity;

import tech.metavm.object.meta.Type;
import tech.metavm.util.InternalException;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

public class SerializeContext implements Closeable {

    public static final ThreadLocal<SerializeContext> THREAD_LOCAL = new ThreadLocal<>();

    private final Map<Type, Long> tmpIdMap = new HashMap<>();
    private long nextTmpId = 1;
    private int level;

    private SerializeContext() {
    }

    public static SerializeContext enter() {
        SerializeContext context = THREAD_LOCAL.get();
        if (context == null) {
            context = new SerializeContext();
            THREAD_LOCAL.set(context);
        }
        context.level++;
        return context;
    }

    public boolean isVisited(Type type) {
        return tmpIdMap.containsKey(type);
    }

    public long visit(Type type) {
        if(isVisited(type)) throw new InternalException(type + " is already visited");
        long tmpId = nextTmpId++;
        tmpIdMap.put(type, tmpId);
        return tmpId;
    }

    public long getTmpId(Type type) {
        if(!isVisited(type)) throw new InternalException(type + " has not been visited");
        return tmpIdMap.get(type);
    }

    @Override
    public void close() {
        if (--level <= 0) THREAD_LOCAL.remove();
    }
}
