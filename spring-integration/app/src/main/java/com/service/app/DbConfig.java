package com.service.app;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.inject.Inject;
import javax.sql.DataSource;

@Configuration("dbConfig")
public class DbConfig {
    @Bean("dataSource")
    @Inject
    public DataSource setDataSource() {
        return 
            DataSourceBuilder
                .create()
                .driverClassName("org.postgresql.Driver")
                .username("postgres")
                .password("orders")
                .url("jdbc:postgresql://spring-integration-database-1:5432/orders")
                .build();
    }
    
    @Bean("jdbcTemplate")
    @DependsOn("dataSource")
    @Inject
    public JdbcTemplate setJdbcTemplate(final DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
