package com.quizai.agent.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AgentConcurrencyService {

    private final AtomicInteger maxConcurrency = new AtomicInteger(5);
    private final AtomicInteger activeCount = new AtomicInteger(0);

    public boolean tryAcquire() {
        int current = activeCount.get();
        int max = maxConcurrency.get();
        while (current < max) {
            if (activeCount.compareAndSet(current, current + 1)) {
                return true;
            }
            current = activeCount.get();
        }
        return false;
    }

    public void release() {
        activeCount.decrementAndGet();
    }

    public int getActiveCount() {
        return activeCount.get();
    }

    public int getMaxConcurrency() {
        return maxConcurrency.get();
    }

    public void setMaxConcurrency(int max) {
        if (max < 1) {
            throw new IllegalArgumentException("最大并发数必须 >= 1");
        }
        this.maxConcurrency.set(max);
    }
}
