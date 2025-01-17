package org.metavm.autograph;

import org.junit.Assert;
import org.metavm.application.rest.dto.ApplicationCreateRequest;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.rest.InstanceQueryDTO;
import org.metavm.user.rest.dto.LoginRequest;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.metavm.util.TestUtils.doInTransaction;

public class UserCompilingTest extends CompilerTestBase {

    public static final Logger logger = LoggerFactory.getLogger(UserCompilingTest.class);
    public static final String USERS_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/users";
    public static final String userKlass = "org.metavm.user.LabPlatformUser";
    public static final String platformApplicationKlass = "org.metavm.application.PlatformApplication";
    public static final String verificationCodeKlass = "org.metavm.user.LabVerificationCode";
    public static final String userApplicationKlass = "org.metavm.application.UserApplication";
    
    public void testUsers() {
        submit(() -> {
            var sysApp = doInTransaction(() -> applicationManager.createBuiltin(ApplicationCreateRequest.fromNewUser("test", "admin", "123456")));
            var sysLoginResult = doInTransaction(() -> loginService.login(new LoginRequest(
                    Constants.PLATFORM_APP_ID,
                    "admin",
                    "123456"
            ), "127.0.0.1"));
            ContextUtil.setAppId(Constants.PLATFORM_APP_ID);
            ContextUtil.setUserId(Id.parse(sysLoginResult.userId()));
            var sysLoginResult2 = doInTransaction(() -> platformUserManager.enterApp(sysApp.appId()));
            logger.info(sysLoginResult2.toString());
            var loginInfo = loginService.verify(requireNonNull(sysLoginResult2.token()));
            logger.info(loginInfo.toString());
            APP_ID = sysApp.appId();
            AUTH_CONFIG = new AuthConfig("admin", "123456");
        });
        compileTwice(USERS_SOURCE_ROOT);
//        compile(USERS_SOURCE_ROOT);
        submit(() -> {
            var profiler = ContextUtil.getProfiler();
            try (var ignored = profiler.enter("submit")) {
                var roleId = doInTransaction(() -> apiClient.newInstance(
                        "org.metavm.user.LabRole",
                        List.of("admin")
                ));
                var role = getObject(roleId);
                Assert.assertEquals("admin", role.getString("name"));

                // send verification code by invoking LabVerificationCode.sendVerificationCode
                String email = "15968879210@163.com";
                sendVerificationCode(email);
                var verificationCode = getLastSentEmailContent();
                var platformUserId = (String) doInTransaction(() -> apiClient.callMethod(
                        userKlass,
                        "register",
                        List.of(
                                Map.of(
                                        "loginName", email,
                                        "name", "lyq",
                                        "password", "123456",
                                        "verificationCode", verificationCode
                                )
                        )
                ));
                waitForAllTasksDone();
                var platformUser = getObject(platformUserId);
                logger.info("{}", Utils.toPrettyJsonString(platformUser));
                Assert.assertEquals(email, platformUser.getString("loginName"));
                Assert.assertEquals("lyq", platformUser.getString("name"));
                var platformUserApplications = platformUser.getArray("applications");
                Assert.assertEquals(0, platformUserApplications.size());

                // test platform user view list
                var platformUserList = instanceManager.query(
                        new InstanceQueryDTO(
                                userKlass,
                                null,
                                null,
                                List.of(),
                                1,
                                20,
                                false,
                                false,
                                List.of()
                        )
                ).page().data();
                Assert.assertEquals(1, platformUserList.size());
                // test platform user view update
//                var platformUser1 = platformUserList.getFirst();
                // reload platform user view and check its roles field
//                var platformUser2 = getObject(platformUser1.id());
//                Assert.assertEquals(1, platformUser2.getArray("roles").size());

                // create an UserApplication by invoking the UserApplication.create method
                var applicationId = (String) callMethod(userApplicationKlass, "create",
                        List.of("lab", platformUserId));
                DebugEnv.stringId = applicationId;
                var application = getObject(applicationId);
                var reloadedPlatformUser = getObject(platformUserId);
                var joinedApplications = reloadedPlatformUser.getArray("applications");
                Assert.assertEquals(1, joinedApplications.size());
                Assert.assertEquals(applicationId, joinedApplications.getFirst());

                // get PlatformApplication
                var platformApplicationId = (String) doInTransaction(() -> apiClient.callMethod(
                        platformApplicationKlass, "getInstance", List.of()
                ));
                var platformApplication = getObject(platformApplicationId);

                // login
                var token = login(platformApplication.id(), email, "123456");

                // enter application
                // noinspection unchecked
                var loginResult = (Map<String, Object>) doInTransaction(() -> apiClient.callMethod(
                        userKlass,
                        "enterApp", List.of(platformUserId, application.id())
                ));
                token = (String) loginResult.get("token");
                Assert.assertNotNull(token);

                // test leave application
                try {
                    callMethod(
                            userKlass,
                            "leaveApp",
                            List.of(List.of(platformUserId), application.id())
                    );
                    Assert.fail("Owner can leave the application");
                } catch (Exception e) {
                    Assert.assertEquals("The owner of the application cannot exit the application", Utils.getRootCause(e).getMessage());
                }

                // create a platform user to join the application and then leave
                var anotherPlatformUserId = (String) doInTransaction(() -> apiClient.newInstance(
                        userKlass,
                        List.of("lyq2", "123456", "lyq2", List.of(roleId))
                ));
//                var platformUser3 = getObject(anotherPlatformUserId);

                // send invitation
                callMethod(
                        userApplicationKlass, "invite",
                        List.of(
                                Map.of(
                                        "application", application.id(),
                                        "user", anotherPlatformUserId,
                                        "isAdmin", true
                                )
                        )
                );
                // Login as platformUser2
                login(platformApplication.id(), "lyq2", "123456");

                // query the latest message
                waitForAllTasksDone();
                var messageList = instanceManager.query(
                        new InstanceQueryDTO(
                                "org.metavm.message.LabMessage",
                                null,
                                "receiver = $$" + anotherPlatformUserId,
                                List.of(),
                                1,
                                20,
                                false,
                                false,
                                List.of()
                        )
                ).page().data();
                Assert.assertEquals(1, messageList.size());
                var messageId = requireNonNull(messageList.getFirst());
                var message = getObject(messageId);
                // check that the message is not read
                Assert.assertFalse(message.getBoolean("read"));
                // read the message
                callMethod("org.metavm.message.LabMessage", "read", List.of(messageId));
                // get invitationId from the message
                // accept invitation
                callMethod(
                        userApplicationKlass, "acceptInvitation",
                        List.of(message.getString("target"))
                );
                // assert that the user has joined the application
                var reloadedAnotherPlatformUser = getObject(anotherPlatformUserId);
                var anotherJoinedApplications = reloadedAnotherPlatformUser.getArray("applications");
                Assert.assertEquals(1, anotherJoinedApplications.size());
                //noinspection unchecked
                loginResult = (Map<String, Object>) doInTransaction(() -> apiClient.callMethod(
                        userKlass,
                        "enterApp",
                        List.of(anotherPlatformUserId, application.id())
                ));
                token = (String) loginResult.get("token");
                Assert.assertNotNull(token);

                // test leaving the application
                doInTransaction(() -> apiClient.callMethod(
                        userKlass, "leaveApp",
                        List.of(List.of(anotherPlatformUserId), application.id())
                ));
                // assert that the user has left the application
                var reloadedAnotherPlatformUser2 = getObject(anotherPlatformUserId);
                var anotherJoinedApplications2 = reloadedAnotherPlatformUser2.getArray("applications");
                Assert.assertEquals(0, anotherJoinedApplications2.size());
                try {
                    doInTransaction(() -> apiClient.callMethod(
                            userKlass, "enterApp",
                            List.of(anotherPlatformUserId, application.id())
                    ));
                    Assert.fail("Users that are not member of the application should not be able to enter it");
                } catch (Exception e) {
                    Assert.assertEquals("User not joined in the application cannot enter", Utils.getRootCause(e).getMessage());
                }

                // test application view list
                waitForAllTasksDone();
                var applicationList = instanceManager.query(
                        new InstanceQueryDTO(
                                userApplicationKlass,
                                null,
                                null,
                                List.of(),
                                1,
                                20,
                                false,
                                false,
                                List.of()
                        )
                ).page().data();
                Assert.assertEquals(1, applicationList.size());

                // create an ordinary user
                var userId = doInTransaction(() -> apiClient.newInstance(
                        "org.metavm.user.LabUser",
                        List.of("leen", "123456", "leen", List.of(roleId), application.id())
                ));
                var user = getObject(userId);
                Assert.assertEquals("leen", user.getString("loginName"));
                Assert.assertEquals("leen", user.getString("name"));
                var userRoles = user.getArray("roles");
                Assert.assertEquals(1, userRoles.size());
                Assert.assertEquals(roleId, userRoles.getFirst());

                // test login
                token = login(application.id(), "leen", "123456");

                // test login with too many attempts
                for (int i = 0; i < 5; i++) {
                    try {
                        login(application.id(), "leen", "123123", "192.168.0.1", false);
                        if (i == 4) {
                            Assert.fail("Exception should be raised when there are too many failed login attempts");
                        }
                    } catch (Exception e) {
                        Assert.assertEquals("Too many login attempts, please try again later", Utils.getRootCause(e).getMessage());
                    }
                }

                // execute the LabUser.verify method and check verification result
                var tokenValue = createTokenValue(application.id(), token);
                var loginInfo = verify(tokenValue);
                Assert.assertEquals(application.id(), loginInfo.get("application"));
                Assert.assertEquals(userId, loginInfo.get("user"));

                // test logout
                var tokenF = token;
                callMethod(
                        "org.metavm.user.LabUser", "logout",
                        List.of(List.of(Map.of(
                                "application", application.id(),
                                "token", tokenF
                        )))
                );
                // verify that the token has been invalidated
                assertTokenInvalidated(tokenValue);

                // login again
                login(platformApplication.id(), email, "123456");

//            // test logout platform user
                logout();

                // assert that the token has been invalidated
                assertTokenInvalidated(tokenValue);

                // test changePassword
                sendVerificationCode(email);
                doInTransaction(() -> apiClient.callMethod(
                        userKlass, "changePassword",
                        List.of(Map.of(
                                "verificationCode", getLastSentEmailContent(),
                                "loginName", email,
                                "password", "888888"
                        ))
                ));
                var token2 = login(platformApplication.id(), email, "123456", "127.0.0.1", false);
                Assert.assertNull(token2);
                login(platformApplication.id(), email, "888888");
            }
            System.out.println(profiler.finish(false, true).output());
        });
    }

