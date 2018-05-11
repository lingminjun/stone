package com.lmj.stone.idl;

import com.lmj.stone.utils.MD5;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/8/10.
 * 最终实现RPC基础类
 */
public final class IDLInvocation implements Serializable {

    private static final long serialVersionUID = -2784657834930449742L;

    /**
     * 拼接一个唯一表示key,作为RPC实现的uuid
     * scheme://serverName:serverPort/methodName;version?paramTypes
     * @return
     */
    public String getURI() {
        StringBuilder builder = new StringBuilder();
        builder.append(scheme.toLowerCase());//不区分大小写
        builder.append("://");
        builder.append(serverName.toLowerCase());//不区分大小写
        builder.append(":");
        builder.append(serverPort);
        builder.append("/");
        builder.append(methodName);
        if (version != null) {
            builder.append(";" + version);
        }
        if (paramTypes != null && paramTypes.length > 0) {
            builder.append("?");
            for (int i = 0; i < paramTypes.length; i++) {
                IDLField param = paramTypes[i];
                if (i != 0) {
                    builder.append("&");
                }
                builder.append("var" + i + "=" + param.getFinalType());
            }
        }
        return builder.toString();
    }

    /**
     * 拼接一个唯一表示key,作为RPC实现的uuid
     * scheme://serverName:serverPort/methodName;version?paramTypes&timeout=timeout&retries=retries
     * @return
     */
    public String getURL() {

        StringBuilder builder = new StringBuilder();
        builder.append(getURI());
        if (paramTypes == null || paramTypes.length == 0) {
            builder.append("?");
        } else {
            builder.append("&");
        }
        builder.append("timeout="+timeout + "&retries="+retries);

        return builder.toString();
    }

    /**
     * (protocol uri).md5
     * 返回唯一id
     * @return
     */
    public String getMD5() {
        return MD5.md5("" + protocol.toLowerCase() + " " + getURI());
    }


    //invoke基本描述
    public String protocol;//Dubbo、Http、其他自定义协议:如利用netty、tcp封装的自定义协议
    public String scheme;//协议头
    public String serverName;//服务名
    public int serverPort;//服务端口
    public String methodName;//调用方法

    public String version;//接口版本
    public int timeout = 30000;//默认30秒
    public int retries;//不重试

    public String encoding;//编码方式 utf8、asii、传输内容解码方式
    public String serialization;//序列化方式 json、xml、binary; 返回值和参数统一

    public IDLField[] paramTypes;//参数类型列表

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("\turi: " + getURI() + "&timeout="+timeout + "&retries="+retries+"\n");
        builder.append("\tmd5: " + getMD5()+"\n");
        builder.append("\tencoding: " + encoding+"\n");
        builder.append("\tserialization: " + serialization+"\n");
        builder.append("\r\n");
        builder.append("\tObject ");
        builder.append(serverName + "." + methodName + "(");
        if (paramTypes != null) {
            for (int i = 0; i < paramTypes.length; i++) {
                IDLField param = paramTypes[i];
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(param.getDisplayType() + " " + (param.name != null ? param.name : "var" + i));
                if (param.defaultValue != null && param.defaultValue.length() > 0) {
                    builder.append(" = " + param.defaultValue);
                }
            }
        }
        builder.append(");\r\n");

        return builder.toString();
    }

}
