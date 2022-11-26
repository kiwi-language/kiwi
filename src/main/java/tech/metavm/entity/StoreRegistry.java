package tech.metavm.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StoreRegistry {

    private final Map<Class<?>, ModelStore<?>> storeMap = new HashMap<>();

    public <T extends Model> ModelStore<T> getStore(Class<T> type) {
        if(type == null) {
            throw new NullPointerException("category required");
        }
        Class<?> t = type;
        ModelStore<?> store;
        while(t != Object.class && t != null) {
            if((store = storeMap.get(t)) != null) {
                return (ModelStore<T>) store;
            }
            t = t.getSuperclass();
        }
        throw new RuntimeException("No store found for category: " + type.getName());
    }

    @Autowired
    public void setStores(List<ModelStore<?>> stores) {
        for (ModelStore<?> store : stores) {
            storeMap.put(store.getType(), store);
        }
    }

}
