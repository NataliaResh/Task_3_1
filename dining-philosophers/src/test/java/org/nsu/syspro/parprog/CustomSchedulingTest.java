package org.nsu.syspro.parprog;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nsu.syspro.parprog.base.DefaultFork;
import org.nsu.syspro.parprog.base.DiningTable;
import org.nsu.syspro.parprog.examples.DefaultPhilosopher;
import org.nsu.syspro.parprog.helpers.TestLevels;
import org.nsu.syspro.parprog.interfaces.Fork;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomSchedulingTest extends TestLevels {

    static final class CustomizedPhilosopher extends DefaultPhilosopher {
        @Override
        public void onHungry(Fork left, Fork right) {
            sleepMillis(this.id * 20);
            System.out.println(Thread.currentThread() + " " + this + ": onHungry");
            super.onHungry(left, right);
        }
    }

    static final class CustomizedFork extends DefaultFork {
        @Override
        public void acquire() {
            System.out.println(Thread.currentThread() + " trying to acquire " + this);
            super.acquire();
            System.out.println(Thread.currentThread() + " acquired " + this);
            sleepMillis(100);
        }
    }

    static final class CustomizedTable extends DiningTable<CustomizedPhilosopher, CustomizedFork> {
        public CustomizedTable(int N) {
            super(N);
        }

        @Override
        public CustomizedFork createFork() {
            return new CustomizedFork();
        }

        @Override
        public CustomizedPhilosopher createPhilosopher() {
            return new CustomizedPhilosopher();
        }
    }

    @EnabledIf("easyEnabled")
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @Timeout(4)
    void testDeadlockFreedom(int N) {
        final CustomizedTable table = dine(new CustomizedTable(N), 1);
    }

    static final class CustomizedPhilosopherSingleSlow extends DefaultPhilosopher {
        @Override
        public void onHungry(Fork left, Fork right) {
            if (this.id == 0) {
                sleepSeconds(1);
            }
            super.onHungry(left, right);
        }
    }

    static final class CustomizedTableSingleSlow extends DiningTable<CustomizedPhilosopherSingleSlow, DefaultFork> {
        public CustomizedTableSingleSlow(int N) {
            super(N);
        }

        @Override
        public DefaultFork createFork() {
            return new DefaultFork();
        }

        @Override
        public CustomizedPhilosopherSingleSlow createPhilosopher() {
            return new CustomizedPhilosopherSingleSlow();
        }
    }

    @EnabledIf("easyEnabled")
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @Timeout(2)
    void testSingleSlow(int N) {
        final CustomizedTableSingleSlow table = dine(new CustomizedTableSingleSlow(N), 1);
        System.out.println(table.maxMeals());
        assertTrue(table.maxMeals() >= 1000);
    }

    static final class CustomizedPhilosopherWeakFairness extends DefaultPhilosopher {
        private int countTryEat = 0;
        @Override
        public void onHungry(Fork left, Fork right) {
            countTryEat++;
            if (countTryEat % 10 != 0) {
                return;
            }
            super.onHungry(left, right);
        }
    }

    static final class CustomizedTableWeakFairness extends DiningTable<CustomizedPhilosopherWeakFairness, DefaultFork> {
        public CustomizedTableWeakFairness(int N) {
            super(N);
        }

        @Override
        public DefaultFork createFork() {
            return new DefaultFork();
        }

        @Override
        public CustomizedPhilosopherWeakFairness createPhilosopher() {
            return new CustomizedPhilosopherWeakFairness();
        }
    }

    @EnabledIf("mediumEnabled")
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @Timeout(2)
    void testWeakFairness(int N) {
        final CustomizedTableWeakFairness table = dine(new CustomizedTableWeakFairness(N), 1);
        assertTrue(table.minMeals() > 0); // every philosopher eat at least once
    }

    static final class CustomizedPhilosopherStrongFairness extends DefaultPhilosopher {
        @Override
        public void onHungry(Fork left, Fork right) {
            if (this.id % 2 != 0) {
                sleepMillis(CustomizedTableStrongFairness.miles);
            }
            super.onHungry(left, right);
        }
    }

    static final class CustomizedTableStrongFairness extends DiningTable<CustomizedPhilosopherStrongFairness, DefaultFork> {
        static public long miles;
        public CustomizedTableStrongFairness(int N) {
            super(N);
        }

        @Override
        public DefaultFork createFork() {
            return new DefaultFork();
        }

        @Override
        public CustomizedPhilosopherStrongFairness createPhilosopher() {
            return new CustomizedPhilosopherStrongFairness();
        }
    }

    @EnabledIf("hardEnabled")
    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 5})
    @Timeout(2 * 20)
    void testStrongFairness(int N) {
        for (long i = 1; i <= 8; i *= 2) {
            CustomizedTableStrongFairness.miles = i;
            final CustomizedTableStrongFairness table = dine(new CustomizedTableStrongFairness(N), 1);
            final long minMeals = table.minMeals();
            final long maxMeals = table.maxMeals();
            assertFalse(maxMeals < 1.5 * minMeals); // some king of gini index for philosophers
        }
    }
}
