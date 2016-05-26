/*
 * Copyright 2015 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dav.daf.
 * 
 * de.bsvrz.dav.daf is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dav.daf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with de.bsvrz.dav.daf; If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dav.daf.util.cron;

import java.util.concurrent.*;

/**
 * ScheduledExecutorService-Implementierung, die anhand einer {@link CronDefinition} periodische Aufträge planen kann
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 */
public class CronScheduler extends ScheduledThreadPoolExecutor {

	/**
	 * Erstellt einen CronScheduler mit einem Thread
	 */
	public CronScheduler() {
		super(1);
	}

	/**
	 * Erstellt einen CronScheduler
	 * @param corePoolSize Anzahl Threads
	 */
	public CronScheduler(final int corePoolSize) {
		super(corePoolSize);
	}

	/**
	 * Erstellt einen CronScheduler
	 * @param corePoolSize Anzahl Threads
	 * @param threadFactory ThreadFactory
	 */
	public CronScheduler(final int corePoolSize, final ThreadFactory threadFactory) {
		super(corePoolSize, threadFactory);
	}

	/**
	 * Plant einen Auftrag
	 * @param command Auszuführender Befehl
	 * @param cronDefinition Auszuführende Zeitpunkte
	 * @return ScheduledFuture-Objekt zum Abfragen des Ergebnisses (sofern vorhanden)
	 */
	public ScheduledFuture<?> schedule(final Runnable command, final CronDefinition cronDefinition) {
		return schedule(Executors.callable(command), cronDefinition);
	}

	/**
	 * Plant einen Auftrag
	 * @param callable Auszuführender Befehl
	 * @param cronDefinition Auszuführende Zeitpunkte
	 * @return ScheduledFuture-Objekt zum Abfragen des Ergebnisses (sofern vorhanden)
	 */
	public <V> ScheduledFuture<V> schedule(final Callable<V> callable, final CronDefinition cronDefinition) {
		CronTask<V> task = new CronTask<V>(callable, cronDefinition);
		super.submit(task);
		return task;
	}

	@Override
	protected <V> RunnableScheduledFuture<V> decorateTask(final Runnable runnable, final RunnableScheduledFuture<V> task) {
		if(runnable instanceof CronTask) {
			return (CronTask<V>) runnable;
		}
		return super.decorateTask(runnable, task);
	}

	private class CronTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V>{

		private final CronDefinition _cronDefinition;
		private long _lastRunTime = getTime();
		private long _nextScheduledTime;

		public CronTask(final Callable<V> callable, final CronDefinition cronDefinition) {
			super(callable);
			_cronDefinition = cronDefinition;
			_nextScheduledTime = _cronDefinition.nextScheduledTime(_lastRunTime);
		}

		@Override
		public boolean isPeriodic() {
			return true;
		}

		@Override
		public long getDelay(final TimeUnit unit) {
			return unit.convert(_nextScheduledTime - getTime(), TimeUnit.MILLISECONDS);
		}

		@Override
		public int compareTo(final Delayed o) {
			if (o == this)
				return 0;
			long d = (getDelay(TimeUnit.MILLISECONDS) -	o.getDelay(TimeUnit.MILLISECONDS));
			return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
		}

		public boolean cancel(boolean mayInterruptIfRunning) {
			boolean cancelled = super.cancel(mayInterruptIfRunning);
			if (cancelled)
				remove(this);
			return cancelled;
		}

		/**
		 * Overrides FutureTask version so as to reset/requeue if periodic.
		 */
		public void run() {
			if (CronTask.super.runAndReset()) {
				_lastRunTime = _nextScheduledTime;
				_nextScheduledTime = _cronDefinition.nextScheduledTime(_lastRunTime + 1);
				submit(this);
			}
		}

		@Override
		public String toString() {
			return "CronTask{" +
					"_cronDefinition=" + _cronDefinition +
					'}';
		}
	}

	/**
	 * Zum testen überschreibbar um eine andere Uhr zu benutzen.
	 * @return aktuelle Zeit in Millisekunden analog zu System.currentTimeMillis()
	 */
	protected long getTime() {
		return System.currentTimeMillis();
	}
}
