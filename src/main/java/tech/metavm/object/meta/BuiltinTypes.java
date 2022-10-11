package tech.metavm.object.meta;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityContextFactory;

@Component
public class BuiltinTypes implements InitializingBean {

    private static Type object;

    private static Type bool;

    private static Type int32;

    private static Type int64;

    private static Type string;

    private static Type _double;

    private static Type date;

    private static Type time;

    public static Type getObject() {
        return object;
    }

    public static Type getBool() {
        return bool;
    }

    public static Type getInt32() {
        return int32;
    }

    public static Type getInt64() {
        return int64;
    }

    public static Type getString() {
        return string;
    }

    public static Type getDouble() {
        return _double;
    }

    public static Type getDate() {
        return date;
    }

    public static Type getTime() {
        return time;
    }

    @Autowired
    private TypeStore typeStore;

    @Autowired
    private EntityContextFactory entityContextFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        EntityContext entityContext = entityContextFactory.newContext(-1L);
        object = typeStore.getByCategory(TypeCategory.OBJECT, entityContext);
        string = typeStore.getByCategory(TypeCategory.STRING, entityContext);
        bool = typeStore.getByCategory(TypeCategory.BOOL, entityContext);
        int32 = typeStore.getByCategory(TypeCategory.INT32, entityContext);
        int64 = typeStore.getByCategory(TypeCategory.INT64, entityContext);
        _double = typeStore.getByCategory(TypeCategory.DOUBLE, entityContext);
        date = typeStore.getByCategory(TypeCategory.DATE, entityContext);
        time = typeStore.getByCategory(TypeCategory.TIME, entityContext);
    }

}
