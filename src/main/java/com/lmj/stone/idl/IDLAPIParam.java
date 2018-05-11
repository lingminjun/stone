package com.lmj.stone.idl;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/8/11.
 * 用于IDL接口参数描述(IDL一部分)
 */
public final class IDLAPIParam implements Serializable {

    private static final long serialVersionUID = 171493272155466671L;

    public String type;//参数类型描述
    public String name;//参数名称
    public String desc;//参数描述
    public boolean required;//是否必传
    public String defaultValue;//默认值【必传时忽略默认值】
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
        return "param{type:"+type+",name:"+name+",desc"+desc+",isArray:"+isArray
                +",required:"+required+",default:"+defaultValue+"}";
    }
}
