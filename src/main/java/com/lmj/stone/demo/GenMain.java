package com.lmj.stone.demo;


import com.lmj.stone.gen.MybatisGenerator;

public class GenMain {
    public static void main(String[] args) {
        //生成数据相关对象
//        MybatisGenerator.gen("ssn.lmj.soph.db","sqls/city.sql");
        MybatisGenerator.gen("com.lmj.stone.demo.persistence","sqls/city.sql");
    }
}
