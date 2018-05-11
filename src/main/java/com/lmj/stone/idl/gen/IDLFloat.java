package com.lmj.stone.idl.gen;


import com.lmj.stone.idl.IDLException;
import com.lmj.stone.idl.IDLExceptions;
import com.lmj.stone.idl.annotation.IDLDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@IDLDesc("float类型")
public class IDLFloat extends IDLResultWrapper {
    private static final long serialVersionUID = 2878450218561387527L;

    @IDLDesc("值")
    public float value;

    @Override
    public void setValue(Object value) throws IDLException {
        if (Float.class == value.getClass()) {
            this.value = ((Float)value).floatValue();
        } else {
            throw IDLExceptions.PARSE_ERROR("解析float类型不对,输入类型"+value.getClass());
        }
    }
}
