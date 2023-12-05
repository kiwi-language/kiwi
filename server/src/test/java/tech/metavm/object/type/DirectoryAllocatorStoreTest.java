package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.util.TestConstants;

import java.util.List;

public class DirectoryAllocatorStoreTest extends TestCase {

    public void test() {
        DirectoryAllocatorStore store = new DirectoryAllocatorStore(TestConstants.TEST_RESOURCE_CP_ROOT);
        List<String> fileNames = store.getFileNames();
        Assert.assertEquals(3, fileNames.size());
    }

}