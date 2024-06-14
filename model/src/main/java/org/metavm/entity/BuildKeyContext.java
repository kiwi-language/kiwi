package org.metavm.entity;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;

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

//    public Type getJavaType(org.metavm.object.type.Type type) {
//        return identityContext.getJavaType(type);
//    }

}
