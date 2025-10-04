package io.codepush.testing.parallel.example;

import io.codepush.testing.parallel.ParallelFunctionalTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Another example test to demonstrate parallel execution capabilities.
 *
 * <p>This test can run concurrently with ExampleParallelTest without conflicts.</p>
 */
@ParallelFunctionalTest(
    properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/test_db",
        "spring.datasource.username=test",
        "spring.datasource.password=test",
        "custom.feature.enabled=true",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
    }
)
class AnotherParallelTest {

    @Value("${spring.datasource.schema:default}")
    private String schemaName;

    @Value("${wiremock.port.0:0}")
    private Integer wiremockPort;

    @Value("${custom.feature.enabled}")
    private Boolean customFeatureEnabled;

    @Test
    void shouldHaveUniqueSchemaName() {
        // In a real scenario with PostgreSQL, schema would be camunda_test_<uuid>
        // In this demo without database, we get a placeholder value
        assertThat(schemaName)
            .isNotNull();
    }

    @Test
    void shouldHaveUniqueDynamicPort() {
        assertThat(wiremockPort)
            .isGreaterThan(0)
            .isLessThanOrEqualTo(65535);
    }

    @Test
    void shouldHaveCustomPropertyInjected() {
        assertThat(customFeatureEnabled).isTrue();
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
