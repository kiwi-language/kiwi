package tech.metavm.object.meta.generic;

import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.IndexDef;
import tech.metavm.object.meta.CompositeType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import java.util.*;

public abstract class CompositeTypeContext<T extends CompositeType> {

    private final IEntityContext context;
    private final Set<T> newTypes = new IdentitySet<>();
    private final Map<String, T> persistedTypes = new HashMap<>();
    protected final IndexDef<T> indexDef;

    protected CompositeTypeContext(IEntityContext context, IndexDef<T> indexDef) {
        this.context = context;
        this.indexDef = indexDef;
    }

    public T get(List<Type> componentTypes) {
        checkComponentTypes(componentTypes);
        var match = NncUtils.find(newTypes, t -> componentTypesEquals(t.getComponentTypes(), componentTypes));
        if (match != null) {
            return match;
        }
        if (context != null && NncUtils.allMatch(componentTypes, context::isPersisted)) {
            String key = getKey(componentTypes);
            T existing;
            if ((existing = persistedTypes.get(key)) != null) {
                return existing;
            }
            existing = context.selectByUniqueKey(indexDef, key);
            if (existing != null) {
                persistedTypes.put(key, existing);
                return existing;
            }
        }
        T type = create(componentTypes);
        newTypes.add(type);
        if(context != null && context.isBindSupported()) {
            context.bind(type);
        }
        return type;
    }

    protected boolean componentTypesEquals(List<Type> types1, List<Type> types2) {
        return types1.equals(types2);
    }

    public void checkComponentTypes(List<Type> types) {
    }

    protected String getKey(List<Type> componentTypes) {
        return CompositeType.getKey(componentTypes);
    }

    public Set<T> getNewTypes() {
        return Collections.unmodifiableSet(newTypes);
    }

    public void add(T type) {
        newTypes.add(type);
    }

    protected abstract T create(List<Type> componentTypes);

}
