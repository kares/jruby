/***** BEGIN LICENSE BLOCK *****
 * Version: EPL 2.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v20.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2006 Michael Studman <me@michaelstudman.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/

package org.jruby;

import org.jruby.anno.JRubyMethod;
import org.jruby.anno.JRubyModule;
import org.jruby.exceptions.RaiseException;
import org.jruby.exceptions.StopIteration;
import org.jruby.exceptions.Unrescuable;
import org.jruby.runtime.Arity;
import org.jruby.runtime.Binding;
import org.jruby.runtime.Block;
import org.jruby.runtime.BlockCallback;
import org.jruby.runtime.CallBlock;
import org.jruby.runtime.Helpers;
import org.jruby.runtime.JavaSites;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.Signature;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ArraySupport;
import org.jruby.util.ByteList;
import org.jruby.util.cli.Options;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Stream;

import static org.jruby.runtime.Visibility.PRIVATE;

/**
 * Implementation of Ruby's Enumerator module.
 */
@JRubyModule(name="Enumerator", include="Enumerable")
public class RubyEnumerator extends RubyObject implements java.util.Iterator<Object> {
    public static final String GENERATOR = "@__generator__";
    public static final String LOOKAHEAD = "@__lookahead__";
    public static final String FEEDVALUE = "@__feedvalue__";
    public static final String OBJECT = "@__object__";
    public static final String METHOD = "@__method__";
    public static final String ARGS = "@__args__";
    /** target for each operation */
    private IRubyObject object;

    /** method to invoke for each operation */
    private String method;

    /** args to each method */
    private IRubyObject[] methodArgs;

    /** A value or proc to provide the size of the Enumerator contents*/
    private IRubyObject size;

    /** Function object for lazily computing size (used for internally created enumerators) */
    private SizeFn sizeFn;

    private IRubyObject feedValue;

    public static void defineEnumerator(Ruby runtime) {
        final RubyModule Enumerable = runtime.getModule("Enumerable");

        final RubyClass Enumerator;
        Enumerator = runtime.defineClass("Enumerator", runtime.getObject(), ALLOCATOR);

        Enumerator.includeModule(Enumerable);
        Enumerator.defineAnnotatedMethods(RubyEnumerator.class);
        runtime.setEnumerator(Enumerator);

        RubyGenerator.createGeneratorClass(runtime);
        RubyYielder.createYielderClass(runtime);
    }

    private static final ObjectAllocator ALLOCATOR = new ObjectAllocator() {
        @Override
        public IRubyObject allocate(Ruby runtime, RubyClass klass) {
            return new RubyEnumerator(runtime, klass);
        }
    };

    private RubyEnumerator(Ruby runtime, RubyClass type) {
        super(runtime, type);
        object = runtime.getNil();
        initialize(runtime, runtime.getNil(), RubyString.newEmptyString(runtime), IRubyObject.NULL_ARRAY);
    }

    private RubyEnumerator(Ruby runtime, RubyClass type, IRubyObject object, IRubyObject method, IRubyObject[] args, IRubyObject size) {
        super(runtime, type);
        initialize(runtime, object, method, args, size, null);
    }

    private RubyEnumerator(Ruby runtime, RubyClass type, IRubyObject object, IRubyObject method, IRubyObject[] args, SizeFn sizeFn) {
        super(runtime, type);
        initialize(runtime, object, method, args, null, sizeFn);
    }

    private RubyEnumerator(Ruby runtime, RubyClass type, IRubyObject object, IRubyObject method, IRubyObject[] args) {
        super(runtime, type);
        initialize(runtime, object, method, args);
    }

    /**
     * Transform object into an Enumerator with the given size
     */
    public static IRubyObject enumeratorizeWithSize(ThreadContext context, final IRubyObject object, String method, IRubyObject[] args, SizeFn sizeFn) {
        Ruby runtime = context.runtime;
        return new RubyEnumerator(runtime, runtime.getEnumerator(), object, runtime.fastNewSymbol(method), args, sizeFn);
    }

    public static IRubyObject enumeratorizeWithSize(ThreadContext context, IRubyObject object, String method, SizeFn sizeFn) {
        return enumeratorizeWithSize(context, object, method, NULL_ARRAY, sizeFn);
    }

    public static IRubyObject enumeratorizeWithSize(ThreadContext context, IRubyObject object, String method,IRubyObject arg, IRubyObject size) {
        Ruby runtime = context.runtime;
        return new RubyEnumerator(runtime, runtime.getEnumerator(), object, runtime.fastNewSymbol(method), new IRubyObject[] { arg }, size);
    }

