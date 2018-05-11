package com.lmj.stone.idl;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/8/11.
 */
public final class IDLReturnType implements Serializable {
    private static final long serialVersionUID = 645098260044178465L;

    public String type;//参数类型描述
    public String desc;//参数描述
    public boolean isArray;//数组支持,标准接口定义,只需要一种形式

    public String getCoreType() {return IDLT.convertCoreType(type);}

    public String getFinalType() {
        return IDLT.convertFinalType(type,false,isArray);
    }

    public String getDeclareType() {
        return IDLT.convertDeclareType(getFinalType());
    }

    public String getDisplayType() {
        return IDLT.convertDisplayType(getFinalType());
    }

    @Override
    public String toString() {
        return "return{type:"+type+",desc"+desc+",isArray:"+isArray+"}";
    }
}
