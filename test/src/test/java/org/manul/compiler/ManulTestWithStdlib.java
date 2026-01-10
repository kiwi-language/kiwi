package org.manul.compiler;

import org.manul.object.instance.core.ApiObject;
import org.manul.util.ApiNamedObject;
import org.manul.util.TestUtils;
import org.manul.util.Utils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ManulTestWithStdlib extends ManulTestBase {

    public void testAuthPrinciple() {
        deploy("manul/auth/auth.mnl");
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
        return Utils.listFilePathsRecursively(ManulEnv.getStdLibPath().resolve("src"), "mnl");
    }

}