    public static IRubyObject enumeratorize(Ruby runtime, IRubyObject object, String method) {
        return new RubyEnumerator(runtime, runtime.getEnumerator(), object, runtime.fastNewSymbol(method), IRubyObject.NULL_ARRAY);
    }

    public static IRubyObject enumeratorize(Ruby runtime, IRubyObject object, String method, IRubyObject arg) {
        return new RubyEnumerator(runtime, runtime.getEnumerator(), object, runtime.fastNewSymbol(method), new IRubyObject[] {arg});
    }

    public static IRubyObject enumeratorize(Ruby runtime, IRubyObject object, String method, IRubyObject... args) {
        return new RubyEnumerator(runtime, runtime.getEnumerator(), object, runtime.fastNewSymbol(method), args); // TODO: make sure it's really safe to not to copy it
    }

    public static IRubyObject enumeratorize(Ruby runtime, RubyClass type, IRubyObject object, String method) {
        return new RubyEnumerator(runtime, type, object, runtime.fastNewSymbol(method), IRubyObject.NULL_ARRAY);
    }

    public static IRubyObject enumeratorize(Ruby runtime, RubyClass type, IRubyObject object, String method, IRubyObject arg) {
        return new RubyEnumerator(runtime, type, object, runtime.fastNewSymbol(method), new IRubyObject[] {arg});
    }

    public static IRubyObject enumeratorize(Ruby runtime, RubyClass type, IRubyObject object, String method, IRubyObject[] args) {
        return new RubyEnumerator(runtime, type, object, runtime.fastNewSymbol(method), args); // TODO: make sure it's really safe to not to copy it
    }

    @Override
    public IRubyObject initialize(ThreadContext context) {
        return initialize(context, Block.NULL_BLOCK);
    }

    @JRubyMethod(name = "initialize", visibility = PRIVATE)
    public IRubyObject initialize(ThreadContext context, Block block) {
        return initialize(context, NULL_ARRAY, block);
    }

    @Deprecated
    public IRubyObject initialize19(ThreadContext context, Block block) {
        return initialize(context, block);
    }

    @Deprecated
    public IRubyObject initialize20(ThreadContext context, Block block) {
        return initialize(context, block);
    }

    @JRubyMethod(name = "initialize", visibility = PRIVATE)
    public IRubyObject initialize(ThreadContext context, IRubyObject object, Block block) {
        return initialize(context, new IRubyObject[]{ object }, block);
    }

    @Deprecated
    public IRubyObject initialize20(ThreadContext context, IRubyObject object, Block block) {
        return initialize(context, object, block);
    }

    @JRubyMethod(name = "initialize", visibility = PRIVATE, rest = true)
    public IRubyObject initialize(ThreadContext context, IRubyObject[] args, Block block) {
        Ruby runtime = context.runtime;
        IRubyObject object;
        IRubyObject method = runtime.newSymbol("each");
        IRubyObject size = null;

        if (block.isGiven()) {
            Arity.checkArgumentCount(runtime, args, 0, 1);
            if (args.length > 0) {
                size = args[0];
                args = Arrays.copyOfRange(args, 1, args.length);

                if ( ! (size.isNil() || size.respondsTo("call")) &&
                     ! (size instanceof RubyFloat && ((RubyFloat) size).value == Float.POSITIVE_INFINITY) &&
                     ! (size instanceof RubyInteger) ) {
                    throw runtime.newTypeError(size, runtime.getInteger());
                }
            }
            object = runtime.getGenerator().newInstance(context, IRubyObject.NULL_ARRAY, block);

        } else {
            Arity.checkArgumentCount(runtime, args, 1, -1);
            // TODO need a deprecation WARN here, but can't add it until ruby/jruby/kernel20/enumerable.rb is deleted or stops calling this without a block
            object = args[0];
            args = Arrays.copyOfRange(args, 1, args.length);
            if (args.length > 0) {
                method = args[0];
                args = Arrays.copyOfRange(args, 1, args.length);
            }
        }

        return initialize(runtime, object, method, args, size, null);
    }

    @Deprecated
    public IRubyObject initialize20(ThreadContext context, IRubyObject[] args, Block block) {
        return initialize(context, args, block);
    }

    @JRubyMethod(name = "initialize", visibility = PRIVATE)
    public IRubyObject initialize(ThreadContext context, IRubyObject object, IRubyObject method, Block block) {
        if (block.isGiven()) {
            throw context.runtime.newArgumentError(2, 1);
        }
        return initialize(context.runtime, object, method, NULL_ARRAY);
    }

