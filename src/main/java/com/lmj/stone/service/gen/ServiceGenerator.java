package com.lmj.stone.service.gen;

import com.lmj.stone.core.gen.Generator;
import com.lmj.stone.dao.gen.MybatisGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description: 生成部分固定的service形式以及doa文件
 * User: lingminjun
 * Date: 2018-06-12
 * Time: 下午11:22
 */
public class ServiceGenerator extends Generator {

    private MybatisGenerator mybatisGenerator;
    private List<MybatisGenerator.Table> tables;

    /**
     *
     * @param packageName     项目包名【必填】
     * @param sqlsSourcePath  资源路径【必填】
     */
    public ServiceGenerator(String packageName, String sqlsSourcePath) {
        this(packageName,sqlsSourcePath,null);
    }

    /**
     *
     * @param packageName     项目包名【必填】
     * @param sqlsSourcePath  资源路径【必填】
     * @param projectDir      工程目录【可选】
     */
    public ServiceGenerator(String packageName, String sqlsSourcePath, String projectDir) {
        super(packageName, sqlsSourcePath);

        // doa包名处理
        String doaPackage = packageName;
        if (packageName.endsWith("service")) {
            doaPackage = packageName.substring(0,packageName.length() - "service".length()) + "db";
        } else {//直接放到其子目录
            doaPackage = packageName + File.separator + "db";
        }

        mybatisGenerator = new MybatisGenerator(doaPackage, projectDir, sqlsSourcePath);
        tables = mybatisGenerator.getTables();

    }

    @Override
    public boolean gen() {

        //先生成DOA
        if (!mybatisGenerator.gen()) {
            return false;
        }

        // 构建目录
        String entitiesPath = this.packagePath + File.separator + "entities";
        new File(entitiesPath).mkdirs();
        String crudPath = this.packagePath + File.separator + "inc";
        new File(crudPath).mkdirs();


        //生成基类
        for (MybatisGenerator.Table table : tables) {
            //产生实体类
            File pojoFile = new File(entitiesPath + File.separator + table.getSimplePOJOClassName() + ".java");
            writeEntity(pojoFile,packageName,table);

            //产生实体类集合
            File pojosFile = new File(entitiesPath + File.separator + table.getSimplePOJOClassName() + "Results.java");
            writeEntityResults(pojosFile,packageName,table);

        }

        return false;
    }

