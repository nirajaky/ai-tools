package com.nexacorp.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class DatabaseConnectivityTest {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void testDatabaseConnectivity() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        System.out.println("DB connectivity check result = " + result);
    }
}