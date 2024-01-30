package tech.metavm.autograph;

import com.fasterxml.jackson.core.type.TypeReference;
import junit.framework.TestCase;
import tech.metavm.object.type.rest.dto.BatchSaveRequest;
import tech.metavm.util.HttpUtils;
import tech.metavm.util.LoginUtils;
import tech.metavm.util.NncUtils;

import java.io.File;
import java.util.List;

public class MainTest extends TestCase {

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/java";

    public static final String AUTH_FILE = "/Users/leen/workspace/object/compiler/src/test/resources/auth";

    public static final String REQUEST_FILE =
            "/Users/leen/workspace/object/compiler/src/test/resources/requests/request.2023-12-05 10:44:23.json";

    public static final String AUTH_FIle = "/Users/leen/workspace/object/compiler/src/test/resources/auth";

    public static final String HOME = Main.DEFAULT_HOME;

    public static final String HOME_1 = System.getProperty("user.home") + File.separator + ".metavm_1";

    private Main main;

    @Override
    protected void setUp() {
        main = new Main(HOME, SOURCE_ROOT, AUTH_FILE);
    }

    public void test() {
        main.run();
    }

    public void testResend() {
        LoginUtils.loginWithAuthFile(AUTH_FILE);
        var request = NncUtils.readJsonFromFile(REQUEST_FILE, BatchSaveRequest.class);
        HttpUtils.post("/type/batch-save", request, new TypeReference<List<Long>>() {});
    }

}