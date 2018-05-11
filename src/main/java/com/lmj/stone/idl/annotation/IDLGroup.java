package com.lmj.stone.idl.annotation;


import com.lmj.stone.idl.IDLExceptions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务
 * @author lingminjun
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IDLGroup {
    /**
     * 服务名称,只能是包含字母、数字
     *
     * @return
     */
    String domain();

    /**
     * 服务描述
     *
     * @return
     */
    String desc();

    /**
     * 负责人
     *
     * @return
     */
    String owner() default "";

    /**
     * 错误码定义类,定义格式如 @see IDLExceptions
     *
     * @return
     */
    Class<?> codeDefine() default IDLExceptions.class;
}
