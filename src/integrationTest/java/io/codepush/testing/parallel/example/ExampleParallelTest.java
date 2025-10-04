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
        "spring.datasource.password=test"
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
        assertThat(schemaName)
            .isNotNull()
            .startsWith("camunda_test_")
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
    void shouldHavePortsInEphemeralRange() {
        assertThat(wiremockPort0)
            .isGreaterThanOrEqualTo(49152)
            .isLessThanOrEqualTo(65535);

        assertThat(wiremockPort1)
            .isGreaterThanOrEqualTo(49152)
            .isLessThanOrEqualTo(65535);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        DataSource dataSource() {
            // Mock DataSource for testing
            return new org.springframework.jdbc.datasource.SimpleDriverDataSource();
        }
    }
}
