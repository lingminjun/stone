package com.lmj.stone.service.gen;

import com.lmj.stone.core.gen.Generator;
import com.lmj.stone.dao.gen.MybatisGenerator;
import com.lmj.stone.idl.IDLAPISecurity;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lingminjun
 * Date: 2018-06-24
 * Time: 下午12:45
 */
public class RestfulControllerGenerator extends Generator {

    public final ServiceGenerator serviceGenerator;
    public final String apiBasePath;
    private List<MybatisGenerator.Table> tables;

    /**
     *
     * @param packageName     项目包名【必填】
     * @param apiBasePath     接口基本路径【必填】
     * @param sqlsSourcePath  资源路径【必填】
     * @param transactionManager  事务管理【必填】
     * @param exceptionClass  异常类地址【必填】
     */
    public RestfulControllerGenerator(String packageName, String apiBasePath, String sqlsSourcePath, String transactionManager, Class exceptionClass) {
        this(packageName,apiBasePath,sqlsSourcePath,transactionManager,exceptionClass, IDLAPISecurity.UserLogin);
    }

    /**
     *
     * @param packageName     项目包名【必填】
     * @param apiBasePath     接口基本路径【必填】
     * @param sqlsSourcePath  资源路径【必填】
     * @param transactionManager  事务管理【必填】
     * @param exceptionClass  异常类地址【必填】
     * @param security 接口的验权等级，仅仅支持idl API时有用
     */
    public RestfulControllerGenerator(String packageName, String apiBasePath, String sqlsSourcePath, String transactionManager, Class exceptionClass, IDLAPISecurity security) {
        this(packageName,apiBasePath,sqlsSourcePath,transactionManager,exceptionClass,null,security);
    }

    /**
     *
     * @param packageName     项目包名【必填】
     * @param apiBasePath     接口基本路径【必填】
     * @param sqlsSourcePath  资源路径【必填】
     * @param exceptionClass  异常类地址【必填】
     * @param projectDir      工程目录【可选】
     * @param security 接口的验权等级，仅仅支持idl API时有用
     */
    public RestfulControllerGenerator(String packageName, String apiBasePath, String sqlsSourcePath, String transactionManager, Class exceptionClass, String projectDir, IDLAPISecurity security) {
        super(packageName, projectDir);

        // doa包名处理
        String servicePackage = packageName;
        if (packageName.endsWith("controller")) {
            servicePackage = packageName.substring(0,packageName.length() - "controller".length()) + "service";
        } else {//直接放到其子目录
            servicePackage = packageName + File.separator + "service";
        }

        this.serviceGenerator = new ServiceGenerator(servicePackage,sqlsSourcePath,transactionManager,exceptionClass,projectDir,security);
        this.tables = serviceGenerator.mybatisGenerator.getTables();
        if (!apiBasePath.startsWith("/")) {
            apiBasePath = "/" + apiBasePath;
        }

        if (!apiBasePath.endsWith("/")) {
            apiBasePath = apiBasePath + "/";
        }
        this.apiBasePath = apiBasePath;
    }



    @Override
    public boolean gen() {

        if (!this.serviceGenerator.gen()) {
            return false;
        }

        //生成基类
        for (MybatisGenerator.Table table : tables) {
            //生成rest api
            File serviceImplFile = new File(this.packagePath + File.separator + table.getSimpleRestControllerName() + ".java");
            writeRestController(serviceImplFile,apiBasePath,packageName,this.serviceGenerator.packageName,this.serviceGenerator.mybatisGenerator.sqlsSourcePath,table);
        }


        return true;
    }

