package com.warrantyportal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Basic application context loading test.
 * Phase 3 — Backend Initial Setup
 */
@SpringBootTest
class WarrantyPortalApplicationTests {

    @MockBean
    private DataSource dataSource;

    @BeforeEach
    void setupMockDataSource() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        when(dataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(mockMetaData.getDatabaseMajorVersion()).thenReturn(17);
    }

    @Test
    void contextLoads() {
        // Confirms that the Spring application context starts and configuration is valid
        assertNotNull(dataSource, "DataSource bean should be configured or mocked");
    }
}
