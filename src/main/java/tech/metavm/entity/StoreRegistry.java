package tech.metavm.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StoreRegistry {

    private final Map<Class<?>, EntityStore<?>> storeMap = new HashMap<>();

    public <T extends Entity> EntityStore<T> getStore(Class<T> type) {
        if(type == null) {
            throw new NullPointerException("category required");
        }
        Class<?> t = type;
        EntityStore<?> store;
        while(t != Object.class && t != null) {
            if((store = storeMap.get(t)) != null) {
                return (EntityStore<T>) store;
            }
            t = t.getSuperclass();
        }
        throw new RuntimeException("No store found for category: " + type.getName());
    }

    @Autowired
    public void setStores(List<EntityStore<?>> stores) {
        for (EntityStore<?> store : stores) {
            storeMap.put(store.getEntityType(), store);
        }
    }

}
