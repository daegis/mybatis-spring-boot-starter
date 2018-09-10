package com.hnair.spring.boot.mybatis;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Using IntelliJ IDEA.
 *
 * @author XIANYINGDA at 9/10/2018 2:33 PM
 */
@Configuration
@EnableConfigurationProperties(MybatisProperties.class)
@ConditionalOnProperty(prefix = "spring.mybatis", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class MybatisAutoConfiguration {

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




}