    private static void writeRestController(File file, String apiBasePath, String packageName,String servicePackagaeName, String sqlsSourcePath, MybatisGenerator.Table table) {

        StringBuilder serviceContent = new StringBuilder();
        serviceContent.append("package " + packageName + ";\n\r\n\r");
        serviceContent.append("import com.lmj.stone.idl.IDLException;\n");
        serviceContent.append("import org.springframework.web.bind.annotation.*;\n");
        serviceContent.append("import java.util.ArrayList;\n");
        serviceContent.append("import java.util.List;\n");
        serviceContent.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        serviceContent.append("import " + table.getPOJOClassName(servicePackagaeName) + ";\n");
        serviceContent.append("import " + table.getPOJOResultsClassName(servicePackagaeName) + ";\n");
        serviceContent.append("import " + table.getCRUDServiceBeanName(servicePackagaeName) + ";\n");
        serviceContent.append("\n\r\n\r");

        serviceContent.append("/**\n");
        serviceContent.append(" * Owner: Minjun Ling\n");
        serviceContent.append(" * Creator: Robot\n");
        serviceContent.append(" * Version: 1.0.0\n");
        serviceContent.append(" * Since: " + new Date() + "\n");
        serviceContent.append(" * SQLFile: " + sqlsSourcePath + "\n");
        serviceContent.append(" */\n");

        serviceContent.append("@RestController\n");
        serviceContent.append("public class " + table.getSimpleRestControllerName() + " {\n\n");

        // 定义service属性
        serviceContent.append("    @Autowired\n");
        String serviceClassName = table.getSimpleCRUDServiceBeanName();
        String servicePropertyName = toLowerHeadString(serviceClassName);
        serviceContent.append("    private ");
        serviceContent.append(serviceClassName);
        serviceContent.append(" ");
        serviceContent.append(servicePropertyName);
        serviceContent.append(";\n\n");


        String pojoName = table.getSimplePOJOClassName();
        //所有基本的增删修查

        //新增方法
        writeCreateMethod(pojoName, apiBasePath, serviceContent, table);

        //删，单个删除
        writeDeleteMethod(pojoName, apiBasePath, serviceContent, table);

        //更新某个数据，拆开每个字段
        writeUpdateMethod(pojoName, apiBasePath, serviceContent, table);

        //主键查询
        writeFindByIdMethod(pojoName, apiBasePath, serviceContent, table);

        /* 由于多个查询，无法确定哪一个 //查询，索引查询，翻页
        Map<String, List<MybatisGenerator.Column>> queryMethods = table.allIndexQueryMethod();
        List<String> methodNames = new ArrayList<String>(queryMethods.keySet());
        Collections.sort(methodNames);
        for (String methodName : methodNames) {
            List<MybatisGenerator.Column> cols = queryMethods.get(methodName);

            writeQueryMethod(pojoName, apiBasePath, methodName, cols, serviceContent, table);
        }*/


        serviceContent.append("}\n\r\n\r");

        try {
            writeFile(file,serviceContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeCreateMethod(String pojoName, String apiBasePath, StringBuilder serviceContent, MybatisGenerator.Table table) {
        String tableModelName = toHumpString(table.getName(),true);
        String path = apiBasePath + table.getName();
        String param = toLowerHeadString(pojoName);

        serviceContent.append("    /**\n");
        serviceContent.append("     * create a " + tableModelName + "\n");
        serviceContent.append("     * @return " + tableModelName + ".id \n");
        serviceContent.append("     */\n");

        String createMethod = "create" + tableModelName;
        serviceContent.append("    @RequestMapping(value = \"" + path + "\", method = RequestMethod.POST)\n");
        serviceContent.append("    public long " + createMethod + "(@RequestBody final " + pojoName + " " + param + ") throws IDLException {\n");

        //实现代码
        String theServiceBean = toLowerHeadString(table.getSimpleCRUDServiceBeanName());

        serviceContent.append("        return " + theServiceBean + ".add" + tableModelName + "(" + param + ");\n");

        serviceContent.append("    }\n\n");
    }

    private static void writeDeleteMethod(String pojoName, String apiBasePath, StringBuilder serviceContent, MybatisGenerator.Table table) {
        String tableModelName = toHumpString(table.getName(),true);
        String path = apiBasePath + table.getName();

        MybatisGenerator.Column column = table.getDeleteStateColumn();
        if (column == null) {
            return;
        }

        serviceContent.append("    /**\n");
        serviceContent.append("     * delete the " + tableModelName + "\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        String removeMethod = "deleteThe" + tableModelName;

        serviceContent.append("    @RequestMapping(value = \"" + path + "/{id}\", method = RequestMethod.DELETE)\n");
        serviceContent.append("    public boolean " + removeMethod + "(@PathVariable(\"id\") final long id) throws IDLException {\n");

        //实现代码
        String theServiceBean = toLowerHeadString(table.getSimpleCRUDServiceBeanName());

        serviceContent.append("        return " + theServiceBean + ".removeThe" + tableModelName + "(id);\n");
        serviceContent.append("    }\n\n");

    }

    private static void writeUpdateMethod(String pojoName, String apiBasePath, StringBuilder serviceContent, MybatisGenerator.Table table) {
        String tableModelName = toHumpString(table.getName(),true);
        String path = apiBasePath + table.getName();
        String param = toLowerHeadString(pojoName);

        serviceContent.append("    /**\n");
        serviceContent.append("     * update the " + tableModelName + "\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");

        String updateMethod = "updateThe" + tableModelName;

        serviceContent.append("    @RequestMapping(value = \"" + path + "\", method = RequestMethod.PUT)\n");
        serviceContent.append("    public boolean " + updateMethod + "(@RequestBody final " + pojoName + " " + param + ") throws IDLException {\n");

        //实现代码
        String theServiceBean = toLowerHeadString(table.getSimpleCRUDServiceBeanName());

        serviceContent.append("        return " + theServiceBean + ".updateThe" + tableModelName + "(" + param + ".id");
        for (MybatisGenerator.Column cl : table.getColumns()) {
            //忽略字段
            if (cl.getName().equals("is_delete")
                    || cl.getName().equals("delete")
                    || cl.getName().equals("id")
                    || cl.getName().equals("create_at")
                    || cl.getName().equals("modified_at")
                    ) {
                continue;
            }

            String paramName = toHumpString(cl.getName(),false);
            serviceContent.append(",\n");
            serviceContent.append("                ");
            serviceContent.append(param + "." + paramName);
        }

        serviceContent.append(");\n");

        serviceContent.append("    }\n\n");
    }

    private static void writeFindByIdMethod(String pojoName, String apiBasePath, StringBuilder serviceContent, MybatisGenerator.Table table) {
        String tableModelName = toHumpString(table.getName(),true);
        String path = apiBasePath + table.getName();

        serviceContent.append("    /**\n");
        serviceContent.append("     * find the " + tableModelName + "\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");

        String findMethod = "findThe" + tableModelName;


        serviceContent.append("    @RequestMapping(value = \"" + path + "/{id}\", method = RequestMethod.GET)\n");
        serviceContent.append("    public " + pojoName + " " + findMethod + "(@PathVariable(\"id\") final long id) throws IDLException {\n");

        //实现代码
        String theServiceBean = toLowerHeadString(table.getSimpleCRUDServiceBeanName());

        serviceContent.append("        return " + theServiceBean + ".findThe" + tableModelName + "(id,false);");

        serviceContent.append("    }\n\n");

    }

    private static void writeQueryMethod(String pojoName, String apiBasePath, String queryMethodName, List<MybatisGenerator.Column> columns, StringBuilder serviceContent, MybatisGenerator.Table table) {
        String tableModelName = toHumpString(table.getName(),true);
        String path = apiBasePath + table.getName() + "/search";

        String methodName = "search" + tableModelName + queryMethodName.substring(5,queryMethodName.length());//.replace("query", "query" + tableModelName);

        String defineMethod = "    public " + table.getSimplePOJOResultsClassName() + " " + methodName + "(@RequestParam(value = \"pageIndex\", required = true) final int pageIndex";
        String spacing = formatSpaceParam(defineMethod);



        //提前处理参数
        StringBuilder methodParams = new StringBuilder();
        StringBuilder methodParamsDef = new StringBuilder();
        for (MybatisGenerator.Column column : columns) {

            String param = toHumpString(column.getName(),false);
            if (methodParams.length() > 0) {
                methodParams.append(",");
            }
            methodParams.append(param);

            methodParamsDef.append(",\n");
            methodParamsDef.append(spacing);
            methodParamsDef.append("@RequestParam(value = \"" + param + "\", required = true) final " + column.getDataType() + " " + param);
        }

        // 判断是否有delete参数
        boolean hasDeleted = false;
        MybatisGenerator.Column theDelete = null;
        if (table != null) {
            theDelete = table.getDeleteStateColumn();
            if (!MybatisGenerator.Table.hasDeleteStateColumn(columns) && theDelete != null) {
                hasDeleted = true;

                methodParamsDef.append(",\n");
                methodParamsDef.append(spacing);
                methodParamsDef.append("@RequestParam(value = \"isDeleted\", required = false) final boolean isDeleted");
            }
        }

        //开始编写函数代码
        serviceContent.append("    /**\n");
        serviceContent.append("     * search " + tableModelName + "s\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");


        serviceContent.append("    @RequestMapping(value = \"" + path + "\", method = RequestMethod.GET)\n");
        serviceContent.append(defineMethod);//首段写入
        serviceContent.append(",\n");
        serviceContent.append(spacing);
        serviceContent.append("@RequestParam(value = \"pageSize\", required = true) final int pageSize");

        //定义所有参数
        serviceContent.append(methodParamsDef.toString());
        serviceContent.append(") throws IDLException {\n");


        //实现代码
        String theServiceBean = toLowerHeadString(table.getSimpleCRUDServiceBeanName());
        String resultsName = table.getSimplePOJOResultsClassName();
        String methodParamsString = methodParams.toString();
        String serviceMethodName = "query" + tableModelName + queryMethodName.substring(5,queryMethodName.length());

        serviceContent.append("        return " + theServiceBean + "." + serviceMethodName + "(pageIndex,pageSize,");
        serviceContent.append(methodParamsString);
        if (hasDeleted) {
            serviceContent.append(",");
            serviceContent.append("isDeleted");
        }
        serviceContent.append(",false);\n");

        serviceContent.append("    }\n\n");
    }


}
