package com.lmj.stone.idl.gen;


import com.lmj.stone.idl.IDLException;
import com.lmj.stone.idl.IDLExceptions;
import com.lmj.stone.idl.annotation.IDLDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@IDLDesc("long类型")
public class IDLLong extends IDLResultWrapper {
    private static final long serialVersionUID = -1651450767602263495L;

    @IDLDesc("值")
    public long value;

    @Override
    public void setValue(Object value) throws IDLException {
        if (Long.class == value.getClass()) {
            this.value = ((Long)value).longValue();
        } else {
            throw IDLExceptions.PARSE_ERROR("解析long类型不对,输入类型"+value.getClass());
        }
    }
}
