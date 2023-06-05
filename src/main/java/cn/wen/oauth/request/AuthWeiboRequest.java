package cn.wen.oauth.request;import cn.wen.oauth.cache.AuthStateCache;import cn.wen.oauth.config.AuthConfig;import cn.wen.oauth.config.AuthDefaultSource;import cn.wen.oauth.entity.AuthCallback;import cn.wen.oauth.entity.AuthResponse;import cn.wen.oauth.entity.AuthToken;import cn.wen.oauth.entity.AuthUser;import cn.wen.oauth.enums.AuthResponseStatus;import cn.wen.oauth.enums.AuthUserGender;import cn.wen.oauth.enums.scope.AuthWeiboScope;import cn.wen.oauth.exception.AuthException;import cn.wen.oauth.utils.*;import com.alibaba.fastjson.JSONObject;import com.xkcoding.http.support.HttpHeader;/** * @ClassName: AuthWeiboRequest * @Author: 小飞 * @Date: 2023/5/10 20:01 * @Description: 微博登录权限请求处理 */public class AuthWeiboRequest extends AuthDefaultRequest{    public AuthWeiboRequest(AuthConfig config) {        super(config, AuthDefaultSource.WEIBO);    }    public AuthWeiboRequest(AuthConfig config, AuthStateCache authStateCache) {        super(config, AuthDefaultSource.WEIBO, authStateCache);    }    @Override    protected AuthToken getAccessToken(AuthCallback authCallback) {        String response = doPostAuthorizationCode(authCallback.getCode());        JSONObject accessTokenObject = JSONObject.parseObject(response);        if (accessTokenObject.containsKey("error")) {            throw new AuthException(accessTokenObject.getString("error_description"));        }        return AuthToken.builder()                .accessToken(accessTokenObject.getString("access_token"))                .uid(accessTokenObject.getString("uid"))                .openId(accessTokenObject.getString("uid"))                .expireIn(accessTokenObject.getIntValue("expires_in"))                .build();    }    @Override    protected AuthUser getUserInfo(AuthToken authToken) {        // 三个必要参数拼接成URL来请求数据        String accessToken = authToken.getAccessToken();        String uid = authToken.getUid();        String oauthParam = String.format("uid=%s&access_token=%s", uid, accessToken);        HttpHeader httpHeader = new HttpHeader();        httpHeader.add("Authorization", "OAuth2 " + oauthParam);        httpHeader.add("API-RemoteIP", IpUtils.getLocalIp());        String userInfo = new HttpUtils(config.getHttpConfig())                .get(userInfoUrl(authToken), null, httpHeader, false).getBody();        JSONObject object = JSONObject.parseObject(userInfo);        if (object.containsKey("error")) {            throw new AuthException(object.getString("error"));        }        return AuthUser.builder()                .rawUserInfo(object)                .uuid(object.getString("id"))                .username(object.getString("name"))                .avatar(object.getString("profile_image_url"))                .blog(StringUtils.isEmpty(object.getString("url")) ? "https://weibo.com/" + object.getString("profile_url") : object                        .getString("url"))                .nickname(object.getString("screen_name"))                .location(object.getString("location"))                .remark(object.getString("description"))                .gender(AuthUserGender.getRealGender(object.getString("gender")))                .token(authToken)                .source(source.toString())                .build();    }    /**     * 返回获取userInfo的url     *     * @param authToken authToken     * @return 返回获取userInfo的url     */    @Override    protected String userInfoUrl(AuthToken authToken) {        return UrlBuilder.fromBaseUrl(source.userInfo())                .queryParam("access_token", authToken.getAccessToken())                .queryParam("uid", authToken.getUid())                .build();    }    @Override    public String authorize(String state) {        return UrlBuilder.fromBaseUrl(super.authorize(state))                .queryParam("scope", this.getScopes(",", false, AuthScopeUtils.getDefaultScopes(AuthWeiboScope.values())))                .build();    }    /**     * 取消授权     * @param authToken 登录成功后返回的Token信息     * @return     */    @Override    public AuthResponse revoke(AuthToken authToken) {        String response = doGetRevoke(authToken);        JSONObject object = JSONObject.parseObject(response);        if (object.containsKey("error")) {            return AuthResponse.builder()                    .code(AuthResponseStatus.FAILURE.getCode())                    .msg(object.getString("error"))                    .build();        }        // 返回 result = true 表示取消授权成功，否则失败        AuthResponseStatus status = object.getBooleanValue("result") ?                AuthResponseStatus.SUCCESS : AuthResponseStatus.FAILURE;        return AuthResponse.builder().code(status.getCode()).msg(status.getMsg()).build();    }}