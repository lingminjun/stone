package com.lmj.stone.idl.gen;

import com.lmj.stone.idl.*;
import com.lmj.stone.idl.annotation.*;
import com.lmj.stone.utils.JavaCodeAssist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by lingminjun on 17/2/9.
 * 方便从java方法+注解定义来生产IDLAPIInfo
 */
public class IDLAPIHelper {

    private static Logger logger = LoggerFactory.getLogger(IDLAPIHelper.class);
    static HashMap<String,IDLPOJOWrapper> pojos = new HashMap<String, IDLPOJOWrapper>();

    public static List<IDLAPIInfo> generate(Class<?> dubboProvider, boolean ignoreError) {
        if (dubboProvider == null ) {
            throw new RuntimeException("请务必输入正确的dubboProvider和methodName");
        }

        List<IDLAPIInfo> list = new ArrayList<IDLAPIInfo>();
        Method[] methods = dubboProvider.getMethods();
        for (Method method : methods) {
            //排出静态方法
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            IDLAPIInfo info = null;
            try {
                info = generate(dubboProvider,method,null,null,null,null,null,null);
            } catch (Throwable e) {
                e.printStackTrace();
                if (!ignoreError) {
                    throw new RuntimeException(e);
                }
            }

            if (info != null) {
                list.add(info);
            }
        }

        return list;
    }

