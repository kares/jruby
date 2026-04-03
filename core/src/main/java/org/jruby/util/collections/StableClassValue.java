package org.jruby.util.collections;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * An implementation of JRuby's ClassValue, using java.lang.ClassValue directly but ensuring computation of each value
 * occurs at most once.
 */
final class StableClassValue<T> extends ClassValue<T> {
    private final ReentrantLock lock = new ReentrantLock();

    public StableClassValue(ClassValueCalculator<T> calculator) {
        super(calculator);
    }

    public T get(Class<?> cls) {
        // We don't check for null on the WeakReference since the
        // value is strongly referenced by proxy's list
        return proxy.get(cls).get(cls);
    }

    /**
     * Represents a stable value, which is computed at most once and then stored forever.
     *
     * This should be replaced with a JDK StableValue once it becomes available in Java 25. The double-checked locking
     * implementation cannot be folded by the JVM and will not have the same performance characteristics.
     *
     * @param <Input> input of the computation
     * @param <Result> result of the computation
     */
    private static class StableValue<Input, Result> {
        private final ReentrantLock lock;
        private final Function<Input, Result> calculator;
        private volatile Result result;
        StableValue(ReentrantLock lock, Function<Input, Result> calculator) {
            this.lock = lock;
            this.calculator = calculator;
        }
        Result get(Input input) {
            Result result = this.result;

            if (result != null) return result;

            // Use shared lock to ensure only one StableValue can initialize at a time
            lock.lock();
            try {
                result = this.result;

                if (result != null) return result;

                result = this.calculator.apply(input);
                this.result = result;
            } finally {
                lock.unlock();
            }

            return result;
        }
    }

    private final java.lang.ClassValue<StableValue<Class<?>, T>> proxy = new java.lang.ClassValue<StableValue<Class<?>, T>>() {
        @Override
        protected StableValue<Class<?>, T> computeValue(Class<?> type) {
            return new StableValue<>(lock, calculator);
        }
    };
}
