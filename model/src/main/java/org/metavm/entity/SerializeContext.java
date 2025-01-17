package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.type.Klass;
import org.metavm.util.ContextUtil;
import org.metavm.util.IdentitySet;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SerializeContext implements Closeable {

    public static final ThreadLocal<SerializeContext> THREAD_LOCAL = new ThreadLocal<>();

    private final Map<Object, Long> tmpIdMap = new HashMap<>();
    private final Set<Object> visited = new IdentitySet<>();
    private int level;
    private final Set<Klass> writingCodeTypes = new IdentitySet<>();

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

    public void addWritingCodeType(Klass type) {
        this.writingCodeTypes.add(type);
    }

    public Long getTmpId(Object model) {
        Objects.requireNonNull(model);
        if (model instanceof Entity entity) {
            if (entity.tryGetId() != null)
                return null;
            else if (entity.getTmpId() != null)
                return entity.getTmpId();
        }
        return tmpIdMap.computeIfAbsent(model, k -> ContextUtil.nextTmpId());
    }

    public String getStringId(Object model) {
        if (model instanceof Entity entity && entity.getStringId() != null) {
            return entity.getStringId();
        } else {
            return TmpId.of(getTmpId(model)).toString();
        }
    }

    public Id getId(Object obj) {
        Id id;
        if (obj instanceof Entity entity && (id = entity.tryGetId()) != null)
            return id;
        else
            return TmpId.of(getTmpId(obj));
    }

    public SerializeContext includingValueType(boolean includingValueType) {
        return this;
    }

    public SerializeContext includeNodeOutputType(boolean includingNodeOutputType) {
        return this;
    }

    public SerializeContext includeBuiltin(boolean includeBuiltin) {
        return this;
    }

    public SerializeContext includingCode(boolean includingCode) {
        return this;
    }

    @Override
    public void close() {
        if (--level <= 0) THREAD_LOCAL.remove();
    }
}
