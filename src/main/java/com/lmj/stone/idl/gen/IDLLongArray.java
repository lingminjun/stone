package com.lmj.stone.idl.gen;


import com.lmj.stone.idl.IDLException;
import com.lmj.stone.idl.IDLExceptions;
import com.lmj.stone.idl.IDLT;
import com.lmj.stone.idl.annotation.IDLDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@IDLDesc("long数组类型")
public class IDLLongArray extends IDLResultWrapper {
    private static final long serialVersionUID = 6007922982960225023L;

    @IDLDesc("值")
    public long[] value;

    @Override
    public void setValue(Object value) throws IDLException {
        if (long[].class == value.getClass()) {
            this.value = (long[])value;
        } else if (Long[].class == value.getClass()) {
            Long[] bary = (Long[])value;
            this.value = new long[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = IDLT.longInteger(bary[i]);
            }
        } else {
            throw IDLExceptions.PARSE_ERROR("解析long[]类型不对,输入类型"+value.getClass());
        }
    }
}
