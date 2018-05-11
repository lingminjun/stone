package com.lmj.stone.dao.gen;

import com.lmj.stone.dao.TableDAO;
import org.apache.ibatis.annotations.Mapper;
import com.lmj.stone.dao.SQL;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MybatisGenerator {
    private final static HashSet<String> MYSQL_TAGS = new HashSet<String>();
    static {
        MYSQL_TAGS.add("PRIMARY");
        MYSQL_TAGS.add("KEY");
        MYSQL_TAGS.add("UNIQUE");
        MYSQL_TAGS.add("USING");
        MYSQL_TAGS.add("BTREE");
        MYSQL_TAGS.add("INDEX");
        MYSQL_TAGS.add("ENGINEInnoDB");
        MYSQL_TAGS.add("IDXUSERID");
        MYSQL_TAGS.add("UNIIDXQUERY");
        MYSQL_TAGS.add("DEFAULT");
    }


    private final static HashSet<String> MYSQL_LONG_TYPE = new HashSet<String>();
    static {
        MYSQL_LONG_TYPE.add("BIGINT");
    }

    private final static HashSet<String> MYSQL_BOOL_TYPE = new HashSet<String>();
    static {
        MYSQL_BOOL_TYPE.add("BOOL");
    }

    private final static HashSet<String> MYSQL_DOUBLE_TYPE = new HashSet<String>();
    static {
        MYSQL_DOUBLE_TYPE.add("FLOAT");
        MYSQL_DOUBLE_TYPE.add("DOUBLE");
        MYSQL_DOUBLE_TYPE.add("REAL");

        //另外使用java.math.BigDecimal存储
        MYSQL_DOUBLE_TYPE.add("DECIMAL");
        MYSQL_DOUBLE_TYPE.add("DEC");
        MYSQL_DOUBLE_TYPE.add("NUMERIC");
    }

    private final static HashSet<String> MYSQL_INT_TYPE = new HashSet<String>();
    static {
        MYSQL_INT_TYPE.add("TINYINT");
        MYSQL_INT_TYPE.add("BIT");
        MYSQL_INT_TYPE.add("SMALLINT");
        MYSQL_INT_TYPE.add("INT");
        MYSQL_INT_TYPE.add("INTEGER");
    }

    private final static HashSet<String> MYSQL_STRING_TYPE = new HashSet<String>();
    static {
        MYSQL_STRING_TYPE.add("CHAR");
        MYSQL_STRING_TYPE.add("VARCHAR");
        MYSQL_STRING_TYPE.add("TINYBLOB");
        MYSQL_STRING_TYPE.add("TINYTEXT");
        MYSQL_STRING_TYPE.add("BLOB");
        MYSQL_STRING_TYPE.add("TEXT");
        MYSQL_STRING_TYPE.add("MEDIUMBLOB");
        MYSQL_STRING_TYPE.add("MEDIUMTEXT");
        MYSQL_STRING_TYPE.add("LONGBLOB");
        MYSQL_STRING_TYPE.add("LONGTEXT");
        MYSQL_STRING_TYPE.add("ENUM");
        MYSQL_STRING_TYPE.add("SET");
    }

    private final static HashSet<String> MYSQL_DATE_TYPE = new HashSet<String>();
    static {
        MYSQL_DATE_TYPE.add("DATETIME");
        MYSQL_DATE_TYPE.add("DATE");
        MYSQL_DATE_TYPE.add("TIMESTAMP");
        MYSQL_DATE_TYPE.add("TIME");
        MYSQL_DATE_TYPE.add("YEAR");
    }

    /**
     * 生成DAO层代码
     * @param packageName 指定包名【必填】
     * @param sqlsSourcePath    sqls文件资源路径:sqls/xxx.sql【必填】
     */
    public static void gen(String packageName,  String sqlsSourcePath) {
        gen(packageName,sqlsSourcePath,null);
    }

    /**
     * 生成DAO层代码
     * @param packageName 指定包名【必填】
     * @param sqlsSourcePath    sqls文件资源路径:sqls/xxx.sql【必填】
     * @param projectDir  项目目录，可以不填
     */
    public static void gen( String packageName,  String sqlsSourcePath, String projectDir) {
        gen(packageName,sqlsSourcePath,projectDir,null);
    }

    /**
     * 生成DAO层代码
     * @param packageName 指定包名【必填】
     * @param sqlsSourcePath    sqls文件资源路径:sqls/xxx.sql【必填】
     * @param projectDir  项目目录，可以不填
     * @param mapperPath  Mybatis Configuration配置文件路径:资源路径
     */
    public static void gen( String packageName,  String sqlsSourcePath, String projectDir, String mapperPath) {

        //工程目录
        File projFile = getCurrentProjectDirFile();
        if (projectDir == null || projectDir.length() == 0) {
            projectDir = projFile.getAbsolutePath();
        }

        if (projectDir.endsWith(File.separator)) {
            projectDir = projectDir.substring(0,projectDir.length() - 1);
        }

        String[] pcks = packageName.split("\\.");

        String dobjDir = null;
        String daoDir = null;
        String mapDir = null;
        String confPath = null;

        //因为考虑有些工程，并不是main/java目录，可能直接就上是java目录[暂时不去兼容]
        StringBuilder srcBuilder = new StringBuilder(projectDir);
        srcBuilder.append(File.separator);
        srcBuilder.append("src");
        srcBuilder.append(File.separator);
        srcBuilder.append("main");

        mapDir = srcBuilder.toString() + File.separator + "resources" + File.separator + "sqlmap";
        new File(mapDir).mkdirs();

        //mybatis配置路径
        if (mapperPath != null && mapperPath.length() > 0) {
            confPath = srcBuilder.toString() + File.separator + "resources" + File.separator + mapperPath;
        } else {
            confPath = srcBuilder.toString() + File.separator + "resources" + File.separator + "mybatis-sqlmap-config.xml";
        }

        srcBuilder.append(File.separator);
        srcBuilder.append("java");

        //包名
        for (String pck : pcks) {
            if (pck.length() > 0 && !pck.equals("/") && !pck.equals("\\")) {
                srcBuilder.append(File.separator);
                srcBuilder.append(pck);
            }
        }

        dobjDir = srcBuilder.toString() + File.separator + "dobj";
        new File(dobjDir).mkdirs();
        daoDir = srcBuilder.toString() + File.separator + "dao";
        new File(daoDir).mkdirs();

        //解析sqls中的tables
        List<Table> tables = parseSqlTables(sqlsSourcePath);

        for (Table table : tables) {
            genTheTable(table,packageName,dobjDir,daoDir,mapDir,confPath);
        }
    }

    private static List<Table> parseSqlTables(String sqlsSourcePath) {
        List<Table> tables = new ArrayList<Table>();

        //读取sql文件
        String sqlsContent = getSqlsContent(sqlsSourcePath);
        //不能分割
        Pattern p = Pattern.compile("create\\s+table",Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sqlsContent);
        final int size = sqlsContent.length();

        while (m.find())
        {
            //匹配到一组
            int end = m.end();
            int idx = sqlsContent.indexOf("(",end);//从匹配命令后开始
            //解析表头
            StringBuilder builder = new StringBuilder();
            for (int i = idx - 1; i >= 0; i--) {
                char c = sqlsContent.charAt(i);
                if (isLetterChar(c) || isNumberChar(c)) {
                    builder.insert(0,c);
                } else if (c == '_') {//是否采用驼峰
                    builder.insert(0,c);
                } else {
                    if (builder.length() > 0) {
                        break;
                    }
                }
            }
            String tableName = builder.toString();
            System.out.println("tableName:" + tableName);
            Table table = new Table();
            table.name = tableName;
            tables.add(table);

            //开始检查列属性
            int i = idx + 1;
            StringBuilder column = new StringBuilder();
            StringBuilder type = new StringBuilder();
            StringBuilder comment = new StringBuilder();
            int flag = 0;//0匹配column,1匹配类型,2匹配comment
            boolean isCmmtTag = true;
            while (i < size) {
                final int index = i;
                char c = sqlsContent.charAt(i++);
                if (flag == 0) {//匹配列名
                    if (dealAppendColumnName(column,c)) {
                        flag = 1;
                    }
                } else if (flag == 1) {//匹配类型
                    if (dealAppendColumnType(type,c)) {
                        flag = 2;
                    }
                } else if (flag == 2) {

                    //表示结束了，并没有写注解
                    if (isSQLEndChar(c)) {

                        //添加列
                        addColumnNodeInTable(column,type,comment,table);

                        //重置数据
                        column = new StringBuilder();
                        type = new StringBuilder();
                        comment = new StringBuilder();

                        flag = 0;
                        isCmmtTag = false;

                        //跳到下一个命令
                        if (c == ';') {
                            break;
                        }
                    }

                    //嵌套字符串（小bug,当注解中穿插其他单引号或者双引号，后续再改进）
                    if ((c == '\'' || c == '\"') && sqlsContent.charAt(index-1) != '\\') {
                        flag++;
                        // COMMENT '
                        isCmmtTag = isCommentTag(index,sqlsContent);
                    }
                } else {
                    //嵌套字符 出（小bug,当注解中穿插其他单引号或者双引号，后续再改进）
                    if ((c == '\'' || c == '\"') && sqlsContent.charAt(index-1) != '\\') {
                        flag--;
                    } else if (isCmmtTag){//此处最好检查下，是不是写入了默认值
                        comment.append(c);
                    }

                }
            }
        }

        return tables;
    }

    private static boolean isSpacingChar(char c) {
        if (c == '\f' || c == '\n' || c == '\r' || c == '\t' || c == ' ') {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isLetterChar(char c) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isNumberChar(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isSQLEndChar(char c) {
        if (c == ',' || c == ';') {
            return true;
        } else {
            return false;
        }
    }

    private static boolean dealAppendColumnName(StringBuilder column, char c) {
        if (isSpacingChar(c)) {
            //进入下一个阶段
            if (column != null && column.length() > 0) {
                return true;
            }
        } else if (isLetterChar(c) || isNumberChar(c)) {
//            if (column == null) {column = new StringBuilder();}
            column.append(c);
        } else if (c == '_') {//是否采用驼峰
//            if (column == null) {column = new StringBuilder();}
            column.append(c);
        }
        return false;
    }

    private static boolean dealAppendColumnType(StringBuilder type, char c) {
        if (isSpacingChar(c) || c == '(') {//处理verchar(12)的可能
            //进入下一个阶段
            if (type != null && type.length() > 0) {
                return true;
            }
        } else if (isLetterChar(c)) {//纯字符
//            if (type == null) {type = new StringBuilder();}
            type.append(c);
        }
        return false;
    }

    private static void addColumnNodeInTable(StringBuilder column, StringBuilder type, StringBuilder comment, Table table) {
        String clm = column.toString();
        String typ = type.toString();

        if (!MYSQL_TAGS.contains(clm) && !MYSQL_TAGS.contains(typ)) {
            Column cl = new Column();
            cl.name = clm;
            cl.type = typ.toUpperCase();
            cl.cmmt = comment != null ? comment.toString() : null;
            table.columns.add(cl);
        }

        System.out.println("column:" + column.toString() + " type:" + type.toString() + " cmm:" + comment);
    }

    private static boolean isCommentTag(int idx, String sqlsContent) {

        // COMMENT '
        StringBuilder prefix = new StringBuilder();
        int j = idx - 1;
        while (j >= 0) {
            char cc = sqlsContent.charAt(j--);
            if (isSpacingChar(cc)) {
                if (prefix.length() > 0) {
                    break;
                }
            } else {
                prefix.insert(0,cc);
            }
        }

        return prefix.toString().toUpperCase().equals("COMMENT");

    }

    private static class Column {
        String name;
        String type;
        String cmmt;
    }

    private static class Table {
        String name;
        List<Column> columns = new ArrayList<Column>();
    }

    private static class MapperMethod {
        String id;//方法名
        String returnType;//返回值类型
        String sql;//对应的sql
    }

    private static void genTheTable(Table table,String packName, String dobjDir,String daoDir,String mapDir,String confPath) {
        if (table == null || table.columns.size() == 0) {
            return;
        }

        //驼峰法
        String name = toHumpString(table.name,true);

        String dobjFileName = name + "DO.java";
        String daoFileName = name + "DAO.java";
        String mapperFileName = table.name.replaceAll("_","-") + "-sqlmap.xml";

        File dobjFile = new File(dobjDir + File.separator + dobjFileName);
        File daoFile = new File(daoDir + File.separator + daoFileName);
        File dmapFile = new File(mapDir + File.separator + mapperFileName);


        wirteDObject(dobjFile,name,packName,table);
        List<MapperMethod> methods = wirteDAObject(daoFile,name,packName,table);
        wirteMapper(dmapFile,name,packName,table, methods);
    }

    private static void wirteDObject(File file, String className, String packageName, Table table) {
        StringBuilder dobjContent = new StringBuilder();
        dobjContent.append("package " + packageName + ".dobj;\n\r\n\r");
        dobjContent.append("import java.io.Serializable;\n\r\n\r");
        dobjContent.append("/**\n");
        dobjContent.append(" * Owner: Robot\n");
        dobjContent.append(" * Creator: lingminjun\n");
        dobjContent.append(" * Version: 1.0.0\n");
        dobjContent.append(" * Since: " + new Date() + "\n");
        dobjContent.append(" * Table: " + table.name + "\n");
        dobjContent.append(" */\n");
        dobjContent.append("public final class " + className + "DO implements Serializable {\n");
        dobjContent.append("    private static final long serialVersionUID = 1L;\n");

        for (Column cl : table.columns) {
            if (MYSQL_LONG_TYPE.contains(cl.type)) {
                dobjContent.append("    public Long    ");
            } else if (MYSQL_BOOL_TYPE.contains(cl.type)) {
                dobjContent.append("    public Boolean ");
            } else if (MYSQL_DOUBLE_TYPE.contains(cl.type)) {
                dobjContent.append("    public Double  ");
            } else if (MYSQL_INT_TYPE.contains(cl.type)) {
                dobjContent.append("    public Integer ");
            } else if (MYSQL_STRING_TYPE.contains(cl.type)) {
                dobjContent.append("    public String  ");
            } else {
                continue;
            }


            dobjContent.append(toHumpString(cl.name,false) + ";");
            if (cl.cmmt != null && cl.cmmt.length() > 0) {
                dobjContent.append(" // " + cl.cmmt);
            }
            dobjContent.append("\n");
        }

        dobjContent.append("}\n\r\n\r");

        try {
            writeFile(file,dobjContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String,String> getImportLineFromJavaSource(String content) {
        //找import语句
        Pattern p = Pattern.compile("import\\s+[\\w$.]+\\s*;");
        Matcher m = p.matcher(content);
        Map<String,String> map = new HashMap<String, String>();
        while (m.find())
        {
//            System.out.println(m.group(0));
            String imp = m.group(0);
            String[] strs = imp.trim().split("\\s+");
            if (strs.length == 2) {
                map.put(strs[1].substring(0,strs[1].length() - 1),imp);
            } else {
                map.put(imp,imp);
            }
        }
        return map;
    }

    private static String getBodyFromJavaSource(String content) {
        //找import语句
        Pattern p = Pattern.compile("public\\s+(interface|class)\\s+[\\w$.]+[\\w$.<>\\s]*\\{[\\S\\s]*}");
        Matcher m = p.matcher(content);
        while (m.find())
        {
            return m.group(0);
        }
        return null;
    }

    private static List<MapperMethod> getInterfaceMapperMetthods(Class clazz) {
        List<MapperMethod> list = new ArrayList<MapperMethod>();

        Method[] methods = clazz.getMethods();
        if (methods == null || methods.length == 0) {
            return list;
        }

        for (int i = 0; i < methods.length; i++) {
            Method md = methods[i];

            SQL mapper = md.getAnnotation(SQL.class);
            if (mapper == null) {
                continue;
            }

            MapperMethod mapperMethod = new MapperMethod();
            mapperMethod.id = md.getName();
            mapperMethod.sql = mapper.value();

            //返回值
            String type = md.getGenericReturnType().toString();
            if (type.contains("<")) {
                type = type.split("<")[1];
                type = type.substring(0,type.length() - 1);
            } else if (type.startsWith("class ")) {
                type = type.substring("class ".length(), type.length());
            }

            //转包装类型
            if (type.equals("int")) {
                type = Integer.class.getName();
            } else if (type.equals("short")) {
                type = Short.class.getName();
            } else if (type.equals("long")) {
                type = Long.class.getName();
            } else if (type.equals("boolean")) {
                type = Boolean.class.getName();
            } else if (type.equals("byte")) {
                type = Byte.class.getName();
            } else if (type.equals("char")) {
                type = Character.class.getName();
            } else if (type.equals("float")) {
                type = Float.class.getName();
            } else if (type.equals("double")) {
                type = Double.class.getName();
            }
            mapperMethod.returnType = type;
//            System.out.println(type);
            list.add(mapperMethod);
        }

        return list;
    }

    private static List<MapperMethod> wirteDAObject(File file, String className, String packageName, Table table) {
        List<MapperMethod> methods = null;

        //此类全称
        String daobj = packageName + ".dao." + className + "DAO";

        //如果文件本身存在，则保留文件体
        Map<String,String> imports = new HashMap<String, String>();
        String body = null;
        if (file.exists()) {

            //先获取要执行的额外内容
            try {
                methods = getInterfaceMapperMetthods(Class.forName(daobj));
            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
                System.out.println("抱歉！没有加载到原类，请采用单元测试执行Generator！");
            }

            //保留java代码
            try {
                String old = readFile(file.getAbsolutePath());
                imports = getImportLineFromJavaSource(old);
                body = getBodyFromJavaSource(old);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        imports.put(TableDAO.class.getName(),"import " + TableDAO.class.getName() + ";");
        String dobj = packageName + ".dobj." + className + "DO";
        imports.put(dobj,"import " + dobj + ";");
        imports.put(Mapper.class.getName(),"import " + Mapper.class.getName() + ";");

        StringBuilder content = new StringBuilder();
        content.append("package " + packageName + ".dao;\n\r\n\r");

        //imports
        Iterator<Map.Entry<String, String>> entries = imports.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            content.append(entry.getValue() + "\n");
        }
        content.append("\n\n");
        content.append("/**\n");
        content.append(" * Owner: Robot\n");
        content.append(" * Creator: lingminjun\n");
        content.append(" * Version: 1.0.0\n");
        content.append(" * Since: " + new Date() + "\n");
        content.append(" * Table: " + table.name + "\n");
        content.append(" */\n");

        //保留body
        if (body != null && body.length() > 0) {
            content.append(body);
        } else {
            content.append("@Mapper\n");
            content.append("public interface " + className + "DAO extends TableDAO<" + className + "DO> { }");
            content.append("\n\r\n\r");
        }

        try {
            writeFile(file,content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return methods;
    }

    private static void wirteMapper(File file, String className, String packageName, Table table, List<MapperMethod> methods) {

        String doName = packageName + ".dobj." + className + "DO";
        String daoName = packageName + ".dao." + className + "DAO";
        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD SQL 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\" >\n" +
                "<mapper namespace=\"" + daoName + "\">\n\n");
        String resultEntity = className.substring(0,1).toLowerCase() + className.substring(1) + "DOResult";
        content.append("    <resultMap id=\"" + resultEntity + "\" type=\"" + doName + "\">\n");
        for (Column cl : table.columns) {
            content.append("        <result column=\"" + cl.name + "\" property=\"" + toHumpString(cl.name,false) + "\"/>\n");
        }
        content.append("    </resultMap>\n\n");

        //将column修改
        StringBuilder flds = new StringBuilder();
        StringBuilder cols = new StringBuilder();
        boolean isFirst = true;
        for (Column cl : table.columns) {
//            if (cl.name.equals("id")) {
//                continue;
//            }
            if (isFirst) {
                isFirst = false;
            } else {
                flds.append(",");
                cols.append(",");
            }
            cols.append("`" + cl.name + "`");
            if (cl.name.equals("create_at") || cl.name.equals("modified_at")) {
                flds.append("(unix_timestamp() * 1000)");
            } else {
                flds.append("#{" + toHumpString(cl.name,false) + "}");
            }
        }

        StringBuilder upBuilder = new StringBuilder();
        for (Column cl : table.columns) {
            if (cl.name.equals("id") || cl.name.equals("create_at")) {
                continue;
            }

            if (cl.name.equals("modified_at")) {
                if (isFirst) {
                    isFirst = false;
                    upBuilder.append("            ");
                } else {
                    upBuilder.append("            ,");
                }
                upBuilder.append("modified_at = (unix_timestamp() * 1000) \n");
            } else {
                upBuilder.append("        <if test=\""+ toHumpString(cl.name,false) + " != null\">\n");
                if (isFirst) {
                    isFirst = false;
                    upBuilder.append("            ");
                } else {
                    upBuilder.append("            ,");
                }
                upBuilder.append("`"+ cl.name +"` = #{"+ toHumpString(cl.name,false) + "}\n");
                upBuilder.append("        </if>\n");
            }
        }

        //默认的sql文件编写
//        public void insert(DO entity) throws DataAccessException;
        content.append("    <insert id=\"insert\" useGeneratedKeys=\"true\" keyProperty=\"id\" parameterType=\"" + doName + "\">\n");
        content.append("        insert into `"+table.name+"` (" + cols.toString() +") values (" + flds.toString() + ")\n");
        content.append("    </insert>\n\n");

//        public void insertOrUpdate(DO entity) throws DataAccessException;
        content.append("    <insert id=\"insertOrUpdate\" useGeneratedKeys=\"true\" keyProperty=\"id\" parameterType=\"" + doName + "\">\n");
        content.append("        insert into `"+table.name+"` (" + cols.toString() +") values (" + flds.toString() + ") on duplicate key update \n");
        content.append(upBuilder.toString());
        content.append("    </insert>\n\n");

//        public int update(DO entity) throws DataAccessException;
        content.append("    <update id=\"update\" parameterType=\"" + doName + "\">\n");
        content.append("        update `" + table.name + "` set \n");
        content.append(upBuilder.toString());
        content.append("        where id = #{id} \n");
        content.append("    </update>\n\n");

//        public int deleteById(Long pk) throws DataAccessException;
        content.append("    <delete id=\"deleteById\">\n");
        content.append("        delete from `" + table.name + "` where id = #{id} \n");
        content.append("    </delete>\n\n");

//        public DO getById(Long pk) throws DataAccessException;
        content.append("    <select id=\"getById\" resultMap=\"" + resultEntity + "\">\n");
        content.append("        select " + cols.toString() + " \n");
        content.append("        from `" + table.name + "` \n");
        content.append("        where id = #{id} \n");
        content.append("    </select>\n\n");

//        public DO getByIdForUpdate(Long pk) throws DataAccessException;
        content.append("    <select id=\"getByIdForUpdate\" resultMap=\"" + resultEntity + "\">\n");
        content.append("        select " + cols.toString() + " \n");
        content.append("        from `" + table.name + "` \n");
        content.append("        where id = #{id} \n");
        content.append("        for update \n");
        content.append("    </select>\n\n");

        //public List<DO> queryByIds(List<Long> pks);
        content.append("    <select id=\"queryByIds\" resultMap=\"" + resultEntity + "\">\n");
        content.append("        select " + cols.toString() + " \n");
        content.append("        from `" + table.name + "` \n");
        content.append("        where id in \n");
        content.append("        <foreach collection=\"list\" item=\"theId\" index=\"index\" \n");
        content.append("             open=\"(\" close=\")\" separator=\",\"> \n");
        content.append("             #{theId}  \n");
        content.append("        </foreach>  \n");
        content.append("    </select>\n\n");


        //自定的mapper添加
        if (methods != null && methods.size() > 0) {
            content.append("    <!-- Custom sql mapper -->\n");
            for (MapperMethod mapperMethod : methods) {
                String sql = mapperMethod.sql.trim().toLowerCase();

                //处理特殊字符
                sql = sql.replaceAll("<\\!\\[cdata\\[\\s+<>\\s+\\]\\]>"," <> ");
                sql = sql.replaceAll("<\\!\\[cdata\\[\\s+<=\\s+\\]\\]>"," <= ");
                sql = sql.replaceAll("<\\!\\[cdata\\[\\s+>=\\s+\\]\\]>"," >= ");
                sql = sql.replaceAll("<\\!\\[cdata\\[\\s+<\\s+\\]\\]>"," < ");
                sql = sql.replaceAll("<\\!\\[cdata\\[\\s+>\\s+\\]\\]>"," > ");

                sql = sql.replaceAll("<>","_@!#0#!@_");
                sql = sql.replaceAll("<=","_@!#1#!@_");
                sql = sql.replaceAll(">=","_@!#2#!@_");
                sql = sql.replaceAll(" < ","_@!#3#!@_");
                sql = sql.replaceAll(" > ","_@!#4#!@_");


                sql = sql.replaceAll("_@!#0#!@_","<![CDATA[ <> ]]>");
                sql = sql.replaceAll("_@!#1#!@_","<![CDATA[ <= ]]>");
                sql = sql.replaceAll("_@!#2#!@_","<![CDATA[ >= ]]>");
                sql = sql.replaceAll("_@!#3#!@_","<![CDATA[ < ]]>");
                sql = sql.replaceAll("_@!#4#!@_","<![CDATA[ > ]]>");

                if (sql.startsWith("insert")) {
                    content.append("    <insert id=\"" + mapperMethod.id + "\" useGeneratedKeys=\"true\" keyProperty=\"id\" >\n");
                    content.append("        " + sql + "\n");
                    content.append("    </insert>\n\n");
                } else if (sql.startsWith("update")) {
                    content.append("    <update id=\"" + mapperMethod.id + "\" >\n");
                    content.append("        " + sql + "\n");
                    content.append("    </update>\n\n");
                } else if (sql.startsWith("delete")) {
                    content.append("    <delete id=\"" + mapperMethod.id + "\">\n");
                    content.append("        " + sql + "\n");
                    content.append("    </delete>\n\n");
                } else {
                    //已经映射过返回值，直接使用
                    if (mapperMethod.returnType.equals(doName)) {
                        content.append("    <select id=\"" + mapperMethod.id + "\" resultMap=\"" + resultEntity + "\">\n");
                    } else {
                        content.append("    <select id=\"" + mapperMethod.id + "\" resultType=\"" + mapperMethod.returnType + "\">\n");
                    }
                    content.append("        " + sql + "\n");
                    content.append("    </select>\n\n");
                }
            }
        }

        content.append("</mapper>\n\n");

        try {
            writeFile(file,content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String toHumpString(String string, boolean head) {
        StringBuilder name = new StringBuilder();
        boolean toUpper = head;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (toUpper) {
                name.append(("" + c).toUpperCase());
                toUpper = false;
            } else if (c == '_') {
                toUpper = true;
            } else {
                name.append(c);
            }
        }
        return name.toString();
    }

    private static File getCurrentProjectDirFile() {
//        String filePath = System.getProperty("user.dir");//当前运行目录[可能是根目录]
        //当前运行目录
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String path = url.getPath();
        File file = new File(path);
        return file.getParentFile().getParentFile();
    }

    public static String getCurrentProjectDir() {
//        String filePath = System.getProperty("user.dir");//当前运行目录[可能是根目录]
        //当前运行目录
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String path = url.getPath();
        File file = new File(path);
        return file.getParentFile().getParent();
    }

    private static  String getSqlsContent(String sqlsSourcePath) {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(sqlsSourcePath);
        String content = null;
        try {
            content = readFile(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    private static String readFile(InputStream in) throws IOException {
        try {
//            System.out.println("以字节为单位读取文件内容，一次读多个字节：");
            // 一次读多个字节
            byte[] tempbytes = new byte[1024];
            int byteread = 0;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            ReadFromFile.showAvailableBytes(in);
            // 读入多个字节到字节数组中，byteread为一次读入的字节数
            while ((byteread = in.read(tempbytes)) != -1) {
                out.write(tempbytes,0,byteread);
//                System.out.write(tempbytes, 0, byteread);
            }
            return new String(out.toByteArray(), "utf-8");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    private static String readFile(String path) throws IOException {
        InputStream in = null;
        try {
//            System.out.println("以字节为单位读取文件内容，一次读多个字节：");
            // 一次读多个字节
            byte[] tempbytes = new byte[1024];
            int byteread = 0;
            in = new FileInputStream(path);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            ReadFromFile.showAvailableBytes(in);
            // 读入多个字节到字节数组中，byteread为一次读入的字节数
            while ((byteread = in.read(tempbytes)) != -1) {
                out.write(tempbytes,0,byteread);
//                System.out.write(tempbytes, 0, byteread);
            }
            return new String(out.toByteArray(), "utf-8");
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                }
            }
        }
        return null;
    }

    private static boolean writeFile(File filePath, String content) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            out.write(content.getBytes("utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return true;
    }

    private static boolean writeFile(String path, String content) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(content.getBytes("utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return true;
    }
}
