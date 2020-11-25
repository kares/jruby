package org.jruby.java.proxies;

import static org.jruby.runtime.Visibility.PUBLIC;

import java.lang.reflect.*;
import java.util.ArrayList;

import org.jruby.*;
import org.jruby.ast.*;
import org.jruby.exceptions.ArgumentError;
import org.jruby.internal.runtime.AbstractIRMethod;
import org.jruby.internal.runtime.methods.*;
import org.jruby.internal.runtime.methods.JavaMethod.*;
import org.jruby.ir.IRMethod;
import org.jruby.java.dispatch.CallableSelector;
import org.jruby.java.dispatch.CallableSelector.CallableCache;
import org.jruby.javasupport.*;
import org.jruby.javasupport.Java.JCtorCache;
import org.jruby.javasupport.proxy.*;
import org.jruby.runtime.*;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ByteList;
import org.jruby.util.collections.NonBlockingHashMapLong;

public class ConcreteJavaProxy extends JavaProxy {

    public ConcreteJavaProxy(Ruby runtime, RubyClass klazz) {
        super(runtime, klazz);
    }

    public ConcreteJavaProxy(Ruby runtime, RubyClass klazz, Object object) {
        super(runtime, klazz, object);
    }

    public static final ObjectAllocator ALLOCATOR = new ObjectAllocator() {
        public IRubyObject allocate(Ruby runtime, RubyClass klazz) {
            return new ConcreteJavaProxy(runtime, klazz);
        }
    };

    public static RubyClass createConcreteJavaProxy(final ThreadContext context) {
        final Ruby runtime = context.runtime;
        final RubyClass JavaProxy = runtime.getJavaSupport().getJavaProxyClass();
        RubyClass ConcreteJavaProxy = runtime.defineClass("ConcreteJavaProxy", JavaProxy, ALLOCATOR);
        initialize(ConcreteJavaProxy);
        return ConcreteJavaProxy;
    }

    ///jcreates site
    private static final class InitializeMethod extends org.jruby.internal.runtime.methods.JavaMethod {

        private final CallSite jcreateSite = MethodIndex.getFunctionalCallSite("__jcreate!");

        InitializeMethod(final RubyClass clazz) { super(clazz, Visibility.PRIVATE, "initialize"); }

        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
            return jcreateSite.call(context, self, self, args, block);
        }
        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, Block block) {
            return jcreateSite.call(context, self, self, block);
        }
        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject arg0, Block block) {
            return jcreateSite.call(context, self, self, arg0, block);
        }
        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject arg0, IRubyObject arg1, Block block) {
            return jcreateSite.call(context, self, self, arg0, arg1, block);
        }
        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject arg0, IRubyObject arg1, IRubyObject arg2, Block block) {
            return jcreateSite.call(context, self, self, arg0, arg1, arg2, block);
        }
        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args) {
            return jcreateSite.call(context, self, self, args);
        }
        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name) {
            return jcreateSite.call(context, self, self);
        }
        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject arg0) {
            return jcreateSite.call(context, self, self, arg0);
        }
        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject arg0, IRubyObject arg1) {
            return jcreateSite.call(context, self, self, arg0, arg1);
        }
        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject arg0, IRubyObject arg1, IRubyObject arg2) {
            return jcreateSite.call(context, self, self, arg0, arg1, arg2);
        }

    }
