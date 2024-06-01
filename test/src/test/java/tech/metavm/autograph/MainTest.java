package tech.metavm.autograph;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.application.rest.dto.ApplicationCreateRequest;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.ClassKind;
import tech.metavm.object.type.PrimitiveKind;
import tech.metavm.object.type.TypeExpressionBuilder;
import tech.metavm.object.type.TypeExpressions;
import tech.metavm.object.type.rest.dto.GetTypeRequest;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.user.rest.dto.LoginRequest;
import tech.metavm.util.*;

import java.util.List;
import java.util.Objects;

import static tech.metavm.util.NncUtils.requireNonNull;
import static tech.metavm.util.TestUtils.doInTransaction;
import static tech.metavm.util.TestUtils.getFieldIdByCode;

public class MainTest extends CompilerTestBase {

    public static final Logger LOGGER = LoggerFactory.getLogger(MainTest.class);

    public static final String SHOPPING_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/shopping";

    public static final String USERS_SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/users";

    public void testShopping() {
        compileTwice(SHOPPING_SOURCE_ROOT);
        submit(() -> {
            var productStateType = queryClassType("商品状态");
            var productNormalState = TestUtils.getEnumConstantByName(productStateType, "正常");
            var productType = queryClassType("AST产品");
            var product = TestUtils.createInstanceWithCheck(instanceManager, InstanceDTO.createClassInstance(
                    Constants.CONSTANT_ID_PREFIX + productType.id(),
                    List.of(
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(productType, "title"),
                                    PrimitiveFieldValue.createString("鞋子")
                            ),
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(productType, "orderCount"),
                                    PrimitiveFieldValue.createLong(0L)
                            ),
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(productType, "price"),
                                    PrimitiveFieldValue.createLong(100L)
                            ),
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(productType, "inventory"),
                                    PrimitiveFieldValue.createLong(100L)
                            ),
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(productType, "state"),
                                    ReferenceFieldValue.create(productNormalState)
                            )
                    )
            ));
            var directCouponType = queryClassType("AST立减优惠券");
            var couponStateType = queryClassType("优惠券状态");
            var couponNormalState = TestUtils.getEnumConstantByName(couponStateType, "未使用");
            var coupon = TestUtils.createInstanceWithCheck(instanceManager, InstanceDTO.createClassInstance(
                    TypeExpressions.getClassType(directCouponType.id()),
                    List.of(
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(directCouponType, "discount"),
                                    PrimitiveFieldValue.createLong(5L)
                            ),
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(directCouponType, "state"),
                                    ReferenceFieldValue.create(couponNormalState)
                            ),
                            InstanceFieldDTO.create(
                                    getFieldIdByCode(directCouponType, "product"),
                                    ReferenceFieldValue.create(product)
                            )
                    )
            ));
            var couponType = queryClassType("AST优惠券");
            var order = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                    TestUtils.getMethodRefByCode(productType, "buy"),
                    product.id(),
                    List.of(
                            PrimitiveFieldValue.createLong(1L),
                            InstanceFieldValue.of(InstanceDTO.createArrayInstance(
                                    TypeExpressions.getReadWriteArrayType(TypeExpressions.getClassType(couponType.id())),
                                    false,
                                    List.of(ReferenceFieldValue.create(coupon))
                            ))
                    )
            )));
            var orderType = queryClassType("AST订单");
            var price = (long) ((PrimitiveFieldValue) order.getFieldValue(getFieldIdByCode(orderType, "price"))).getValue();
            var orderCoupons = ((InstanceFieldValue) order.getFieldValue(getFieldIdByCode(orderType, "coupons"))).getInstance();
            Assert.assertEquals(1, orderCoupons.getListSize());
            Assert.assertEquals(95, price);
        });
    }

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
            LOGGER.info(sysLoginResult2.toString());
            var loginInfo = loginService.verify(requireNonNull(sysLoginResult2.token()));
            LOGGER.info(loginInfo.toString());
            AUTH_CONFIG = new AuthConfig(sysApp.appId(), "admin", "123456");
        });
        compileTwice(USERS_SOURCE_ROOT);
