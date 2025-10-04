package io.codepush.testing.parallel.example;

import io.codepush.testing.parallel.ParallelFunctionalTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example integration test demonstrating the use of @ParallelFunctionalTest annotation.
 *
 * <p>This test demonstrates:</p>
 * <ul>
 *   <li>Isolated schema name injection</li>
 *   <li>Dynamic port allocation for WireMock</li>
 *   <li>Ability to run parallel with other test classes</li>
 * </ul>
 */
@ParallelFunctionalTest(
    properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/test_db",
        "spring.datasource.username=test",
        "spring.datasource.password=test",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
    }
)
class ExampleParallelTest {

    @Value("${spring.datasource.schema:default}")
    private String schemaName;

    @Value("${wiremock.port.0:0}")
    private Integer wiremockPort0;

    @Value("${wiremock.port.1:0}")
    private Integer wiremockPort1;

    @Test
    void shouldHaveIsolatedSchemaName() {
        // In a real scenario with PostgreSQL, schema would be camunda_test_<uuid>
        // In this demo without database, we get a placeholder value
        assertThat(schemaName)
            .isNotNull()
            .isNotEqualTo("default");
    }

    @Test
    void shouldHaveDynamicallyAllocatedPorts() {
        assertThat(wiremockPort0)
            .isGreaterThan(0)
            .isLessThanOrEqualTo(65535);

        assertThat(wiremockPort1)
            .isGreaterThan(0)
            .isLessThanOrEqualTo(65535)
            .isNotEqualTo(wiremockPort0);
    }

    @Test
    void shouldHavePortsInValidDynamicRange() {
        // OS dynamic port range varies: Linux/Mac 32768-60999, Windows 49152-65535
        assertThat(wiremockPort0)
            .isGreaterThanOrEqualTo(1024)
            .isLessThanOrEqualTo(65535);

        assertThat(wiremockPort1)
            .isGreaterThanOrEqualTo(1024)
            .isLessThanOrEqualTo(65535);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        DataSource dataSource() {
            // Mock DataSource for testing - returns mock connection
            return org.mockito.Mockito.mock(DataSource.class);
        }
    }
}
