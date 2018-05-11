package com.lmj.stone.idl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IDLError {
    /**
     * 暴露的错误码,会自动绑定到当前domain上,
     * 也可以在通过IDLException.isExposed来控制对外暴露
     * 所有错误定义描述将会查找 IDLGroup.codeDefine 指定的文件中定义
     * 若没有找到,则忽略
     * @return
     */
    int[] value();
}
