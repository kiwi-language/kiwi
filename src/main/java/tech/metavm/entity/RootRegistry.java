package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelMap;

public class RootRegistry {

    private static volatile DefContext DEF_CONTEXT;

    private static void ensureInitialized() {
        if(DEF_CONTEXT == null) {
            synchronized (RootRegistry.class) {
                if(DEF_CONTEXT != null) {
                    return;
                }
                DEF_CONTEXT = new DefContext(
                        o -> null,
                        new ModelMap() {
                            @Override
                            public <T> T get(Class<T> klass, Instance instance) {
                                return null;
                            }
                        }
                );
            }
        }
    }

    private static Instance getInstanceByModel(Object model) {
        return RootInstanceContext.getInstance().getEntityContext().getInstanceByModel(model);
    }

    public static Instance createInstance(Object model) {
        ensureInitialized();
        return DEF_CONTEXT.getDef(model.getClass()).newInstanceHelper(model, o -> null);
    }

    public static DefContext getDefContext() {
        ensureInitialized();
        return DEF_CONTEXT;
    }

}