//new override
    private static final class NewMethod extends org.jruby.internal.runtime.methods.JavaMethod {
    	final DynamicMethod newMethod;

        NewMethod(final RubyClass clazz) {
            super(clazz, Visibility.PUBLIC, "new");
            newMethod = clazz.searchMethod("new");
        }
// TODO: reload this on method changes?
        private DynamicMethod reifyAndNewMethod(IRubyObject clazz) { 

        	RubyClass parent = ((RubyClass)clazz);
        	System.err.println(parent.getName() + " is, (from NewMethod, original, a proxy) " + parent.getJavaProxy());// TODO: remove
        	if (parent.getJavaProxy()) return newMethod;
        	
        	// overridden class: reify and re-lookup new as reification changes it
            if (parent.getReifiedAnyClass() == null) {
            	parent.reifyWithAncestors(); // TODO: is this good?
            }
            //System.err.println(parent.getName() + " is " + parent.getJavaProxy());
            return new NewMethodReified(parent, parent.getReifiedJavaClass());
        }

        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args, Block block) {
            return reifyAndNewMethod(self).call(context, self, clazz, "new_proxy", args, block);
        }

        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, Block block) {
        	return reifyAndNewMethod(self).call(context, self, clazz, "new",block);
        }

        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject arg0, Block block) {
        	return reifyAndNewMethod(self).call(context, self, clazz, "new",arg0, block);
        }

        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject arg0, IRubyObject arg1, Block block) {
        	return reifyAndNewMethod(self).call(context, self, clazz, "new",arg0, arg1, block);
        }

        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject arg0, IRubyObject arg1, IRubyObject arg2, Block block) {
        	return reifyAndNewMethod(self).call(context, self, clazz, "new",arg0, arg1, arg2, block);
        }

        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject[] args) {
        	return reifyAndNewMethod(self).call(context, self, clazz, "new",args);
        }

        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name) {
        	return reifyAndNewMethod(self).call(context, self, clazz,"new_proxy");
        }

        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject arg0) {
        	return reifyAndNewMethod(self).call(context, self, clazz, "new_proxy",arg0);
        }

        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject arg0, IRubyObject arg1) {
        	return reifyAndNewMethod(self).call(context, self, clazz,"new", arg0, arg1);
        }

        @Override
        public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name, IRubyObject arg0, IRubyObject arg1, IRubyObject arg2) {
        	return reifyAndNewMethod(self).call(context, self, clazz,"new", arg0, arg1, arg2);
        }

    }
    
    public static class StaticJCreateMethod extends JavaMethodNBlock {

		private Constructor<? extends ReifiedJavaProxy> withBlock;
		private DynamicMethod oldInit;

        StaticJCreateMethod(RubyModule cls, Constructor<? extends ReifiedJavaProxy> withBlock2, DynamicMethod oldinit) {
            super(cls, PUBLIC, "__jcreate_static!");
            this.withBlock = withBlock2;
            this.oldInit = oldinit;
        }

		@Override
		public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name,
				IRubyObject[] args, Block block)
		{
			try
			{
				withBlock.newInstance((ConcreteJavaProxy)self, args, block, context.runtime, clazz);
				// note: the generated ctor sets self.object = our discarded return of the new object
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return self;
		}
        
		
        public DynamicMethod getOriginal()
        {
        	return oldInit;
        }
        
        

		public static void tryInstall(Ruby runtime, RubyClass clazz, JavaProxyClass proxyClass,
				Class<? extends ReifiedJavaProxy> reified)
		{
			try
			{
				Constructor<? extends ReifiedJavaProxy> withBlock = reified.getConstructor(
							new Class[] { ConcreteJavaProxy.class, IRubyObject[].class, Block.class,
										Ruby.class, RubyClass.class});
				//TODO: move initialize to real_initialize
				//TODO: don't lock in this initialize method
		        clazz.addMethod("initialize", new StaticJCreateMethod(clazz, withBlock, clazz.searchMethod("initialize")));
			}
			catch (SecurityException | NoSuchMethodException e)
			{
				// TODO log?
				e.printStackTrace();
				// ignore, don't install
			}
		}
    }

  //TODO: cleanup
      public static final class NewMethodReified extends org.jruby.internal.runtime.methods.JavaMethod.JavaMethodNBlock {

          private final DynamicMethod initialize;
          private final Constructor<? extends ReifiedJavaProxy> ctor;

          //TODO: package?
          public NewMethodReified(final RubyClass clazz, Class<? extends ReifiedJavaProxy> reified) {
              super(clazz, Visibility.PUBLIC, "new");
              initialize = clazz.searchMethod("__jcreate!");

              Constructor<? extends ReifiedJavaProxy> withBlock;
              try
	  			{
	  				withBlock = reified.getConstructor(
	  							new Class[] { ConcreteJavaProxy.class, IRubyObject[].class, Block.class,
	  										Ruby.class, RubyClass.class});
	  			}
	  			catch (SecurityException | NoSuchMethodException e)
	  			{
	  				// ignore, don't install
	  				withBlock = null;
	  			}
              ctor = withBlock;
          }

  		@Override
  		public IRubyObject call(ThreadContext context, IRubyObject self, RubyModule clazz, String name,
  				IRubyObject[] args, Block blk)
  		{
			//TODO: deduplicate this method, and decide on order of preference after testing 
  			if (ctor == null)
  			{
	  			JavaObject jo = (JavaObject)initialize.call(context, self, clazz, "new", args);
	  			return ((ReifiedJavaProxy)jo.getValue()).___jruby$rubyObject();
  			}
  			else
  			{
  				try
  				{

  		  			JavaObject jo = (JavaObject)initialize.call(context, self, clazz, "new", args);
  		  			return ((ReifiedJavaProxy)jo.getValue()).___jruby$rubyObject();
  				}
  				catch (ArgumentError ae)
  				{
  					System.out.println("AE");
  					// assume no easy conversions, use ruby fallback.
	  				ConcreteJavaProxy object = new ConcreteJavaProxy(context.runtime, (RubyClass) self);
	  				try
	  				{
	  					ctor.newInstance(object, args, blk, context.runtime, clazz);
	  					// note: the generated ctor sets self.object = our discarded return of the new object
	  				}
	  				catch (InstantiationException | IllegalAccessException | IllegalArgumentException
	  						| InvocationTargetException e)
	  				{
	  					e.printStackTrace();
	  					JavaProxyConstructor.mapInstantiationException(context.runtime, e);
	  				}
	  				return object;
  				}
  			}
  		}

      }
      

    //TODO: cleanup
    public static final class SimpleJavaInitializes {

        public static RubyArray freshMethodArray(DynamicMethod initialize, Ruby runtime, IRubyObject self, RubyModule clazz, String name,
				IRubyObject[] args)
        {

			return runtime.newArray(
					runtime.newArray(args), //TODO:? 
		            runtime.newProc(Block.Type.LAMBDA, new Block(new JavaInternalBlockBody(runtime, Signature.from(initialize.getArity()))
					{
						
						@Override
						public IRubyObject yield(ThreadContext _context, IRubyObject[] _args)
						{
							return initialize.call(_context, self, clazz, name, args);
						}
						
					}))
		           );
        }
        
        public static RubyArray freshNopArray(Ruby runtime, IRubyObject[] args)
        {

			return runtime.newArray(
					runtime.newArray(args), 
		            runtime.newProc(Block.Type.LAMBDA, new Block(new JavaInternalBlockBody(runtime, Signature.OPTIONAL)
					{
						@Override
						public IRubyObject yield(ThreadContext _context, IRubyObject[] _args)
						{
							return _context.nil; // no body/super is java
						}
						
					}))
		           );
        }

    }
    
    public static int findSuperLine(Ruby runtime, DynamicMethod dm, int start)
    {
    	try
    	{
		if (dm != null && !(dm instanceof InitializeMethod))
		{
            //TODO: if not defined, then ctors = all valid superctors
			DefNode def = ((IRMethod)((AbstractIRMethod)dm).getIRScope()).desugar();
			System.out.println("def is " + (def == null));
			FlatExtractor flat = new FlatExtractor(runtime, def); 
			System.out.println("defb is " + (def.getBodyNode() == null));
			Node body = def.getBodyNode().accept(flat);
			if (flat.foundsuper && flat.superline > -1)
				return flat.superline + 1; // convert from 0-based to 1-based
		}
    	}
		catch(Exception e)
    	{
			e.printStackTrace();
    	}
		return start;
    }
    
    // used by reified classes
    public RubyArray splitInitialized(IRubyObject[] args, Block blk)
    {

		DynamicMethod dm = this.getMetaClass().searchMethod("j_initialize");
		DynamicMethod dma = this.getMetaClass().searchMethod("j_initialize");
		if (!(dm instanceof AbstractIRMethod))
		{
			dm = getMetaClass().searchMethod("initialize");
			if (dm != null && (dm instanceof StaticJCreateMethod))
				dm = ((StaticJCreateMethod)dm).getOriginal();
			DynamicMethod dm1 = getMetaClass().retrieveMethod("initialize"); // only on ourself
			if ((dm1 != null && !(dm instanceof InitializeMethod)&& !(dm instanceof StaticJCreateMethod))) //jcreate is for nested ruby classes from a java class
			{
	            //TODO: if not defined, then ctors = all valid superctors
			DefNode def = ((IRMethod)((AbstractIRMethod)dm).getIRScope()).desugar();
			FlatExtractor flat = new FlatExtractor(this.getRuntime(), def); 
			Node body = def.getBodyNode().accept(flat);
			if (!flat.foundsuper)
			{
				System.err.println("NO SUPER");
				body = flat.buildRewrite(def.getBodyNode().getLine(), new NilNode(def.getBodyNode().getLine()), def.getBodyNode());
			}
			if (flat.error)
				System.err.println("error");
			System.err.println(def.toString());
			DefNode rdnbody = new DefnNode(def.getBodyNode().getLine(), 
					RubySymbol.newSymbol(this.getRuntime(),"j_initialize"), 
					def.getArgsNode(), 
					def.getScope(), 
					body, 
					def.getEndLine());
			System.err.println(rdnbody.toString());
		
		
			IRMethod irm = ((IRMethod)((AbstractIRMethod)dm).getIRScope());
			irm.builtInterpreterContext();

			irm = new IRMethod(irm.getManager(), irm.getLexicalParent(), rdnbody, 
					new ByteList("j_initialize".getBytes(), getRuntime().getEncodingService().getJavaDefault()), true, 
					irm.getLine(), irm.getStaticScope(), irm.getCoverageMode());
			dm1 = dm;
			dm  = new MixedModeIRMethod(irm, Visibility.PUBLIC, this.getMetaClass());

			//irm.builtInterpreterContext();
//			/System.out.println(irm.getLexicalScopes().get(0).getInterpreterContext());
//			((IRMethod)((AbstractIRMethod)dma).getIRScope()).builtInterpreterContext();

			this.getMetaClass().addMethod("j_initialize", dm);
			}
			else
			{
				//TODO: pass ruby into this
				if (dm instanceof InitializeMethod)
					return SimpleJavaInitializes.freshNopArray(this.getRuntime(), args);
				else 
					return SimpleJavaInitializes.freshMethodArray(dm, this.getRuntime(), this, getMetaClass(), "initialize", args);
			}
		
		}
    	///  TODO: move gen here
    	return callMethod(getRuntime().getCurrentContext(),  "j_initialize", args, blk).convertToArray();
    }
    
    // used by reified classes
    public void ensureThis(Object self)
    {
    	if (getObject() == null)
    		setObject(self);
    }

    protected static void initialize(final RubyClass concreteJavaProxy) {
        concreteJavaProxy.addMethod("initialize", new InitializeMethod(concreteJavaProxy));
        if (concreteJavaProxy.getName().equals("ConcreteJavaProxy"))
        {}
        else if (concreteJavaProxy.getName().equals("MapJavaProxy"))
        {}
        else
        System.err.println("adding to " + concreteJavaProxy.getName()); //TODO: remove
        // We define a custom "new" method to ensure that __jcreate! is getting called,
        // so that if the user doesn't call super in their subclasses, the object will
        // still get set up properly. See JRUBY-4704.
        RubyClass singleton = concreteJavaProxy.getSingletonClass();
        singleton.addMethod("new", new NewMethod(singleton));
    }

    // This alternate ivar logic is disabled because it can cause self-referencing
    // chains to keep the original object alive. See JRUBY-4832.
