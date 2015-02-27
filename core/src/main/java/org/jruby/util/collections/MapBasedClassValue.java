package org.jruby.util.collections;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple Map-based cache of proxies.
 */
public class MapBasedClassValue<T> extends ClassValue<T> {

    public MapBasedClassValue(ClassValueCalculator<T> calculator) {
        super(calculator);
    }

    @Override
    public T get(final Class klass) {
        T value = cache.get(klass);

        if (value == null) {
            T newValue = calculator.computeValue(klass);
            value = cache.putIfAbsent(klass, newValue);
            if ( value == null ) value = newValue; // did put newValue in
        }

        return value;
    }

    @Override
    public boolean has(final Class klass) {
        return cache.containsKey(klass);
    }

    // There's not a compelling reason to keep JavaClass instances in a weak map
    // (any proxies created are [were] kept in a non-weak map, so in most cases they will
    // stick around anyway), and some good reasons not to (JavaClass creation is
    // expensive, for one; many lookups are performed when passing parameters to/from
    // methods; etc.).
    // TODO: faster custom concurrent map
    private final ConcurrentHashMap<Class,T> cache =
        new ConcurrentHashMap<Class, T>(128);
}