    public IRubyObject initialize(ThreadContext context, IRubyObject object, IRubyObject method) {
        return initialize(context, object, method, Block.NULL_BLOCK);
    }

    @Deprecated
    public IRubyObject initialize19(ThreadContext context, IRubyObject object, IRubyObject method, Block block) {
        return initialize(context, object, method, block);
    }

    @Deprecated
    public IRubyObject initialize20(ThreadContext context, IRubyObject object, IRubyObject method, Block block) {
        return initialize(context, object, method, block);
    }

    @JRubyMethod(name = "initialize", visibility = PRIVATE)
    public IRubyObject initialize(ThreadContext context, IRubyObject object, IRubyObject method, IRubyObject methodArg, Block block) {
        if (block.isGiven()) {
            throw context.runtime.newArgumentError(3, 1);
        }
        return initialize(context.runtime, object, method, new IRubyObject[] { methodArg });
    }

    public IRubyObject initialize(ThreadContext context, IRubyObject object, IRubyObject method, IRubyObject methodArg) {
        return initialize(context, object, method, methodArg, Block.NULL_BLOCK);
    }

    @Deprecated
    public IRubyObject initialize19(ThreadContext context, IRubyObject object, IRubyObject method, IRubyObject methodArg, Block block) {
        return initialize(context, object, method, methodArg, Block.NULL_BLOCK);
    }

    @Deprecated
    public IRubyObject initialize20(ThreadContext context, IRubyObject object, IRubyObject method, IRubyObject methodArg, Block block) {
        return initialize(context, object, method, methodArg, block);
    }

    public IRubyObject initialize(ThreadContext context, IRubyObject[] args) {
        return initialize(context, args, Block.NULL_BLOCK);
    }

    @Deprecated
    public IRubyObject initialize19(ThreadContext context, IRubyObject[] args, Block block) {
        return initialize(context, args, block);
    }

    private IRubyObject initialize(Ruby runtime, IRubyObject object, IRubyObject method, IRubyObject[] methodArgs) {
        return initialize(runtime, object, method, methodArgs, null, null);
    }

    private IRubyObject initialize(Ruby runtime, IRubyObject object, IRubyObject method, IRubyObject[] methodArgs, IRubyObject size, SizeFn sizeFn) {
        this.object = object;
        this.method = method.asJavaString();
        this.methodArgs = methodArgs;
        this.size = size;
        this.sizeFn = sizeFn;
        this.feedValue = runtime.getNil();
        setInstanceVariable(OBJECT, object);
        setInstanceVariable(METHOD, method);
        setInstanceVariable(ARGS, RubyArray.newArrayMayCopy(runtime, methodArgs));
        setInstanceVariable(GENERATOR, runtime.getNil());
        setInstanceVariable(LOOKAHEAD, RubyArray.newArray(runtime));
        setInstanceVariable(FEEDVALUE, runtime.getNil());

        return this;
    }

    @JRubyMethod(name = "dup")
    @Override
    public IRubyObject dup() {
        // JRUBY-5013: Enumerator needs to copy private fields in order to have a valid structure
        RubyEnumerator copy = (RubyEnumerator) super.dup();
        copy.object     = this.object;
        copy.method     = this.method;
        copy.methodArgs = this.methodArgs;
        copy.size       = this.size;
        copy.sizeFn     = this.sizeFn;
        copy.feedValue  = getRuntime().getNil();
        return copy;
    }

    /**
     * Send current block and supplied args to method on target. According to MRI
     * Block may not be given and "each" should just ignore it and call on through to
     * underlying method.
     */
    @JRubyMethod
    public IRubyObject each(ThreadContext context, Block block) {
        if (!block.isGiven()) {
            return this;
        }

        return object.callMethod(context, method, methodArgs, block);
    }

    @JRubyMethod(rest = true)
    public IRubyObject each(ThreadContext context, IRubyObject[] args, Block block) {
        if (args.length == 0) {
            return each(context, block);
        }

        final int mlen = methodArgs.length;
        IRubyObject[] newArgs = new IRubyObject[mlen + args.length];
        ArraySupport.copy(methodArgs, newArgs, 0, mlen);
        ArraySupport.copy(args, newArgs, mlen, args.length);

        return new RubyEnumerator(context.runtime, getType(), object, context.runtime.newSymbol("each"), newArgs);
    }

