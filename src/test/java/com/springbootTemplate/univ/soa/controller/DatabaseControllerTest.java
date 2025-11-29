package com.springbootTemplate.univ.soa.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class DatabaseControllerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    private DatabaseController databaseController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        databaseController = new DatabaseController();
        setField(databaseController, "dataSource", dataSource);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("testDatabaseConnections devrait retourner succès pour MySQL")
    void testDatabaseConnections_Success() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("testdb");
        doNothing().when(connection).close();

        Map<String, Object> result = databaseController.testDatabaseConnections();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsKey("mysql"));
        assertTrue(result.containsKey("database"));
        assertTrue(result.containsKey("status"));
        assertTrue(result.get("mysql").toString().contains("successful"));
        assertEquals("testdb", result.get("database"));
        assertEquals("ready", result.get("status"));
    }

    @Test
    @DisplayName("testDatabaseConnections devrait gérer l'échec MySQL")
    void testDatabaseConnections_MySQLFailure() throws SQLException {
        // Arrange
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

        // Act
        Map<String, Object> result = databaseController.testDatabaseConnections();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("mysql"));
        assertTrue(result.containsKey("status"));
        assertTrue(result.get("mysql").toString().contains("failed"));
        assertEquals("error", result.get("status"));
    }

    @Test
    @DisplayName("testDatabaseConnections devrait fermer la connexion MySQL")
    void testDatabaseConnections_ClosesConnection() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("testdb");
        doNothing().when(connection).close();

        databaseController.testDatabaseConnections();


        verify(connection, times(1)).close();
    }

    @Test
    @DisplayName("La Map de résultat devrait toujours contenir les clés requises")
    void testDatabaseConnections_AlwaysReturnsRequiredKeys() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("testdb");
        doNothing().when(connection).close();

        Map<String, Object> result = databaseController.testDatabaseConnections();

        assertNotNull(result);
        assertTrue(result.containsKey("mysql"));
        assertTrue(result.containsKey("database"));
        assertTrue(result.containsKey("status"));
        assertEquals(3, result.size());
    }
}
