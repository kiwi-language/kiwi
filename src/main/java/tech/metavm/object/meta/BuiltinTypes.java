package tech.metavm.object.meta;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityContextFactory;

@Component
public class BuiltinTypes implements InitializingBean {


//
//    private static Type object;
//
//    private static Type bool;
//
//    private static Type int32;
//
//    private static Type int64;
//
//    private static Type string;
//
//    private static Type _double;
//
//    private static Type date;
//
//    private static Type time;

    public static Type getObject() {
        return entityContext.getObjectType();
    }

    public static Type getBool() {
        return entityContext.getBoolType();
    }

    public static Type getInt32() {
        return entityContext.getIntType();
    }

    public static Type getInt64() {
        return entityContext.getLongType();
    }

    public static Type getString() {
        return entityContext.getStringType();
    }

    public static Type getDouble() {
        return entityContext.getDoubleType();
    }

    public static Type getDate() {
        return entityContext.getDateType();
    }

    public static Type getTime() {
        return entityContext.getTimeType();
    }

    @Autowired
    private EntityContextFactory entityContextFactory;

    private static EntityContext entityContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        entityContext = entityContextFactory.newContext(-1L);
//        object = typeStore.getByCategory(TypeCategory.OBJECT, entityContext);
//        string = typeStore.getByCategory(TypeCategory.STRING, entityContext);
//        bool = typeStore.getByCategory(TypeCategory.BOOL, entityContext);
//        int32 = typeStore.getByCategory(TypeCategory.INT, entityContext);
//        int64 = typeStore.getByCategory(TypeCategory.LONG, entityContext);
//        _double = typeStore.getByCategory(TypeCategory.DOUBLE, entityContext);
//        date = typeStore.getByCategory(TypeCategory.DATE, entityContext);
//        time = typeStore.getByCategory(TypeCategory.TIME, entityContext);
    }

}