//    @Override
//    public Object getVariable(int index) {
//        return getRuntime().getJavaSupport().getJavaObjectVariable(this, index);
//    }
//
//    @Override
//    public void setVariable(int index, Object value) {
//        getRuntime().getJavaSupport().setJavaObjectVariable(this, index, value);
//    }

    /**
     * Because we can't physically associate an ID with a Java object, we can
     * only use the identity hashcode here.
     *
     * @return The identity hashcode for the Java object.
     */
    @Override
    public IRubyObject id() {
        return getRuntime().newFixnum(System.identityHashCode(getObject()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> type) {
        final Object object = getObject();
        if (object == null)
        {
        	System.out.println(":-(");
        	return null;
        }
        final Class clazz = object.getClass();

        if ( type.isPrimitive() ) {
            if ( type == Void.TYPE ) return null;

            if ( object instanceof Number && type != Boolean.TYPE ||
                 object instanceof Character && type == Character.TYPE ||
                 object instanceof Boolean && type == Boolean.TYPE ) {
                // FIXME in more permissive call paths, like invokedynamic, this can allow
                // precision-loading downcasts to happen silently
                return (T) object;
            }
        }
        else if ( type.isAssignableFrom(clazz) ) {
            if ( Java.OBJECT_PROXY_CACHE || metaClass.getCacheProxy() ) {
                getRuntime().getJavaSupport().getObjectProxyCache().put(object, this);
            }
            return type.cast(object);
        }
        else if ( type.isAssignableFrom(getClass()) ) return type.cast(this); // e.g. IRubyObject.class

        throw getRuntime().newTypeError("failed to coerce " + clazz.getName() + " to " + type.getName());
    }
}
