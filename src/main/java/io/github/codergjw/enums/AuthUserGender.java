package io.github.codergjw.enums;import io.github.codergjw.utils.StringUtils;import lombok.AllArgsConstructor;import lombok.Getter;import java.util.Arrays;/** * @ClassName: AuthUserGender * @Author: 小飞 * @Date: 2023/5/9 21:04 * @Description: 用户性别 因为不同的平台使用的不同符号代表不同性别 */@Getter@AllArgsConstructorpublic enum AuthUserGender {    /**     * MALE/FAMALE为正常值，通过{@link AuthUserGender#getRealGender(String)}方法获取真实的性别     * UNKNOWN为容错值，部分平台不会返回用户性别，为了方便统一，使用UNKNOWN标记所有未知或不可测的用户性别信息     */    MALE("1", "男"),    FEMALE("0", "女"),    UNKNOWN("-1", "未知");    private String code;    private String desc;    /**     * 获取用户的实际性别，常规网站     *     * @param originalGender 用户第三方标注的原始性别     * @return 用户性别     */    public static AuthUserGender getRealGender(String originalGender) {        if (null == originalGender || UNKNOWN.getCode().equals(originalGender)) {            return UNKNOWN;        }        String[] males = {"m", "男", "1", "male"};        if (Arrays.asList(males).contains(originalGender.toLowerCase())) {            return MALE;        }        return FEMALE;    }    /**     * 获取微信平台用户的实际性别，0表示未定义，1表示男性，2表示女性     *     * @param originalGender 用户第三方标注的原始性别     * @return 用户性别     */    public static AuthUserGender getWechatRealGender(String originalGender) {        if (StringUtils.isEmpty(originalGender) || "0".equals(originalGender)) {            return AuthUserGender.UNKNOWN;        }        return getRealGender(originalGender);    }}