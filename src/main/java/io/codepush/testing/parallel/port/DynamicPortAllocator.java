package io.codepush.testing.parallel.port;

import java.util.List;

/**
 * Strategy interface for allocating and releasing dynamic ports for parallel test execution.
 *
 * <p>Implementations should provide thread-safe port allocation to avoid conflicts
 * when multiple test classes run concurrently.</p>
 */
public interface DynamicPortAllocator {

    /**
     * Allocates a specified number of available ports.
     *
     * @param count the number of ports to allocate (must be positive)
     * @return list of allocated port numbers
     * @throws IllegalArgumentException if count is not positive
     * @throws PortAllocationException if ports cannot be allocated
     */
    List<Integer> allocatePorts(int count);

    /**
     * Releases previously allocated ports, making them available for reuse.
     *
     * @param ports the list of ports to release
     * @throws NullPointerException if ports is null
     */
    void releasePorts(List<Integer> ports);
}
