package tech.metavm.entity;

import tech.metavm.dto.RefDTO;
import tech.metavm.util.IdentitySet;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SerializeContext implements Closeable {

    public static final ThreadLocal<SerializeContext> THREAD_LOCAL = new ThreadLocal<>();

    private final Map<Object, Long> tmpIdMap = new HashMap<>();
    private final Set<Object> visited = new IdentitySet<>();
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

    public Long getTmpId(Object model) {
        if(model instanceof Entity entity) {
            if(entity.getId() != null) {
                return null;
            }
            else if(entity.getTmpId() != null) {
                return entity.getTmpId();
            }
        }
        return tmpIdMap.computeIfAbsent(model, k -> nextTmpId++);
    }

    public RefDTO getRef(Object model) {
        if (model instanceof Identifiable identifiable && identifiable.getId() != null) {
            return new RefDTO(identifiable.getId(), null);
        } else {
            return new RefDTO(null, getTmpId(model));
        }
    }

    @Override
    public void close() {
        if (--level <= 0) THREAD_LOCAL.remove();
    }
}
