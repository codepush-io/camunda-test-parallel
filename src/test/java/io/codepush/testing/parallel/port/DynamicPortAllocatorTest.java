package io.codepush.testing.parallel.port;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DynamicPortAllocatorTest {

    private DynamicPortAllocator allocator;

    @BeforeEach
    void setUp() {
        allocator = new SimpleDynamicPortAllocator();
    }

    @Test
    void shouldAllocateSinglePort() {
        List<Integer> ports = allocator.allocatePorts(1);

        assertThat(ports)
            .hasSize(1)
            .allMatch(port -> port >= 1024 && port <= 65535);
    }

    @Test
    void shouldAllocateMultiplePorts() {
        List<Integer> ports = allocator.allocatePorts(5);

        assertThat(ports)
            .hasSize(5)
            .doesNotHaveDuplicates()
            .allMatch(port -> port >= 1024 && port <= 65535);
    }

    @Test
    void shouldAllocatePortsInValidRange() {
        List<Integer> ports = allocator.allocatePorts(3);

        // Ports should be in valid dynamic/private port range (varies by OS)
        // Linux/Mac typically use 32768-60999, Windows uses 49152-65535
        assertThat(ports)
            .allMatch(port -> port >= 1024 && port <= 65535);
    }

    @Test
    void shouldThrowExceptionWhenCountIsZero() {
        assertThatThrownBy(() -> allocator.allocatePorts(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("count must be positive");
    }

    @Test
    void shouldThrowExceptionWhenCountIsNegative() {
        assertThatThrownBy(() -> allocator.allocatePorts(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("count must be positive");
    }

    @Test
    void shouldReleaseAllocatedPorts() {
        List<Integer> ports = allocator.allocatePorts(3);

        allocator.releasePorts(ports);

        // After release, should be able to allocate again
        List<Integer> newPorts = allocator.allocatePorts(3);
        assertThat(newPorts).hasSize(3);
    }

    @Test
    void shouldHandleReleaseOfEmptyList() {
        allocator.releasePorts(List.of());
        // Should not throw exception
    }

    @Test
    void shouldHandleReleaseOfNullList() {
        assertThatThrownBy(() -> allocator.releasePorts(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldAllocateDifferentPortsForConcurrentRequests() {
        List<Integer> ports1 = allocator.allocatePorts(5);
        List<Integer> ports2 = allocator.allocatePorts(5);

        assertThat(ports1)
            .doesNotContainAnyElementsOf(ports2);
    }
}