    @JRubyMethod(name = "inspect")
    public IRubyObject inspect19(ThreadContext context) {
        Ruby runtime = context.runtime;
        if (runtime.isInspecting(this)) return inspect(context, true);

        try {
            runtime.registerInspecting(this);
            return inspect(context, false);
        } finally {
            runtime.unregisterInspecting(this);
        }
    }

    private IRubyObject inspect(ThreadContext context, boolean recurse) {
        Ruby runtime = context.runtime;
        ByteList bytes = new ByteList();
        bytes.append((byte)'#').append((byte)'<');
        bytes.append(getMetaClass().getName().getBytes());
        bytes.append((byte)':').append((byte)' ');

        if (recurse) {
            bytes.append("...>".getBytes());
            return RubyString.newStringNoCopy(runtime, bytes).taint(context);
        } else {
            boolean tainted = isTaint();
            bytes.append(RubyObject.inspect(context, object).getByteList());
            bytes.append((byte)':');
            bytes.append(method.getBytes());
            if (methodArgs.length > 0) {
                bytes.append((byte)'(');
                for (int i= 0; i < methodArgs.length; i++) {
                    bytes.append(RubyObject.inspect(context, methodArgs[i]).getByteList());
                    if (i < methodArgs.length - 1) {
                        bytes.append((byte)',').append((byte)' ');
                    } else {
                        bytes.append((byte)')');
                    }
                    if (methodArgs[i].isTaint()) tainted = true;
                }
            }
            bytes.append((byte)'>');
            RubyString result = RubyString.newStringNoCopy(runtime, bytes);
            if (tainted) result.setTaint(true);
            return result;
        }
    }

    protected static IRubyObject newEnumerator(ThreadContext context, IRubyObject arg) {
        Ruby runtime = context.runtime;
        return new RubyEnumerator(runtime, runtime.getEnumerator(), arg, runtime.newSymbol("each"), IRubyObject.NULL_ARRAY);
    }

    protected static IRubyObject newEnumerator(ThreadContext context, IRubyObject arg1, IRubyObject arg2) {
        Ruby runtime = context.runtime;
        return new RubyEnumerator(runtime, runtime.getEnumerator(), arg1, arg2, IRubyObject.NULL_ARRAY);
    }

    protected static IRubyObject newEnumerator(ThreadContext context, IRubyObject arg1, IRubyObject arg2, IRubyObject arg3) {
        Ruby runtime = context.runtime;
        return new RubyEnumerator(runtime, runtime.getEnumerator(), arg1, arg2, new IRubyObject[]{arg3});
    }

    @JRubyMethod(required = 1)
    public IRubyObject each_with_object(final ThreadContext context, IRubyObject arg, Block block) {
        return block.isGiven() ? RubyEnumerable.each_with_objectCommon(context, this, block, arg) :
                enumeratorizeWithSize(context, this, "each_with_object", new IRubyObject[]{arg}, enumSizeFn(context));
    }

    @JRubyMethod
    public IRubyObject with_object(ThreadContext context, final IRubyObject arg, final Block block) {
        return block.isGiven() ? RubyEnumerable.each_with_objectCommon(context, this, block, arg) : enumeratorizeWithSize(context, this, "with_object", new IRubyObject[]{arg}, enumSizeFn(context));
    }

    @JRubyMethod(rest = true)
    public IRubyObject each_entry(ThreadContext context, final IRubyObject[] args, final Block block) {
        return block.isGiven() ? RubyEnumerable.each_entryCommon(context, this, args, block) : enumeratorize(context.runtime, getType(), this, "each_entry", args);
    }

    @Deprecated
    public IRubyObject each_slice19(ThreadContext context, IRubyObject arg, final Block block) {
        return each_slice(context, arg, block);
    }

    @JRubyMethod(name = "each_slice")
    public IRubyObject each_slice(ThreadContext context, IRubyObject arg, final Block block) {
        int size = (int) RubyNumeric.num2long(arg);
        if (size <= 0) throw context.runtime.newArgumentError("invalid size");

        return block.isGiven() ? RubyEnumerable.each_sliceCommon(context, this, size, block) : enumeratorize(context.runtime, getType(), this, "each_slice", arg);
    }

    @Deprecated
    public IRubyObject each_cons19(ThreadContext context, IRubyObject arg, final Block block) {
        return each_cons(context, arg, block);
    }