//        compile(USERS_SOURCE_ROOT);
        submit(() -> {
            var profiler = ContextUtil.getProfiler();
            try (var ignored = profiler.enter("submit")) {
                var roleType = queryClassType("LabRole");
                var roleReadWriteListType = TypeExpressionBuilder.fromKlassId(roleType.id()).readWriteList().build();
                var roleNameFieldId = getFieldIdByCode(roleType, "name");
                var role = doInTransaction(() -> flowExecutionService.execute(
                        new FlowExecutionRequest(
                                TestUtils.getMethodRef(roleType, "LabRole", "string"),
                                null,
                                List.of(PrimitiveFieldValue.createString("admin"))
                        )
                ));
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
                var platformUser = doInTransaction(() -> flowExecutionService.execute(
                        new FlowExecutionRequest(
                                TestUtils.getMethodRefByCode(platformUserType, "register"),
                                null,
                                List.of(
                                        InstanceFieldValue.of(
                                                InstanceDTO.createClassInstance(
                                                        TypeExpressions.getClassType(registerRequestType.id()),
                                                        List.of(
                                                                InstanceFieldDTO.create(
                                                                        getFieldIdByCode(registerRequestType, "loginName"),
                                                                        PrimitiveFieldValue.createString(email)
                                                                ),
                                                                InstanceFieldDTO.create(
                                                                        getFieldIdByCode(registerRequestType, "name"),
                                                                        PrimitiveFieldValue.createString("lyq")
                                                                ),
                                                                InstanceFieldDTO.create(
                                                                        getFieldIdByCode(registerRequestType, "password"),
                                                                        PrimitiveFieldValue.createString("123456")
                                                                ),
                                                                InstanceFieldDTO.create(
                                                                        getFieldIdByCode(registerRequestType, "verificationCode"),
                                                                        PrimitiveFieldValue.createString(verificationCode)
                                                                )
                                                        )

                                                )
                                        )
                                )
                        )
                ));
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
                var application = doInTransaction(() -> flowExecutionService.execute(
                        new FlowExecutionRequest(
                                TestUtils.getMethodRefByCode(userApplicationType, "create"),
                                null,
                                List.of(
                                        PrimitiveFieldValue.createString("lab"),
                                        ReferenceFieldValue.create(platformUser)
                                )
                        )
                ));
                var reloadedPlatformUser = instanceManager.get(platformUser.id(), 1).instance();
                var joinedApplications = ((InstanceFieldValue) reloadedPlatformUser.getFieldValue(platformUserApplicationsFieldId)).getInstance();
                Assert.assertEquals(1, joinedApplications.getListSize());
                Assert.assertEquals(application.id(), joinedApplications.getElement(0).referenceId());

                // get PlatformApplication
                var platformApplicationType = queryClassType("PlatformApplication");
                var platformApplication = doInTransaction(() -> flowExecutionService.execute(
                        new FlowExecutionRequest(
                                TestUtils.getMethodRefByCode(platformApplicationType, "getInstance"),
                                null,
                                List.of()
                        )
                ));

                var loginResultType = queryClassType("LabLoginResult");

                // login
                var token = login(userType, loginResultType, platformApplication, email, "123456");

                var enterAppMethodRef = TestUtils.getStaticMethodRef(platformUserType, "enterApp",
                        TypeExpressions.getClassType(platformUserType.id()), TypeExpressions.getClassType(userApplicationType.id()));

                // enter application
                var loginResult = doInTransaction(() -> flowExecutionService.execute(
                        new FlowExecutionRequest(
                                enterAppMethodRef,
                                null,
                                List.of(
                                        ReferenceFieldValue.create(platformUser),
                                        ReferenceFieldValue.create(application)
                                )
                        )
                ));
                token = (String) ((PrimitiveFieldValue) loginResult.getFieldValue(getFieldIdByCode(loginResultType, "token"))).getValue();
                Assert.assertNotNull(token);

                // test leave application
                var platformUserListType = TypeExpressions.getListType(TypeExpressions.getClassType(platformUserType.id()));
                var platformUserReadWriteListType = TypeExpressionBuilder.fromKlassId(platformUserType.id()).readWriteList().build();
                var leaveAppMethodRef = TestUtils.getStaticMethodRef(platformUserType, "leaveApp",
                        platformUserListType, TypeExpressions.getClassType(userApplicationType.id()));

                try {
                    doInTransaction(() -> flowExecutionService.execute(
                            new FlowExecutionRequest(
                                    leaveAppMethodRef,
                                    null,
                                    List.of(
                                            new InstanceFieldValue(
                                                    null,
                                                    InstanceDTO.createListInstance(
                                                            platformUserReadWriteListType,
                                                            false,
                                                            List.of(ReferenceFieldValue.create(platformUser))
                                                    )
                                            ),
                                            ReferenceFieldValue.create(application))
                            )
                    ));
                    Assert.fail("应用所有人无法退出应用");
                } catch (FlowExecutionException e) {
                    Assert.assertEquals("应用所有人无法退出应用", e.getMessage());
                }

                // create a platform user to join the application and then leave
                var anotherPlatformUser = doInTransaction(() -> flowExecutionService.execute(
                        new FlowExecutionRequest(
                                TestUtils.getMethodRefByCode(platformUserType, "LabPlatformUser"),
                                null,
                                List.of(
                                        PrimitiveFieldValue.createString("lyq2"),
                                        PrimitiveFieldValue.createString("123456"),
                                        PrimitiveFieldValue.createString("lyq2"),
                                        new InstanceFieldValue(
                                                null,
                                                InstanceDTO.createListInstance(
                                                        roleReadWriteListType,
                                                        false,
                                                        List.of(ReferenceFieldValue.create(role))
                                                )
                                        )
                                )
                        )
                ));

                // send invitation
                var appInvitationRequestType = queryClassType("LabAppInvitationRequest");
                doInTransaction(() -> flowExecutionService.execute(
                        new FlowExecutionRequest(
                                TestUtils.getMethodRefByCode(userApplicationType, "invite"),
                                null,
                                List.of(
                                        InstanceFieldValue.of(
                                                InstanceDTO.createClassInstance(
                                                        TypeExpressions.getClassType(appInvitationRequestType.id()),
                                                        List.of(
                                                                InstanceFieldDTO.create(
                                                                        getFieldIdByCode(appInvitationRequestType, "application"),
                                                                        ReferenceFieldValue.create(application)
                                                                ),
                                                                InstanceFieldDTO.create(
                                                                        getFieldIdByCode(appInvitationRequestType, "user"),
                                                                        ReferenceFieldValue.create(anotherPlatformUser)
                                                                ),
                                                                InstanceFieldDTO.create(
                                                                        getFieldIdByCode(appInvitationRequestType, "isAdmin"),
                                                                        PrimitiveFieldValue.createBoolean(true)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                ));

                // Login as anotherPlatformUser
                login(userType, loginResultType, platformApplication, "lyq2", "123456");

                // query the latest message
                var messageType = queryClassType("LabMessage", List.of(ClassKind.CLASS.code()));
                var messageList = instanceManager.query(
                        new InstanceQueryDTO(
                                TypeExpressions.getClassType(messageType.id()),
                                null,
                                null,
                                "接受者 = $$" + anotherPlatformUser.id(),
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
                doInTransaction(() -> flowExecutionService.execute(
                        new FlowExecutionRequest(
                                TestUtils.getMethodRefByCode(messageType, "read"),
                                null,
                                List.of(ReferenceFieldValue.create(message))
                        )
                ));

                // get invitationId from the message
                var messageTargetFieldId = getFieldIdByCode(messageType, "target");

                // accept invitation
                doInTransaction(() -> flowExecutionService.execute(
                                new FlowExecutionRequest(
                                        TestUtils.getMethodRefByCode(userApplicationType, "acceptInvitation"),
                                        null,
                                        List.of(
                                                message.getFieldValue(messageTargetFieldId))
                                )
                        )
                );

                // assert that the user has joined the application
                var reloadedAnotherPlatformUser = instanceManager.get(anotherPlatformUser.id(), 1).instance();
                var anotherJoinedApplications = ((InstanceFieldValue) reloadedAnotherPlatformUser.getFieldValue(platformUserApplicationsFieldId)).getInstance();
                Assert.assertEquals(1, anotherJoinedApplications.getListSize());
                loginResult = doInTransaction(() -> flowExecutionService.execute(
                        new FlowExecutionRequest(
                                enterAppMethodRef,
                                null,
                                List.of(
                                        ReferenceFieldValue.create(anotherPlatformUser),
                                        ReferenceFieldValue.create(application)
                                )
                        )
                ));
                loginResultType = queryClassType("LabLoginResult");
                token = (String) ((PrimitiveFieldValue) loginResult.getFieldValue(getFieldIdByCode(loginResultType, "token"))).getValue();
                Assert.assertNotNull(token);

                // test leaving the application
                doInTransaction(() -> flowExecutionService.execute(
                        new FlowExecutionRequest(
                                leaveAppMethodRef,
                                null,
                                List.of(
                                        InstanceFieldValue.of(
                                                InstanceDTO.createListInstance(
                                                        platformUserReadWriteListType,
                                                        false,
                                                        List.of(ReferenceFieldValue.create(anotherPlatformUser))
                                                )
                                        ),
                                        ReferenceFieldValue.create(application)
                                )
                        )
                ));

                // assert that the user has left the application
                var reloadedAnotherPlatformUser2 = instanceManager.get(anotherPlatformUser.id(), 1).instance();
                var anotherJoinedApplications2 = ((InstanceFieldValue) reloadedAnotherPlatformUser2.getFieldValue(platformUserApplicationsFieldId)).getInstance();
                Assert.assertEquals(0, anotherJoinedApplications2.getListSize());
                try {
                    doInTransaction(() -> flowExecutionService.execute(
                            new FlowExecutionRequest(
                                    enterAppMethodRef,
                                    null,
                                    List.of(
                                            ReferenceFieldValue.create(anotherPlatformUser),
                                            ReferenceFieldValue.create(application)
                                    )
                            )
                    ));
                    Assert.fail("用户未加入应用无法进入");
                } catch (FlowExecutionException e) {
                    Assert.assertEquals("用户未加入应用无法进入", e.getMessage());
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
                var user = doInTransaction(() -> flowExecutionService.execute(
                        new FlowExecutionRequest(
                                TestUtils.getMethodRefByCode(userType, "LabUser"),
                                null,
                                List.of(
                                        PrimitiveFieldValue.createString("leen"),
                                        PrimitiveFieldValue.createString("123456"),
                                        PrimitiveFieldValue.createString("leen"),
                                        InstanceFieldValue.of(
                                                InstanceDTO.createListInstance(
                                                        roleReadWriteListType,
                                                        false,
                                                        List.of(ReferenceFieldValue.create(role))
                                                )
                                        ),
                                        ReferenceFieldValue.create(application)
                                )
                        )
                ));

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
                token = login(userType, loginResultType, application, "leen", "123456");

                // test login with too many attempts
                for (int i = 0; i < 5; i++) {
                    try {
                        login(userType, loginResultType, application, "leen", "123123", "192.168.0.1", false);
                        if (i == 4) {
                            Assert.fail("登录尝试次数过多，应该抛出异常");
                        }
                    } catch (FlowExecutionException e) {
                        Assert.assertEquals("登录尝试次数过多，请稍后再试", e.getMessage());
                    }
                }

                // execute the LabUser.verify method and check verification result
                var tokenValue = createTokenValue(tokenType, application, token);
                var loginInfo = verify(userType, tokenValue);
                var loginInfoType = queryClassType("LabLoginInfo");
                assertNoError(loginInfoType);
                var applicationFieldId = getFieldIdByCode(loginInfoType, "application");
                var userFieldId = getFieldIdByCode(loginInfoType, "user");
                Assert.assertEquals(application.id(), ((ReferenceFieldValue) loginInfo.getFieldValue(applicationFieldId)).getId());
                Assert.assertEquals(user.id(), ((ReferenceFieldValue) loginInfo.getFieldValue(userFieldId)).getId());

                // test logout
                doInTransaction(() -> flowExecutionService.execute(
                        new FlowExecutionRequest(
                                TestUtils.getMethodRefByCode(userType, "logout"),
                                null,
                                List.of(
                                        InstanceFieldValue.of(
                                                InstanceDTO.createListInstance(
                                                        tokenReadWriteListType,
                                                        false,
                                                        List.of(tokenValue)
                                                )
                                        )
                                )
                        )
                ));

                // verify that the token has been invalidated
                assertTokenInvalidated(userType, tokenValue, applicationFieldId);

                // login again
                token = login(userType, loginResultType, platformApplication, email, "123456");
                var tokenValue2 = createTokenValue(tokenType, platformApplication, token);

//            // test logout platform user
                logout(platformUserType);

                // assert that the token has been invalidated
                assertTokenInvalidated(userType, tokenValue, applicationFieldId);

                // test changePassword
                sendVerificationCode(verificationCodeType, email);

                var changePasswordRequestType = queryClassType("LabChangePasswordRequest");
                doInTransaction(() -> flowExecutionService.execute(
                        new FlowExecutionRequest(
                                TestUtils.getMethodRefByCode(platformUserType, "changePassword"),
                                null,
                                List.of(
                                        InstanceFieldValue.of(
                                                InstanceDTO.createClassInstance(
                                                        TypeExpressions.getClassType(changePasswordRequestType.id()),
                                                        List.of(
                                                                InstanceFieldDTO.create(
                                                                        getFieldIdByCode(changePasswordRequestType, "verificationCode"),
                                                                        PrimitiveFieldValue.createString(getLastSentEmailContent())
                                                                ),
                                                                InstanceFieldDTO.create(
                                                                        getFieldIdByCode(changePasswordRequestType, "loginName"),
                                                                        PrimitiveFieldValue.createString(email)
                                                                ),
                                                                InstanceFieldDTO.create(
                                                                        getFieldIdByCode(changePasswordRequestType, "password"),
                                                                        PrimitiveFieldValue.createString("888888")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                ));
                var token2 = login(userType, loginResultType, platformApplication, email, "123456", "127.0.0.1", false);
                Assert.assertNull(token2);
                login(userType, loginResultType, platformApplication, email, "888888");
            }
            System.out.println(profiler.finish(false, true).output());
        });
    }

    private void sendVerificationCode(TypeDTO verificationCodeType, String email) {
        doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        TestUtils.getMethodRefByCode(verificationCodeType, "sendVerificationCode"),
                        null,
                        List.of(
                                PrimitiveFieldValue.createString(email),
                                PrimitiveFieldValue.createString("MetaVM验证码"),
                                PrimitiveFieldValue.createString("127.0.0.1")
                        )
                )
        ));
    }

    private String getLastSentEmailContent() {
        return Objects.requireNonNull(MockEmailSender.INSTANCE.getLastSentEmail()).content();
    }

    private void logout(TypeDTO platformUserType) {
        doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        TestUtils.getMethodRefByCode(platformUserType, "logout"),
                        null,
                        List.of()
                )
        ));
    }

    private void assertTokenInvalidated(TypeDTO userType, FieldValue tokenValue, String applicationFieldId) {
        var loginInfo = verify(userType, tokenValue);
        Assert.assertNull(((PrimitiveFieldValue) loginInfo.getFieldValue(applicationFieldId)).getValue());
    }

    private InstanceDTO verify(TypeDTO userType, FieldValue tokenValue) {
        return doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        TestUtils.getMethodRefByCode(userType, "verify"),
                        null,
                        List.of(tokenValue)
                )
        ));
    }

    private String login(TypeDTO userType, TypeDTO loginResultType, InstanceDTO platformApplication, String loginName, @SuppressWarnings("SameParameterValue") String password) {
        return login(userType, loginResultType, platformApplication, loginName, password, "127.0.0.1", true);
    }

    private String login(TypeDTO userType, TypeDTO loginResultType, InstanceDTO platformApplication, String loginName, String password, String clientIP, boolean checkToken) {
        var loginResult = doInTransaction(() -> flowExecutionService.execute(
                new FlowExecutionRequest(
                        TestUtils.getMethodRefByCode(userType, "login"),
                        null,
                        List.of(
                                ReferenceFieldValue.create(platformApplication),
                                PrimitiveFieldValue.createString(loginName),
                                PrimitiveFieldValue.createString(password),
                                PrimitiveFieldValue.createString(clientIP)
                        )
                )
        ));
        var token = (String) ((PrimitiveFieldValue) loginResult.getFieldValue(getFieldIdByCode(loginResultType, "token"))).getValue();
        if (checkToken)
            Assert.assertNotNull(token);
        return token;
    }

    private FieldValue createTokenValue(TypeDTO tokenType, InstanceDTO application, String token) {
        return InstanceFieldValue.of(InstanceDTO.createClassInstance(
                TypeExpressions.getClassType(tokenType.id()),
                List.of(
                        InstanceFieldDTO.create(
                                getFieldIdByCode(tokenType, "application"),
                                ReferenceFieldValue.create(application)
                        ),
                        InstanceFieldDTO.create(
                                getFieldIdByCode(tokenType, "token"),
                                PrimitiveFieldValue.createString(token)
                        )
                )
        ));
    }

//    public void testResend() {
//        LoginUtils.loginWithAuthFile(AUTH_FILE, typeClient);
//        var request = NncUtils.readJsonFromFile(REQUEST_FILE, BatchSaveRequest.class);
//        HttpUtils.post("/type/batch-save", request, new TypeReference<List<Long>>() {
//        });
//    }

}