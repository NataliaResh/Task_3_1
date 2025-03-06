package org.nsu.syspro.parprog.base;

import org.nsu.syspro.parprog.interfaces.Fork;
import org.nsu.syspro.parprog.interfaces.Philosopher;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class DiningTable<P extends Philosopher, F extends Fork> {
    private final ArrayList<F> forks;
    private final ArrayList<P> phils;
    ExecutorService executor;

    public DiningTable(int N) {
        if (N < 2) {
            throw new IllegalStateException("Too small dining table");
        }
        forks = new ArrayList<>(N);
        phils = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            forks.add(createFork());
            phils.add(createPhilosopher());
        }
    }

    public synchronized void start() {
        if (executor != null) {
            throw new IllegalStateException("Restart is not supported");
        }
        executor = Executors.newFixedThreadPool(10);
        final int N = phils.size();
        for (int i = 0; i < N; i++) {
            final Philosopher p = phils.get(i);
            final Fork left = forks.get(i);
            final Fork right = forks.get((i + 1) % N);
            executor.submit(() -> {
                while (true) {
                    p.onHungry(left, right);
                }
            });
        }
    }

    public synchronized void stop() {
        if (executor == null) {
            throw new IllegalStateException("Start first");
        }
        if (executor.isShutdown()) {
            throw new IllegalStateException("Repeated stop is illegal");
        }
        executor.shutdownNow();
    }

    public P philosopherAt(int index) {
        return phils.get(index);
    }

    public F forkAt(int index) {
        return forks.get(index);
    }

    public long maxMeals() {
        return phils.stream()
                .mapToLong(Philosopher::meals)
                .max()
                .getAsLong();
    }

    public long minMeals() {
        return phils.stream()
                .mapToLong(Philosopher::meals)
                .min()
                .getAsLong();
    }

    public long totalMeals() {
        return phils.stream()
                .mapToLong(Philosopher::meals)
                .sum();
    }

    public abstract F createFork();

    public abstract P createPhilosopher();
}
