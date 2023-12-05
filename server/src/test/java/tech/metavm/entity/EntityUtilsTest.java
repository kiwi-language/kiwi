package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.mocks.Foo;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;

import static tech.metavm.util.NncUtils.requireNonNull;

public class EntityUtilsTest extends TestCase {

    private EntityIdProvider idProvider;
    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(idProvider = new MockIdProvider());
    }

    public void testClearIdRecursively() {
        EntityContext entityContext = new MockEntityContext(
                null, idProvider, MockRegistry.getDefContext()
        );

        Foo foo = MockRegistry.getFoo();
        entityContext.bind(foo);
        entityContext.initIds();
        
        EntityUtils.clearIdRecursively(foo);

        Assert.assertNull(foo.getId());
        Assert.assertNull(requireNonNull(foo.getQux()).getId());
        Assert.assertNull(requireNonNull(foo.getBazList()).getId());
        Assert.assertNull(foo.getBazList().get(0).getId());

    }
}