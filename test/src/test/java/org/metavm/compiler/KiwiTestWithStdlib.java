package org.metavm.compiler;

import org.metavm.object.instance.core.ApiObject;
import org.metavm.util.ApiNamedObject;
import org.metavm.util.TestUtils;
import org.metavm.util.Utils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class KiwiTestWithStdlib extends KiwiTestBase {

    public void testAuthPrinciple() {
        deploy("kiwi/auth/auth.kiwi");
        var userService = ApiNamedObject.of("userService");
        saveInstance("auth.User", Map.of(
                "name", "leen",
                "password", "123456"
        ));
        var r = (ApiObject) callMethod(
                userService,
                "login",
                List.of("leen", "123456")
        );
        var token = r.getString("token");
        assertNotNull(token);
        var name = TestUtils.doInTransaction(() -> apiClient.callMethod(
                userService,
                "getUserName",
                List.of(),
                false,
                Map.of(
                        "Authorization", "Bearer " + token
                )
        ));
        assertEquals("leen", name);
    }

    @Override
    protected List<Path> additionalSourcePaths() {
        return Utils.listFilePathsRecursively(Path.of(TestUtils.getResourcePath("/stdlib")).resolve("src"), "kiwi");
    }

}
