package com.lmj.stone.idl;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/8/11.
 * 用于IDL接口参数返回值对象描述(IDL一部分)
 */
public final class IDLAPIStruct implements Serializable {

    private static final long serialVersionUID = 1312133579930085713L;

    public String type;//类型 java中尽量取包名全称, 自行配置,给自定义报名
    public String desc;//对象描述

    public IDLAPIParam[] fields; //属性列表

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("struct{type:"+type+",desc"+desc+",");
        builder.append("fields[");
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                IDLAPIParam field = fields[i];
                if (i > 0) {
                    builder.append(",");
                }
                builder.append(field.getDisplayType());
            }
        }
        builder.append("]");
        return "";
    }
}
