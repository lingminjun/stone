package com.lmj.stone.idl.gen;


import com.lmj.stone.idl.IDLException;
import com.lmj.stone.idl.IDLExceptions;
import com.lmj.stone.idl.annotation.IDLDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@IDLDesc("String类型")
public class IDLString extends IDLResultWrapper {

    private static final long serialVersionUID = -7499491788148343061L;

    @IDLDesc("值")
    public String value;

    @Override
    public void setValue(Object value) throws IDLException {
        if (value instanceof String) {
            this.value = (String)value;
        } else {
            throw IDLExceptions.PARSE_ERROR("解析String类型不对,输入类型"+value.getClass());
        }
    }

}
