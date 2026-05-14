package com.quizai.agent.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AgentStatusService {

    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong activeConnections = new AtomicLong(0);

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean value) {
        enabled.set(value);
    }

    public void toggleEnabled() {
        enabled.set(!enabled.get());
    }

    public long incrementRequests() {
        return totalRequests.incrementAndGet();
    }

    public long incrementConnections() {
        return activeConnections.incrementAndGet();
    }

    public long decrementConnections() {
        return activeConnections.decrementAndGet();
    }

    public long getTotalRequests() {
        return totalRequests.get();
    }

    public long getActiveConnections() {
        return activeConnections.get();
    }
}
