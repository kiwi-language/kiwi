package tech.metavm.entity;

import tech.metavm.util.NncUtils;
import tech.metavm.util.TestContext;

public class MockEntityContext extends EntityContext {

    public MockEntityContext(IEntityContext parent, EntityIdProvider idProvider, DefContext defContext) {
        super(
                new MemInstanceContext(
                        TestContext.getTenantId(),
                        idProvider,
                        new MemInstanceStore(),
                        NncUtils.get(parent, IEntityContext::getInstanceContext)),
                parent,
                defContext
        );
    }

}
