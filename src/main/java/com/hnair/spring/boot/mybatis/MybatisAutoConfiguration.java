package com.hnair.spring.boot.mybatis;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import com.hnair.spring.boot.mybatis.component.service.impl.CommonServiceImpl;
import com.hnair.spring.boot.mybatis.component.spi.impl.CommonDaoImpl;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Using IntelliJ IDEA.
 *
 * @author XIANYINGDA at 9/10/2018 2:33 PM
 */
@Configuration
@EnableConfigurationProperties(MybatisProperties.class)
@Slf4j
public class MybatisAutoConfiguration {

    private final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
    private final Integer INITIAL_SIZE = 10;
    private final Integer MIN_IDLE = 10;
    private final Integer MAX_ACTIVE = 200;
    private final Long MAX_WAIT = 60000L;
    private final Long TIMEBETWEENEVICTIONRUNSMILLIS = 60000L;
    private final Long MINEVICTABLEIDLETIMEMILLIS = 300000L;
    private final String VALIDATION_QUERY = "SELECT now()";
    private final Boolean TESTWHILEIDLE = true;
    private final Boolean TESTONBORROW = false;
    private final Boolean TESTONRETURN = false;
    private final String MAPPER_LOCATION = "mybatis/mapper/*.xml";
    private final String MAPPER_CONFIG = "mybatis/sql-map-config.xml";

