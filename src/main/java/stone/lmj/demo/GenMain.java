package stone.lmj.demo;


import stone.lmj.com.gen.MybatisGenerator;

public class GenMain {
    public static void main(String[] args) {
        //生成数据相关对象
//        MybatisGenerator.gen("ssn.lmj.soph.db","sqls/city.sql");
        MybatisGenerator.gen("stone.lmj.demo.persistence","sqls/city.sql");
    }
}
