package org.nuclos.common2;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;

import javax.swing.SwingUtilities;

public class Delayer<T> extends TimerTask {
	
	public static interface IHandler<T> {
		
		void touch(T event);
		
		void resetTouched();
		
		void trigger();
		
	}
	
	private static Map<Object,IHandler<? extends Object>> ONCE_MAP = new WeakHashMap<Object,IHandler<? extends Object>>();
	
	public static <T> void delay(long gracePeriodMillis, IHandler<T> h) {
		final IRealHandler<T> handler = new RealHandler<T>(h);
		final Delayer<T> delayer = new Delayer<T>(handler, gracePeriodMillis);
		delayer.schedule();
	}
	
	public static <T> void delayOnlyOnce(long gracePeriodMillis, IHandler<T> h) {
		final Delayer<T> delayer;
		synchronized (ONCE_MAP) {
			final IRealHandler<T> old = (IRealHandler<T>) ONCE_MAP.get(h);
			if (old != null) {
				// prolong grace period
				old.touch(null);
				return;
			}
			final IRealHandler<T> handler = new RealHandler<T>(h);
			delayer = new Delayer<T>(handler, gracePeriodMillis);
			ONCE_MAP.put(h, handler);
		}
		if (delayer != null)
			delayer.schedule();
	}
	
	public static <T> void runOnlyOnce(long gracePeriodMillis, final Runnable runnable) {
		final Delayer<T> delayer;
		synchronized (ONCE_MAP) {
			final IRealHandler<T> old = (IRealHandler<T>) ONCE_MAP.get(runnable);
			if (old != null) {
				// prolong grace period
				old.touch(null);
				return;
			}
			final IRealHandler<T> handler = new RealRunnableHandler<T>(runnable);
			delayer = new Delayer<T>(handler, gracePeriodMillis);
			ONCE_MAP.put(runnable, handler);
		}
		if (delayer != null)
			delayer.schedule();
	}
	
	public static <T> void invokeLaterOnlyOnce(long gracePeriodMillis, final Runnable runnable) {
		final Delayer<T> delayer;
		synchronized (ONCE_MAP) {
			final IRealHandler<T> old = (IRealHandler<T>) ONCE_MAP.get(runnable);
			if (old != null) {
				// prolong grace period
				old.touch(null);
				return;
			}
			final IRealHandler<T> handler = new InvokeRunnableLaterHandler<T>(runnable);
			delayer = new Delayer<T>(handler, gracePeriodMillis);
			ONCE_MAP.put(runnable, handler);
		}
		if (delayer != null)
			delayer.schedule();
	}
	
	//
	
	private IRealHandler<T> handler;
	
	private final long gracePeriodMillis;
	
	// former Spring injection
	
	private Timer timer;
	
	// end of former Spring injection
	
	private Delayer(IRealHandler<T> handler, long gracePeriodMillis) {
		if (handler == null) {
			throw new NullPointerException();
		}
		if (gracePeriodMillis <= 0L) {
			throw new IllegalArgumentException();
		}
		this.handler = handler;
		this.gracePeriodMillis = gracePeriodMillis;		
	}
	
	final void setTimer(Timer timer) {
		this.timer = timer;
	}
	
	final Timer getTimer() {
		return timer;
	}
	
	private void schedule() {
		getTimer().schedule(this, gracePeriodMillis, gracePeriodMillis);
	}

	@Override
	public void run() {
		final boolean touched = handler.isTouched();
		if (touched) {
			handler.resetTouched();
		}
		else {
			cancel();
			handler.trigger();
			// This is important, don't skip it!
			// As the WeakHashMap has strong reference to <em>value</em>,
			// we must invalidate wrapped to get gc'ed. (tp)
			handler = null;
		}
	}
	
	private static interface IRealHandler<T> extends IHandler<T> {
		
		boolean isTouched();
			
	}
	
	private static final class RealHandler<T> implements IRealHandler<T> {
		
		private IHandler<T> wrapped;
		
		private volatile boolean touched = false;
		
		public RealHandler(IHandler<T> wrapped) {
			if (wrapped == null) {
				throw new NullPointerException();
			}
			this.wrapped = wrapped;
		}
		
		@Override
		public void touch(T event) {
			touched = true;
			wrapped.touch(event);
		}
		
		@Override
		public void resetTouched() {
			touched = false;
		}
		
		@Override
		public void trigger() {
			wrapped.trigger();
			// This is important, don't skip it!
			// As the WeakHashMap has strong reference to <em>value</em>,
			// we must invalidate wrapped to get gc'ed. (tp)
			final IHandler<T> temp = wrapped;
			wrapped = null;
			synchronized (ONCE_MAP) {
				ONCE_MAP.remove(temp);
			}
		}
		
		@Override
		public boolean isTouched() {
			return touched;
		}
		
	}
	
	private static final class RealRunnableHandler<T> implements IRealHandler<T> {
		
		private Runnable wrapped;
		
		private volatile boolean touched = false;
		
		public RealRunnableHandler(Runnable wrapped) {
			if (wrapped == null) {
				throw new NullPointerException();
			}
			this.wrapped = wrapped;
		}
		
		@Override
		public void touch(T event) {
			touched = true;
		}
		
		@Override
		public void resetTouched() {
			touched = false;
		}
		
		@Override
		public void trigger() {
			wrapped.run();
			// This is important, don't skip it!
			// As the WeakHashMap has strong reference to <em>value</em>,
			// we must invalidate wrapped to get gc'ed. (tp)
			final Runnable temp = wrapped;
			wrapped = null;
			synchronized (ONCE_MAP) {
				ONCE_MAP.remove(temp);
			}
		}
		
		@Override
		public boolean isTouched() {
			return touched;
		}		
		
	}

	private static final class InvokeRunnableLaterHandler<T> implements IRealHandler<T> {
		
		private Runnable wrapped;
		
		private volatile boolean touched = false;
		
		public InvokeRunnableLaterHandler(Runnable wrapped) {
			if (wrapped == null) {
				throw new NullPointerException();
			}
			this.wrapped = wrapped;
		}
		
		@Override
		public void touch(T event) {
			touched = true;
		}
		
		@Override
		public void resetTouched() {
			touched = false;
		}
		
		@Override
		public void trigger() {
			SwingUtilities.invokeLater(wrapped);
			// This is important, don't skip it!
			// As the WeakHashMap has strong reference to <em>value</em>,
			// we must invalidate wrapped to get gc'ed. (tp)
			final Runnable temp = wrapped;
			wrapped = null;
			synchronized (ONCE_MAP) {
				ONCE_MAP.remove(temp);
			}
		}
		
		@Override
		public boolean isTouched() {
			return touched;
		}		
		
	}

}
