package com.lmj.stone.core.gen;

import java.io.*;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * Description: 提供生成代码所属环境，工具方法
 * User: lingminjun
 * Date: 2018-06-12
 * Time: 下午11:30
 */
public abstract class Generator {

    public final String packageName;
    public final String projectDir;

    public Generator(String packageName) {
        this(packageName,null);
    }

    public Generator(String packageName, String projectDir) {
        //工程目录
        File projFile = getCurrentProjectDirFile();
        if (projectDir == null || projectDir.length() == 0) {
            projectDir = projFile.getAbsolutePath();
        }

        if (projectDir.endsWith(File.separator)) {
            projectDir = projectDir.substring(0,projectDir.length() - 1);
        }
        this.packageName = packageName;
        this.projectDir = projectDir;
    }

    public abstract boolean gen();

    //转驼峰命名
    public static String toHumpString(String string, boolean head) {
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

    public static File getCurrentProjectDirFile() {
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

    public static String getSqlsContent(String sqlsSourcePath) {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(sqlsSourcePath);
        String content = null;
        try {
            content = readFile(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    protected static String readFile(InputStream in) throws IOException {
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

    protected static String readFile(String path) throws IOException {
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

    protected static boolean writeFile(File filePath, String content) throws IOException {
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

    protected static boolean writeFile(String path, String content) throws IOException {
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
