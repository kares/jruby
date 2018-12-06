package org.jruby.runtime.callsite;

import org.jruby.Ruby;
import org.jruby.RubySymbol;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.RubyClass;
import org.jruby.internal.runtime.methods.DynamicMethod;

import static org.jruby.RubyBasicObject.getMetaClass;

public class RespondToCallSite extends NormalCachingCallSite {
    private RespondToTuple respondToTuple = RespondToTuple.NULL_CACHE;
    private final String respondToName;
    private RubySymbol respondToNameSym;

    private static class RespondToTuple {
        static final RespondToTuple NULL_CACHE = new RespondToTuple("", true, CacheEntry.NULL_CACHE, CacheEntry.NULL_CACHE);

        public final String name;
        public final boolean checkVisibility;
        public final CacheEntry respondToMethod;
        public final CacheEntry entry;
        public final IRubyObject respondsTo;
        public final boolean respondsToBoolean;
        
        RespondToTuple(String name, boolean checkVisibility, CacheEntry respondToMethod, CacheEntry entry, IRubyObject respondsTo) {
            this.name = name;
            this.checkVisibility = checkVisibility;
            this.respondToMethod = respondToMethod;
            this.entry = entry;
            this.respondsTo = respondsTo;
            this.respondsToBoolean = respondsTo.isTrue();
        }

        private RespondToTuple(String name, boolean checkVisibility, CacheEntry respondToMethod, CacheEntry entry) {
            this.name = name;
            this.checkVisibility = checkVisibility;
            this.respondToMethod = respondToMethod;
            this.entry = entry;
            this.respondsTo = null;
            this.respondsToBoolean = false;
        }

        public boolean cacheOk(RubyClass klass) {
            return respondToMethod.typeOk(klass) && entry.typeOk(klass);
        }
    }

    public RespondToCallSite() {
        super("respond_to?");
        respondToName = null;
    }

    public RespondToCallSite(String name) {
        super("respond_to?");
        respondToName = name;
    }

    @Override
    public IRubyObject call(ThreadContext context, IRubyObject caller, IRubyObject self, IRubyObject name) { 
        RubyClass klass = getMetaClass(self);
        RespondToTuple tuple = respondToTuple;
        if (tuple.cacheOk(klass)) {
            String respondToName = name.asJavaString(); // RubySymbol
            if (respondToName.equals(tuple.name) && tuple.checkVisibility) return tuple.respondsTo;
        }
        // go through normal call logic, which will hit overridden cacheAndCall
        return super.call(context, caller, self, name);
    }

    @Override
    public IRubyObject call(ThreadContext context, IRubyObject caller, IRubyObject self, IRubyObject name, IRubyObject all) {
        RubyClass klass = getMetaClass(self);
        RespondToTuple tuple = respondToTuple;
        if (tuple.cacheOk(klass)) {
            String respondToName = name.asJavaString(); // RubySymbol
            if (respondToName.equals(tuple.name) && !all.isTrue() == tuple.checkVisibility) return tuple.respondsTo;
        }
        // go through normal call logic, which will hit overridden cacheAndCall
        return super.call(context, caller, self, name, all);
    }

    public boolean respondsTo(ThreadContext context, IRubyObject caller, IRubyObject self) {
        RubyClass klass = getMetaClass(self);
        RespondToTuple tuple = respondToTuple;
        if (tuple.cacheOk(klass)) {
            if (respondToName.equals(tuple.name) && tuple.checkVisibility) return tuple.respondsToBoolean;
        }
        // go through normal call logic, which will hit overridden cacheAndCall
        return super.call(context, caller, self, getRespondToNameSym(context)).isTrue();
    }

    public boolean respondsTo(ThreadContext context, IRubyObject caller, IRubyObject self, boolean includePrivate) {
        RubyClass klass = getMetaClass(self);
        RespondToTuple tuple = respondToTuple;
        if (tuple.cacheOk(klass)) {
            if (respondToName.equals(tuple.name) && !includePrivate == tuple.checkVisibility) return tuple.respondsToBoolean;
        }
        // go through normal call logic, which will hit overridden cacheAndCall
        return super.call(context, caller, self, getRespondToNameSym(context), context.runtime.newBoolean(includePrivate)).isTrue();
    }

    private RubySymbol getRespondToNameSym(ThreadContext context) {
        RubySymbol sym = respondToNameSym;
        if (sym == null) {
            respondToNameSym = sym = context.runtime.newSymbol(respondToName);
        }
        return sym;
    }

    @Override
    protected IRubyObject cacheAndCall(ThreadContext context, IRubyObject caller,
        IRubyObject self, RubyClass selfType, IRubyObject name) {
        CacheEntry entry = selfType.searchWithCache(methodName);
        DynamicMethod method = entry.method;
        if (methodMissing(method, caller)) {
            return callMethodMissing(context, self, selfType, method, name);
        }

        // alternate logic to cache the result of respond_to if it's the standard one
        if (entry.method.isBuiltin()) {
            RespondToTuple tuple = newRespondToTuple(context.runtime, entry, selfType, name, true);

            // only cache if it does respond_to? OR there's no custom respond_to_missing? logic
            if (tuple.respondsToBoolean ||
                    selfType.searchWithCache("respond_to_missing?").method == context.runtime.getRespondToMissingMethod()) {
                respondToTuple = tuple;
                return tuple.respondsTo;
            }
        }

        // normal logic if it's not the builtin respond_to? method
        cache = entry;
        return method.call(context, self, selfType, methodName, name);
    }

    @Override
    protected IRubyObject cacheAndCall(ThreadContext context, IRubyObject caller,
        IRubyObject self, RubyClass selfType, IRubyObject name, IRubyObject all) {
        CacheEntry entry = selfType.searchWithCache(methodName);
        DynamicMethod method = entry.method;
        if (methodMissing(method, caller)) {
            return callMethodMissing(context, self, selfType, method, name, all);
        }

        // alternate logic to cache the result of respond_to if it's the standard one
        if (entry.method.equals(context.runtime.getRespondToMethod())) {
            RespondToTuple tuple = newRespondToTuple(context.runtime, entry, selfType, name, !all.isTrue());

            // only cache if it does respond_to? OR there's no custom respond_to_missing? logic
            if (tuple.respondsToBoolean ||
                    selfType.searchWithCache("respond_to_missing?").method == context.runtime.getRespondToMissingMethod()) {
                respondToTuple = tuple;
                return tuple.respondsTo;
            }
        }

        // normal logic if it's not the builtin respond_to? method
        cache = entry;
        return method.call(context, self, selfType, methodName, name, all);
    }

    private static RespondToTuple newRespondToTuple(final Ruby runtime, CacheEntry respondToMethod,
                                                    RubyClass klass, IRubyObject nameSym, boolean checkVisibility) {
        final String name = nameSym.asJavaString();
        CacheEntry respondToLookupResult = klass.searchWithCache(name);
        boolean respondsTo = Helpers.respondsToMethod(respondToLookupResult.method, checkVisibility);

        return new RespondToTuple(name, checkVisibility, respondToMethod, respondToLookupResult, runtime.newBoolean(respondsTo));
    }
}