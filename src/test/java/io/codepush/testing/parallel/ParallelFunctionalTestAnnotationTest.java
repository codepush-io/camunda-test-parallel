package io.codepush.testing.parallel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.assertThat;

class ParallelFunctionalTestAnnotationTest {

    @Test
    void shouldBeAnnotationWithRuntimeRetention() {
        assertThat(ParallelFunctionalTest.class.isAnnotation()).isTrue();

        Retention retention = ParallelFunctionalTest.class.getAnnotation(Retention.class);
        assertThat(retention).isNotNull();
        assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
    }

    @Test
    void shouldTargetTypeLevel() {
        Target target = ParallelFunctionalTest.class.getAnnotation(Target.class);
        assertThat(target).isNotNull();
        assertThat(target.value()).containsExactly(ElementType.TYPE);
    }

    @Test
    void shouldBeMetaAnnotatedWithSpringBootTest() {
        SpringBootTest springBootTest = ParallelFunctionalTest.class.getAnnotation(SpringBootTest.class);
        assertThat(springBootTest).isNotNull();
        assertThat(springBootTest.webEnvironment()).isEqualTo(WebEnvironment.RANDOM_PORT);
    }

    @Test
    void shouldBeMetaAnnotatedWithEnableAutoConfiguration() {
        EnableAutoConfiguration autoConfig = ParallelFunctionalTest.class.getAnnotation(EnableAutoConfiguration.class);
        assertThat(autoConfig).isNotNull();
    }

    @Test
    void shouldBeMetaAnnotatedWithActiveProfiles() {
        ActiveProfiles activeProfiles = ParallelFunctionalTest.class.getAnnotation(ActiveProfiles.class);
        assertThat(activeProfiles).isNotNull();
        assertThat(activeProfiles.value()).containsExactly("test");
    }

    @Test
    void shouldHavePropertiesAttribute() throws NoSuchMethodException {
        var method = ParallelFunctionalTest.class.getDeclaredMethod("properties");
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(String[].class);
    }

    @Test
    void shouldHaveDefaultPropertyForBannerMode() throws Exception {
        var method = ParallelFunctionalTest.class.getDeclaredMethod("properties");
        String[] defaultValue = (String[]) method.getDefaultValue();
        assertThat(defaultValue).contains("spring.main.banner-mode=off");
    }
}
