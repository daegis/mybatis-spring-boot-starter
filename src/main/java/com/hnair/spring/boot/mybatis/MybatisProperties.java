package com.hnair.spring.boot.mybatis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Using IntelliJ IDEA.
 *
 * @author XIANYINGDA at 9/10/2018 2:26 PM
 */
@ConfigurationProperties(prefix = "spring.mybatis")
@Data
public class MybatisProperties {
    private String url;
}
