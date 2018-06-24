package com.lmj.stone.cache;

import com.lmj.stone.core.Dispatcher;
import com.lmj.stone.idl.annotation.IDLParam;
import com.lmj.stone.idl.gen.IDLFieldDesc;
import com.lmj.stone.service.Pickup;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.MethodInvoker;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-06-22
 * Time: 下午3:38
 */
//@Aspect// 这个注解表明 使用spring 的aop，需要开启aop <!--开启AOP自动代理 --><aop:aspectj-autoproxy />
//@Component
public abstract class RemoteCacheAspect {

    public abstract RemoteCache remoteCache();

    @Pointcut("@annotation(com.lmj.stone.cache.AutoCache)")
    public void methodsToBeInspected() {
    }

    @Around("methodsToBeInspected()")
    public Object interceptCaches(ProceedingJoinPoint joinPoint) throws Throwable {

        //先看是否加载了缓存器
        RemoteCache cache = remoteCache();
        if (cache == null) {
            return joinPoint.proceed();
        }

        // No sonar comment is to avoid "throws Throwable" sonar violation
        Method annotatedElement = getSpecificmethod(joinPoint);
        List<AutoCache> annotations = getMethodAnnotations(annotatedElement, AutoCache.class);
        if (annotations.size() > 1) {
            System.out.println("注解修饰过多！" + joinPoint.getTarget().getClass().getSimpleName() + "." + annotatedElement.getName());
            return joinPoint.proceed();
        }

        AutoCache cacheable = annotations.get(0);

        //先判断条件
        String condition = generateKey(joinPoint,cacheable.condition());
        if (condition == null || condition.length() == 0) {
            return joinPoint.proceed();
        }

        if (!evaluateCondition(condition)) {
            return joinPoint.proceed();
        }

        String key = generateKey(joinPoint,cacheable.key());
        if (key == null || key.length() == 0) {
            return joinPoint.proceed();
        }


        if (cacheable.evict()) {
            Object result = joinPoint.proceed();
            cache.del(key);
            System.out.println("删除缓存 " + key);
            return result;
        }

        // 开始获取缓存
        RemoteCache.CacheHolder holder = null;
        try {
            if (cacheable.json()) {
                holder = cache.accessJSON(key,annotatedElement.getReturnType());
            } else {
                holder = cache.access(key,(Class<Serializable>)annotatedElement.getReturnType());
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("获取缓存异常 " + key);
        }

        //数据有效
        if (holder != null && !holder.isExpired) {
            System.out.println("从缓存获取有效数据 " + key);
            return holder.obj;
        }

        //过期数据失效
        int age = cacheable.age();
        int invalid = age > 0 ? age + 10 : 0;

        if (cacheable.async() && holder != null && holder.obj != null) {

            asyncCache(cache,key,cacheable.json(),age,invalid,joinPoint.getTarget(),annotatedElement,joinPoint.getArgs());
            //发起异步请求
            System.out.println("从缓存获取过期数据 " + key);
            return holder.obj;
        }

        // 同步刷新数据
        Object result = joinPoint.proceed();
        System.out.println("从DB获取数据 " + key);

        if (result != null) {
            try {
                cacheObject(cache,key,result,cacheable.json(),age,invalid);
            } catch (Throwable e) {
                System.out.println("存储缓存异常 " + key);
            }
        }
        return result;
    }

    private static boolean evaluateCondition(String condition) {
        ExpressionParser parser=new SpelExpressionParser();
        try {
            Expression exp=parser.parseExpression(condition);
            //parserExpression("'Hello World'.concat('!')");
            Boolean v = (Boolean)exp.getValue(Boolean.class);
            if (v != null && v.booleanValue()) {
                return true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void asyncCache(final RemoteCache cache, final String key, final boolean json, final int age, final int invalid, Object bean, Method method, Object[] args) {
        Object[] arguments = Arrays.copyOf(args, args.length);
        final MethodInvoker invoker = new MethodInvoker();
        invoker.setTargetObject(bean);
        invoker.setArguments(arguments);
        invoker.setTargetMethod(method.getName());

        Dispatcher.commonQueue().execute(new Runnable() {
            @Override
            public void run() {
                Object obj = null;
                try {
                    invoker.prepare();
                    obj = invoker.invoke();
                    System.out.println("异步调用方法 " + key);
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                if (obj != null) {
                    try {
                        cacheObject(cache,key,obj,json,age,invalid);
                    } catch (Throwable e) {
                        System.out.println("异步存储缓存异常 " + key);
                    }
                }
            }
        });
    }

    private static void cacheObject(RemoteCache cache, String key, Object object, boolean json, int age, int invalid) {
        boolean status = false;
        if (json) {
            status = cache.setJSON(key,object,age,invalid);
        } else {
            status = cache.set(key,(Serializable) object,age,invalid);
        }
        if (status) {
            System.out.println("存储缓存成功 " + key);
        } else {
            System.out.println("存储缓存失败 " + key);
        }
    }

    /**
     * Finds out the most specific method when the execution reference is an
     * interface or a method with generic parameters
     *
     * @param pjp
     * @return
     */
    private Method getSpecificmethod(ProceedingJoinPoint pjp) {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();
        // The method may be on an interface, but we need attributes from the
        // target class. If the target class is null, the method will be
        // unchanged.
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(pjp.getTarget());
        if (targetClass == null && pjp.getTarget() != null) {
            targetClass = pjp.getTarget().getClass();
        }
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        // If we are dealing with method with generic parameters, find the
        // original method.
        specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
        return specificMethod;
    }

    /**
     * Parses all annotations declared on the Method
     *
     * @param ae
     * @param annotationType
     *            Annotation type to look for
     * @return
     */
    private static <T extends Annotation> List<T> getMethodAnnotations(AnnotatedElement ae, Class<T> annotationType) {
        List<T> anns = new ArrayList<T>(2);
        // look for raw annotation
        T ann = ae.getAnnotation(annotationType);
        if (ann != null) {
            anns.add(ann);
        }
        // look for meta-annotations
        for (Annotation metaAnn : ae.getAnnotations()) {
            ann = metaAnn.annotationType().getAnnotation(annotationType);
            if (ann != null) {
                anns.add(ann);
            }
        }
        return (anns.isEmpty() ? null : anns);
    }

    private static String generateKey(ProceedingJoinPoint pjp, String key) {
        if (key == null) {return null;}

        key = key.trim();
        if (key.length() == 0) {return null;}

        StringBuilder builder = new StringBuilder(key);
        Map<String,Object> params = null;

        int begin = builder.indexOf("#{");
        int flag = 100;
        while (flag > 0 && begin >= 0 && begin < builder.length()) {
            int end = builder.indexOf("}");
            if (end < 0 || end >= builder.length()) {
                break;
            }

            if (params == null) {//延迟处理
                params = argsToMap(pjp);
            }

            String path = builder.substring(begin + 2, end);
            Object obj = Pickup.get(params,path);
            String replace = "NULL";
            if (obj != null) { replace = obj.toString(); }

            builder.replace(begin,end + 1,replace);

            //继续下一个
            begin = builder.indexOf("#{");
            flag--;
        }

        return builder.toString();
    }

    private static Map<String,Object> argsToMap(ProceedingJoinPoint pjp) {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] args = pjp.getArgs();
        if (args.length != parameterAnnotations.length) {
            throw new RuntimeException("存在未被注解IDLParam标记的参数" + methodSignature.toLongString());
        }

        HashMap<String,Object> params = new HashMap<String, Object>();

        for (int i = 0; i < args.length; i++) {
            Annotation[] a = parameterAnnotations[i];
            for (int j = 0; a != null && j < a.length; j++) {
                Annotation n = a[j];
                if (n.annotationType() == IDLParam.class) {
                    IDLParam p = (IDLParam) n;
                    if (p.name() != null && p.name().length() > 0) {
                        params.put(p.name(), args[i]);
                        break;
                    }
                } else if (n.annotationType() == RequestParam.class) {
                    RequestParam p = (RequestParam)n;
                    String name = aob(p.name(),p.value());
                    if (name != null && name.length() > 0) {
                        params.put(name, args[i]);
                        break;
                    }
                } else if (n.annotationType() == PathVariable.class) {
                    PathVariable p = (PathVariable)n;
                    String name = aob(p.name(),p.value());
                    if (name != null && name.length() > 0) {
                        params.put(name, args[i]);
                        break;
                    }
                } else if (n.annotationType() == RequestAttribute.class) {
                    RequestAttribute p = (RequestAttribute)n;
                    String name = aob(p.name(),p.value());
                    if (name != null && name.length() > 0) {
                        params.put(name, args[i]);
                        break;
                    }
                } else if (n.annotationType() == CookieValue.class) {
                    CookieValue p = (CookieValue)n;
                    String name = aob(p.name(),p.value());
                    if (name != null && name.length() > 0) {
                        params.put(name, args[i]);
                        break;
                    }
                } else if (n.annotationType() == RequestHeader.class) {
                    RequestHeader p = (RequestHeader)n;
                    String name = aob(p.name(),p.value());
                    if (name != null && name.length() > 0) {
                        params.put(name, args[i]);
                        break;
                    }
                } else if (n.annotationType() == SessionAttribute.class) {
                    SessionAttribute p = (SessionAttribute)n;
                    String name = aob(p.name(),p.value());
                    if (name != null && name.length() > 0) {
                        params.put(name, args[i]);
                        break;
                    }
                } else if (n.annotationType() == RequestPart.class) {
                    RequestPart p = (RequestPart)n;
                    String name = aob(p.name(),p.value());
                    if (name != null && name.length() > 0) {
                        params.put(name, args[i]);
                        break;
                    }
                }
            }
        }

        return params;
    }

    private static String aob(String a,String b) {
        if (a != null && a.length() > 0) {
            return a;
        }
        return b;
    }

    /**
     * Creates a MethodInvoker instance from the cached invocation object and
     * invokes it to get the return value
     *
     * @param invocation
     * @return Return value resulted from the method invocation
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
//    private Object execute(CachedInvocation invocation)
//            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        final MethodInvoker invoker = new MethodInvoker();
//        invoker.setTargetObject(invocation.getTargetBean());
//        invoker.setArguments(invocation.getArguments());
//        invoker.setTargetMethod(invocation.getTargetMethod().getName());
//        invoker.prepare();
//        return invoker.invoke();
//    }
}
