package tech.metavm.transpile;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PackageScannerTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(PackageScannerTest.class);

    public void test() {

        PackageScanner scanner = new PackageScanner();
        List<String> classNames = scanner.getClassNames("tech.metavm.util");
        for (String className : classNames) {
            LOGGER.info(className);
        }
    }

}