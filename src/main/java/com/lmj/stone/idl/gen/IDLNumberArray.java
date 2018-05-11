package com.lmj.stone.idl.gen;


import com.lmj.stone.idl.IDLException;
import com.lmj.stone.idl.IDLExceptions;
import com.lmj.stone.idl.IDLT;
import com.lmj.stone.idl.annotation.IDLDesc;

/**
 * Created by lingminjun on 17/4/22.
 * 数值型返回值，包含byte, char, short, int
 */
@IDLDesc("int数组类型")
public class IDLNumberArray extends IDLResultWrapper {
    private static final long serialVersionUID = 7304912395341184154L;

    @IDLDesc("值")
    public int[] value;

    @Override
    public void setValue(Object value) throws IDLException {
        if (byte[].class == value.getClass()) {
            byte[] bs = (byte[])value;
            this.value = new int[bs.length];
            for (int i = 0; i < bs.length; i++) {
                this.value[i] = bs[i];
            }
        } else if (Byte[].class == value.getClass()) {
            Byte[] bary = (Byte[])value;
            this.value = new int[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = IDLT.byteNumber(bary[i]);
            }
        } else if (char[].class == value.getClass()) {
            char[] bs = (char[])value;
            this.value = new int[bs.length];
            for (int i = 0; i < bs.length; i++) {
                this.value[i] = bs[i];
            }
        } else if (Character[].class == value.getClass()) {
            Character[] bary = (Character[])value;
            this.value = new int[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = IDLT.character(bary[i]);
            }
        } else if (short[].class == value.getClass()) {
            short[] bs = (short[])value;
            this.value = new int[bs.length];
            for (int i = 0; i < bs.length; i++) {
                this.value[i] = bs[i];
            }
        } else if (Short[].class == value.getClass()) {
            Short[] bary = (Short[])value;
            this.value = new int[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = IDLT.shortInteger(bary[i]);
            }
        } else if (int[].class == value.getClass()) {
            this.value = (int[])value;
        } else if (Integer[].class == value.getClass()) {
            Integer[] bary = (Integer[])value;
            this.value = new int[bary.length];
            for (int i = 0; i < bary.length; i++) {
                this.value[i] = IDLT.integer(bary[i]);
            }
        } else {
            throw IDLExceptions.PARSE_ERROR("解析int[]类型不对,输入类型"+value.getClass());
        }
    }
}
