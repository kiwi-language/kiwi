package tech.metavm.entity;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;

public class BuildKeyContext {

    private final IdentityContext identityContext;
    private final IdentityHashMap<Object, ModelIdentity> identityMap = new IdentityHashMap<>();

    public BuildKeyContext(IdentityContext identityContext) {
        this.identityContext = identityContext;
    }

    public String getModelName(Object object, @Nullable Object current) {
        return identityContext.getModelName(object, this, current);
    }

    public void addIdentity(Object object, ModelIdentity identity) {
        identityMap.put(object, identity);
    }

    public IdentityHashMap<Object, ModelIdentity> getIdentityMap() {
        return identityMap;
    }

//    public Type getJavaType(tech.metavm.object.type.Type type) {
//        return identityContext.getJavaType(type);
//    }

}
