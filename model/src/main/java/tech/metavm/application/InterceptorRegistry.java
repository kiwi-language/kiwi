package tech.metavm.application;

import tech.metavm.entity.*;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.util.NncUtils;

import java.util.List;

@EntityType
public class InterceptorRegistry extends Entity {

    public static final IndexDef<InterceptorRegistry> IDX_ALL_FLAG = IndexDef.create(InterceptorRegistry.class, "allFlags");

    @ChildEntity
    private final ReadWriteArray<ClassInstance> interceptors = addChild(new ReadWriteArray<>(ClassInstance.class), "interceptors");

    @SuppressWarnings("unused")
    private final boolean allFlags = true;

    public static InterceptorRegistry getInstance(IEntityContext context) {
        return context.selectFirstByKey(IDX_ALL_FLAG, true);
    }

    public void addInterceptor(ClassInstance interceptor) {
        if(NncUtils.exists(interceptors, i -> i.getKlass() == interceptor.getKlass()))
            throw new IllegalStateException("Interceptor already added to registry");
        interceptors.add(interceptor);
    }

    public List<ClassInstance> getInterceptors() {
        return interceptors.toList();
    }

}
