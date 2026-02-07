package io.codepush.testing.parallel.listener;

import io.codepush.testing.parallel.ParallelFunctionalTest;
import io.codepush.testing.parallel.port.DynamicPortAllocator;
import io.codepush.testing.parallel.port.SimpleDynamicPortAllocator;
import io.codepush.testing.parallel.schema.PostgresSchemaIsolationStrategy;
import io.codepush.testing.parallel.schema.SchemaIsolationStrategy;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TestExecutionListener that manages schema isolation and dynamic port allocation
 * for parallel test execution.
 *
 * <p>This listener:</p>
 * <ul>
 *   <li>Creates isolated database schemas before test class execution</li>
 *   <li>Allocates dynamic ports for WireMock configurations</li>
 *   <li>Injects schema and port properties into the Spring test context</li>
 *   <li>Cleans up resources after test class completion</li>
 * </ul>
 *
 * <p>Thread-safe for concurrent test class execution.</p>
 */
public class ParallelTestExecutionListener extends AbstractTestExecutionListener {

    private static final String SCHEMA_PROPERTY_NAME = "spring.datasource.schema";
    private static final String WIREMOCK_PORT_PREFIX = "wiremock.port.";
    private static final int DEFAULT_WIREMOCK_PORT_COUNT = 3;

    private final Map<Class<?>, TestResourceContext> testResources = new ConcurrentHashMap<>();
    private final DynamicPortAllocator portAllocator = new SimpleDynamicPortAllocator();

    @Override
    public int getOrder() {
        return 1000; // Run early in the listener chain
    }

    @Override
    public void beforeTestClass(TestContext testContext) {
        Class<?> testClass = testContext.getTestClass();

        if (testResources.containsKey(testClass)) {
            return; // Already initialized
        }

        // Create schema isolation if DataSource is available
        SchemaIsolationStrategy schemaStrategy = null;
        String schemaName = null;

        try {
            DataSource dataSource = getDataSource(testContext);
            schemaStrategy = new PostgresSchemaIsolationStrategy(dataSource);
            schemaName = schemaStrategy.createSchema();
            // TODO: Wrap dataSource with SchemaAwareDataSource and register it back
            //  into the ApplicationContext so connections use the isolated schema.
        } catch (Exception e) {
            // DataSource not available - skip schema isolation
            schemaName = "test_schema_unavailable";
        }

        // Determine WireMock port count from annotation
        int wiremockPortCount = DEFAULT_WIREMOCK_PORT_COUNT;
        ParallelFunctionalTest annotation = testClass.getAnnotation(ParallelFunctionalTest.class);
        if (annotation != null) {
            wiremockPortCount = annotation.wiremockPorts();
        }
        if (wiremockPortCount < 0) {
            throw new IllegalArgumentException(
                "wiremockPorts must not be negative, but was: " + wiremockPortCount);
        }

        // Allocate dynamic ports
        List<Integer> allocatedPorts = wiremockPortCount > 0
            ? portAllocator.allocatePorts(wiremockPortCount)
            : List.of();

        // Store resources for cleanup
        TestResourceContext resourceContext = new TestResourceContext(
            schemaStrategy,
            allocatedPorts
        );
        testResources.put(testClass, resourceContext);

        // Inject properties into test context
        injectDynamicProperties(testContext, schemaName, allocatedPorts);
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        Class<?> testClass = testContext.getTestClass();
        TestResourceContext resourceContext = testResources.remove(testClass);

        if (resourceContext != null) {
            cleanupResources(resourceContext);
        }
    }

    private DataSource getDataSource(TestContext testContext) {
        return testContext.getApplicationContext().getBean(DataSource.class);
    }

    private void injectDynamicProperties(TestContext testContext, String schemaName, List<Integer> ports) {
        ConfigurableEnvironment environment = (ConfigurableEnvironment)
                testContext.getApplicationContext().getEnvironment();

        List<String> properties = new ArrayList<>();
        properties.add(SCHEMA_PROPERTY_NAME + "=" + schemaName);

        for (int i = 0; i < ports.size(); i++) {
            properties.add(WIREMOCK_PORT_PREFIX + i + "=" + ports.get(i));
        }

        TestPropertyValues.of(properties).applyTo(environment);
    }

    private void cleanupResources(TestResourceContext context) {
        if (context.schemaStrategy() != null) {
            try {
                context.schemaStrategy().cleanupSchema();
            } catch (Exception e) {
                // Log but don't fail cleanup
                System.err.println("Failed to cleanup schema: " + e.getMessage());
            }
        }

        try {
            portAllocator.releasePorts(context.allocatedPorts());
        } catch (Exception e) {
            // Log but don't fail cleanup
            System.err.println("Failed to release ports: " + e.getMessage());
        }
    }

    private record TestResourceContext(
        SchemaIsolationStrategy schemaStrategy,
        List<Integer> allocatedPorts
    ) {}
}
