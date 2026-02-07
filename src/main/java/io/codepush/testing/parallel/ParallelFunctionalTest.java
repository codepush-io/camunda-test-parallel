package io.codepush.testing.parallel;

import io.codepush.testing.parallel.listener.ParallelTestExecutionListener;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Spring Boot integration tests that enables parallel test execution
 * with isolated database schemas and dynamic WireMock port allocation.
 *
 * <p>This annotation is designed for tests using embedded Camunda engines where
 * multiple test classes need to run concurrently without resource conflicts.</p>
 *
 * <p>Each test class annotated with {@code @ParallelFunctionalTest} will:</p>
 * <ul>
 *   <li>Use an isolated Camunda database schema (e.g., camunda_test_uuid)</li>
 *   <li>Have dynamically allocated WireMock ports injected into configuration</li>
 *   <li>Run with Spring Boot test infrastructure and random web port</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * @ParallelFunctionalTest
 * class ProcessDeploymentTest {
 *
 *     @Autowired
 *     private ProcessEngine processEngine;
 *
 *     @Test
 *     void shouldDeployProcess() {
 *         // Test runs in isolated schema with dynamic ports
 *     }
 * }
 * }</pre>
 *
 * <p><b>Custom Properties:</b></p>
 * <pre>{@code
 * @ParallelFunctionalTest(
 *     properties = {
 *         "custom.feature.enabled=true",
 *         "custom.timeout=5000"
 *     }
 * )
 * class CustomFeatureTest {
 *     // Custom properties merged with dynamic configuration
 * }
 * }</pre>
 *
 * @see SpringBootTest
 * @see EnableAutoConfiguration
 * @see ActiveProfiles
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@ActiveProfiles("test")
@TestExecutionListeners(
    listeners = ParallelTestExecutionListener.class,
    mergeMode = MergeMode.MERGE_WITH_DEFAULTS
)
public @interface ParallelFunctionalTest {

    /**
     * Properties to add to the Spring Environment. These properties are merged with
     * dynamically generated properties for schema isolation and port allocation.
     *
     * <p>Use the form {@code key=value}.</p>
     *
     * @return the properties to add
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "properties")
    String[] properties() default {"spring.main.banner-mode=off"};

    /**
     * Number of WireMock ports to allocate for this test class.
     *
     * <p>Each allocated port is injected as a system property with the naming
     * convention {@code wiremock.port.0}, {@code wiremock.port.1}, etc.</p>
     *
     * <p>Set to {@code 0} to disable WireMock port allocation entirely.
     * Must not be negative.</p>
     *
     * @return the number of WireMock ports to allocate (default 3)
     */
    int wiremockPorts() default 3;
}
