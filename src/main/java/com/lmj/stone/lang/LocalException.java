package com.lmj.stone.lang;


import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by lingminjun on 17/2/7.
 * 运行时逻辑异常, 当前运行代码产生的异常
 */
public final class LocalException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = -4533417417926672855L;

    private final int code;   //错误码
    private final String msg; //错误描述,用于展示, 多语言支持
    private String l10n; //本地化展示
    private final String domain;//错误域
    private transient String reason;    //错误原因描述,真实原因
    private transient Throwable inner;  //内部错误

    public LocalException(String message, String domain, int code, Throwable cause) {
        super(message, cause);
        this.msg = message;
        this.l10n = I18N.l10n(message);
        this.code = code;
        this.domain = domain;
        this.inner = cause;
        this.reason = cause != null ? cause.getMessage() : message;
    }

    public LocalException(String message, String domain, int code, String reason) {
        super(message);
        this.msg = message;
        this.l10n = I18N.l10n(message);
        this.code = code;
        this.domain = domain;
        this.reason = reason != null ? reason : message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return msg;
    }

    /**
     * 本地化文案返回
     * @return
     */
    @Override
    public String getLocalizedMessage() {
        return l10n != null ? l10n : msg;
    }

    public String getDomain() {
        return domain;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setLocalizedMessage(String l10nMsg) {
        this.l10n = l10nMsg;
    }
    /**
     * 日志时,可以看core cause内容来定位问题
     * @return
     */
    public Throwable getCoreCause() {
        return inner != null ? inner : this;
    }

    public Exception setCoreCause(Throwable inner) {
        this.inner = inner;
        //内置cause原因,打印日志时可以将真实原因打印全
        if (getCause() == null && inner != null) {
            try {
                Field cause = Throwable.class.getDeclaredField("cause");
                cause.setAccessible(true);
                cause.set(this,inner);
            } catch (Throwable e) {}
        }
        return this;
    }


    @Override
    public String toString() {
        return "LogicException{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", l10n='" + l10n + '\'' +
                ", domain='" + domain + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
