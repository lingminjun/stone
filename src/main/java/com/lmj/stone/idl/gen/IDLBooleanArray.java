package com.lmj.stone.idl.gen;


import com.lmj.stone.idl.IDLException;
import com.lmj.stone.idl.IDLExceptions;
import com.lmj.stone.idl.IDLT;
import com.lmj.stone.idl.annotation.IDLDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@IDLDesc("boolean数组类型")
public class IDLBooleanArray extends IDLResultWrapper {
    private static final long serialVersionUID = 3789506240877925157L;

    @IDLDesc("值")
    public boolean[] value;

    @Override
    public void setValue(Object value) throws IDLException {
        if (boolean[].class == value.getClass()) {
            this.value = (boolean[])value;
        } else if (Boolean[].class == value.getClass()) {
            Boolean[] bary = (Boolean[])value;
            this.value = new boolean[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = IDLT.bool(bary[i]);
            }
        } else {
            throw IDLExceptions.PARSE_ERROR("解析boolean[]类型不对,输入类型"+value.getClass());
        }
    }
}
