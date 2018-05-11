package com.lmj.stone.idl.gen;


import com.lmj.stone.idl.IDLException;
import com.lmj.stone.idl.IDLExceptions;
import com.lmj.stone.idl.IDLT;
import com.lmj.stone.idl.annotation.IDLDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@IDLDesc("float数组类型")
public class IDLFloatArray extends IDLResultWrapper {
    private static final long serialVersionUID = -8918859727433003387L;

    @IDLDesc("值")
    public float[] value;

    @Override
    public void setValue(Object value) throws IDLException {
        if (float[].class == value.getClass()) {
            this.value = (float[])value;
        } else if (Float[].class == value.getClass()) {
            Float[] bary = (Float[])value;
            this.value = new float[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = IDLT.floatDecimal(bary[i]);
            }
        } else {
            throw IDLExceptions.PARSE_ERROR("解析float[]类型不对,输入类型"+value.getClass());
        }
    }
}
