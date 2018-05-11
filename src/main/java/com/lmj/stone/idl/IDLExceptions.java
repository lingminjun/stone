package com.lmj.stone.idl;

/**
 * Created by lingminjun on 17/4/22.
 */
public final class IDLExceptions {
    public final static String IDL_EXCEPTION_DOMAIN = "IDL";

    public final static int UNKNOWN_ERROR_CODE = -100;//"服务端返回未知错误"
    public static IDLException UNKNOWN_ERROR(String reason) {
        return new IDLException("未知错误",IDL_EXCEPTION_DOMAIN,UNKNOWN_ERROR_CODE,reason);
    }

    public final static int INTERNAL_SERVER_ERROR_CODE = -101;
    public final static IDLException INTERNAL_SERVER_ERROR(String reason) {
        return new IDLException("服务器异常",IDL_EXCEPTION_DOMAIN,INTERNAL_SERVER_ERROR_CODE,reason);
    }

    public final static int DUBBO_SERVICE_NOTFOUND_CODE = -107;
    public static IDLException DUBBO_SERVICE_NOTFOUND(String reason) {
        return new IDLException("服务未找到",IDL_EXCEPTION_DOMAIN,DUBBO_SERVICE_NOTFOUND_CODE,reason);
    }

    public final static int DUBBO_NETWORK_EXCEPTION_CODE = -110;
    public static IDLException DUBBO_NETWORK_EXCEPTION(String reason) {
        return new IDLException("服务网络异常",IDL_EXCEPTION_DOMAIN,DUBBO_NETWORK_EXCEPTION_CODE,reason);
    }

    public final static int DUBBO_BIZ_EXCEPTION_CODE = -111;
    public static IDLException DUBBO_BIZ_EXCEPTION(String reason) {
        return new IDLException("服务内部异常",IDL_EXCEPTION_DOMAIN,DUBBO_BIZ_EXCEPTION_CODE,reason);
    }

    public final static int DUBBO_FORBIDDEN_EXCEPTION_CODE = -112;
    public static IDLException DUBBO_FORBIDDEN_EXCEPTION(String reason) {
        return new IDLException("服务请求被拒绝",IDL_EXCEPTION_DOMAIN,DUBBO_FORBIDDEN_EXCEPTION_CODE,reason);
    }

    public final static int DUBBO_SERVICE_TIMEOUT_CODE = -108;
    public static IDLException DUBBO_SERVICE_TIMEOUT(String reason) {
        return new IDLException("服务请求超时",IDL_EXCEPTION_DOMAIN,DUBBO_SERVICE_TIMEOUT_CODE,reason);
    }

    public final static int DUBBO_SERVICE_ERROR_CODE = -109;
    public static IDLException DUBBO_SERVICE_ERROR(String reason) {
        return new IDLException("未知错误",IDL_EXCEPTION_DOMAIN,DUBBO_SERVICE_ERROR_CODE,reason);
    }



    public final static int UNKNOWN_METHOD_CODE = -120;
    public static IDLException UNKNOWN_METHOD(String reason) {
        return new IDLException("服务未找到",IDL_EXCEPTION_DOMAIN,UNKNOWN_METHOD_CODE,reason);
    }

    public final static int PARSE_ERROR_CODE = -200;//"解析错误"
    public static IDLException PARSE_ERROR(String reason) {
        return new IDLException("请求解析错误",IDL_EXCEPTION_DOMAIN,PARSE_ERROR_CODE,reason);
    }

    public final static int PARAMETER_ERROR_CODE = -140;
    public static IDLException PARAMETER_ERROR(String reason) {
        return new IDLException("参数错误",IDL_EXCEPTION_DOMAIN,PARAMETER_ERROR_CODE,reason);
    }

    public final static int ACCESS_DENIED_CODE = -160;
    public static IDLException ACCESS_DENIED(String reason) {
        return new IDLException("访问被拒绝",IDL_EXCEPTION_DOMAIN,ACCESS_DENIED_CODE,reason);
    }

    public final static int NEED_CAPTCHA_CODE = -162;//人机验证,可以采用多个手段,图片或者滑块
    public static IDLException NEED_CAPTCHA(String reason) {
        return new IDLException("检查到访问异常",IDL_EXCEPTION_DOMAIN,NEED_CAPTCHA_CODE,reason);
    }

    public final static int SIGNATURE_ERROR_CODE = -180;
    public static IDLException SIGNATURE_ERROR(String reason) {
        return new IDLException("签名错误",IDL_EXCEPTION_DOMAIN,SIGNATURE_ERROR_CODE,reason);
    }

    public final static int ILLEGAL_MULTIAPI_ASSEMBLY_CODE = -190;
    public static IDLException ILLEGAL_MULTIAPI_ASSEMBLY(String reason) {
        return new IDLException("非法的请求组合",IDL_EXCEPTION_DOMAIN,ILLEGAL_MULTIAPI_ASSEMBLY_CODE,reason);
    }

    public final static int SERIALIZE_FAILED_CODE = -102;
    public static IDLException SERIALIZE_FAILED(String reason) {
        return new IDLException("解析数据出错",IDL_EXCEPTION_DOMAIN,SERIALIZE_FAILED_CODE,reason);
    }

    public final static int TOKEN_EXPIRED_CODE = -300;
    public static IDLException TOKEN_EXPIRED(String reason) {
        return new IDLException("token已过期",IDL_EXCEPTION_DOMAIN,TOKEN_EXPIRED_CODE,reason);
    }

    public final static int TOKEN_ERROR_CODE = -360;
    public static IDLException TOKEN_ERROR(String reason) {
        return new IDLException("token错误",IDL_EXCEPTION_DOMAIN,TOKEN_ERROR_CODE,reason);
    }

    public final static int MOCKER_FAILED_CODE = -441;
    public static IDLException MOCKER_FAILED(String reason) {
        return new IDLException("MOCKER调用出错",IDL_EXCEPTION_DOMAIN,MOCKER_FAILED_CODE,reason);
    }
}
