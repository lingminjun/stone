package com.lmj.stone.idl.gen;

import com.alibaba.fastjson.annotation.JSONField;
import com.lmj.stone.idl.IDLAPIParam;
import com.lmj.stone.idl.IDLField;
import com.lmj.stone.idl.IDLT;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/4/12.
 * 用于IDL接口结构属性描述
 */
public final class IDLFieldDesc implements Serializable {
    private static final long serialVersionUID = 968739920098447637L;

    public String type;//属性元素类型 class 全称
    public String name;//属性名
    public String desc;//描述
    public boolean isArray;//数组支持
    public boolean isList;//ArrayList支持

    @JSONField(serialize = false, deserialize = false)
    private transient Class<?> typeClass;//值,用于

    //记录属性时需要
    public boolean isInner;
    public boolean canEntrust;

    //记录参数时需要
    public boolean required = true;//
    public boolean autoInjected = false;
    public String defaultValue; //默认值不为空,则认为是必传参数
    public boolean isQuiet = false;

    @JSONField(serialize = false, deserialize = false)
    private transient Object value;//值,用于


    public void setTypeClass(Class typeClass) {
        this.typeClass = typeClass;
        this.type = typeClass.getName();
        this.isArray = typeClass.isArray();
//        this.isList = List.class.isAssignableFrom(typeClass);
    }

    public Class<?> getTypeClass() {
        if (typeClass != null) {
            return typeClass;
        }
        try {
            typeClass = IDLT.classForName(type);
        } catch (Throwable e) {}
        return typeClass;
    }

    public Object getTransientValue() {
        return value;
    }

    public void setTransientValue(Object value) {
        this.value = value;
    }

    public String getCoreType() {
        return IDLT.convertCoreType(type);
    }

    public String getFinalType() {
        return IDLT.convertFinalType(type,isList,isArray);
    }

    //转化为IDLParam
    public IDLAPIParam getFieldParam() {
        IDLAPIParam param = new IDLAPIParam();
        param.type = this.type;
        param.name = this.name;
        param.desc = this.desc;
        if (this.isList) {
            param.isArray = true;
            param.type = IDLT.packArrayType(this.type);
        } else if (this.isArray || this.type.startsWith("[")) {
            param.isArray = true;
        }
        param.required = this.required;
        param.defaultValue = this.defaultValue;
        return param;
    }

    //转化为IDLField,java对象feild描述
    public IDLField getField() {
        IDLField field = new IDLField();
        field.type = this.type;
        field.name = this.name;
        field.desc = this.desc;
        field.isArray = this.isArray;
        field.isList = this.isList;
//        field.typeClass = this.typeClass;
        field.defaultValue = this.defaultValue;
        return field;
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {return true;}
//        if (obj == null) {return false;}
//        if (IDLFieldDesc.class != obj.getClass()) {return false;}
//        String typeDesc = ((IDLFieldDesc)obj).getFinalType();
//        if (typeDesc == null || typeDesc.equals("")) {
//            return false;
//        }
//        return typeDesc.equals(this.getFinalType());
//    }
//
//    @Override
//    public int hashCode() {
//        return ("" + IDLFieldDesc.class + this.getFinalType()).hashCode();
//    }
//
//    public IDLFieldDesc copy() {
//        IDLFieldDesc field = new IDLFieldDesc();
//        field.type = this.type;//属性元素类型 class 全称
//        field.isArray = this.isArray;//数组支持
//        field.isList = this.isList;//ArrayList支持
//        field.name = this.name;//属性名
//        field.desc = this.desc;//描述
//        field.required = this.required;//
//        field.autoInjected = this.autoInjected;
//        field.defaultValue = this.defaultValue; //默认值不为空,则认为是比传参数
//        field.isQuiet = this.isQuiet;
//        return field;
//    }
    //整个数据的结构,采用json描述
    /*
    struct person{
        name:String;
        age:int;
        height:float;
        depart:{}
    } =
     */
//    public String struct;
//
//    private static class Struct {
//
//    }
}
