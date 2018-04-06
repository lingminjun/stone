package com.lmj.stone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


/**
 * Spring Boot 应用启动类
 *
 * Created by bysocket on 16/4/26.
 */
// Spring Boot 应用的标识
@SpringBootApplication
// mapper 接口类扫描包配置
//@MapperScan("ssn.lmj.demo.dao")
//@MapperScan("ssn.lmj.soph.db")
public class Application {

    public static void main(String[] args) {
        // 程序启动入口
        // 启动嵌入式的 Tomcat 并初始化 Spring 环境及其各 Spring 组件
        ApplicationContext ac = SpringApplication.run(Application.class,args);
        Object b = ac.getBean("defaultDataSource");
        System.out.println(".DataSource >>> " + b.hashCode());

    }
}
