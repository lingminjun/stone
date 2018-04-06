package com.lmj.stone.demo.persistence.ds;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import com.lmj.stone.dao.DataSource;
import com.lmj.stone.dao.DataSourceConfiguration;

@DataSourceConfiguration(
        url = @Value("${master.datasource.url}"),
        user = @Value("${master.datasource.username}"),
        password = @Value("${master.datasource.password}"),
        basePackages = "com.lmj.stone.demo.persistence.dao",
        sqlSessionFactoryRef = "defaultSqlSessionFactory")
public class TheDataSource extends DataSource {

    @Override
    @Primary
    @Bean(name = "defaultDataSource")
    public javax.sql.DataSource dataSource() {
        return genDataSource();
    }

    @Override
    @Primary
    @Bean(name = "defaultTransactionManager")
    public DataSourceTransactionManager transactionManager() {
        return genTransactionManager();
    }

    @Override
    @Primary
    @Bean(name = "defaultSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        return genSqlSessionFactory();
    }
}
