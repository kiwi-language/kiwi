//package tech.metavm.entity;
//
//import tech.metavm.object.instance.Instance;
//
//public class RootRegistry {
//
//    private static volatile DefContext DEF_CONTEXT;
//
//    private static void ensureInitialized() {
//        if(DEF_CONTEXT == null) {
//            synchronized (RootRegistry.class) {
//                if(DEF_CONTEXT != null) {
//                    return;
//                }
//                DEF_CONTEXT = new DefContext(o -> null);
//            }
//        }
//    }
//
//    private static Instance getInstanceByModel(Object model) {
//        return RootInstanceContext.getInstance().getEntityContext().getInstance(model);
//    }
//
//    public static Instance createInstance(Object model) {
//        ensureInitialized();
//        return DEF_CONTEXT.getDef(model.getClass()).createInstanceHelper(model, DEF_CONTEXT);
//    }
//
//    public static ModelDef<?,?> getDef(Class<?> klass) {
//        ensureInitialized();
//        return DEF_CONTEXT.getDef(klass);
//    }
//
//    public static DefContext getDefContext() {
//        ensureInitialized();
//        return DEF_CONTEXT;
//    }
//
//}
