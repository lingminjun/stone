package com.lmj.stone.idl.gen;


import com.lmj.stone.idl.IDLException;
import com.lmj.stone.idl.IDLExceptions;
import com.lmj.stone.idl.annotation.IDLDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@IDLDesc("boolean类型")
public final class IDLBoolean extends IDLResultWrapper {
    private static final long serialVersionUID = -9054823490873492068L;

    @IDLDesc("值")
    public boolean value;

    @Override
    public void setValue(Object value) throws IDLException {
        if (Boolean.class == value.getClass()) {
            this.value = ((Boolean)value).booleanValue();
        } else {
            throw IDLExceptions.PARSE_ERROR("解析boolean类型不对,输入类型"+value.getClass());
        }
    }
}
