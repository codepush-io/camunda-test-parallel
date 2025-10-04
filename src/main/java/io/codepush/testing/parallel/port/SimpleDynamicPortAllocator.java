package io.codepush.testing.parallel.port;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple implementation of dynamic port allocation using ephemeral port range.
 *
 * <p>Allocates ports in the ephemeral range (49152-65535) by attempting to bind
 * to available ports. Thread-safe for concurrent access.</p>
 *
 * <p>Ports are tracked to prevent double allocation until explicitly released.</p>
 */
public final class SimpleDynamicPortAllocator implements DynamicPortAllocator {

    private static final int EPHEMERAL_PORT_START = 49152;
    private static final int EPHEMERAL_PORT_END = 65535;
    private static final int MAX_ALLOCATION_ATTEMPTS = 100;

    private final Set<Integer> allocatedPorts = ConcurrentHashMap.newKeySet();

    @Override
    public List<Integer> allocatePorts(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive, got: " + count);
        }

        List<Integer> ports = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int port = allocateSinglePort();
            ports.add(port);
        }

        return List.copyOf(ports);
    }

    @Override
    public void releasePorts(List<Integer> ports) {
        Objects.requireNonNull(ports, "ports must not be null");
        allocatedPorts.removeAll(ports);
    }

    private int allocateSinglePort() {
        for (int attempt = 0; attempt < MAX_ALLOCATION_ATTEMPTS; attempt++) {
            int port = findAvailablePort();
            if (allocatedPorts.add(port)) {
                return port;
            }
        }

        throw new PortAllocationException(
            "Failed to allocate port after " + MAX_ALLOCATION_ATTEMPTS + " attempts"
        );
    }

    private int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            int port = socket.getLocalPort();

            // Verify port is in ephemeral range
            if (port >= EPHEMERAL_PORT_START && port <= EPHEMERAL_PORT_END) {
                return port;
            }

            // If not in ephemeral range, try again with explicit binding
            return findAvailablePortInRange();

        } catch (IOException e) {
            throw new PortAllocationException("Failed to find available port", e);
        }
    }

    private int findAvailablePortInRange() throws IOException {
        // Try to bind to a random port in ephemeral range
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }
}
