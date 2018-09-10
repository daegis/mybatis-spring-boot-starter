package com.hnair.spring.boot.mybatis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MybatisSpringBootStarterApplicationTests {

    @Test
    public void contextLoads() {
    }
/**
 * initialSize                                   10
 * minIdle                                   10
 * maxActive                                   200
 * maxWait                                   60000
 * timeBetweenEvictionRunsMillis                                   60000
 * minEvictableIdleTimeMillis                                   300000
 * validationQuery                                   SELECT now()
 * testWhileIdle                                   true
 * testOnBorrow                                   false
 * testOnReturn                                   false
 */
}
