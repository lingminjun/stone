package com.lmj.stone.dao;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.StringValueResolver;


/**
 * //你可以定义自己的DataSource
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

        // 若你定义的是只读数据源，则不需要加载TransactionManager
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
*/
public abstract class DataSource implements EmbeddedValueResolverAware {

//    private ApplicationContext context;
    private StringValueResolver resolver;

    @Override
    public void setEmbeddedValueResolver(StringValueResolver stringValueResolver) {
        resolver = stringValueResolver;

        //需要重新获取环境数据
        this.url = resolver.resolveStringValue(this.url);
        this.user = resolver.resolveStringValue(this.user);
        this.password = resolver.resolveStringValue(this.password);
    }

    public DataSource() {

        //取DataSource的子类，因为Spring加载的并非子类，而是aop了一个代理类
        Class<?> clz = this.getClass();
        while (clz != null && clz.getSuperclass() != DataSource.class) {
            clz = clz.getSuperclass();
        }
        //com.lmj.stone.demo.persistence.ds.TheDataSource$$EnhancerBySpringCGLIB$$affcf6af
        DataSourceConfiguration cfg = clz.getAnnotation(DataSourceConfiguration.class);
        this.url = cfg.url().value();
        this.user = cfg.user().value();
        this.password = cfg.password().value();
        this.mapperPath = cfg.mapperPath();
        this.driverClass = cfg.driverClass();
    }

    private String url;
    private String user;
    private String password;
    private String driverClass;
    private String mapperPath;


    @Bean(name = "defaultDataSource") @Primary
    public abstract javax.sql.DataSource dataSource();/* {
        return genDataSource();
    }*/

    // 若你定义的是只读数据源，则不需要加载TransactionManager
    @Bean(name = "defaultTransactionManager") @Primary
    public abstract DataSourceTransactionManager transactionManager();/* {
        return genTransactionManager();
    }*/

    @Bean(name = "defaultSqlSessionFactory") @Primary
    public abstract SqlSessionFactory sqlSessionFactory() throws Exception;/* {
        return genSqlSessionFactory();
    }*/


    protected javax.sql.DataSource genDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
//        System.out.println(this.getClass().getSimpleName() + ".DataSource >>> " + dataSource.hashCode());
        return dataSource;
    }


    protected DataSourceTransactionManager genTransactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    protected SqlSessionFactory genSqlSessionFactory()
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(mapperPath));
//        sessionFactory.setTypeAliasesPackage(MasterDataSourceConfig.DOMAIN_PACKAGE);
        return sessionFactory.getObject();
    }
}