package com.lmj.stone.idl.gen;


import com.lmj.stone.idl.IDLException;
import com.lmj.stone.idl.IDLExceptions;
import com.lmj.stone.idl.annotation.IDLDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@IDLDesc("double类型")
public class IDLDouble extends IDLResultWrapper {
    private static final long serialVersionUID = 2878450218561387527L;

    @IDLDesc("值")
    public double value;

    @Override
    public void setValue(Object value) throws IDLException {
        if (Double.class == value.getClass()) {
            this.value = ((Double)value).doubleValue();
        } else {
            throw IDLExceptions.PARSE_ERROR("解析double类型不对,输入类型"+value.getClass());
        }
    }
}
