package com.lmj.stone.idl;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 17/4/22.
 * 一般的POJO以及POJO的容器类型
 */
public final class IDLPOJOWrapper implements Serializable {
    private static final long serialVersionUID = 1182619094441927088L;

    public String type;//此泛型类型对应的实际类型
    public boolean isList;//是容器类型
    public String desc;//对象描述

    @JSONField(serialize = false, deserialize = false)
    private transient Class<?> typeClass;

//    public String[] genericType;//返回值泛型类型
    public List<IDLField> fields;

    public void setTypeClass(Class<?> typeClass) {
        this.typeClass = typeClass;
        this.type = typeClass.getName();
//        this.isList = List.class.isAssignableFrom(typeClass);
    }

    public Class<?> getTypeClass() {
        if (typeClass != null) {
            return typeClass;
        }
        try {
            typeClass = IDLT.classForName(this.type);
        } catch (Throwable e) {}
        return typeClass;
    }

    /**
     * 方法申明时需要的类型描述
     * @return
     */
    public String getFinalType() {
        return IDLT.convertFinalType(type,isList,false);
    }

    public String getCoreType() {return IDLT.convertCoreType(type);}

    /**
     * 方法申明时需要的类型描述
     * @return
     */
    public String getDeclareType() {
        return IDLT.convertDeclareType(getFinalType());
    }

    /**
     * 展示的java类型描述
     * @return
     */
    public String getDisplayType() {
        return IDLT.convertDisplayType(getFinalType());
    }

    //转idl Struct
    public IDLAPIStruct convertStruct() {
        IDLAPIStruct struct = new IDLAPIStruct();
        struct.type = this.type;
        struct.desc = this.desc;
        if (this.fields != null || this.fields.size() > 0) {
            struct.fields = new IDLAPIParam[this.fields.size()];
            for (int i = 0; i < this.fields.size(); i++) {
                struct.fields[i] = this.fields.get(i).convertParam();
            }
        }
        return struct;
    }

    //转idl ReturnType
    public IDLReturnType getReturnType() {
        IDLReturnType type = new IDLReturnType();
        type.type = this.type;
        type.desc = this.desc;
        if (this.isList) {
            type.isArray = true;
            type.type = IDLT.packArrayType(this.type);
        } else if (this.type.startsWith("[")) {
            type.isArray = true;
        }
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {return true;}
        if (obj == null) {return false;}
        if (IDLPOJOWrapper.class != obj.getClass()) {return false;}
        String typeDesc = ((IDLPOJOWrapper)obj).getFinalType();
        if (typeDesc == null || typeDesc.equals("")) {
            return false;
        }
        return typeDesc.equals(this.getFinalType());
    }

    @Override
    public int hashCode() {
        return ("" + IDLPOJOWrapper.class + this.getFinalType()).hashCode();
    }

    /**
     * 比较是否有修改,若有修改需要更新
     * @return
     */
    public boolean isModify(IDLPOJOWrapper target) {
        if (this == target) {return false;}
        if (target == null) {return false;}
        if (this.type == null || !this.type.equals(target.type)) {
            return true;
        }

        if (this.isList != target.isList) {
            return true;
        }

        if (this.desc == null || !this.desc.equals(target.desc)) {
            return true;
        }

        int this_size = 0;
        if (this.fields != null) {
            this_size = this.fields.size();
        }
        int target_size = 0;
        if (target.fields != null) {
            target_size = target.fields.size();
        }

        if (this_size != target_size) {
            return true;
        }

        if (this_size != 0) {
            return !this.fields.equals(target.fields);
        }

        return false;
    }

    public IDLPOJOWrapper copy() {
        IDLPOJOWrapper obj = new IDLPOJOWrapper();
        obj.type = this.type;//属性元素类型 class 全称
        obj.isList = this.isList;//ArrayList支持
        obj.desc = this.desc;//描述
        obj.fields = new ArrayList<IDLField>();
        if (this.fields != null) {
            for (IDLField field : this.fields) {
                obj.fields.add(field.copy());
            }
        }
        return obj;
    }
}
