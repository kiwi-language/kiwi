package tech.metavm.transpile.ir;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.EntityUtils;

import java.util.List;
import java.util.Set;

public class TypeStoreTest extends TestCase {

    private TypeStore typeStore;

    @Override
    protected void setUp() throws Exception {
        typeStore = new TypeStore(List.of(), Set.of(), Set.of());
    }

    public void test() {
        IRClass numberClass = typeStore.fromClass(Number.class);
        EntityUtils.ensureProxyInitialized(numberClass);
        Assert.assertNotNull(numberClass);
    }

}