package com.lmj.stone.idl;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/8/11.
 * 用于IDL接口返回码描述(idl一部分)
 */
public final class IDLAPICode implements Serializable {
    private static final long serialVersionUID = -3231070107106910462L;

    public int code;//错误码
    public String desc;//错误码描述
    public String domain;//所属域名

    @Override
    public int hashCode() {
        return ("" + domain + "." + code).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {return true;}
        if (obj instanceof IDLAPICode) {
            return this.code == ((IDLAPICode) obj).code && (this.domain != null ? this.domain.equals(((IDLAPICode) obj).domain) : false);
        }
        return false;
    }

    @Override
    public String toString() {
        return "error{domain:" +domain+ ",code:"+ code+",desc:"+desc+"}";
    }
}
