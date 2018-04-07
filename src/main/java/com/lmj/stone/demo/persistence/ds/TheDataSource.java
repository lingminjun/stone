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
        sqlSessionFactoryRef = TheDataSource.SQL_SESSION_FACTORY_BEAN)
public class TheDataSource extends DataSource {

    final static String DATA_SOURCE_BEAN = "defaultDataSource";
    final static String TRANSACTION_MANAGER_BEAN = "defaultTransactionManager";
    final static String SQL_SESSION_FACTORY_BEAN = "defaultSqlSessionFactory";

    @Override
    @Primary
    @Bean(name = TheDataSource.DATA_SOURCE_BEAN)
    public javax.sql.DataSource dataSource() {
        return genDataSource();
    }

    @Override
    @Primary
    @Bean(name = TheDataSource.TRANSACTION_MANAGER_BEAN)
    public DataSourceTransactionManager transactionManager() {
        return genTransactionManager();
    }

    @Override
    @Primary
    @Bean(name = TheDataSource.SQL_SESSION_FACTORY_BEAN)
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        return genSqlSessionFactory();
    }
}
