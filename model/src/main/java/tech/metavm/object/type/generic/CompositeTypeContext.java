package tech.metavm.object.type.generic;

import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.IndexDef;
import tech.metavm.object.type.CompositeType;
import tech.metavm.object.type.Type;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

public abstract class CompositeTypeContext<T extends CompositeType> {

    private final IEntityContext context;
    private final Map<Object, T> newTypes = new HashMap<>();
    private final Map<String, T> persistedTypes = new HashMap<>();
    @Nullable
    private final CompositeTypeContext<T> parent;
    protected final IndexDef<T> indexDef;

    protected CompositeTypeContext(IEntityContext context, IndexDef<T> indexDef, @Nullable CompositeTypeContext<T> parent) {
        this.context = context;
        this.indexDef = indexDef;
        this.parent = parent;
    }

    public T get(List<Type> componentTypes) {
        return get(componentTypes, null);
    }

    private T getNew(List<Type> componentTypes) {
//        try(var ignored = ContextUtil.getProfiler().enter("CompositeTypeContext.getNew")) {
            if (parent != null) {
                var t = parent.getNew(componentTypes);
                if (t != null)
                    return t;
            }
//            return NncUtils.find(newTypes, t -> componentTypesEquals(t.getComponentTypes(), componentTypes));
            return newTypes.get(getMemKey(componentTypes));
//        }
    }

    public T get(List<Type> componentTypes, Long tmpId) {
        checkComponentTypes(componentTypes);
        var existing = getNew(componentTypes);
        if (existing != null)
            return existing;
        if (context != null && NncUtils.allMatch(componentTypes, context::isPersisted)) {
            String key = getKey(componentTypes);
            if ((existing = persistedTypes.get(key)) != null) {
                return existing;
            }
            existing = context.selectFirstByKey(indexDef, key);
            if (existing != null) {
                persistedTypes.put(key, existing);
                return existing;
            }
        }
        T type = create(componentTypes, tmpId);
        newTypes.put(getMemKey(componentTypes), type);
        if(context != null && context.isBindSupported()) {
            context.bind(type);
        }
        return type;
    }

    protected Object getMemKey(List<Type> types) {
        return types;
    }

    public void checkComponentTypes(List<Type> types) {
    }

    protected String getKey(List<Type> componentTypes) {
        return CompositeType.getKey(componentTypes);
    }

    public Set<T> getNewTypes() {
        return new IdentitySet<>(newTypes.values());
    }

    public void addNewType(T type) {
        newTypes.put(getMemKey(type.getComponentTypes()), type);
    }

    protected abstract T create(List<Type> componentTypes, Long tmpId);

}
