package org.metavm.user.rest.controller;

import org.metavm.context.http.Controller;
import org.metavm.context.http.Mapping;
import org.metavm.context.http.Post;
import org.metavm.context.http.RequestBody;
import org.metavm.user.LoginService;
import org.metavm.user.PlatformUserManager;
import org.metavm.user.Token;
import org.metavm.user.rest.dto.AuthenticateRequest;
import org.metavm.user.rest.dto.IssueTokenRequest;
import org.metavm.user.rest.dto.LogoutRequest;
import org.metavm.user.rest.dto.UserDTO;
import org.metavm.util.Constants;

import java.util.List;

@Controller
@Mapping("/internal-api/user")
public class UserInternalApi {

    private final LoginService loginService;
    private final PlatformUserManager platformUserManager;

    public UserInternalApi(LoginService loginService, PlatformUserManager platformUserManager) {
        this.loginService = loginService;
        this.platformUserManager = platformUserManager;
    }

    @Post("/save")
    public String save(@RequestBody UserDTO user) {
        return platformUserManager.save(user);
    }

    @Post("/issue-token")
    public String issueToken(@RequestBody IssueTokenRequest request) {
        return loginService.issueToken(Constants.PLATFORM_APP_ID, request.userId()).token();
    }

    @Post("/logout")
    public void logout(@RequestBody LogoutRequest request) {
        loginService.logout(List.of(new Token(Constants.PLATFORM_APP_ID, request.token())));
    }

    @Post("/authenticate")
    public String authenticate(@RequestBody AuthenticateRequest request) {
        return loginService.authenticate(new Token(Constants.PLATFORM_APP_ID, request.token())).userId();
    }

}
