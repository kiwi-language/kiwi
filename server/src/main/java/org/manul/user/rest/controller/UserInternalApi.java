package org.manul.user.rest.controller;

import org.manul.context.http.Controller;
import org.manul.context.http.Mapping;
import org.manul.context.http.Post;
import org.manul.context.http.RequestBody;
import org.manul.user.LoginService;
import org.manul.user.PlatformUserManager;
import org.manul.user.Token;
import org.manul.user.rest.dto.AuthenticateRequest;
import org.manul.user.rest.dto.IssueTokenRequest;
import org.manul.user.rest.dto.LogoutRequest;
import org.manul.user.rest.dto.UserDTO;
import org.manul.util.Constants;

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
