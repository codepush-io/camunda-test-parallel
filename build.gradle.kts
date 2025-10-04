plugins {
    `java-library`
    `maven-publish`
}

group = "io.codepush.testing"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
    withJavadocJar()
}

// Configure integration test source set
sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
        java.srcDir("src/integrationTest/java")
        resources.srcDir("src/integrationTest/resources")
    }
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.testImplementation.get())
}

val integrationTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.runtimeOnly.get())
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    // Spring Boot dependencies (BOM)
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.0"))

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-test")

    // Camunda Platform 8
    implementation("io.camunda:camunda-bpm-spring-boot-starter:7.20.0")

    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    // AssertJ
    testImplementation("org.assertj:assertj-core:3.24.2")

    // Mockito
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
    }
}

// Register integration test task
val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    shouldRunAfter(tasks.test)

    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
    }
}

tasks.check {
    dependsOn(integrationTest)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("Camunda Test Parallel")
                description.set("A library to support parallel execution of Spring Boot integration tests with embedded Camunda runtime")
                url.set("https://github.com/codepush-io/camunda-test-parallel")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("irmac")
                        name.set("Iain MacLeod")
                        email.set("irmac@users.noreply.github.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/codepush-io/camunda-test-parallel.git")
                    developerConnection.set("scm:git:ssh://github.com/codepush-io/camunda-test-parallel.git")
                    url.set("https://github.com/codepush-io/camunda-test-parallel")
                }
            }
        }
    }
}