    public static IDLAPIInfo generate(Class<?> dubboProvider,
                                      String methodName,
                                      String apiDomain,
                                      String apiModule,
                                      String apiName,
                                      String apiDesc,
                                      String apiOwner,
                                      String apiDetail) {
        if (dubboProvider == null || StringUtils.isEmpty(methodName)) {
            throw new RuntimeException("请务必输入正确的dubboProvider和methodName");
        }

        Method[] methods = dubboProvider.getMethods();
        Method theMethod = null;
        for (Method method : methods) {
            //排出静态方法
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getName().equals(methodName)) {
                theMethod = method;
                break;
            }
        }
        return generate(dubboProvider,theMethod,apiDomain,apiModule,apiName,apiDesc,apiOwner,apiDetail);
    }

    private static IDLAPIInfo generate(Class<?> dubboProvider,
                                       Method method,
                                       String apiDomain,
                                       String apiModule,
                                       String apiName,
                                       String apiDesc,
                                       String apiOwner,
                                       String apiDetail) {
        if (dubboProvider == null || method == null) {
            throw new RuntimeException("请务必输入正确的dubboProvider和methodName");
        }

        if (!dubboProvider.isInterface()) {
            throw new RuntimeException("请务必输入正确的dubboProvider接口类");
        }

        String methodName = method.getName();

        IDLGroup group = dubboProvider.getAnnotation(IDLGroup.class);
        IDLAPI esbapi = method.getAnnotation(IDLAPI.class);

        //必要参数整理
        apiDomain = tidyDomainString(dubboProvider, apiDomain, group);
        apiModule = tidyAPIModule(apiModule,esbapi,apiDomain);
        apiName = tidyAPIName(methodName, apiName, esbapi);

        if (!verifyDomain(apiDomain) || !verifyDomain(apiModule) || !verifyAPIName(apiName)) {
            throw new RuntimeException("请务必输入正确的apiDomain、apiModule和apiName");
        }

        //api对象
        IDLAPIInfo esbapiInfo = new IDLAPIInfo();
        esbapiInfo.createAt = System.currentTimeMillis();
        esbapiInfo.modifyAt = esbapiInfo.createAt;

        //IDL部分
        esbapiInfo.api = new IDLAPIDef();
        esbapiInfo.api.domain = apiDomain;
        esbapiInfo.api.module = apiModule;
        esbapiInfo.api.methodName = apiName;
        esbapiInfo.api.desc = apiDesc;
        esbapiInfo.api.detail = apiDetail;
        esbapiInfo.api.owner = apiOwner;

        //填充其他属性
        fillAPIInfo(apiName, group, esbapi, esbapiInfo.api);

        //异常码部分【文档需要,不影响接口调用,所以未找到则忽略】
        parseCodes(apiDomain, group, method.getAnnotation(IDLError.class), esbapiInfo);

        //返回值处理
        parseReturnType(method,dubboProvider,esbapiInfo);

        //自动生成 dubbo 接口
        IDLDubboMethod dubboMethod = new IDLDubboMethod();
        dubboMethod.dubbo = dubboProvider.getName();
        dubboMethod.method = methodName;

        //最后处理参数部分
        parseParameterTypes(esbapiInfo,dubboMethod,method,dubboProvider);

        //最后转换出 dubbo的方法
        esbapiInfo.invocations = new HashMap<String, IDLInvocation>();
        IDLInvocation invocation = dubboMethod.getInvocation();
        esbapiInfo.invocations.put(invocation.getMD5(),invocation);

        return esbapiInfo;
    }

    /**
     * 此处小范围的缓存,以免不断的反射错误码定义类
     * @param group
     * @param error
     * @param esbapiInfo
     */
    private static void parseCodes(String apiDomain, IDLGroup group, IDLError error, IDLAPIInfo esbapiInfo) {
        if (error == null || error.value() == null || error.value().length == 0) {
            return;
        }

        if (group == null || group.codeDefine() == null) {
            return;
        }

        Class<?> codeDefineClazz = group.codeDefine();
        int[] codes = error.value();

        HashMap<Integer,IDLAPICode> map = codesDefinedCache.get(codeDefineClazz.getName());
        if (map == null) {
            map = new HashMap<Integer, IDLAPICode>();
            //获取所有变量的值
            Field[] fields = codeDefineClazz.getFields();
            for (Field field : fields) {
                if (field.getType() != int.class) {
                    continue;
                }

                String name = field.getName();
                //错误码的定义不符合规范,建议检查
                if (!name.endsWith("_CODE")) {
                    System.out.println("错误码命名定义不符合规范,请检查" +
                            codeDefineClazz.getName() +".java文件中对" +
                            name + "的定义。必须以_CODE结尾。");
                    continue;
                }
                String methodName = name.substring(0,name.length() - "_CODE".length());

                IDLException exception = null;
                //命名
                try {
                    Method mtd = codeDefineClazz.getMethod(methodName,String.class);

                    exception = (IDLException)mtd.invoke(codeDefineClazz,"代码生成需要");
                } catch (NoSuchMethodException e) {
                    System.out.println("错误码build方法定义不符合规范,请检查" +
                            codeDefineClazz.getName() +".java文件,命名需与" +
                            name + "对应,应该定义为: public static IDLException " +
                            methodName + "(String reason);");
                    continue;
                } catch (InvocationTargetException e) {
                    System.out.println("错误码build方法定义不符合规范,请检查" +
                            codeDefineClazz.getName() +".java文件中对" +
                            methodName + "的定义。");
                    continue;
                } catch (IllegalAccessException e) {
                    System.out.println("错误码build方法定义不符合规范,请检查" +
                            codeDefineClazz.getName() +".java文件中对" +
                            methodName + "的定义。");
                    continue;
                }

                if (map.get(exception.getCode()) != null) {
                    System.out.println("错误码定义重复,请检查" +
                            codeDefineClazz.getName() +".java文件中对" +
                            name + "的定义,有重复的定义错误码:" + exception.getCode());
                }

                //名字命名: domain_codeName_code
//                exception.name = apiDomain + "_" + methodName + "_" + exception.getCode();
                IDLAPICode code = new IDLAPICode();
                code.code = exception.getCode();
                code.desc = exception.getMessage();
                code.domain = apiDomain;
                map.put(exception.getCode(),code);
            }

            codesDefinedCache.put(codeDefineClazz.getName(),map);
        }

        //遍历
        for (int i = 0; i < codes.length; i++) {
            IDLAPICode code = map.get(codes[i]);
            if (code == null) {
                System.out.println("未在" +
                        codeDefineClazz.getName() +
                        ".java文件中找到错误码" + codes[i] +
                        "的定义");
            } else {
                if (esbapiInfo.api.codes == null) {
                    esbapiInfo.api.codes = new HashMap<Integer, IDLAPICode>();
                }
                esbapiInfo.api.codes.put(code.code,code);
            }
        }
    }
    //按文件存储错误码
    private static HashMap<String,HashMap<Integer,IDLAPICode>> codesDefinedCache = new HashMap<String, HashMap<Integer, IDLAPICode>>();

    private static void fillAPIInfo(String apiName, IDLGroup group, IDLAPI esbapi, IDLAPIDef apiDef) {
        //owner
        if (StringUtils.isEmpty(apiDef.owner) && esbapi != null) {
            apiDef.owner = esbapi.owner();
        }
        if (StringUtils.isEmpty(apiDef.owner) && group != null) {
            apiDef.owner = group.owner();
        }

        //desc
        if (StringUtils.isEmpty(apiDef.desc) && esbapi != null) {
            apiDef.desc = esbapi.desc();
        }

        //detail
        if (StringUtils.isEmpty(apiDef.detail) && esbapi != null) {
            apiDef.detail = esbapi.detail();
        }

        //只有注解形成的接口,才去配安全级别
        if (esbapi != null && apiName.equals(esbapi.name())) {
            IDLAPISecurity securityLevel = esbapi.security();
            if (securityLevel != null) {
                apiDef.security = securityLevel.code;
            }
            apiDef.needVerify = esbapi.needVerify();
        }
    }

    private static String tidyAPIName(String methodName, String apiName, IDLAPI esbapi) {
        //参数优先
        if (!StringUtils.isEmpty(apiName)) {
            return apiName;
        }

        //注解
        if (esbapi != null) {
            return esbapi.name();
        }

        //方法名
        return methodName;
    }

    private static String tidyAPIModule(String apiModule, IDLAPI esbapi, String apiDomain) {
        //参数优先
        if (!StringUtils.isEmpty(apiModule)) {
            return apiModule;
        }

        //注解
        if (esbapi != null) {
            return esbapi.module();
        }

        //方法名
        return apiDomain;
    }

    private static String tidyDomainString(Class<?> dubboProvider, String apiDomain, IDLGroup group) {
        //首先使用输入参数作为最后module
        if (!StringUtils.isEmpty(apiDomain)) {
            return apiDomain;
        }

        //取配置
        if (group != null) {
            return group.domain();
        }

        //取包名中合适的服务名,最好是取root工程的artifactId
        //此处简单处理,一个原则,取第三位或者第四位:com.sfebiz.xxxx.xxxxClass
        //com.sfebiz.dubbo.demo.inferface.GenericService
        String[] strs = dubboProvider.getName().split("\\.");
        if (strs.length > 4) {
            apiDomain = strs[2];
            //排出掉通用词
            if (apiDomain.contains("parent")
                    || apiDomain.contains("group")
                    || apiDomain.contains("dubbo")
                    || apiDomain.contains("module")
                    || apiDomain.contains("domain")
                    || apiDomain.contains("server")
                    || apiDomain.contains("service")
                    || apiDomain.contains("client")
                    ) {
                apiDomain = strs[3];
            }
        } else if (strs.length == 4) {
            apiDomain = strs[2];
        } else if (strs.length > 2){
            apiDomain = strs[strs.length - 2];
        } else {
            //报错,此处必须设置domain
            apiDomain = "server";
        }
        return apiDomain;
    }

    private static void parseObjectType(Class<?> clazz, IDLAPIInfo esbapiInfo) {
        //基础类型忽略,不要再放进来
        if (isBaseType(clazz)) {
            return;
        }

        IDLPOJOWrapper tempPojo = new IDLPOJOWrapper();
        IDLDesc desc = clazz.getAnnotation(IDLDesc.class);
        if (desc != null) {
            tempPojo.desc = desc.value();
        }

        tempPojo.setTypeClass(clazz);

        //将解析过的对象记录下来(必须提前放入,因为要防止属性递归依赖本身类)
        String coreType = tempPojo.getCoreType();
        IDLPOJOWrapper pojo = pojos.get(coreType);
        if (pojo == null) {
            pojo = tempPojo;
            pojos.put(tempPojo.getCoreType(),pojo);
            //从类型遍历所有属性
            parsePOJOFields(pojo, pojo.getTypeClass(), esbapiInfo);
        }

        savePojoToAPI(pojo,esbapiInfo);
    }

    private static void parseReturnType(Method method, Class dubboServiceClazz, IDLAPIInfo esbapiInfo) {
        IDLPOJOWrapper tempReturnedPOJO = new IDLPOJOWrapper();

        Class<?> returnType = method.getReturnType();

        //返回结果需要装箱处理(统一接口规范)
        if (String.class == returnType) {//RawString
            tempReturnedPOJO.setTypeClass(IDLString.class);
        } else if (String[].class == returnType) {//不适用ObjectArrayResp，考虑到代码生成的时候会生成重复代码
            tempReturnedPOJO.setTypeClass(IDLStringArray.class);
        } else if (boolean.class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLBoolean.class);
        } else if (boolean[].class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLBooleanArray.class);
        } else if (byte.class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLNumber.class);
        } else if (short.class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLNumber.class);
        } else if (char.class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLNumber.class);
        } else if (int.class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLNumber.class);
        } else if (byte[].class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLNumberArray.class);
        } else if (short[].class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLNumberArray.class);
        } else if (char[].class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLNumberArray.class);
        } else if (int[].class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLNumberArray.class);
        } else if (long.class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLLong.class);
        } else if (long[].class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLLongArray.class);
        } else if (double.class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLDouble.class);
        } else if (float.class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLFloat.class);
        } else if (double[].class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLDoubleArray.class);
        } else if (float[].class == returnType) {
            tempReturnedPOJO.setTypeClass(IDLFloatArray.class);
            /*
        } else if (JSONString.class == apiInfo.returned) {
            apiInfo.serializer = Serializer.jsonStringSerializer;
            apiInfo.wrapper = ResponseWrapper.objectWrapper;
        } else if (IDLRawString.class == returned) {
            tempReturnedPOJO.setTypeClass(IDLRawString.class);
        } else if (net.pocrd.responseEntity.RawString.class == apiInfo.returned) {
            //TODO remove,RawString不应对外暴露
            apiInfo.serializer = Serializer.deprecatedRawStringSerializer;
            apiInfo.wrapper = ResponseWrapper.objectWrapper;
            */
        } else if (Collection.class.isAssignableFrom(returnType)) {
            //增加对Collection自定义Object的支持+List<String>的支持
            Class<?> genericClazz;
            try {
                genericClazz = getListActuallyGenericType(returnType,method.getGenericReturnType());
            } catch (Exception e) {
                throw new RuntimeException("generic type load failed:" + method.getGenericReturnType() + " in " + dubboServiceClazz.getName() + " method name:" + method.getName(), e);
            }

            //不支持的泛型对象
            if (genericClazz == null) {
                throw new RuntimeException("generic type load failed:" + method.getGenericReturnType() + " in " + dubboServiceClazz.getName() + " method name:" + method.getName());
            }

            if (String.class == genericClazz) {//如果要支持更多的jdk中已有类型的序列化
                tempReturnedPOJO.setTypeClass(IDLStringArray.class);
            }
            else {//需要被包装起来
                tempReturnedPOJO.setTypeClass(genericClazz);
                tempReturnedPOJO.isList = true;
            }
        } else if (returnType.isArray()) {//
            throw new RuntimeException("unsupported return type, method name:" + method.getName());
        } else {
            tempReturnedPOJO.setTypeClass(returnType);
        }

        IDLDesc desc = tempReturnedPOJO.getTypeClass().getAnnotation(IDLDesc.class);;
        if (desc != null) {
            tempReturnedPOJO.desc = desc.value();
        }

        //将解析过的对象记录下来(必须提前放入,因为要防止属性递归依赖本身类)
        String finalType = tempReturnedPOJO.getFinalType();
        IDLPOJOWrapper returned = pojos.get(finalType);
        if (returned == null) {
            returned = tempReturnedPOJO;
            pojos.put(finalType,tempReturnedPOJO);

            //从类型遍历所有属性
            parsePOJOFields(tempReturnedPOJO, tempReturnedPOJO.getTypeClass(), esbapiInfo);
        }

        //记录到esbapiInfo中
        esbapiInfo.api.returned = returned.getReturnType();

        //没有什么规则