    private static void writeEntity(File file, String packageName, MybatisGenerator.Table table) {
        StringBuilder pojoContent = new StringBuilder();
        pojoContent.append("package " + packageName + ".entities;\n\r\n\r");
        pojoContent.append("import com.lmj.stone.idl.annotation.IDLDesc;\n");
        pojoContent.append("import java.io.Serializable;\n\r\n\r");
        pojoContent.append("/**\n");
        pojoContent.append(" * Owner: Minjun Ling\n");
        pojoContent.append(" * Creator: Robot\n");
        pojoContent.append(" * Version: 1.0.0\n");
        pojoContent.append(" * Since: " + new Date() + "\n");
        pojoContent.append(" * Table: " + table.getName() + "\n");
        pojoContent.append(" */\n");
        pojoContent.append("@IDLDesc(\"" + table.getName() + "\")\n");
        pojoContent.append("public final class " + table.getSimplePOJOClassName() + " implements Serializable {\n");
        pojoContent.append("    private static final long serialVersionUID = 1L;\n");

        for (MybatisGenerator.Column cl : table.getColumns()) {
            if (cl.getName().equals("is_delete") || cl.getName().equals("delete")) {
                continue;
            }
            if (MybatisGenerator.MYSQL_LONG_TYPE.contains(cl.getType())) {
                pojoContent.append("    @IDLDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    public long    ");
            } else if (MybatisGenerator.MYSQL_BOOL_TYPE.contains(cl.getType())) {
                pojoContent.append("    @IDLDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    public boolean ");
            } else if (MybatisGenerator.MYSQL_DOUBLE_TYPE.contains(cl.getType())) {
                pojoContent.append("    @IDLDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    public double  ");
            } else if (MybatisGenerator.MYSQL_INT_TYPE.contains(cl.getType())) {
                pojoContent.append("    @IDLDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    public int     ");
            } else if (MybatisGenerator.MYSQL_STRING_TYPE.contains(cl.getType())) {
                pojoContent.append("    @IDLDesc(\"" + cl.getCmmt() + "\")\n");
                pojoContent.append("    public String  ");
            } else {
                continue;
            }
            pojoContent.append(toHumpString(cl.getName(),false) + ";\n");
        }

        pojoContent.append("}\n\r\n\r");

        try {
            writeFile(file,pojoContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeEntityResults(File file, String packageName, MybatisGenerator.Table table) {
        //在增加一个结果集
        StringBuilder resultContent = new StringBuilder();
        resultContent.append("package " + packageName + ".entities;\n\r\n\r");
        resultContent.append("import com.lmj.stone.idl.annotation.IDLDesc;\n");
        resultContent.append("import com.lmj.stone.service.PageResults;\n\r\n\r");
        resultContent.append("/**\n");
        resultContent.append(" * Owner: Minjun Ling\n");
        resultContent.append(" * Creator: Robot\n");
        resultContent.append(" * Version: 1.0.0\n");
        resultContent.append(" * Since: " + new Date() + "\n");
        resultContent.append(" * Description: " + table.getSimplePOJOClassName() + "结果集\n");
        resultContent.append(" */\n");
        resultContent.append("@IDLDesc(\"" + table.getSimplePOJOClassName() + "结果集\")\n");
        resultContent.append("public final class " + table.getSimplePOJOClassName() + "Results extends PageResults<" + table.getSimplePOJOClassName() + "> { \n");
        resultContent.append("    /* nothing */\n");
        resultContent.append("}\n\r\n\r");

        try {
            writeFile(file,resultContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeCRUDService(File file, String className, String packageName, MybatisGenerator.Table table) {
        StringBuilder serviceContent = new StringBuilder();
        serviceContent.append("package " + packageName + ".inc;\n\r\n\r");
        serviceContent.append("import com.lmj.stone.idl.IDLException;\n");
        serviceContent.append("import com.lmj.stone.idl.annotation.IDLAPI;\n");
        serviceContent.append("import com.lmj.stone.idl.annotation.IDLError;\n");
        serviceContent.append("import com.lmj.stone.idl.annotation.IDLGroup;\n");
        serviceContent.append("import com.lmj.stone.idl.annotation.IDLParam;\n");
        serviceContent.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        serviceContent.append("import " + table.getDAOClassName(packageName) + ";\n");
        serviceContent.append("import " + table.getPOJOClassName(packageName) + ";\n\r\n\r");
        serviceContent.append("/**\n");
        serviceContent.append(" * Owner: Minjun Ling\n");
        serviceContent.append(" * Creator: Robot\n");
        serviceContent.append(" * Version: 1.0.0\n");
        serviceContent.append(" * Since: " + new Date() + "\n");
        serviceContent.append(" * Table: " + table.getName() + "\n");
        serviceContent.append(" */\n");
        serviceContent.append("public abstract class " + table.getSimpleCRUDServiceBeanName() + " {\n\n");

        //获取doa接口
        serviceContent.append("    /**\n");
        serviceContent.append("     * " + table.getSimpleDAOClassName() + "\n");
        serviceContent.append("     * @return \n");
        serviceContent.append("     */\n");
        serviceContent.append("    @Autowired\n");

        String theDAO = toLowerHeadString(table.getSimpleDAOClassName());
        serviceContent.append("    " + table.getSimpleDAOClassName() + " " + theDAO + ";\n\n");
        //        serviceContent.append("    protected abstract " + table.getSimpleDAOClassName() + " get" + table.getSimpleDAOClassName() + ";\n\n");

        String getTheDAO = "this." + theDAO;

        //所有基本的增删修查


        serviceContent.append("}\n\r\n\r");

        try {
            writeFile(file,serviceContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
