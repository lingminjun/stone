package com.lmj.stone.idl.gen;


import com.lmj.stone.idl.IDLException;
import com.lmj.stone.idl.IDLExceptions;
import com.lmj.stone.idl.annotation.IDLDesc;

/**
 * Created by lingminjun on 17/4/22.
 * 数值型返回值，包含byte, char, short, int
 */
@IDLDesc("int数值类型[byte,char,short,int]")
public class IDLNumber extends IDLResultWrapper {
    private static final long serialVersionUID = 3966772992544417210L;

    //"数值型返回值，包含byte, char, short, int"
    @IDLDesc("值")
    public int value;

    @Override
    public void setValue(Object value) throws IDLException {
        if (Integer.class == value.getClass()) {
            this.value = ((Integer)value).intValue();
        } else if (Byte.class == value.getClass()) {
            this.value = ((Byte)value).byteValue();
        } else if (Character.class == value.getClass()) {
            this.value = ((Character)value).charValue();
        } else if (Short.class == value.getClass()) {
            this.value = ((Short)value).shortValue();
        } else {
            throw IDLExceptions.PARSE_ERROR("解析int类型不对,输入类型"+value.getClass());
        }
    }
}
