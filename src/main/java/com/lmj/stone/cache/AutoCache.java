package com.lmj.stone.cache;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-06-22
 * Time: 下午4:06
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AutoCache {

    //自定义缓存key，支持"#"取参数，必填字段
    String key() default "";

    //缓存时效 单位秒，默认是有60+10秒钟后丢失数据
    int age() default 60;//1分钟

    //当前是否为删除缓存操作，一律在方法调用后删除缓存，如果有更新操作，操作成功后删除缓存
    boolean evict() default false;

    //是否异步刷新缓存，不阻塞当前栈
    boolean async() default false;

    //采用缓存方式为json缓存
    boolean json() default true;
}
