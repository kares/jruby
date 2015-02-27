package org.jruby.util.collections;

/**
 * A proxy cache that uses Java 7's ClassValue.
 */
public class Java7ClassValue<T> extends ClassValue<T> {

    public Java7ClassValue(ClassValueCalculator<T> calculator) {
        super(calculator);
    }

    @Override
    public T get(final Class klass) {
        return proxy.get(klass);
    }

    @Override
    public boolean has(final Class klass) {
        final ThreadLocal<Object> hasOnly = proxy.hasOnly;
        try {
            hasOnly.set(Boolean.TRUE);
            return proxy.get(klass) != null;
        }
        finally {
            hasOnly.remove();
        }
    }

    private final ProxyClassValue proxy = new ProxyClassValue();

    private class ProxyClassValue extends java.lang.ClassValue<T> {

        final ThreadLocal<Object> hasOnly = new ThreadLocal<Object>();

        @Override
        protected T computeValue(Class<?> type) {
            if ( hasOnly.get() != null ) return null;
            return calculator.computeValue(type);
        }

    }

}
