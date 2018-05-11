package com.lmj.stone.idl;

/**
 * 大小请限制在 int 最大值以内 0x8000,0000
 */
public enum IDLAPISecurity {

    /**
     * 测试用, 只对内网环境访问开放此权限
     */
    Test(-1),

    /**
     * 无认证, 用户无关的接口. eg. 无任何安全风险的接口
     */
    None(0x0000),

    /**
     * 设备认证, 验证设备签名. eg. 有一定安全风险但与用户无关的接口(发送下行短信密码)
     * 验证要素
     * 1. 设备token
     * 2. 设备签名
     */
    DeviceRegister(0x0001),

    /**
     * 账号认证，已拥有账号id 如oauth认证获取
     * 验证要素
     * 1. 账户token
     * 2. 设备签名
     */
    AccountLogin(0x0010),

    /**
     * 用户认证，已拥有用户id
     * 验证要素
     * 1. 用户token
     * 2. 设备签名
     */
    UserLogin(0x0100),

    /**
     * 第三方集成认证, 验证第三方证书签名
     * 颁发秘钥加签，公钥验证
     */
    Integrated(0x1000);



    public final int code;

    /**
     * @param code security16进制编码
     */
    IDLAPISecurity(int code) {
        this.code = code;
    }

    /**
     * 检查auth权限是否包含当前权限
     */
    public boolean check(int auth) {
        return (auth & code) == code;
    }

    /**
     * 判断auth权限是否为空
     */
    public static boolean isNone(int auth) {
        return auth == 0;
    }

}
