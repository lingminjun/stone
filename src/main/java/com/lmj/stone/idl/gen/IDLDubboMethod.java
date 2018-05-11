package com.lmj.stone.idl.gen;

import com.lmj.stone.idl.IDLField;
import com.lmj.stone.idl.IDLInvocation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 17/4/23.
 */
public final class IDLDubboMethod implements Serializable {
    private static final long serialVersionUID = 888087124025783857L;

    public String dubbo;//服务名
    public String method;//方法
    public String version;//dubbo接口版本
    public int timeout = 30000;//默认30秒
    public int retries;//不重试
//    public long modifyAt;//最后一次编辑时间

    public List<IDLField> params = new ArrayList<IDLField>();//参数列表,顺序一致

    public IDLInvocation getInvocation() {
        IDLInvocation invocation = new IDLInvocation();
        invocation.protocol = "dubbo 4.0.5";
        invocation.scheme = "dubbo";
        invocation.serverName = this.dubbo;
        invocation.serverPort = 20880;
        invocation.methodName = this.method;
        invocation.timeout = this.timeout;
        invocation.retries = this.retries;
        invocation.version = this.version;
        invocation.encoding = "utf-8";
        invocation.serialization = "json";

        //参数部分
        invocation.paramTypes = this.params.toArray(new IDLField[0]);

        return invocation;
    }

    /**
     * 实际是要看方法签名,此处简化逻辑,仅仅看名字加参数个数,并不看参数类型,类型配合IDLAPIInfo来用
     * @return
     */
//    public String getUUID() {
//        if (StringUtils.isEmpty(this.dubbo)) {
//            return "";
//        }
//        //方法签名,
//        return dubbo + "." + method + "_" + (params == null ? 0 : params.length);
//    }

//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {return true;}
//        if (obj == null) {return false;}
//        if (IDLDubboMethod.class != obj.getClass()) {return false;}
//        String typeDesc = ((IDLDubboMethod)obj).getUUID();
//        if (typeDesc == null || typeDesc.equals("")) {
//            return false;
//        }
//        return typeDesc.equals(this.getUUID());
//    }
//
//    @Override
//    public int hashCode() {
//        return ("" + IDLDubboMethod.class + this.getUUID()).hashCode();
//    }
}