    @JRubyMethod(name = "each_cons")
    public IRubyObject each_cons(ThreadContext context, IRubyObject arg, final Block block) {
        int size = (int) RubyNumeric.num2long(arg);
        if (size <= 0) throw context.runtime.newArgumentError("invalid size");
        return block.isGiven() ? RubyEnumerable.each_consCommon(context, this, size, block) : enumeratorize(context.runtime, getType(), this, "each_cons", arg);
    }

    @JRubyMethod
    public final IRubyObject size(ThreadContext context) {
        if (sizeFn != null) {
            return sizeFn.size(methodArgs);
        }

        IRubyObject size = this.size;
        if (size != null) {
            if (size.respondsTo("call")) {
                if (context == null) context = getRuntime().getCurrentContext();
                return size.callMethod(context, "call");
            }

            return size;
        }

        if (context == null) context = getRuntime().getCurrentContext();

        return context.nil;
    }

    public long size() {
        final IRubyObject size = size(null);
        if ( size instanceof RubyNumeric ) {
            return ((RubyNumeric) size).getLongValue();
        }
        return -1;
    }

    private SizeFn enumSizeFn(final ThreadContext context) {
        final RubyEnumerator self = this;
        return new SizeFn() {
            @Override
            public IRubyObject size(IRubyObject[] args) {
                return self.size(context);
            }
        };
    }

    private IRubyObject with_index_common(ThreadContext context, final Block block, final String rubyMethodName, IRubyObject arg) {
        final Ruby runtime = context.runtime;
        final int index = arg.isNil() ? 0 : RubyNumeric.num2int(arg);
        if ( ! block.isGiven() ) {
            return arg.isNil() ?
                    enumeratorizeWithSize(context, this, rubyMethodName, enumSizeFn(context)) :
                        enumeratorizeWithSize(context, this, rubyMethodName, new IRubyObject[]{runtime.newFixnum(index)}, enumSizeFn(context));
        }

        return RubyEnumerable.callEach(runtime, context, this, new RubyEnumerable.EachWithIndex(block, index));
    }

    @JRubyMethod
    public IRubyObject each_with_index(ThreadContext context, final Block block) {
        return with_index_common(context, block, "each_with_index", context.nil);
    }

    @JRubyMethod(name = "with_index")
    public IRubyObject with_index(ThreadContext context, final Block block) {
        return with_index_common(context, block, "with_index", context.nil);
    }

    @Deprecated
    public IRubyObject with_index19(ThreadContext context, final Block block) {
        return with_index(context, block);
    }

    @JRubyMethod(name = "with_index")
    public IRubyObject with_index(ThreadContext context, IRubyObject arg, final Block block) {
        return with_index_common(context, block, "with_index", arg);
    }

    @Deprecated
    public IRubyObject with_index19(ThreadContext context, IRubyObject arg, final Block block) {
        return with_index(context, arg, block);
    }

    // java.util.Iterator :

    @Override
    public synchronized boolean hasNext() {
        ThreadContext context = getRuntime().getCurrentContext();
        try {
            // We don't care about the result, just whether it succeeds.
            sites(context).peek.call(context, this, this);
            return true;
        } catch (StopIteration si) {
            return false;
        }
    }

    private IRubyObject getGenerator() {
        return getInstanceVariable(GENERATOR);
    }

    @Override
    public Object next() {
        ThreadContext context = getRuntime().getCurrentContext();
        return sites(context).next.call(context, this, this).toJava(java.lang.Object.class);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    // Java 8 stream support :

    public Stream<Object> stream() {
        return stream(false);
    }

    public Stream<Object> stream(final boolean parallel) {
        return java.util.stream.StreamSupport.stream(spliterator(), parallel);
    }

    public Spliterator<Object> spliterator() {
        final long size = size();
        // we do not have Array detection - assume immutable
        int mod = java.util.Spliterator.IMMUTABLE;
        if (size >= 0) mod |= java.util.Spliterator.SIZED;
        return java.util.Spliterators.spliterator(this, size, mod);
    }

    public Spliterator<Object> spliterator(final int mod) {
        return java.util.Spliterators.spliterator(this, size(), mod);
    }

    /**
     * "Function" type for java-created enumerators with size.  Should be implemented so that calls to
     * SizeFn#size are kept in sync with the size of the created enum (i.e. if the object underlying an enumerator
     * changes, calls to SizeFn#size should reflect that change).
     *
     * TODO (CON): fix this to receive context and state to we're not reallocating it all the time
     */
    public interface SizeFn {
        IRubyObject size(IRubyObject[] args);
    }

    private static JavaSites.FiberSites sites(ThreadContext context) {
        return context.sites.Fiber;
    }
}
