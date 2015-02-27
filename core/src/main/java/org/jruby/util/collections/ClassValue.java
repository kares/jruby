package org.jruby.util.collections;

/**
 * Represents a cache or other mechanism for getting the Ruby-level proxy classes
 * for a given Java class.
 */
public abstract class ClassValue<T> {

    protected final ClassValueCalculator<T> calculator;

    public ClassValue(ClassValueCalculator<T> calculator) {
        this.calculator = calculator;
    }

    public abstract T get(Class klass);

    public boolean has(Class klass) {
        return get(klass) != null; // naive impl (for extender's compatibility)
    }

}
