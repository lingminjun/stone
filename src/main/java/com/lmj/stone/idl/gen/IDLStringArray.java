package com.lmj.stone.idl.gen;

import com.lmj.stone.idl.IDLException;
import com.lmj.stone.idl.IDLExceptions;
import com.lmj.stone.idl.annotation.IDLDesc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 17/4/22.
 */
@IDLDesc("String列表类型")
public class IDLStringArray extends IDLResultWrapper {
    private static final long serialVersionUID = 8299930723569109319L;

    @IDLDesc("值")
    public List<String> value;

    @Override
    public void setValue(Object value) throws IDLException {
        if (value instanceof List) {
            List list = (List)value;
            for (Object obj : list) {
                if (String.class != obj.getClass()) {
                    throw IDLExceptions.PARSE_ERROR("解析List<String>类型不对,输入类型List<"+obj.getClass()+">");
                }
            }
            this.value = list;
        } else if (value.getClass() == String[].class) {
            String[] strs = (String[])value;
            this.value = new ArrayList<String>();
            for (String str : strs) {
                if (str != null) {
                    this.value.add(str);
                }
            }
        } else {
            throw IDLExceptions.PARSE_ERROR("解析String类型不对,输入类型"+value.getClass());
        }
    }
}
