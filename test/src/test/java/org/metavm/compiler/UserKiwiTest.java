package org.metavm.compiler;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.ApiObject;
import org.metavm.object.instance.core.Id;
import org.metavm.util.ApiNamedObject;
import org.metavm.util.ContextUtil;
import org.metavm.util.Utils;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.metavm.util.TestUtils.waitForAllTasksDone;

@Slf4j
public class UserKiwiTest extends KiwiProjectTestBase {

    public void test() {
        deploy("user");
        var profiler = ContextUtil.getProfiler();
        try (var ignored = profiler.enter("submit")) {
            var roleId = saveInstance(
                    "user.Role",
                    Map.of("name","admin")
            );
            var role = getObject(roleId);
            assertEquals("admin", role.getString("name"));

            // send verification code by invoking LabVerificationCode.sendVerificationCode
            String email = "15968879210@163.com";
            sendVerificationCode(email);
            var verificationCode = getLastSentEmailContent();
            var userService = ApiNamedObject.of("userService");

            var userId = (Id) callMethod(
                    userService,
                    "register",
                    List.of(
                            Map.of(
                                    "loginName", email,
                                    "name", "lyq",
                                    "password", "123456",
                                    "verificationCode", verificationCode
                            )
                    )
            );
            waitForAllTasksDone(schedulerAndWorker);
            var user = getObject(userId);
            log.info("{}", Utils.toPrettyJsonString(user.getMap()));
            assertEquals(email, user.getString("loginName"));
            assertEquals("lyq", user.getString("name"));
            var apps = user.getArray("applications");
            assertEquals(0, apps.size());

            // test platform user view list
            var userList = search("user.User", Map.of()).items();
            assertEquals(1, userList.size());
            // test platform user view update
//                var platformUser1 = platformUserList.getFirst();
            // reload platform user view and check its roles field
//                var platformUser2 = getObject(platformUser1.id());
//                assertEquals(1, platformUser2.getArray("roles").size());

            var token = login(email, "123456");
            assertNotNull(token);

            // create an UserApplication by invoking the UserApplication.create method
            var applicationService = ApiNamedObject.of("applicationService");
            var appId = (Id) callMethod(applicationService, "create",
                    List.of("lab"));
            var app = getObject(appId);
            var reloadedUser = getObject(userId);
            var joinedApps = reloadedUser.getArray("applications");
            assertEquals(1, joinedApps.size());
            assertEquals(appId, joinedApps.getFirst());

            // test leave application
            try {
                callMethod(
                        userService,
                        "leaveApp",
                        List.of(app.id())
                );
                fail("Owner can leave the application");
            } catch (Exception e) {
                assertEquals("The owner of the application cannot exit the application", Utils.getRootCause(e).getMessage());
            }

            // create a platform user to join the application and then leave
            var userId1 = saveInstance(
                    "user.User",
                    Map.of(
                            "loginName", "lyq2",
                            "password", "123456",
                            "name", "lyq2",
                            "roles", List.of(roleId))
            );

            // send invitation
            callMethod(
                    applicationService,
                    "invite",
                    List.of(
                            Map.of(
                                    "application", app.id(),
                                    "user", userId1,
                                    "isAdmin", true
                            )
                    )
            );

            login("lyq2", "123456");

            // query the latest message
            waitForAllTasksDone(schedulerAndWorker);
            var messageList = search("message.Message",
                    Map.of("receiver", userId1)
            ).items();
            assertEquals(1, messageList.size());
            var messageId = requireNonNull(messageList.getFirst()).id();
            var message = getObject(messageId);
            // check that the message is not read
            assertFalse(message.getBoolean("read"));
            var messageService = ApiNamedObject.of("messageService");
            // read the message
            callMethod(messageService, "read", List.of(messageId));
            // get invitationId from the message
            // accept invitation
            callMethod(
                    applicationService,
                    "acceptInvitation",
                    List.of(message.get("target"))
            );
            // assert that the user has joined the application
            var reloadedUser1 = getObject(userId1);
            var joinedApps1 = reloadedUser1.getArray("applications");
            assertEquals(1, joinedApps1.size());

            // test leaving the application
            callMethod(
                    userService,
                    "leaveApp",
                    List.of(app.id())
            );
            // assert that the user has left the application
            var reloadedUser2 = getObject(userId1);
            var joinedApps2 = reloadedUser2.getArray("applications");
            assertEquals(0, joinedApps2.size());

            // test application view list
            waitForAllTasksDone(schedulerAndWorker);
            var appList = search(
                    "application.Application",
                    Map.of()
            );
            assertEquals(1, appList.total());

            // create an ordinary user
            var userId2 = saveInstance(
                    "user.User",
                    Map.of(
                            "loginName", "leen",
                            "password", "123456",
                            "name", "leen",
                            "roles", List.of(roleId))
            );
            var user2 = getObject(userId2);
            assertEquals("leen", user2.getString("loginName"));
            assertEquals("leen", user2.getString("name"));
            var userRoles = user2.getArray("roles");
            assertEquals(1, userRoles.size());
            assertEquals(roleId, userRoles.getFirst());

            // test login
            token = login("leen", "123456");

            // test login with too many attempts
            for (int i = 0; i < 5; i++) {
                try {
                    login( "leen", "123123", "192.168.0.1", false);
                    if (i == 4) {
                        fail("Exception should be raised when there are too many failed login attempts");
                    }
                } catch (Exception e) {
                    assertEquals("Too many login attempts, please try again later", Utils.getRootCause(e).getMessage());
                }
            }

            // execute the LabUser.verify method and check verification result
            var sId = (Id) callMethod(
                    userService,
                    "verify",
                    List.of(token)
            );
            assertNotNull(sId);
            assertEquals(userId2, getObject(sId).getId("user"));

            // test logout
            callMethod(userService, "logout", List.of());
            // verify that the token has been invalidated
            assertTokenInvalidated(token);

            // login again
            login(email, "123456");

//            // test logout platform user
            callMethod(userService, "logout", List.of());

            // assert that the token has been invalidated
            assertTokenInvalidated(token);

            // test changePassword
            sendVerificationCode(email);
            callMethod(
                    userService, "changePassword",
                    List.of(Map.of(
                            "verificationCode", getLastSentEmailContent(),
                            "loginName", email,
                            "password", "888888"
                    ))
            );
            var token2 = login(email, "123456", "127.0.0.1", false);
            assertNull(token2);
            login(email, "888888");
        }
        log.trace("\n{}", profiler.finish(false, true).output());
    }

    private String login(String loginName, String password) {
        return login(loginName, password, "127.0.0.1", false);
    }

    private String login(String loginName, String password, String clientIp, boolean checkLogin) {
        var token = ((ApiObject) callMethod(
                ApiNamedObject.of("userService"),
                "login",
                List.of(loginName, password, clientIp)
        )).getString("token");
        if (checkLogin)
            assertNotNull(token);
        return token;
    }

    private void assertTokenInvalidated(String token) {
        var s = callMethod(
                ApiNamedObject.of("userService"),
                "verify",
                List.of(token)
        );
        assertNull(s);
    }

    private void sendVerificationCode(String email) {
        callMethod(
                ApiNamedObject.of("verificationCodeService"),
                "sendVerificationCode",
                List.of(email, "MetaVM Verification Code", "127.0.0.1")
        );
    }

    private String getLastSentEmailContent() {
        var id = (Id) requireNonNull(callMethod(
                ApiNamedObject.of("emailService"),
                "getLastSentEmail",
                List.of()
        ), "No email has been sent");
        return getObject(id).getString("content");
    }
}
