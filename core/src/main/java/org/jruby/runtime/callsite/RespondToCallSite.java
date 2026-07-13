package org.jruby.runtime.callsite;

import org.jruby.RubySymbol;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.RubyClass;
import org.jruby.internal.runtime.methods.DynamicMethod;

import static org.jruby.RubyBasicObject.getMetaClass;
import static org.jruby.api.Check.checkID;
import static org.jruby.api.Convert.asBoolean;
import static org.jruby.api.Convert.asSymbol;

public class RespondToCallSite extends MonomorphicCallSite {
    private volatile RespondToTuple respondToTuple = RespondToTuple.NULL_CACHE;
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
        
        public RespondToTuple(String name, boolean checkVisibility, CacheEntry respondToMethod, CacheEntry entry, IRubyObject respondsTo) {
            this.name = name;
            this.checkVisibility = checkVisibility;
            this.respondToMethod = respondToMethod;
            this.entry = entry;
            this.respondsTo = respondsTo;
            this.respondsToBoolean = respondsTo.isTrue();
        }

        public RespondToTuple(String name, boolean checkVisibility, CacheEntry respondToMethod, CacheEntry entry) {
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
            String id = checkID(context, name).idString();
            if (id.equals(tuple.name) && tuple.checkVisibility) return tuple.respondsTo;
        }
        return callRespondTo(context, caller, self, klass, name);
    }

    @Override
    public IRubyObject call(ThreadContext context, IRubyObject caller, IRubyObject self, IRubyObject name, IRubyObject bool) {
        RubyClass klass = getMetaClass(self);
        RespondToTuple tuple = respondToTuple;
        if (tuple.cacheOk(klass)) {
            String id = checkID(context, name).idString();
            if (id.equals(tuple.name) && !bool.isTrue() == tuple.checkVisibility) return tuple.respondsTo;
        }
        return callRespondTo(context, caller, self, klass, name, bool);
    }

    public boolean respondsTo(ThreadContext context, IRubyObject caller, IRubyObject self) {
        RubyClass klass = getMetaClass(self);
        RespondToTuple tuple = respondToTuple;
        if (tuple.cacheOk(klass)) {
            String strName = respondToName;
            if (strName.equals(tuple.name) && tuple.checkVisibility) return tuple.respondsToBoolean;
        }
        return callRespondTo(context, caller, self, klass, getRespondToNameSym(context)).isTrue();
    }

    public boolean respondsTo(ThreadContext context, IRubyObject caller, IRubyObject self, boolean includePrivate) {
        RubyClass klass = getMetaClass(self);
        RespondToTuple tuple = respondToTuple;
        if (tuple.cacheOk(klass)) {
            String strName = respondToName;
            if (strName.equals(tuple.name) && !includePrivate == tuple.checkVisibility) return tuple.respondsToBoolean;
        }
        return callRespondTo(context, caller, self, klass, getRespondToNameSym(context), asBoolean(context, includePrivate)).isTrue();
    }

    private IRubyObject callRespondTo(ThreadContext context, IRubyObject caller, IRubyObject self, RubyClass selfType,
            IRubyObject arg) {
        CacheEntry cache = this.cache;
        if (cache.typeOk(selfType)) {
            return cache.method.call(context, self, cache.sourceModule, methodName, arg);
        }

        CacheEntry entry = respondToEntry(context, caller, selfType);
        DynamicMethod method = entry.method;

        // alternate logic to cache the result of respond_to if it's the standard one
        if (method.isBuiltin()) {
            IRubyObject tuple = fastRespondTo(context, arg, entry, selfType, true);
            if (tuple != null) return tuple;
        }

        // normal logic if it's not the builtin respond_to? method
        entry = setCache(entry, self); // cache = entry;
        return method.call(context, self, entry.sourceModule, methodName, arg);
    }

    private IRubyObject callRespondTo(ThreadContext context, IRubyObject caller, IRubyObject self, RubyClass selfType,
            IRubyObject arg0, IRubyObject arg1) {
        CacheEntry cache = this.cache;
        if (cache.typeOk(selfType)) {
            return cache.method.call(context, self, cache.sourceModule, methodName, arg0, arg1);
        }

        CacheEntry entry = respondToEntry(context, caller, selfType);
        DynamicMethod method = entry.method;

        // alternate logic to cache the result of respond_to if it's the standard one
        if (method.equals(context.runtime.getRespondToMethod())) {
            IRubyObject tuple = fastRespondTo(context, arg0, entry, selfType, !arg1.isTrue());
            if (tuple != null) return tuple;
        }

        // normal logic if it's not the builtin respond_to? method
        entry = setCache(entry, self); // cache = entry;
        return method.call(context, self, entry.sourceModule, methodName, arg0, arg1);
    }

    private CacheEntry respondToEntry(ThreadContext context, IRubyObject caller, RubyClass selfType) {
        CacheEntry entry = selfType.searchWithCache(methodName);
        DynamicMethod method = entry.method;

        if (methodMissing(method, caller)) {
            entry = Helpers.createMethodMissingEntry(context, selfType, callType, method.getVisibility(), entry.token, methodName);
        }
        return entry;
    }

    private RubySymbol getRespondToNameSym(ThreadContext context) {
        RubySymbol sym = respondToNameSym;
        if (sym == null) respondToNameSym = sym = asSymbol(context, respondToName);
        return sym;
    }

    private IRubyObject fastRespondTo(ThreadContext context, IRubyObject arg, CacheEntry entry, RubyClass selfType, boolean checkVisibility) {
        String id = checkID(context, arg).idString();
        RespondToTuple tuple = recacheRespondsTo(entry, id, selfType, checkVisibility, context);

        // only cache if it does respond_to? OR there's no custom respond_to_missing? logic
        if (tuple.respondsTo.isTrue() ||
                selfType.searchWithCache("respond_to_missing?").method == context.runtime.getRespondToMissingMethod()) {
            respondToTuple = tuple;
            return tuple.respondsTo;
        }
        return null;
    }

    private static RespondToTuple recacheRespondsTo(CacheEntry respondToMethod, String newString, RubyClass klass, boolean checkVisibility, ThreadContext context) {
        CacheEntry respondToLookupResult = klass.searchWithCache(newString);
        boolean respondsTo = Helpers.respondsToMethod(respondToLookupResult.method, checkVisibility);

        return new RespondToTuple(newString, checkVisibility, respondToMethod, respondToLookupResult, asBoolean(context, respondsTo));
    }
}