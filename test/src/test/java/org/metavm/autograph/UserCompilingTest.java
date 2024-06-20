package org.metavm.autograph;

import org.junit.Assert;
import org.metavm.application.rest.dto.ApplicationCreateRequest;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.object.instance.rest.InstanceFieldValue;
import org.metavm.object.instance.rest.InstanceQueryDTO;
import org.metavm.object.instance.rest.PrimitiveFieldValue;
import org.metavm.object.type.ClassKind;
import org.metavm.object.type.PrimitiveKind;
import org.metavm.object.type.TypeExpressionBuilder;
import org.metavm.object.type.TypeExpressions;
import org.metavm.object.type.rest.dto.GetTypeRequest;
import org.metavm.object.type.rest.dto.TypeDTO;
import org.metavm.user.rest.dto.LoginRequest;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.metavm.util.NncUtils.requireNonNull;
import static org.metavm.util.TestUtils.doInTransaction;
import static org.metavm.util.TestUtils.getFieldIdByCode;

public class UserCompilingTest extends CompilerTestBase {

    public static final Logger logger = LoggerFactory.getLogger(UserCompilingTest.class);

    public static final String USERS_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/users";

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
                var roleType = queryClassType("LabRole");
                var roleReadWriteListType = TypeExpressionBuilder.fromKlassId(roleType.id()).readWriteList().build();
                var roleNameFieldId = getFieldIdByCode(roleType, "name");
                var roleId = doInTransaction(() -> apiClient.newInstance(
                        roleType.getCodeNotNull(),
                        List.of("admin")
                ));
                var role = instanceManager.get(roleId, 2).instance();
                Assert.assertEquals(
                        "admin",
                        ((PrimitiveFieldValue) role.getFieldValue(roleNameFieldId)).getValue()
                );
                var userType = queryClassType("LabUser", List.of(ClassKind.CLASS.code()));
                assertNoError(userType);
                var userLoginNameFieldId = getFieldIdByCode(userType, "loginName");
                var userNameFieldId = getFieldIdByCode(userType, "name");
                var userPasswordFieldId = getFieldIdByCode(userType, "password");
                var userRolesFieldId = getFieldIdByCode(userType, "roles");

                var platformUserType = queryClassType("LabPlatformUser", List.of(ClassKind.CLASS.code()));
                assertNoError(platformUserType);

