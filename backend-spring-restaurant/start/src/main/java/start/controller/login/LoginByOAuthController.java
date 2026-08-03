package start.controller.login;

import common.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import start.controller.支付宝.OAuthLogin;

/**
 * 通过OAuth，第三方授权登录
 */
@RestController
@RequestMapping("/auth")
public class LoginByOAuthController {
    @PostMapping
    public Result loginByOAuth() {
        OAuthLogin oAuthLogin = new OAuthLogin();
        return Result.success(oAuthLogin);
    }
}
