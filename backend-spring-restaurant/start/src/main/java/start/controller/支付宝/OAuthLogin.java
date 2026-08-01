package start.controller.支付宝;

import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import start.controller.支付宝.config.AlipayConfig;
import jakarta.servlet.http.HttpServletResponse;
import start.controller.支付宝.config.AlipayProperties;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/oauth")
public class OAuthLogin {
    // 新版沙箱授权域名：openauth-sandbox.dl.alipaydev.com
    private static final String AUTHORIZE_URL = "https://openauth-sandbox.dl.alipaydev.com/oauth2/publicAppAuthorize.htm";

    @Autowired
    private AlipayConfig alipayConfig;
    @Autowired
    private AlipayProperties alipayProperties;

    /** ① 生成授权链接并 302 自动跳转到支付宝授权页（浏览器直接访问即可开始授权） */
    @GetMapping("/authorize")
    public void authorize(@RequestParam String redirectUri, HttpServletResponse response) throws IOException {
        String authorizeUrl = AUTHORIZE_URL
                + "?app_id=" + alipayProperties.getAppId()
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&scope=auth_user";
        // 302 重定向，浏览器自动跳转到支付宝授权页
        response.sendRedirect(authorizeUrl);
    }

    /** ② 回调：auth_code 换 access_token / user_id，并拉取用户资料 */
    @GetMapping("/callback")
    public Map<String, Object> callback(@RequestParam("auth_code") String authCode) throws Exception {
        AlipayClient client = alipayConfig.getAlipayClient();

        // 1. auth_code 换 token
        AlipaySystemOauthTokenRequest tokenReq = new AlipaySystemOauthTokenRequest();
        tokenReq.setCode(authCode);
        tokenReq.setGrantType("authorization_code");
        AlipaySystemOauthTokenResponse tokenResp = client.execute(tokenReq);
        if (!tokenResp.isSuccess()) {
            log.error("支付宝授权失败: {}", tokenResp.getMsg());
            throw new IllegalStateException("支付宝授权失败：" + tokenResp.getMsg());
        }

        // 2. 用 access_token 拉用户资料（alipay.user.info.share，需沙箱应用已开通「获取会员信息」功能）
        AlipayUserInfoShareRequest userReq = new AlipayUserInfoShareRequest();
        userReq.putOtherTextParam("auth_token", tokenResp.getAccessToken());
        AlipayUserInfoShareResponse userResp = client.execute(userReq);

        Map<String, Object> result = new HashMap<>();
        result.put("openId", tokenResp.getOpenId());
        result.put("accessToken", tokenResp.getAccessToken());
        if (userResp.isSuccess()) {
            // 授权成功：把可获取的用户资料一并返回（以下字段均为可选项，未授权则可能为空）
            result.put("nickName", userResp.getNickName());      // 昵称
            result.put("avatar", userResp.getAvatar());          // 头像
            result.put("gender", userResp.getGender());          // 性别
            result.put("isCertified", userResp.getEmail());      //邮箱
        } else {
            // 拉取失败：打印错误码便于排查（常见原因：沙箱应用未开通「获取会员信息」功能）
            log.error("拉取支付宝用户资料失败 code={} subCode={} subMsg={} msg={}",
                    userResp.getCode(), userResp.getSubCode(), userResp.getSubMsg(), userResp.getMsg());
        }
        return result;
    }
}
/**
 *  ① 生成授权链接（GET）：
 *     http://localhost:8080/oauth/authorize?redirectUri=http://localhost:8080/oauth/callback
 *     后端 302 自动跳转到支付宝授权页：
 *     https://openauth-sandbox.dl.alipaydev.com/oauth2/publicAppAuthorize.htm?app_id=xxx&redirect_uri=xxx&scope=auth_user
 *  ② 在授权页用「沙箱买家账号」登录，点击同意授权。
 *  ③ 授权成功后浏览器自动跳回，地址栏变成：
 *     http://localhost:8080/oauth/callback?auth_code=xxxxxx
 */

