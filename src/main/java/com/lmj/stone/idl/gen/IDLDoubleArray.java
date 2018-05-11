package com.lmj.stone.idl.gen;


import com.lmj.stone.idl.IDLException;
import com.lmj.stone.idl.IDLExceptions;
import com.lmj.stone.idl.IDLT;
import com.lmj.stone.idl.annotation.IDLDesc;

/**
 * Created by lingminjun on 17/4/22.
 */
@IDLDesc("double数组类型")
public class IDLDoubleArray extends IDLResultWrapper {
    private static final long serialVersionUID = -8918859727433003387L;

    @IDLDesc("值")
    public double[] value;

    @Override
    public void setValue(Object value) throws IDLException {
        if (double[].class == value.getClass()) {
            this.value = (double[])value;
        } else if (Double[].class == value.getClass()) {
            Double[] bary = (Double[])value;
            this.value = new double[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = IDLT.doubleDecimal(bary[i]);
            }
        } else {
            throw IDLExceptions.PARSE_ERROR("解析double[]类型不对,输入类型"+value.getClass());
        }
    }
}
