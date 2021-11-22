package game.view.boilerplate;

import java.util.function.Supplier;

/**
a Shader is a callback that can draw things on a per-pixel basis using multiple threads.
these are not GLSL or HLSL shaders, unfortunately. they're pure java.
Shaders are run in parallel over an area (currently a
{@link BufferedCanvas#runShaderSquare square}, {@link BufferedCanvas#runShaderCircle circle},
or {@link BufferedCanvas#runShaderEllipse ellipse}),
the Shader will receive a {@link Shader.Context} object which contains
information about where the Shader should draw things in this invocation.

technical details:
when one of {@link BufferedCanvas}'s runShader() methods are called,
the pixels in that area are first split up into regions of equal area.
every region is drawn by a different thread, and every thread
will construct a scope-local {@link Shader.Context} object.
this means the context is also thread-local,
and it will be re-used for all the pixels in the region.

the Shader will have its {@link Shader#run} method called for all pixels in the region,
with the thread/scope-local context object as its argument.
the Shader can modify the {@link Context#buffer} in any way it wants,
so long as it only modifies the pixel at the Context's {@link Context#x}
and {@link Context#y} position (or its {@link Context#baseOffset} value).
the Shader does not need to perform any synchronization or locking operations
when modifying the {@link Context#buffer} at these coordinates.
however, undefined behavior (including possible race conditions) can occur if
the Shader attempts to modify the {@link Context#buffer} at any other location.

if the Shader needs to construct additional objects to run,
it is recommended to store these objects in the Context's {@link Context#perThreadStorage}
so that these objects can be re-used for all pixels in the region
without needing to be re-allocated more times than necessary.
if the Shader needs more than one extra object, consider adding a holder class for them,
and then storing the holder in the Context's {@link Context#perThreadStorage}.

@author Michael Johnston (tky886)
*/
public interface Shader {

	public abstract void run(Shader.Context context);

	public class Context {

		public final PixelBuffer buffer;

		public int x, y;
		/** the {@link PixelBuffer#baseOffset} of our {@link #x} and {@link #y} position. */
		public int baseOffset;
		/**
		null by default, but can be initialized by the shader itself.
		@see #getPerThreadStorage(Supplier)
		*/
		public Object perThreadStorage;

		public Context(PixelBuffer buffer) {
			this.buffer = buffer;
		}

		public void startRow(int x, int y) {
			this.x = x;
			this.y = y;
			this.baseOffset = this.buffer.baseOffset(x, y);
		}

		public void moveRight() {
			this.x++;
			this.baseOffset += PixelBuffer.BYTES_PER_PIXEL;
		}

		/**
		gets, but does not create, this Context's {@link #perThreadStorage}.
		see the implementation notes of {@link #getPerThreadStorage(Supplier)}
		for why you might want to use this method instead.
		*/
		@SuppressWarnings("unchecked")
		public <T> T getPerThreadStorage() {
			return (T)(this.perThreadStorage);
		}

		/**
		sets this Context's {@link #perThreadStorage}, and returns it for convenience.
		see the implementation notes of {@link #getPerThreadStorage(Supplier)}
		for why you might want to use this method instead.
		*/
		public <T> T setPerThreadStorage(T object) {
			this.perThreadStorage = object;
			return object;
		}

		/**
		gets or creates this Context's {@link #perThreadStorage} if it is currently null.
		example usage:
		Foo variable = context.getPerThreadStorage(Foo::new);

		@implNote
		if the defaultValue is a lambda expression,
		then it should not capture any local variables.
		if the lambda has captures, then a new lambda object will be
		constructed every time getPerThreadStorage() is invoked
		(see {@link java.lang.invoke.InnerClassLambdaMetafactory#buildCallSite}).
		{@link #perThreadStorage} was designed to minimize unnecessary object
		allocations in favor of re-using a thread-local mutable object instead,
		and capturing lambdas would completely defeat this purpose.

		if capturing cannot be avoided, consider using {@link #getPerThreadStorage()}
		and {@link #setPerThreadStorage} instead, or just reference {@link #perThreadStorage}
		directly. it is intentionally public to allow this. example usage:

		Foo variable = context.getPerThreadStorage();
		if (variable == null) variable = context.setPerThreadStorage(new Foo(arg1, arg2, ...));
		OR
		if (variable == null) context.perThreadStorage = variable = new Foo(arg1, arg2, ...);
		*/
		@SuppressWarnings("unchecked")
		public <T> T getPerThreadStorage(Supplier<? extends T> defaultValue) {
			if (this.perThreadStorage == null) {
				this.perThreadStorage = defaultValue.get();
			}
			return (T)(this.perThreadStorage);
		}
	}
}