package org.jruby.runtime.callsite;

import org.jruby.RubyFixnum;
import org.jruby.RubyFloat;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

public class MinusCallSite extends NormalCachingCallSite2 {

    public MinusCallSite() {
        super("-");
    }

    @Override
    public IRubyObject call(ThreadContext context, IRubyObject caller, IRubyObject self, long arg) {
        if (self instanceof RubyFixnum) {
            if (isBuiltin(self.getMetaClass())) return ((RubyFixnum) self).op_minus(context, arg);
        } else if (self instanceof RubyFloat) {
            if (isBuiltin2(self.getMetaClass())) return ((RubyFloat) self).op_minus(context, arg);
        }
        return super.call(context, caller, self, arg);
    }

    @Override
    public IRubyObject call(ThreadContext context, IRubyObject caller, IRubyObject self, double arg) {
        if (self instanceof RubyFixnum) {
            if (isBuiltin(self.getMetaClass())) return ((RubyFixnum) self).op_minus(context, arg);
        } else if (self instanceof RubyFloat) {
            if (isBuiltin2(self.getMetaClass())) return ((RubyFloat) self).op_minus(context, arg);
        }
        return super.call(context, caller, self, arg);
    }

    @Override
    public IRubyObject call(ThreadContext context, IRubyObject caller, IRubyObject self, IRubyObject arg) {
        if (self instanceof RubyFixnum) {
            if (isBuiltin(self.getMetaClass())) return ((RubyFixnum) self).op_minus(context, arg);
        } else if (self instanceof RubyFloat) {
            if (isBuiltin2(self.getMetaClass())) return ((RubyFloat) self).op_minus(context, arg);
        }
        return super.call(context, caller, self, arg);
    }

}