//        esbapiInfo.rules = new HashMap<String, IDLRuleNode>();
//        esbapiInfo.rules.put("this",new IDLRuleNode());

        //将pojos转struct
        savePojoToAPI(returned,esbapiInfo);
    }

    private static void parseParameterTypes(IDLAPIInfo esbapiInfo, IDLDubboMethod dubboMethod, Method method, Class dubboService) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        //所有参数是否都被注解记录

        if (parameterTypes.length != parameterAnnotations.length) {
            throw new RuntimeException("存在未被标记的http api参数" + dubboService.getName());
//            fieldNames = JavassistMethodUtils.getMethodParameterNamesByJavassist(method);
        }

        if (parameterTypes.length == 0) {
            throw new RuntimeException("不支持没有参数的dubbo方法" + dubboService.getName());
        }

        //记录参数,主要是通过注解标示的参数名,方便从http接口中获取对应参数
//        esbapiInfo.params = new IDLField[parameterTypes.length];
        List<IDLFieldDesc> params = new ArrayList<IDLFieldDesc>();
        dubboMethod.params.clear();
        String[] fieldNames = null;
        boolean useCompatibility = false;
        for (int i = 0; i < parameterTypes.length; i++) {
            IDLFieldDesc field = new IDLFieldDesc();
//            esbapiInfo.params[i] = field;

            //需要判断是否为泛型类型
            Class<?> type = parameterTypes[i];
            field.setTypeClass(type);

            Class<?> genericClazz = null;
            //入参的递归检查 对于泛型map,array不支持切记,仅仅支持list
            if (Collection.class.isAssignableFrom(type)) {//增加对List自定义Object的支持+List<String>的支持,此处可以支持set
                try {
                    genericClazz = getListActuallyGenericType(type,method.getGenericParameterTypes()[i]);
                } catch (Exception e) {
                    throw new RuntimeException("generic type unsupported:" + method.getGenericParameterTypes()[i] + " in interface:" + dubboService.getName() + " method:" + method.getName(), e);
                }

                if (genericClazz != null) {
                    field.setTypeClass(genericClazz);
                    field.isList = true;
                } else {
                    throw new RuntimeException("only list is support when using collection。in interface:" + dubboService.getName() + " method:" + method.getName());
                }
            } else if (type.isArray()) {//不同类型的array需要支持
                if (isBaseType(type)) {
                    field.isArray = true;
                } else {
                    throw new RuntimeException("only base type array is support when using array。in interface:" + dubboService.getName() + " method:" + method.getName());
                }
            } else {
                //如果参数是非List的容器类型,直接报错
                if (isContainerType(type)) {
                    throw new RuntimeException("only list is support when using collection。in interface:" + dubboService.getName() + " method:" + method.getName());
                }
            }

            int j = 0;
            Annotation[] a = parameterAnnotations[i];
            for (j = 0; a != null && j < a.length; j++) {
                Annotation n = a[j];
                if (n.annotationType() == IDLParam.class) {
                    IDLParam p = (IDLParam) n;
                    field.desc = p.desc();
                    field.required = p.required();
                    field.isQuiet = p.quiet();
                    field.name = p.name();
                    field.autoInjected = p.autoInjected();
                    field.defaultValue = p.defaultValue();
                    break;
                }
            }

            //context类型特殊处理,一定要注入
            /*
            if (IDLContext.class == type) {
                if (field.name == null) {
                    field.name = "_context";
                }
                field.required = true;
                field.desc = "上下文参数";
                field.autoInjected = true;
            }*/

            if (StringUtils.isEmpty(field.name)) {

                //先用兼容方案修复
                if (!useCompatibility) {
                    useCompatibility = true;
                    fieldNames = JavaCodeAssist.methodParamNames(dubboService,method.getName(),parameterTypes.length);
                    System.out.println("WARNING:务必采用注解的方式来修饰要暴露的接口参数,暂时采用兼容方案");
                }

                if (fieldNames != null) {
                    field.name = fieldNames[i];
                }
            }

            if (StringUtils.isEmpty(field.name)) {
                throw new RuntimeException("api参数未被标记" + method.getName() + " in " + dubboService.getName());
            }

            //注入的说明IDL层不需要暴露
            if (!field.autoInjected) {
                params.add(field);
            }

            //转成field存储
            dubboMethod.params.add(field.getField());

            //开始解析对象
            parseObjectType(field.getTypeClass(),esbapiInfo);
        }

        //转参数记录
        esbapiInfo.api.params = convertAPIParams(params);
    }

    private static IDLAPIParam[] convertAPIParams(List<IDLFieldDesc> list) {
        IDLAPIParam[] params = new IDLAPIParam[list.size()];
        for (int i = 0; i < list.size(); i++) {
            IDLFieldDesc desc = list.get(i);
            params[i] = desc.getFieldParam();
        }
        return params;
    }

    private static Class<?> getListActuallyGenericType(Class<?> clazz, Type genericType) {
        Class<?> genericClazz = null;
        if (Collection.class.isAssignableFrom(clazz)) {//增加对List自定义Object的支持+List<String>的支持,此处可以支持set
            if (List.class.isAssignableFrom(clazz)) {

                Type genericArgument;
                try {
                    genericArgument = ((ParameterizedTypeImpl) genericType).getActualTypeArguments()[0];
                } catch (Throwable t) {
                    throw new RuntimeException("generic type unsupported:" + genericType, t);
                }

                try {
                    //当前class loader来加载类型
                    genericClazz = IDLT.classForName(((Class) genericArgument).getName());
                } catch (Exception e) {
                    throw new RuntimeException("generic type unsupported:" + genericType, e);
                }

                //容器内往下不支持容器类型
                if (isContainerType(genericClazz)) {
                    throw new RuntimeException("generic type unsupported:" + genericType);
                }

            } else {
                throw new RuntimeException("only list is support when using collection");
            }
        }
        return genericClazz;
    }

    /**
     * 是否为基本类型
     * @param clazz
     * @return
     */
    private static boolean isBaseType(Class clazz) {
        return IDLT.isBaseType(clazz);
    }

    /**
     * 是容器类型
     * @param clazz
     * @return
     */
    private static boolean isContainerType(Class clazz) {
        if (Collection.class.isAssignableFrom(clazz)) {
            return true;
        }

        if (Map.class.isAssignableFrom(clazz)) {
            return true;
        }

        if (clazz.isArray()) {
            return true;
        }

        return false;
    }

    /**
     * 是否合法
     * @param field
     * @param entityClazz
     * @return 0:不合法, 1合法类型, 2:合法array, 3:合法List
     */
    private static boolean typeDeal(IDLFieldDesc field, Class<?> clazz, Type getGenericType, Class<?> entityClazz, String fieldName) {

        //boolean、byte、short、char、int、long、float、double、String、RawString
        //基础类型全部支持
        if (isBaseType(clazz)) {
            if (field != null) {
                field.setTypeClass(clazz);
//                if (field.isArray) {
//                    return true;
//                }
            }
            return true;
        }

        //枚举不支持
        if (clazz.isEnum()) {
            System.out.println("对象" + entityClazz.getName() + "存在枚举类型属性" + clazz.getName() + " " + fieldName);
            return false;
        }

        //不支持
        if (Map.class.isAssignableFrom(clazz)) {
            System.out.println("对象" + entityClazz.getName() + "存在不合法类型属性" + clazz.getName() + " " + fieldName);
            return false;
        }

        //容器类型
        if (Collection.class.isAssignableFrom(clazz)) {

            Class<?> genericClazz = null;
            try {
                genericClazz = getListActuallyGenericType(clazz,getGenericType);
            } catch (Exception e) {
                System.out.println("对象" + entityClazz.getName() + "存在不合法泛型属性" + clazz.getName() + " " + fieldName);
                return false;
            }

            if (genericClazz == null) {
                System.out.println("对象" + entityClazz.getName() + "存在不合法泛型属性" + clazz.getName() + " " + fieldName);
                return false;
            }

            if (genericClazz == String.class) {
                if (field != null) {
                    field.setTypeClass(String.class);
                    field.isList = true;
                }
                return true;
            }

            //不在继续支持容器内套容器,这样的方式非常不友好
            if (genericClazz.isArray()
                    || Map.class.isAssignableFrom(genericClazz)
                    || Collection.class.isAssignableFrom(genericClazz)
                    || genericClazz.isEnum()) {
                System.out.println("对象" + entityClazz.getName() + "存在不合法泛型属性" + clazz.getName() + " " + fieldName);
                return false;
            }

            if (field != null) {
                field.setTypeClass(genericClazz);
                field.isList = true;
            }

            return true;
        }

        //不支持自定义泛型 getGenericType == clazz,表示没有泛型的参数
        if (getGenericType != null && getGenericType != clazz) {
            System.out.println("对象" + entityClazz.getName() + "存在不合法泛型属性" + clazz.getName() + " " + fieldName);
            return false;
        }

        if (field != null) {
            field.setTypeClass(clazz);
        }

        return true;
    }


    /**
     * 解析属性,放入pojo之中
     * @param pojo
     * @param clazz
     */
    private static void parsePOJOFields(IDLPOJOWrapper pojo, Class clazz, IDLAPIInfo esbapiInfo) {
        Field[] fields = IDLT.getClassDeclaredFields(clazz);

        for (Field field : fields) {

            IDLDesc desc = field.getAnnotation(IDLDesc.class);

            //需要忽略的属性
            if (desc != null && desc.ignore()) {
                continue;
            }

            String fieldName = field.getName();
            IDLFieldDesc fd = new IDLFieldDesc();
            fd.name = fieldName;
            boolean status = typeDeal(fd,field.getType(),field.getGenericType(),clazz,fieldName);
            if (!status) {
                throw new RuntimeException("存在不支持的类型在:" + clazz.getSimpleName() + "." + fieldName);
//                continue;
            }

            if (desc != null) {
                fd.desc = desc.value();
                fd.isInner = desc.inner();
                fd.canEntrust = desc.entrust();
            }

            if (pojo.fields == null) {
                pojo.fields = new ArrayList<IDLField>();
            }

            pojo.fields.add(fd.getField());

            //基础类型就不用解析了
            if (isBaseType(fd.getTypeClass())) {
                continue;
            }

            //判断其类型是否为基础类型,若不是基础类型,则需要不断递归,将类型解析完
            String keyType = fd.getCoreType();

            //类型继续解析
            IDLPOJOWrapper pj = pojos.get(keyType);
            if (pj == null) {
                parseObjectType(fd.getTypeClass(), esbapiInfo);//继续解析参数
            } else {
                savePojoToAPI(pj,esbapiInfo);
            }
        }
    }

    private static void savePojoToAPI(IDLPOJOWrapper wrapper, IDLAPIInfo esbapiInfo) {
        if (esbapiInfo != null && esbapiInfo.api != null && wrapper != null) {
            if (esbapiInfo.api.structs == null) {
                esbapiInfo.api.structs = new HashMap<String, IDLAPIStruct>();
            }
            if (!esbapiInfo.api.structs.containsKey(wrapper.getCoreType())) {
                esbapiInfo.api.structs.put(wrapper.getCoreType(),wrapper.convertStruct());

                //需要将其依赖的所有类型都依赖进来
                if (wrapper.fields != null) {
                    for (IDLField field : wrapper.fields) {
                        String coreType = field.getCoreType();
                        if (!IDLT.isBaseType(coreType)) {
                            IDLPOJOWrapper pj = pojos.get(coreType);
                            if (pj != null) {
                                savePojoToAPI(pj,esbapiInfo);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 字母+数字
     * @param domain
     * @return
     */
    public static boolean verifyDomain(String domain) {
        if (StringUtils.isEmpty(domain)) {
            return false;
        }

        Pattern pattern = Pattern.compile("[0-9|a-z|A-Z]*");
        return pattern.matcher(domain).matches();
    }

    /**
     * 字母+数字+下划线,以字母和下划线开头
     * @param apiName
     * @return
     */
    public static boolean verifyAPIName(String apiName) {
        if (StringUtils.isEmpty(apiName)) {
            return false;
        }

        char c = apiName.charAt(0);
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
            Pattern pattern = Pattern.compile("[0-9|a-z|A-Z|_]*");
            return pattern.matcher(apiName).matches();
        }

        return false;
    }

}
