package com.lmj.stone.idl.gen;


import com.lmj.stone.idl.annotation.IDLDesc;

import java.io.Serializable;

/**
 * Created by lingminjun on 17/4/22.
 */
@IDLDesc("原始字符串类型")
public class IDLRawString implements Serializable {
    private static final long serialVersionUID = 2224563307923723782L;

    @IDLDesc("值")
    public String value;
}
