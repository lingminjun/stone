package com.lmj.stone.service.gen;

import com.lmj.stone.dao.gen.MybatisGenerator;

/**
 * Created with IntelliJ IDEA.
 * Description: 生成部分固定的service形式
 * User: lingminjun
 * Date: 2018-06-12
 * Time: 下午11:22
 */
public class ServiceGenerator extends MybatisGenerator {
    public ServiceGenerator(String packageName, String sqlsSourcePath) {
        super(packageName, sqlsSourcePath);
    }

    public ServiceGenerator(String packageName, String projectDir, String sqlsSourcePath) {
        super(packageName, projectDir, sqlsSourcePath);
    }

    public ServiceGenerator(String packageName, String projectDir, String sqlsSourcePath, String mapperPath) {
        super(packageName, projectDir, sqlsSourcePath, mapperPath);
    }
}