                // send verification code by invoking LabVerificationCode.sendVerificationCode
                var verificationCodeType = queryClassType("LabVerificationCode");
                String email = "15968879210@163.com";
                sendVerificationCode(verificationCodeType, email);
                var verificationCode = getLastSentEmailContent();
                var registerRequestType = queryClassType("LabRegisterRequest");
                var platformUserId = (String) doInTransaction(() -> apiClient.callMethod(
                        platformUserType.getCodeNotNull(),
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
                var platformUser = instanceManager.get(platformUserId, 2).instance();
                Assert.assertEquals(
                        email, ((PrimitiveFieldValue) platformUser.getFieldValue(userLoginNameFieldId)).getValue()
                );
                Assert.assertEquals(
                        "lyq", ((PrimitiveFieldValue) platformUser.getFieldValue(userNameFieldId)).getValue()
                );
                var platformUserRoles = ((InstanceFieldValue) platformUser.getFieldValue(userRolesFieldId)).getInstance();
//            Assert.assertEquals(1, platformUserRoles.getListSize());
//            Assert.assertEquals(role.id(), platformUserRoles.getElement(0).referenceId());
                var platformUserApplicationsFieldId = getFieldIdByCode(platformUserType, "applications");
                var platformUserApplications = ((InstanceFieldValue) platformUser.getFieldValue(platformUserApplicationsFieldId)).getInstance();
                Assert.assertEquals(0, platformUserApplications.getListSize());

                // test platform user view list
                var platformUserMapping = TestUtils.getDefaultMapping(platformUserType);
                var platformUserViewType = typeManager.getType(new GetTypeRequest(TypeExpressions.extractKlassId(platformUserMapping.targetType()), false)).type();
                var platformUserViewList = instanceManager.query(
                        new InstanceQueryDTO(
                                TypeExpressions.getClassType(platformUserViewType.id()),
                                platformUserMapping.id(),
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
                Assert.assertEquals(1, platformUserViewList.size());
                // test platform user view update
                var platformUserView = platformUserViewList.get(0);
                TestUtils.doInTransactionWithoutResult(() -> instanceManager.update(platformUserView));
                // reload platform user view and check its roles field
                var reloadedPlatformUserView = instanceManager.get(platformUserView.id(), 1).instance();
                var userViewRolesFieldId = TestUtils.getFieldIdByCode(platformUserViewType, "roles");
                var reloadedPlatformUserRoles = ((InstanceFieldValue) reloadedPlatformUserView.getFieldValue(userViewRolesFieldId)).getInstance();
//            Assert.assertEquals(1, reloadedPlatformUserRoles.getListSize());

                // create an UserApplication by invoking the UserApplication.create method
                var userApplicationType = queryClassType("UserApplication");
                var applicationId = (String) doInTransaction(() -> apiClient.callMethod(
                        userApplicationType.getCodeNotNull(),
                        "create",
                        List.of("lab", platformUser.getIdNotNull())
                ));
                var application = instanceManager.get(applicationId, 2).instance();
                var reloadedPlatformUser = instanceManager.get(platformUser.id(), 1).instance();
                var joinedApplications = ((InstanceFieldValue) reloadedPlatformUser.getFieldValue(platformUserApplicationsFieldId)).getInstance();
                Assert.assertEquals(1, joinedApplications.getListSize());
                Assert.assertEquals(applicationId, joinedApplications.getElement(0).referenceId());

                // get PlatformApplication
                var platformApplicationType = queryClassType("PlatformApplication");
                var platformApplicationId = (String) doInTransaction(() -> apiClient.callMethod(
                        platformApplicationType.getCodeNotNull(), "getInstance", List.of()
                ));
                var platformApplication = instanceManager.get(platformApplicationId, 2).instance();
                var loginResultType = queryClassType("LabLoginResult");

                // login
                var token = login(userType, platformApplication, email, "123456");

                var enterAppMethodRef = TestUtils.getStaticMethodRef(platformUserType, "enterApp",
                        TypeExpressions.getClassType(platformUserType.id()), TypeExpressions.getClassType(userApplicationType.id()));

                // enter application
                // noinspection unchecked
                var loginResult = (Map<String, Object>) doInTransaction(() -> apiClient.callMethod(
                        platformUserType.getCodeNotNull(),
                        "enterApp", List.of(platformUser.getIdNotNull(), application.getIdNotNull())
                ));
                token = (String) loginResult.get("token");
                Assert.assertNotNull(token);

                // test leave application
                var platformUserListType = TypeExpressions.getListType(TypeExpressions.getClassType(platformUserType.id()));
                var platformUserReadWriteListType = TypeExpressionBuilder.fromKlassId(platformUserType.id()).readWriteList().build();
                var leaveAppMethodRef = TestUtils.getStaticMethodRef(platformUserType, "leaveApp",
                        platformUserListType, TypeExpressions.getClassType(userApplicationType.id()));

                try {
                    doInTransaction(() -> apiClient.callMethod(
                            platformUserType.getCodeNotNull(),
                            "leaveApp",
                            List.of(List.of(platformUser.getIdNotNull()), application.getIdNotNull())
                    ));
                    Assert.fail("Owner can leave the application");
                } catch (Exception e) {
                    Assert.assertEquals("The owner of the application cannot exit the application", NncUtils.getRootCause(e).getMessage());
                }

                // create a platform user to join the application and then leave
                var anotherPlatformUserId = (String) doInTransaction(() -> apiClient.newInstance(
                        platformUserType.getCodeNotNull(),
                        List.of("lyq2", "123456", "lyq2", List.of(role.getIdNotNull()))
                ));
                var platformUser2 = instanceManager.get(anotherPlatformUserId, 2).instance();

                // send invitation
//                var appInvitationRequestType = queryClassType("LabAppInvitationRequest");
                doInTransaction(() -> apiClient.callMethod(
                        userApplicationType.getCodeNotNull(), "invite",
                        List.of(
                                Map.of(
                                        "application", application.getIdNotNull(),
                                        "user", platformUser2.getIdNotNull(),
                                        "isAdmin", true
                                )
                        )
                ));
                // Login as platformUser2
                login(userType, platformApplication, "lyq2", "123456");

                // query the latest message
                var messageType = queryClassType("LabMessage", List.of(ClassKind.CLASS.code()));
                var messageList = instanceManager.query(
                        new InstanceQueryDTO(
                                TypeExpressions.getClassType(messageType.id()),
                                null,
                                null,
                                "receiver = $$" + platformUser2.id(),
                                List.of(),
                                1,
                                20,
                                false,
                                false,
                                List.of()
                        )
                ).page().data();
                Assert.assertEquals(1, messageList.size());
                var message = messageList.get(0);
                // check that the message is not read
                var messageReadFieldId = getFieldIdByCode(messageType, "read");
                Assert.assertFalse((boolean) ((PrimitiveFieldValue) message.getFieldValue(messageReadFieldId)).getValue());
                // read the message
                doInTransaction(() -> apiClient.callMethod(
                        messageType.getCodeNotNull(), "read", List.of(message.getIdNotNull())
                ));
                // get invitationId from the message
                var messageTargetFieldId = getFieldIdByCode(messageType, "target");
                // accept invitation
                doInTransaction(() -> apiClient.callMethod(
                        userApplicationType.getCodeNotNull(), "acceptInvitation",
                        List.of(message.getFieldValue(messageTargetFieldId).toJson())
                ));
                // assert that the user has joined the application
                var reloadedAnotherPlatformUser = instanceManager.get(platformUser2.id(), 1).instance();
                var anotherJoinedApplications = ((InstanceFieldValue) reloadedAnotherPlatformUser.getFieldValue(platformUserApplicationsFieldId)).getInstance();
                Assert.assertEquals(1, anotherJoinedApplications.getListSize());
                //noinspection unchecked
                loginResult = (Map<String, Object>) doInTransaction(() -> apiClient.callMethod(
                        platformUserType.getCodeNotNull(),
                        "enterApp",
                        List.of(platformUser2.getIdNotNull(), application.getIdNotNull())
                ));
                loginResultType = queryClassType("LabLoginResult");
//                token = (String) ((PrimitiveFieldValue) loginResult.getFieldValue(getFieldIdByCode(loginResultType, "token"))).getValue();
                token = (String) loginResult.get("token");
                Assert.assertNotNull(token);

                // test leaving the application
                doInTransaction(() -> apiClient.callMethod(
                        platformUserType.getCodeNotNull(), "leaveApp",
                        List.of(List.of(platformUser2.getIdNotNull()), application.getIdNotNull())
                ));
                // assert that the user has left the application
                var reloadedAnotherPlatformUser2 = instanceManager.get(platformUser2.id(), 1).instance();
                var anotherJoinedApplications2 = ((InstanceFieldValue) reloadedAnotherPlatformUser2.getFieldValue(platformUserApplicationsFieldId)).getInstance();
                Assert.assertEquals(0, anotherJoinedApplications2.getListSize());
                try {
                    doInTransaction(() -> apiClient.callMethod(
                            platformUserType.getCodeNotNull(), "enterApp",
                            List.of(platformUser2.getIdNotNull(), application.getIdNotNull())
                    ));
                    Assert.fail("Users that are not member of the application should not be able to enter it");
                } catch (Exception e) {
                    Assert.assertEquals("User not joined in the application cannot enter", NncUtils.getRootCause(e).getMessage());
                }

                // test application view list
                var applicationMapping = TestUtils.getDefaultMapping(userApplicationType);
                var applicationViewType = typeManager.getType(new GetTypeRequest(TypeExpressions.extractKlassId(applicationMapping.targetType()), false)).type();
                var applicationViewList = instanceManager.query(
                        new InstanceQueryDTO(
                                TypeExpressions.getClassType(applicationViewType.id()),
                                applicationMapping.id(),
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
                Assert.assertEquals(1, applicationViewList.size());

                // test update application view
                var applicationView = applicationViewList.get(0);
                TestUtils.doInTransactionWithoutResult(() -> instanceManager.update(applicationView));

                // assert that fields of LabToken type has been generated correctly
                var tokenType = queryClassType("LabToken");
                var tokenReadWriteListType = TypeExpressions.getReadWriteListType(TypeExpressions.getClassType(tokenType.id()));
                Assert.assertTrue(tokenType.ephemeral());
                Assert.assertEquals(2, tokenType.getClassParam().fields().size());

                // create an ordinary user
                var userId = doInTransaction(() -> apiClient.newInstance(
                        userType.getCodeNotNull(),
                        List.of("leen", "123456", "leen", List.of(role.getIdNotNull()), application.getIdNotNull())
                ));
                var user = instanceManager.get(userId, 1).instance();
                Assert.assertEquals(
                        "leen", ((PrimitiveFieldValue) user.getFieldValue(userLoginNameFieldId)).getValue()
                );
                Assert.assertEquals(
                        "leen", ((PrimitiveFieldValue) user.getFieldValue(userNameFieldId)).getValue()
                );
                var passwordValue = user.getFieldValue(userPasswordFieldId);
                Assert.assertTrue(passwordValue instanceof PrimitiveFieldValue primitiveFieldValue
                        && primitiveFieldValue.getPrimitiveKind() == PrimitiveKind.PASSWORD.code());
                var userRoles = ((InstanceFieldValue) user.getFieldValue(userRolesFieldId)).getInstance();
                Assert.assertEquals(1, userRoles.getListSize());
                Assert.assertEquals(role.id(), userRoles.getElement(0).referenceId());
                Assert.assertEquals(2, userType.getClassParam().constraints().size());

                // test login
                token = login(userType, application, "leen", "123456");

                // test login with too many attempts
                for (int i = 0; i < 5; i++) {
                    try {
                        login(userType, application, "leen", "123123", "192.168.0.1", false);
                        if (i == 4) {
                            Assert.fail("Exception should be raised when there are too many failed login attempts");
                        }
                    } catch (Exception e) {
                        Assert.assertEquals("Too many login attempts, please try again later", NncUtils.getRootCause(e).getMessage());
                    }
                }

                // execute the LabUser.verify method and check verification result
                var tokenValue = createTokenValue(application, token);
                var loginInfo = verify(userType, tokenValue);
                var loginInfoType = queryClassType("LabLoginInfo");
                assertNoError(loginInfoType);
                Assert.assertEquals(application.id(), loginInfo.get("application"));
                Assert.assertEquals(user.id(), loginInfo.get("user"));

                // test logout
                var tokenF = token;
                doInTransaction(() -> apiClient.callMethod(
                        userType.getCodeNotNull(), "logout",
                        List.of(List.of(Map.of(
                                "application", application.getIdNotNull(),
                                "token", tokenF
                        )))
                ));
                // verify that the token has been invalidated
                assertTokenInvalidated(userType, tokenValue);

                // login again
                token = login(userType, platformApplication, email, "123456");
                var tokenValue2 = createTokenValue(platformApplication, token);

//            // test logout platform user
                logout(platformUserType);

                // assert that the token has been invalidated
                assertTokenInvalidated(userType, tokenValue);

                // test changePassword
                sendVerificationCode(verificationCodeType, email);
                doInTransaction(() -> apiClient.callMethod(
                        platformUserType.getCodeNotNull(), "changePassword",
                        List.of(Map.of(
                                "verificationCode", getLastSentEmailContent(),
                                "loginName", email,
                                "password", "888888"
                        ))
                ));
                var token2 = login(userType, platformApplication, email, "123456", "127.0.0.1", false);
                Assert.assertNull(token2);
                login(userType, platformApplication, email, "888888");
            }
            System.out.println(profiler.finish(false, true).output());
        });
    }

    private void sendVerificationCode(TypeDTO verificationCodeType, String email) {
        doInTransaction(() -> apiClient.callMethod(
                verificationCodeType.getCodeNotNull(), "sendVerificationCode",
                List.of(email, "MetaVM Verification Code", "127.0.0.1")
        ));
    }

    private String getLastSentEmailContent() {
        return Objects.requireNonNull(MockEmailSender.INSTANCE.getLastSentEmail()).content();
    }

    private void logout(TypeDTO platformUserType) {
        doInTransaction(() -> apiClient.callMethod(
                platformUserType.getCodeNotNull(), "logout", List.of()
        ));
    }

    private void assertTokenInvalidated(TypeDTO userType, Map<String, Object> tokenValue) {
        var loginInfo = verify(userType, tokenValue);
        Assert.assertNull(loginInfo.get("application"));
    }

    private Map<String, Object> verify(TypeDTO userType, Map<String, Object> tokenValue) {
        //noinspection unchecked
        return (Map<String, Object>) doInTransaction(() -> apiClient.callMethod(
                userType.getCodeNotNull(), "verify", List.of(tokenValue)
        ));
    }

    private String login(TypeDTO userType, InstanceDTO platformApplication, String loginName, @SuppressWarnings("SameParameterValue") String password) {
        return login(userType, platformApplication, loginName, password, "127.0.0.1", true);
    }

    private String login(TypeDTO userType, InstanceDTO platformApplication, String loginName, String password, String clientIP, boolean checkToken) {
        //noinspection unchecked
        var loginResult = (Map<String, Object>) doInTransaction(() -> apiClient.callMethod(
                userType.getCodeNotNull(), "login",
                List.of(platformApplication.getIdNotNull(), loginName, password, clientIP)
        ));
        var token = (String) loginResult.get("token");
        if (checkToken)
            Assert.assertNotNull(token);
        return token;
    }

    private Map<String, Object> createTokenValue(InstanceDTO application, String token) {
        return Map.of(
                "application", application.getIdNotNull(),
                "token", token
        );
    }

//    public void testResend() {
//        LoginUtils.loginWithAuthFile(AUTH_FILE, typeClient);
//        var request = NncUtils.readJsonFromFile(REQUEST_FILE, BatchSaveRequest.class);
//        HttpUtils.post("/type/batch-save", request, new TypeReference<List<Long>>() {
//        });
//    }

}