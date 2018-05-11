package com.lmj.stone.idl;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by lingminjun on 17/8/11.
 * 用于IDL接口描述(IDL一部分)
 */
public final class IDLAPIDef implements Serializable {

    private static final long serialVersionUID = -2728823514971741755L;

    // 接口定义部分
    public String methodName;//方法名
    public IDLAPIParam[] params; //参数列表
    public IDLReturnType returned;//返回值,所有返回值都将是复合类型【IDL规范】

    // 接口其他信息
    public String domain;//所属服务
    public String module;//所属服务模块 为空时,module.methodName
    public String owner;//接口负责人
    public String version;//接口版本
    public String desc;//方法描述/*512字*/
    public String detail;//方法描述/*512字*/

    // 接口权限 0x8000,0000
    public int security; //@See IDLAPISecurity
//    public IDLAPISecurity security;//安全级别,意味着验签和验权
    //兼容网关1.0 合作方调用级别 IDLAPISecurity.Integrated是否让服务验签
    // false:验证由服务提供方完成
    // true:apigw负责签名验证
    @Deprecated
    public boolean needVerify = false;//

    // 所有对象struct定义描述 map<type,struct>
    public Map<String,IDLAPIStruct> structs;

    // 错误码 map<code,api_code>
    public Map<Integer,IDLAPICode> codes;

    /**
     * 返回唯一API统一id
     * 采用.主要是方便后面url中非保留在不需要编译
     * @return
     */
    public String getAPISelector() {
        if (module == null) {
            return "" + domain + "." + domain + "." + methodName;
        } else {
            return "" + domain + "." + module + "." + methodName;
        }
    }

    /**
     * 是否为open api接口
     * @return
     */
    public boolean isOpenAPI() {
        return IDLAPISecurity.Integrated.check(this.security);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("\t * owner: " + owner + "\n");
        builder.append("\t * domain: " + domain + "\n");
        builder.append("\t * version: " + version + "\n");
        builder.append("\t * desc: " + desc + "\n");
        builder.append("\t * security: " + security + "\n");
        builder.append("\t * detail: " + detail + "\n");
        builder.append("\t */\n");
        builder.append("\tfunction " + (returned != null ? returned.getDisplayType() : "void") + " ");
        if (module == null) {
            builder.append(domain + "." + methodName + "(");
        } else {
            builder.append(module + "." + methodName + "(");
        }
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                IDLAPIParam param = params[i];
                if (i > 0) {
                    builder.append(", ");
                }
                if (param.required) {
                    builder.append("@IDLRequired ");
                }
                builder.append(param.getDisplayType() + " " + param.name);
                if (param.defaultValue != null && param.defaultValue.length() > 0) {
                    builder.append(" = " + param.defaultValue);
                }
            }
        }
        builder.append(");\n\r\n");

        //结构展示
        if (structs != null) {
            Iterator<Map.Entry<String, IDLAPIStruct>> entries = structs.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, IDLAPIStruct> entry = entries.next();
                IDLAPIStruct struct = entry.getValue();
                if (struct.desc != null) {
                    builder.append("\t/* " + struct.desc + " */\n");
                }
                builder.append("\tstruct " + entry.getKey() + " {\n");
                if (struct.fields != null) {
                    for (IDLAPIParam param : struct.fields) {
                        builder.append("\t\t" + param.getDisplayType() + " " + param.name + ";");
                        if (param.desc != null) {
                            builder.append(" /* " + param.desc + " */\n");
                        } else {
                            builder.append("\n");
                        }
                    }
                }
                builder.append("\t}\r\n\r\n");
            }
        }

        if (codes != null) {
            //codes { domain_code : {code,desc}}
            Iterator<Map.Entry<Integer, IDLAPICode>> entries = codes.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<Integer, IDLAPICode> entry = entries.next();
                builder.append("\t"+entry.getValue().toString()+"\n");//简单处理
            }
        }

        return builder.toString();
    }
}