    @Bean
    @Autowired
    public Map<String, Object> mybatisMap(MybatisProperties properties) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("message", "ok");
        map.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        map.put("params", properties);
        log.info("初始化参数：{}", JSON.toJSONString(map));
        return map;
    }

    private DruidDataSource parentDatasource(MybatisProperties properties) {
        final DruidDataSource druid = new DruidDataSource();
        druid.setDriverClassName(properties.getDriverClassName() == null ? DRIVER_CLASS_NAME : properties.getDriverClassName());
        druid.setInitialSize(properties.getInitialSize() == null ? INITIAL_SIZE : properties.getInitialSize());
        druid.setMinIdle(properties.getMinIdle() == null ? MIN_IDLE : properties.getMinIdle());
        druid.setMaxActive(properties.getMaxActive() == null ? MAX_ACTIVE : properties.getMaxActive());
        druid.setMaxWait(properties.getMaxWait() == null ? MAX_WAIT : properties.getMaxWait());
        druid.setTimeBetweenEvictionRunsMillis(properties.getTimeBetweenEvictionRunsMillis() == null ? TIMEBETWEENEVICTIONRUNSMILLIS : properties.getTimeBetweenEvictionRunsMillis());
        druid.setMinEvictableIdleTimeMillis(properties.getMinEvictableIdleTimeMillis() == null ? MINEVICTABLEIDLETIMEMILLIS : properties.getMinEvictableIdleTimeMillis());
        druid.setValidationQuery(properties.getValidationQuery() == null ? VALIDATION_QUERY : properties.getValidationQuery());
        druid.setTestWhileIdle(properties.getTestWhileIdle() == null ? TESTWHILEIDLE : properties.getTestWhileIdle());
        druid.setTestOnBorrow(properties.getTestOnBorrow() == null ? TESTONBORROW : properties.getTestOnBorrow());
        druid.setTestOnReturn(properties.getTestOnReturn() == null ? TESTONRETURN : properties.getTestOnReturn());
        return druid;
    }

    private DataSource getDataSource(String dbUrl_slave, String username_slave, String password_slave, MybatisProperties properties) {
        final DruidDataSource dataSource = parentDatasource(properties);
        dataSource.setUrl(dbUrl_slave);
        dataSource.setUsername(username_slave);
        dataSource.setPassword(password_slave);
        return dataSource;
    }

    @Bean
    public DataSource masterDbDatasource(MybatisProperties properties) {
        final String masterUrl = properties.getMasterUrl();
        final String masterUsername = properties.getMasterUsername();
        final String masterPassword = properties.getMasterPassword();
        if (masterUrl == null || masterUrl.trim().equals("")) {
            throw new RuntimeException("Master database url can not be null");
        }
        if (masterUsername == null || masterUsername.trim().equals("")) {
            throw new RuntimeException("Master database username can not be null");
        }
        if (masterPassword == null || masterPassword.trim().equals("")) {
            throw new RuntimeException("Master database password can not be null");
        }
        return getDataSource(masterUrl, masterUsername, masterPassword, properties);
    }

    @Bean
    public DataSource slaveDbDatasource(MybatisProperties properties) {
        final String slaveUrl = properties.getSlaveUrl();
        final String slaveUsername = properties.getSlaveUsername();
        final String slavePassword = properties.getSlavePassword();
        if (slaveUrl == null || slaveUrl.trim().equals("")) {
            throw new RuntimeException("Slave database url can not be null");
        }
        if (slaveUsername == null || slaveUsername.trim().equals("")) {
            throw new RuntimeException("Slave database username can not be null");
        }
        if (slavePassword == null || slavePassword.trim().equals("")) {
            throw new RuntimeException("Slave database password can not be null");
        }
        return getDataSource(slaveUrl, slaveUsername, slavePassword, properties);
    }

    /**
     * 配置从库
     *
     * @return 从库SqlSession
     */
    @Bean
    public SqlSessionFactoryBean slaveSqlSessionFactoryBean(MybatisProperties properties) throws IOException {
        final SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(slaveDbDatasource(properties));
        final ClassPathResource configLocationResource = new ClassPathResource(properties.getMapperConfig() == null ? MAPPER_CONFIG : properties.getMapperConfig());
        factoryBean.setConfigLocation(configLocationResource);
        PathMatchingResourcePatternResolver multiResolver_self = new PathMatchingResourcePatternResolver();
        Resource[] mapperLocationResource = multiResolver_self.getResources(properties.getMapperBasePackage() == null ? MAPPER_LOCATION : properties.getMapperBasePackage());
        factoryBean.setMapperLocations(mapperLocationResource);
        return factoryBean;
    }

    /**
     * 主库模板
     *
     * @return
     */
    @Bean
    public JdbcTemplate contentJdbcTemplate(MybatisProperties properties) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(masterDbDatasource(properties));
        return jdbcTemplate;
    }

    /**
     * 从库模板
     *
     * @return
     */
    @Bean
    public JdbcTemplate contentJdbcQueryTemplate(MybatisProperties properties) {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(slaveDbDatasource(properties));
        return jdbcTemplate;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(MybatisProperties properties) {
        final DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(masterDbDatasource(properties));
        return transactionManager;
    }

    /**
     * 配置主库
     *
     * @return 主库qlSession
     */
    @Bean
    public SqlSessionFactoryBean masterSqlSessionFactoryBean(MybatisProperties properties) throws IOException {
        final SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(masterDbDatasource(properties));
        final ClassPathResource configLocationResource = new ClassPathResource(properties.getMapperConfig() == null ? MAPPER_CONFIG : properties.getMapperConfig());
        factoryBean.setConfigLocation(configLocationResource);
        PathMatchingResourcePatternResolver multiResolver_self = new PathMatchingResourcePatternResolver();
        Resource[] mapperLocationResource = multiResolver_self.getResources(properties.getMapperBasePackage() == null ? MAPPER_LOCATION : properties.getMapperBasePackage());
        factoryBean.setMapperLocations(mapperLocationResource);
        return factoryBean;
    }

    @Bean
    public SqlSessionTemplate masterSqlSession(MybatisProperties properties) throws Exception {
        return new SqlSessionTemplate(Objects.requireNonNull(masterSqlSessionFactoryBean(properties).getObject()));
    }

    @Bean
    public SqlSessionTemplate slaveSqlSession(MybatisProperties properties) throws Exception {
        return new SqlSessionTemplate(Objects.requireNonNull(slaveSqlSessionFactoryBean(properties).getObject()));
    }

    @Bean
    public CommonDaoImpl modelCommonDao(MybatisProperties properties) throws Exception {
        final CommonDaoImpl commonDao = new CommonDaoImpl();
        commonDao.setSqlSession(masterSqlSession(properties));
        commonDao.setSqlSessionQurey(slaveSqlSession(properties));
        return commonDao;
    }

    @Bean
    public CommonServiceImpl modelCommonService(MybatisProperties properties) throws Exception {
        final CommonServiceImpl service = new CommonServiceImpl();
        service.setCommonDao(modelCommonDao(properties));
        return service;
    }

}
