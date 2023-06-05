package cn.wen.oauth.request;import cn.wen.oauth.cache.AuthStateCache;import cn.wen.oauth.config.AuthConfig;import cn.wen.oauth.config.AuthDefaultSource;import cn.wen.oauth.entity.AuthCallback;import cn.wen.oauth.entity.AuthToken;import cn.wen.oauth.entity.AuthUser;import cn.wen.oauth.enums.AuthUserGender;import com.alibaba.fastjson.JSONObject;/** * @ClassName: AuthAliyunRequest * @Author: 小飞 * @Date: 2023/5/11 14:30 * @Description: 阿里云登录请求处理类 */public class AuthAliyunRequest extends AuthDefaultRequest{    public AuthAliyunRequest(AuthConfig config) {        super(config, AuthDefaultSource.ALIYUN);    }    public AuthAliyunRequest(AuthConfig config, AuthStateCache authStateCache) {        super(config, AuthDefaultSource.ALIYUN, authStateCache);    }    @Override    protected AuthToken getAccessToken(AuthCallback authCallback) {        String response = doPostAuthorizationCode(authCallback.getCode());        JSONObject accessTokenObject = JSONObject.parseObject(response);        return AuthToken.builder()                .accessToken(accessTokenObject.getString("access_token"))                .expireIn(accessTokenObject.getIntValue("expires_in"))                .tokenType(accessTokenObject.getString("token_type"))                .idToken(accessTokenObject.getString("id_token"))                .refreshToken(accessTokenObject.getString("refresh_token"))                .build();    }    @Override    protected AuthUser getUserInfo(AuthToken authToken) {        String userInfo = doGetUserInfo(authToken);        JSONObject object = JSONObject.parseObject(userInfo);        return AuthUser.builder()                .rawUserInfo(object)                .uuid(object.getString("sub"))                .username(object.getString("login_name"))                .gender(AuthUserGender.UNKNOWN)                .token(authToken)                .source(source.toString())                .build();    }}