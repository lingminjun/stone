package com.lmj.stone.dao.gen;

import com.lmj.stone.core.gen.Generator;
import com.lmj.stone.dao.TableDAO;
import org.apache.ibatis.annotations.Mapper;
import com.lmj.stone.dao.SQL;
import org.apache.ibatis.annotations.Param;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MybatisGenerator extends Generator {
    public final static HashSet<String> MYSQL_TAGS = new HashSet<String>();
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


    public final static HashSet<String> MYSQL_LONG_TYPE = new HashSet<String>();
    static {
        MYSQL_LONG_TYPE.add("BIGINT");
    }

    public final static HashSet<String> MYSQL_BOOL_TYPE = new HashSet<String>();
    static {
        MYSQL_BOOL_TYPE.add("BOOL");
    }

    public final static HashSet<String> MYSQL_DOUBLE_TYPE = new HashSet<String>();
    static {
        MYSQL_DOUBLE_TYPE.add("FLOAT");
        MYSQL_DOUBLE_TYPE.add("DOUBLE");
        MYSQL_DOUBLE_TYPE.add("REAL");

        //另外使用java.math.BigDecimal存储
        MYSQL_DOUBLE_TYPE.add("DECIMAL");
        MYSQL_DOUBLE_TYPE.add("DEC");
        MYSQL_DOUBLE_TYPE.add("NUMERIC");
    }

    public final static HashSet<String> MYSQL_INT_TYPE = new HashSet<String>();
    static {
        MYSQL_INT_TYPE.add("TINYINT");
        MYSQL_INT_TYPE.add("BIT");
        MYSQL_INT_TYPE.add("SMALLINT");
        MYSQL_INT_TYPE.add("INT");
        MYSQL_INT_TYPE.add("INTEGER");
    }

    public final static HashSet<String> MYSQL_STRING_TYPE = new HashSet<String>();
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

    public final static HashSet<String> MYSQL_DATE_TYPE = new HashSet<String>();
    static {
        MYSQL_DATE_TYPE.add("DATETIME");
        MYSQL_DATE_TYPE.add("DATE");
        MYSQL_DATE_TYPE.add("TIMESTAMP");
        MYSQL_DATE_TYPE.add("TIME");
        MYSQL_DATE_TYPE.add("YEAR");
    }

    public final static HashSet<String> MYSQL_INDEX_TYPE = new HashSet<String>();
    static {
        MYSQL_INDEX_TYPE.add("PRIMARY");
        MYSQL_INDEX_TYPE.add("UNIQUE");
        MYSQL_INDEX_TYPE.add("INDEX");
        MYSQL_INDEX_TYPE.add("KEY");
    }


    public static class Column {
        String name;
        String type;
        String cmmt;

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getCmmt() {
            return cmmt;
        }

        public String getDefinedType() {
            if (MYSQL_LONG_TYPE.contains(type)) {
                return "Long";
            } else if (MYSQL_BOOL_TYPE.contains(type)) {
                return "Boolean";
            } else if (MYSQL_DOUBLE_TYPE.contains(type)) {
                return "Double";
            } else if (MYSQL_INT_TYPE.contains(type)) {
                return "Integer";
            } else if (MYSQL_STRING_TYPE.contains(type)) {
                return "String";
            } else {
                return "";
            }
        }

        public String getDataType() {
            if (MYSQL_LONG_TYPE.contains(type)) {
                return "long";
            } else if (MYSQL_BOOL_TYPE.contains(type)) {
                return "boolean";
            } else if (MYSQL_DOUBLE_TYPE.contains(type)) {
                return "double";
            } else if (MYSQL_INT_TYPE.contains(type)) {
                return "int";
            } else if (MYSQL_STRING_TYPE.contains(type)) {
                return "String";
            } else {
                return "";
            }
        }
    }

    public static class ColumnIndex {
        String name;
        boolean isPrimary;
        boolean isUnique;
        List<Column> columns = new ArrayList<Column>();

        public String getName() {
            return name;
        }

        public boolean isPrimary() {
            return isPrimary;
        }

        public boolean isUnique() {
            return isUnique;
        }

        public Column[] getColumns() {
            return columns.toArray(new Column[0]);
        }

//        public String getQueryMethodName() {
//            StringBuilder queryMethodName = new StringBuilder("queryBy");
//            boolean first = true;
//            for (Column col : columns) {
//                if (first) {first = false;}
//                else {queryMethodName.append("And");}
//                queryMethodName.append(toHumpString(col.name,true));
//            }
//            return queryMethodName.toString();
//        }
    }

    public static class Table {
        String name;
        List<Column> columns = new ArrayList<Column>();
        List<ColumnIndex> indexs = new ArrayList<ColumnIndex>();

        public String getName() {
            return name;
        }

        public Column[] getColumns() {
            return columns.toArray(new Column[0]);
        }

        public Column[] getIndexs() {
            return columns.toArray(new Column[0]);
        }

        //除了主键以外的索引
        public boolean hasIndexQuery() {
            for (ColumnIndex column : indexs) {
                if (!column.isPrimary) { return true; }
            }
            return false;
        }

        public Map<String,List<Column>> allIndexQueryMethod() {

            HashMap<String,List<Column>> methods = new HashMap<String, List<Column>>();
            for (ColumnIndex column : indexs) {
                if (column.isPrimary) { continue; }

                for (int i = 0; i < column.columns.size(); i++) {

                    StringBuilder queryMethodName = new StringBuilder("queryBy");
                    boolean first = true;
                    List<Column> cols = new ArrayList<Column>();
                    for (int j = 0; j <= i; j++) {
                        Column col = column.columns.get(j);
                        cols.add(col);
                        if (first) {first = false;}
                        else {queryMethodName.append("And");}
                        queryMethodName.append(toHumpString(col.name,true));
                    }

                    String methodName = queryMethodName.toString();
                    if (!methods.containsKey(methodName)) {
                        methods.put(methodName,cols);
                    }

                }
            }

            return methods;
        }

        public String getDAOClassName(String packageName) {
            return packageName + ".dao." + getSimpleDAOClassName();
        }

        public String getSimpleDAOClassName() {
            return toHumpString(name,true) + "DAO";
        }

        public String getIncDAOClassName(String packageName) {
            return packageName + ".dao.inc." + getSimpleIncDAOClassName();
        }

        public String getSimpleIncDAOClassName() {
            return toHumpString(name,true) + "IndexQueryDAO";
        }

        public String getDObjectClassName(String packageName) {
            return packageName + ".dobj." + getSimpleDObjectClassName();
        }

        public String getSimpleDObjectClassName() {
            return toHumpString(name,true) + "DO";
        }

        public String getPOJOClassName(String packageName) {
            return packageName + ".entities." + getSimplePOJOClassName();
        }

        public String getSimplePOJOClassName() {
            return toHumpString(name,true) + "POJO";
        }

        public String getCRUDServiceBeanName(String packageName) {
            return packageName + ".inc." + getSimpleCRUDServiceBeanName();
        }

        public String getSimpleCRUDServiceBeanName() {
            return toHumpString(name,true) + "CRUDService";
        }

    }


    public final String sqlsSourcePath;
    public final String mapperPath;
    protected final List<Table> tables;

    /**
     * 生成DAO层代码
     * @param packageName 指定包名【必填】
     * @param sqlsSourcePath    sqls文件资源路径:sqls/xxx.sql【必填】
     */
    public MybatisGenerator(String packageName, String sqlsSourcePath) {
        this(packageName,null,sqlsSourcePath,null);
    }

    /**
     * 生成DAO层代码
     * @param packageName 指定包名【必填】
     * @param projectDir  项目目录，可以不填
     * @param sqlsSourcePath    sqls文件资源路径:sqls/xxx.sql【必填】
     */
    public MybatisGenerator(String packageName, String projectDir,  String sqlsSourcePath) {
        this(packageName,projectDir,sqlsSourcePath,null);
    }

    /**
     * 生成DAO层代码
     * @param packageName 指定包名【必填】
     * @param projectDir  项目目录，可以不填
     * @param sqlsSourcePath    sqls文件资源路径:sqls/xxx.sql【必填】
     * @param mapperPath  Mybatis Configuration配置文件路径:资源路径
     */
    public MybatisGenerator(String packageName, String projectDir,  String sqlsSourcePath, String mapperPath) {
        super(packageName,projectDir);

        if (mapperPath == null || mapperPath.length() == 0) {
            mapperPath = "mybatis-sqlmap-config.xml";
        }

        this.sqlsSourcePath = sqlsSourcePath;
        this.mapperPath = mapperPath;
        this.tables = parseSqlTables(sqlsSourcePath);//解析sqls中的tables
    }

    @Override
    public boolean gen() {

        String dobjDir = null;
        String daoDir = null;
        String mapDir = null;
        String confPath = null;

        //因为考虑有些工程，并不是main/java目录，可能直接就上是java目录[暂时不去兼容]
        mapDir = this.resourcesPath + File.separator + "sqlmap";
        new File(mapDir).mkdirs();

        //mybatis配置路径
        if (mapperPath != null && mapperPath.length() > 0) {
            confPath = this.resourcesPath + File.separator + mapperPath;
        } else {
            confPath = this.resourcesPath + File.separator + "mybatis-sqlmap-config.xml";
        }


        //包名
        dobjDir = this.packagePath + File.separator + "dobj";
        new File(dobjDir).mkdirs();
        daoDir = this.packagePath + File.separator + "dao";
        new File(daoDir).mkdirs();


        for (Table table : tables) {
            genTheTable(table,packageName,dobjDir,daoDir,mapDir,confPath);
        }

        return true;
    }

    /**
     * 获取数据表结构 [拷贝]
     * @return
     */
    public List<Table> getTables() {
        return new ArrayList<Table>(tables);
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
        MybatisGenerator generator = new MybatisGenerator(packageName,projectDir,sqlsSourcePath,mapperPath);
        generator.gen();
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

                    //区分是属性定义还是索引定义
                    if (!isSpacingChar(c)) {
                        int len = readTaleIndex(index, sqlsContent, table);
                        if (len > 0) {
                            i = index + len;
                            continue;
                        }
                    }

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


    private static int readTaleIndex(int index, String sqlsContent, Table table) {
        if (sqlsContent.length() < index + 7) {
            return 0;
        }

        int size = sqlsContent.length();
        String target = sqlsContent.substring(index,index + 7).toUpperCase();//最长也就PRIMARY
        int len = 0;
        for (String key : MYSQL_INDEX_TYPE) {
            if (!target.startsWith(key)) {
                continue;
            }
            len = key.length();

            //说明是index
            ColumnIndex colIdx = new ColumnIndex();
            colIdx.isPrimary = key.equals("PRIMARY");
            colIdx.isUnique = colIdx.isPrimary || key.equals("UNIQUE");

            //检查属性
            int beg = sqlsContent.indexOf('(',index+len);
            int end = sqlsContent.indexOf(')',index+len);
            if (beg < index+len || beg >= size || end < index+len || end >= size) {
                return 0;
            }
            String colString = sqlsContent.substring(beg + 1,end);
            String[] cols = colString.split(",");
            for (String col : cols) {
                col = col.trim();
                if (col.startsWith("`") && col.endsWith("`")) {
                    col = col.substring(1,col.length() - 1);
                }

                for (Column column : table.columns) {
                    if (column.name.equals(col)) {
                        colIdx.columns.add(column);
                        break;
                    }
                }
            }

            if (colIdx.columns.size() > 0) {
                len = end - index;
                table.indexs.add(colIdx);
            }

            break;
        }
        return len;
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


        writeDObject(dobjFile,name,packName,table);
        List<MapperMethod> methods = writeDAObject(daoFile,name,packName,table);
        writeMapper(dmapFile,name,packName,table, methods);
    }

    private static void writeDObject(File file, String className, String packageName, Table table) {
        StringBuilder dobjContent = new StringBuilder();
        dobjContent.append("package " + packageName + ".dobj;\n\r\n\r");
        dobjContent.append("import java.io.Serializable;\n\r\n\r");
        dobjContent.append("/**\n");
        dobjContent.append(" * Owner: Minjun Ling\n");
        dobjContent.append(" * Creator: Robot\n");
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

    private static List<MapperMethod> writeDAObject(File file, String className, String packageName, Table table) {

        //注意 索引查询需要重新生成类
        boolean hasIndexQuery = table.hasIndexQuery();
        String daoDir = file.getParent();//父目录
        File idxDaoFile = new File(daoDir + File.separator + "inc" + File.separator + className + "IndexQueryDAO.java");
        if (idxDaoFile.exists()) {//先删除
            idxDaoFile.delete();
        }

        if (hasIndexQuery) {
            writeIndexQueryDAObject(idxDaoFile,className,packageName,table);
        }

        List<MapperMethod> methods = null;

        //此类全称
        String daobj = table.getDAOClassName(packageName);      //
        String idxDaobj = table.getIncDAOClassName(packageName);//

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
        String dobj = table.getDObjectClassName(packageName);   //
        imports.put(dobj,"import " + dobj + ";");
        imports.put(Mapper.class.getName(),"import " + Mapper.class.getName() + ";");
        if (hasIndexQuery) {
            imports.put(idxDaobj,"import " + idxDaobj + ";");
        }

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
        content.append(" * Owner: Minjun Ling\n");
        content.append(" * Creator: Robot\n");
        content.append(" * Version: 1.0.0\n");
        content.append(" * Since: " + new Date() + "\n");
        content.append(" * Table: " + table.name + "\n");
        content.append(" */\n");

        //类定义

//        content.append("@Mapper\n"); // 扫描方式支持，此处不需要，因为xml中会定义
        content.append("public interface " + className + "DAO ");
        if (hasIndexQuery) {
            content.append("extends " + className + "IndexQueryDAO ");
        } else {
            content.append("extends TableDAO<" + className + "DO> ");
        }

        //保留body
        if (body != null && body.length() > 0) {
            int idx = body.indexOf("{");
            body = body.substring(idx);
            content.append(body);
        } else {
            content.append("{ /* Add custom methods */ }");
            content.append("\n\r\n\r");
        }

        try {
            writeFile(file,content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return methods;
    }

    private static void writeIndexQueryDAObject(File file, String className, String packageName, Table table) {

        File dic = file.getParentFile();
        if (!dic.exists()) {
            dic.mkdirs();
        }

        //如果文件本身存在，则保留文件体
        Map<String,String> imports = new HashMap<String, String>();

        imports.put(TableDAO.class.getName(),"import " + TableDAO.class.getName() + ";");
        String dobj = table.getDObjectClassName(packageName);
        imports.put(dobj,"import " + dobj + ";");
        imports.put(Mapper.class.getName(),"import " + Mapper.class.getName() + ";");
        imports.put(Param.class.getName(),"import " + Param.class.getName() + ";");
        imports.put(List.class.getName(),"import " + List.class.getName() + ";");

        //开始写入
        StringBuilder content = new StringBuilder();
        content.append("package " + packageName + ".dao.inc;\n\r\n\r");

        //imports
        Iterator<Map.Entry<String, String>> entries = imports.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            content.append(entry.getValue() + "\n");
        }
        content.append("\n\n");
        content.append("/**\n");
        content.append(" * Owner: Minjun Ling\n");
        content.append(" * Creator: Robot\n");
        content.append(" * Version: 1.0.0\n");
        content.append(" * Since: " + new Date() + "\n");
        content.append(" * Table: " + table.name + "\n");
        content.append(" */\n");

        //类定义
        content.append("public interface " + className + "IndexQueryDAO extends TableDAO<" + className + "DO> { \n");

        Map<String,List<Column>> queryMethod = table.allIndexQueryMethod();
        List<String> methodNames = new ArrayList<String>(queryMethod.keySet());
        Collections.sort(methodNames);
        for (String methodName : methodNames) {

            List<Column> cols = queryMethod.get(methodName);

            content.append("    /**\n");
            content.append("     * 根据以下索引字段查询实体对象集\n");

            StringBuilder params = new StringBuilder();
            boolean first = true;
            for (Column col : cols) {
                String type = col.getDataType();
                if (type == null || type.length() == 0) {
                    continue;
                }
                String colName = toHumpString(col.name,false);
                content.append("     * @param " + colName + "  " + (col.cmmt == null ? "" : col.cmmt) + "\n");
                if (first) { first = false; }
                else {
                    params.append(", ");
                }
                params.append("@Param(\"" + colName + "\") " + type + " " + colName);
            }

            // 排序与limit
            content.append("     * @param sortField 排序字段，传入null时表示不写入sql\n");
            content.append("     * @param isDesc 排序为降序\n");
            content.append("     * @param offset 其实位置\n");
            content.append("     * @param limit  返回条数\n");
            params.append(",@Param(\"sortField\") String sortField");
            params.append(",@Param(\"isDesc\") boolean isDesc");
            params.append(",@Param(\"offset\") int offset");
            params.append(",@Param(\"limit\") int limit");

            content.append("     * @return\n");
            content.append("     */\n");
            content.append("    public List<" + className + "DO> " + methodName + "(");
            content.append(params);
            content.append(");\n\r\n\r");
        }

        //结束
        content.append("}\n\r\n\r");


        try {
            writeFile(file,content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeMapper(File file, String className, String packageName, Table table, List<MapperMethod> methods) {

        String doName = table.getDObjectClassName(packageName); //
        String daoName = table.getDAOClassName(packageName);    //
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

        //针对索引建查询语句
        Map<String,List<Column>> queryMethod = table.allIndexQueryMethod();
        List<String> methodNames = new ArrayList<String>(queryMethod.keySet());
        Collections.sort(methodNames);
        for (String methodName : methodNames) {

            List<Column> tcols = queryMethod.get(methodName);

            StringBuilder queryWhere = new StringBuilder();
            boolean first = true;
            for (Column cl : tcols) {
                if (first) {
                    first = false;
                } else {
                    queryWhere.append(" and ");
                }
                queryWhere.append("`"+ cl.name +"` = #{"+ toHumpString(cl.name,false) + "}");
            }
            queryWhere.append("\n");


            content.append("    <select id=\"" + methodName + "\" resultMap=\"" + resultEntity + "\">\n");
            content.append("        select " + cols.toString() + " \n");
            content.append("        from `" + table.name + "` \n");
            content.append("        where ");
            content.append(queryWhere.toString());
            content.append("        <if test=\"sortField != null and sortField != ''\">\n");
            content.append("            order by `${sortField}` ");//注意参数为字符替换，而不是"?"掩码
            //MySQL中默认排序是acs(可省略)：从小到大 ; desc ：从大到小，也叫倒序排列。
            content.append("<if test=\"isDesc\"> desc </if> \n");
            content.append("        </if>\n");
            content.append("        limit #{offset},#{limit}\n");//发现limit可以掩码"?"
            content.append("    </select>\n\n");
        }


        //自定的mapper添加
        if (methods != null && methods.size() > 0) {
            content.append("    <!-- Custom sql mapper -->\n");
            for (MapperMethod mapperMethod : methods) {
                String sql = mapperMethod.sql.trim();//.toLowerCase();

                //处理特殊字符
                sql = sql.replaceAll("<\\!\\[((?i)cdata)\\[\\s+<>\\s+\\]\\]>"," <> ");
                sql = sql.replaceAll("<\\!\\[((?i)cdata)\\[\\s+<=\\s+\\]\\]>"," <= ");
                sql = sql.replaceAll("<\\!\\[((?i)cdata)\\[\\s+>=\\s+\\]\\]>"," >= ");
                sql = sql.replaceAll("<\\!\\[((?i)cdata)\\[\\s+<\\s+\\]\\]>"," < ");
                sql = sql.replaceAll("<\\!\\[((?i)cdata)\\[\\s+>\\s+\\]\\]>"," > ");

                sql = sql.replaceAll("<>","_@!#0#!@_");
                sql = sql.replaceAll("<=","_@!#1#!@_");
                sql = sql.replaceAll(">=","_@!#2#!@_");
                sql = sql.replaceAll(" < ","_@!#3#!@_");//防止把sql已有脚本提出
                sql = sql.replaceAll(" > ","_@!#4#!@_");//防止把sql已有脚本提出


                sql = sql.replaceAll("_@!#0#!@_"," <![CDATA[ <> ]]> ");
                sql = sql.replaceAll("_@!#1#!@_"," <![CDATA[ <= ]]> ");
                sql = sql.replaceAll("_@!#2#!@_"," <![CDATA[ >= ]]> ");
                sql = sql.replaceAll("_@!#3#!@_"," <![CDATA[ < ]]> ");
                sql = sql.replaceAll("_@!#4#!@_"," <![CDATA[ > ]]> ");

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


}