    private void sendVerificationCode(String email) {
        callMethod(
                verificationCodeKlass, "sendVerificationCode",
                List.of(email, "MetaVM Verification Code", "127.0.0.1")
        );
    }

    private String getLastSentEmailContent() {
        return requireNonNull(MockEmailSender.INSTANCE.getLastSentEmail()).content();
    }

    private void logout() {
        callMethod(
                userKlass, "logout", List.of()
        );
    }

    private void assertTokenInvalidated(Map<String, Object> tokenValue) {
        var loginInfo = verify(tokenValue);
        Assert.assertNull(loginInfo.get("application"));
    }

    private Map<String, Object> verify(Map<String, Object> tokenValue) {
        //noinspection unchecked
        return (Map<String, Object>) callMethod(
                "org.metavm.user.LabUser", "verify", List.of(tokenValue)
        );
    }

    private String login(String applicationId, String loginName, @SuppressWarnings("SameParameterValue") String password) {
        return login(applicationId, loginName, password, "127.0.0.1", true);
    }

    private String login(String applicationId, String loginName, String password, String clientIP, boolean checkToken) {
        //noinspection unchecked
        var loginResult = (Map<String, Object>) callMethod(
                "org.metavm.user.LabUser", "login",
                List.of(applicationId, loginName, password, clientIP)
        );
        var token = (String) loginResult.get("token");
        if (checkToken)
            Assert.assertNotNull(token);
        return token;
    }

    private Map<String, Object> createTokenValue(String id, String token) {
        return Map.of(
                "application", id,
                "token", token
        );
    }

}