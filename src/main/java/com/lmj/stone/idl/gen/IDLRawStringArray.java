package com.lmj.stone.idl.gen;


import com.lmj.stone.idl.annotation.IDLDesc;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lingminjun on 17/4/22.
 */
@IDLDesc("原始字符串类型")
public class IDLRawStringArray implements Serializable {
    private static final long serialVersionUID = 2224563307923723782L;

    @IDLDesc("值")
    public List<String> value;
}
