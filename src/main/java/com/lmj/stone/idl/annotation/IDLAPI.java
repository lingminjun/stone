package com.lmj.stone.idl.annotation;

import com.lmj.stone.idl.IDLAPISecurity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IDLAPI {
    /**
     * Http 接口所在服务的module, 只能是包含字母、数字 [必填]
     *
     * @return
     */
    String module();

    /**
     * Http 接口名,字母和下划线开头,只能是包含字母、数字和下划线
     *
     * @return
     */
    String name();

    /**
     * Http 接口注释
     *
     * @return
     */
    String desc();

    /**
     * Http 接口短描述
     *
     * @return
     */
    String detail() default "";

    /**
     * 调用接口所需的安全级别,
     *
     * @return
     */
    IDLAPISecurity security();

    /**
     * 接口负责人
     *
     * @return
     */
    String owner() default "";

    /**
     * @return
     *
     * @see IDLAPISecurity . Integrated 级别接口是否需要apigw进行签名验证,false:验证由服务提供方完成,true:apigw负责签名验证
     */
    @Deprecated
    boolean needVerify() default false;
}
