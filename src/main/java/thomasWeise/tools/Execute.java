package thomasWeise.tools;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 * Here we provide a simple and efficient parallel task execution
 * environment. You can submit arbitrarily many tasks which can
 * spawn sub-tasks. The functionality is similar to
 * {@link java.util.concurrent.ForkJoinPool}, but without several
 * restrictions and problems with that class. It is still a bit
 * incomplete, but can be improved in the future.
 * </p>
 * <p>
 * Originally, this class was a wrapper to Java's
 * {@link java.util.concurrent.ForkJoinPool}. But now it became a
 * very light-weight implementation of parallel job execution.
 * The jobs executed via {@link #parallel(Callable)} or
 * {@link #parallel(Runnable)} can spawn new jobs if they wish.
 * In other words, we have a behavior similar to ForkJoinPool,
 * but in much clearer, much simpler, and, hopefully, much
 * easier-to-understand implementation.
 * </p>
 * <p>
 * The move to an own implementation was necessary, since the
 * {@link java.util.concurrent.ForkJoinPool} is not designed for
 * tasks which spawn other tasks and may wait for them in a way
 * that does not form a nice tree. If you start many tasks and
 * then wait for them in a different order, while these tasks may
 * start sub-tasks and so on recursively, you may end up
 * receiving
 * {@link java.util.concurrent.RejectedExecutionException}s. My
 * current method does not produce any such errors.
 * </p>
 */
public final class Execute {

  /** the task has just been created */
  static final int STATE_INITIALIZED = 0;
  /** the task has been selected for out-of-order execution */
  static final int STATE_SELECTED =
      (Execute.STATE_INITIALIZED + 1);
  /** the task is running */
  static final int STATE_RUNNING = (Execute.STATE_SELECTED + 1);
  /** the task is done */
  static final int STATE_DONE = (Execute.STATE_RUNNING + 1);
  /** the task is canceled */
  static final int STATE_CANCELED = (Execute.STATE_DONE + 1);

  /** the number of cores to use */
  private static final String PARAM_CORES = "nCores";//$NON-NLS-1$

  /** the synchronizer object */
  private static final Object SYNCH = new Object();

  /** the queue of tasks */
  private static volatile __Task s_taskQueue = null;

  static {
    final int[] np =
        new int[] { Runtime.getRuntime().availableProcessors() };
    Configuration.synchronizedConfig(() -> {
      final Integer cores =
          Configuration.getInteger(Execute.PARAM_CORES);
      if (cores != null) {
        final int i = cores.intValue();
        if ((i > 0) && (i < 100)) {
          np[0] = i;
        }
      }
      Configuration.putInteger(Execute.PARAM_CORES, np[0]);
    });

    final int numProc = np[0];
    for (int index = 1; index <= numProc; index++) {
      new __Worker(index).start();
    }
    Execute.parallel(() -> {
      ConsoleIO.stdout(("started " + //$NON-NLS-1$
      Execute.PARAM_CORES + '=') + numProc + " worker threads"); //$NON-NLS-1$
    });
  }

  /**
   * print the command line arguments
   *
   * @param out
   *          the print stream to write to
   */
  public static final void printArgs(final PrintStream out) {
    out.println(' ' + Execute.PARAM_CORES + //
        ": 1.., number of cores to use, default: autodetect");//$NON-NLS-1$
  }

  /**
   * Add a task to the queue. Actually: Put it at the front of
   * the queue, as our queue is more something like a stack
   * represented as linked list of {@link __Task} objects.
   *
   * @param task
   *          the task
   */
  static final void _enqueue(final __Task task) {
    __Task queue;
    synchronized (Execute.SYNCH) {
      if (!(task.m_inQueue)) {
        // extract and replace old head of queue
        queue = Execute.s_taskQueue;
        Execute.s_taskQueue = task;

        // re-wire pointers in linked list
        task.m_nextInQueue = queue;
        if (queue != null) {
          queue.m_prevInQueue = task;
        }

        task.m_inQueue = true; // mark the task as enqueued
        Execute.SYNCH.notify(); // wake waiting worker
      }
    }
  }

  /**
   * delete a task from the queue
   *
   * @param task
   *          the task
   */
  static final void _delete(final __Task task) {
    __Task oldPrev, oldNext;

    synchronized (Execute.SYNCH) {
      if (task.m_inQueue) {
        task.m_inQueue = false;// mark task as de-queued

        // remember old previous and next pointers and null them
        // for GC
        oldPrev = task.m_prevInQueue;
        task.m_prevInQueue = null;
        oldNext = task.m_nextInQueue;
        task.m_nextInQueue = null;

        if (oldPrev != null) {
          // we were not the first element in the queue, let our
          // predecessor link to the successor task
          oldPrev.m_nextInQueue = oldNext;
        } else {
          // we were the first element in the queue, queue now
          // points to
          // next
          Execute.s_taskQueue = oldNext;
        }

        if (oldNext != null) {
          // there was a successor task behind us, connect to the
          // predecessor task, if any
          oldNext.m_prevInQueue = oldPrev;
        }
      }
    }
  }

  /**
   * Move the given task to the front of the queue (unless it has
   * already been extracted from the queue).
   *
   * @param task
   *          the task
   */
  static final void _moveToFront(final __Task task) {
    __Task oldPrev, oldNext, oldQueue;

    synchronized (Execute.SYNCH) {
      if (task.m_inQueue) {

        oldPrev = task.m_prevInQueue;
        if (oldPrev == null) {
          return; // we are already at the start of the queue
        }
        task.m_prevInQueue = null;

        // re-connect previous and next task
        oldNext = task.m_nextInQueue;
        oldPrev.m_nextInQueue = oldNext;
        if (oldNext != null) {
          oldNext.m_prevInQueue = oldPrev;
        }

        // re-insert task at head of queue
        oldQueue = Execute.s_taskQueue;
        task.m_nextInQueue = oldQueue;
        oldQueue.m_prevInQueue = task;
        Execute.s_taskQueue = task;
      }
    }
  }

  /**
   * obtain the next task from the queue
   *
   * @param wait
   *          should we wait for a task or return {@code null}?
   * @return the next task from the queue, or {@code null} if the
   *         queue is empty and {@code wait==false}
   */
  static final __Task _next(final boolean wait) {
    __Task candidate, next;

    for (;;) {
      synchronized (Execute.SYNCH) {
        candidate = Execute.s_taskQueue;
        if (candidate != null) {
          candidate.m_inQueue = false;
          next = candidate.m_nextInQueue;
          candidate.m_nextInQueue = null;
          if (next != null) {
            next.m_prevInQueue = null;
          }
          Execute.s_taskQueue = next;
          return candidate;
        }
        if (wait) {
          try {
            Execute.SYNCH.wait();
          } catch (@SuppressWarnings("unused") final InterruptedException iexp) {
            /** ignore **/
          }
          continue;
        }
      }
      return null;
    }
  }

  /**
   * Wait for a set of {@code tasks} to complete and store their
   * results into the {@code destination} array starting at index
   * {@code start}. {@code null} futures are ignored. The tasks
   * array is pruned in the process, i.e., filled with
   * {@code null}. If any of the tasks fails with an exception,
   * the remaining tasks will be ignored (and also not be set to
   * {@code null}).
   *
   * @param tasks
   *          the tasks to wait for
   * @param destination
   *          the destination array to receive the results, or
   *          {@code null} to ignore all results
   * @param start
   *          the start index in the destination array
   * @param ignoreNullResults
   *          should {@code null} results returned by the
   *          {@linkplain java.util.concurrent.Future#get()
   *          futures} be ignored, i.e., not stored in
   *          {@code destination}?
   * @return the index of the next element in {@code destination}
   *         right after the last copied result item (or
   *         {@code start} if {@code destination==null})
   * @param <X>
   *          the data type of the destination array's elements
   * @param <Y>
   *          the result type of the future tasks
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static final <X, Y extends X> int join(
      final Future<Y>[] tasks, final X[] destination,
      final int start, final boolean ignoreNullResults) {
    return Execute.join(((Iterable) (Arrays.asList(tasks))),
        destination, start, ignoreNullResults);
  }

  /**
   * Wait for a set of {@code tasks} to complete and store their
   * results into the {@code destination} array starting at index
   * {@code start}. {@code null} futures are ignored. If any of
   * the tasks fails with an exception, the remaining tasks will
   * be ignored.
   *
   * @param tasks
   *          the tasks to wait for
   * @param destination
   *          the destination array to receive the results, or
   *          {@code null} to ignore all results
   * @param start
   *          the start index in the destination array
   * @param ignoreNullResults
   *          should {@code null} results returned by the
   *          {@linkplain java.util.concurrent.Future#get()
   *          futures} be ignored, i.e., not stored in
   *          {@code destination}?
   * @return the index of the next element in {@code destination}
   *         right after the last copied result item (or
   *         {@code start} if {@code destination==null})
   * @param <X>
   *          the data type of the destination array's elements
   * @param <Y>
   *          the result type of the future tasks
   */
  public static final <X, Y extends X> int join(
      final Iterable<Future<Y>> tasks, final X[] destination,
      final int start, final boolean ignoreNullResults) {
    Throwable cause;
    Y result;
    int index;

    index = start;
    for (final Future<Y> future : tasks) {
      if (future != null) {
        try {
          result = future.get();
        } catch (final ExecutionException executionError) {
          cause = executionError.getCause();
          if (cause instanceof RuntimeException) {
            throw ((RuntimeException) cause);
          }
          throw new RuntimeException(
              "The execution of a task failed.", //$NON-NLS-1$
              executionError);
        } catch (final InterruptedException interrupted) {
          throw new RuntimeException(
              "A task was interrupted before completing.", //$NON-NLS-1$
              interrupted);
        }
        if ((result == null) && ignoreNullResults) {
          continue;
        }
        if (destination != null) {
          destination[index++] = result;
        }
      }
    }

    return index;
  }

  /**
   * Wait for a set of {@code tasks} to complete while ignoring
   * their results.
   *
   * @param tasks
   *          the tasks to wait for
   * @param <Y>
   *          the result type of the future tasks
   */
  public static final <Y> void
      join(final Iterable<Future<Y>> tasks) {
    Execute.join(tasks, null, 0, true);
  }

  /**
   * Wait for a set of {@code tasks} to complete while ignoring
   * their results.
   *
   * @param tasks
   *          the tasks to wait for
   * @param <Y>
   *          the result type of the future tasks
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static final <Y> void join(final Future... tasks) {
    Execute.join((Iterable) (Arrays.asList(tasks)));
  }

  /**
   * Try to execute a {@link java.lang.Runnable} in parallel
   * without waiting for its termination. This method does not
   * guarantee that the task will actually be executed in
   * parallel. If parallel execution is not possible, because,
   * e.g., no {@link java.util.concurrent.ForkJoinPool} could be
   * detected, the task is directly executed.
   *
   * @param job
   *          the job to run
   * @return {@link java.util.concurrent.Future} representing the
   *         job in execution
   */
  @SuppressWarnings("unchecked")
  public static final Future<Void> parallel(final Runnable job) {
    final __Task task;
    task = new __Task(job);
    Execute._enqueue(task);
    return task;
  }

  /**
   * Try to execute a {@link java.util.concurrent.Callable} in
   * parallel without waiting for its termination. This method
   * does not guarantee that the task will actually be executed
   * in parallel. If parallel execution is not possible, because,
   * e.g., no {@link java.util.concurrent.ForkJoinPool} could be
   * detected, the task is directly executed.
   *
   * @param job
   *          the job to run
   * @return {@link java.util.concurrent.Future} representing the
   *         job in execution
   * @param <T>
   *          the data type of the result
   */
  @SuppressWarnings("unchecked")
  public static final <T> Future<T>
      parallel(final Callable<T> job) {
    final __Task task;
    task = new __Task(job);
    Execute._enqueue(task);
    return task;
  }

  /**
   * the internal task class wraps a runnable or a callable and
   * provides the Future interface
   */
  @SuppressWarnings("rawtypes")
  private static final class __Task implements Future {

    /** the thread type is unknown */
    private static final int THREAD_TYPE_UNKNOWN = 0;
    /** the thread is a worker */
    private static final int THREAD_TYPE_WORKER =
        (__Task.THREAD_TYPE_UNKNOWN + 1);
    /** the thread is not a worker */
    private static final int THREAD_TYPE_NO_WORKER =
        (__Task.THREAD_TYPE_WORKER + 1);

    /** the next task in the queue */
    __Task m_nextInQueue;
    /** the previous task in the queue */
    __Task m_prevInQueue;
    /** is the task in the queue? */
    volatile boolean m_inQueue;

    /** the synchronizer */
    private final Object m_synch;
    /** the state */
    private int m_state;
    /** the runnable */
    private Runnable m_runnable;
    /** the callable */
    private Callable m_callable;
    /** the result object */
    private Object m_result;
    /** the caught exception */
    private Throwable m_error;

    /**
     * create a task, which, in this case, is a wrapper for a
     * runnable
     *
     * @param runnable
     *          the runnable
     */
    __Task(final Runnable runnable) {
      super();
      if (runnable == null) {
        throw new IllegalArgumentException(
            "Runnable to execute must not be null."); //$NON-NLS-1$
      }
      this.m_synch = new Object();
      this.m_runnable = runnable;
    }

    /**
     * Create a task, which, in this case, is a wrapper for a
     * callable
     *
     * @param callable
     *          the callable
     */
    __Task(final Callable callable) {
      super();
      if (callable == null) {
        throw new IllegalArgumentException(
            "Callable to execute must not be null."); //$NON-NLS-1$
      }
      this.m_synch = new Object();
      this.m_callable = callable;
    }

    /**
     * Execute the task and store the result as well as any
     * caught exception.
     */
    final void _run() {
      Runnable runnable;
      Callable callable;
      Object result;
      Throwable error;

      synchronized (this.m_synch) {
        if (this.m_state > Execute.STATE_SELECTED) {
          return;
        }
        // If we get here, we are either in STATE_INITIALIZED or
        // STATE_SELECTED. Extract all member variables necessary
        // for
        // execution and already set them to null.
        this.m_state = Execute.STATE_RUNNING;
        runnable = this.m_runnable;
        this.m_runnable = null;
        callable = this.m_callable;
        this.m_callable = null;
      }

      result = null;
      error = null;
      try {
        if (runnable != null) { // We execute a runnable.
          runnable.run();
          runnable = null;
        } else {// No runnable, so it must be Callable.
          result = callable.call();
          callable = null;
        }
      } catch (final Throwable theError) {
        error = theError;// Catch and store error.
      }

      synchronized (this.m_synch) {
        // OK, execution is done, update member variables and
        // notify
        // waiting threads.
        this.m_state = Execute.STATE_DONE;
        this.m_result = result;
        this.m_error = error;
        this.m_synch.notifyAll();
      }
    }

    /** {@inheritDoc} */
    @Override
    public final boolean
        cancel(final boolean mayInterruptIfRunning) {
      synchronized (this.m_synch) {
        if (this.m_state != Execute.STATE_INITIALIZED) {
          return false;
        }
        this.m_state = Execute.STATE_CANCELED;
        this.m_runnable = null;
        this.m_callable = null;
      }
      Execute._delete(this);
      return true;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isCancelled() {
      synchronized (this.m_synch) {
        return (this.m_state == Execute.STATE_CANCELED);
      }
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isDone() {
      synchronized (this.m_synch) {
        return (this.m_state >= Execute.STATE_DONE);
      }
    }

    /**
     * get the thread type
     *
     * @return the thread type
     */
    private static final int __threadType() {
      return (Thread.currentThread() instanceof __Worker)
          ? __Task.THREAD_TYPE_WORKER
          : __Task.THREAD_TYPE_NO_WORKER;
    }

    /** {@inheritDoc} */
    @Override
    public final Object get()
        throws InterruptedException, ExecutionException {
      __Task execute;
      int threadType;

      threadType = __Task.THREAD_TYPE_UNKNOWN;
      looper: for (;;) {
        synchronized (this.m_synch) {
          switcher: switch (this.m_state) {

            case STATE_INITIALIZED: {
              // The task has been initialized but is not
              // running. It must
              // either be in the queue or has just been
              // extracted by a
              // worker thread from the queue and will be
              // executed next.
              if (((threadType == __Task.THREAD_TYPE_UNKNOWN)
                  ? (threadType = __Task.__threadType())
                  : threadType) != __Task.THREAD_TYPE_WORKER) {//
                // If we are not a worker thread, we will simply
                // have to
                // wait for the task to complete and then check
                // again.

                // We therefore move the task to the head of the
                // queue in
                // order to get it done quicker. This method does
                // nothing
                // if the task has already been purged from the
                // queue.
                Execute._moveToFront(this);
                // We will only wait for 1000ms. Afterwards, we
                // move it
                // again to the head of the queue if it was not
                // yet
                // executed.
                this.m_synch.wait(1000L);
                continue looper;
              }
              // If we are a worker thread, we can leave the
              // synchronized
              // block and execute the task directly. We
              // therefore need to
              // remove it from the queue.
              this.m_state = Execute.STATE_SELECTED;
              Execute._delete(this);
              execute = this;
              break switcher;
            }

            case STATE_SELECTED:
            case STATE_RUNNING: {
              // The task is either selected for out-of-order
              // execution by
              // another worker thread or is already running (in
              // another
              // worker thread).
              try {
                // The task is currently running. If we are a
                // worker
                // thread, we can execute another task in the
                // meantime.
                if (((threadType == __Task.THREAD_TYPE_UNKNOWN)
                    ? (threadType = __Task.__threadType())
                    : threadType) == __Task.THREAD_TYPE_WORKER) {//
                  // If we are waiting for a task inside a worker
                  // thread,
                  // then this worker thread might as well do
                  // another task
                  // while waiting. In the worst case, we will
                  // wait a bit
                  // longer. That's OK with me. Anyway, we try to
                  // obtain a
                  // new task from the queue, but we don't wait
                  // for tasks
                  // to arrive if the queue is empty.
                  execute = Execute._next(false);
                  if (execute != null) {
                    // OK there was a new task. We leave the
                    // synchronized
                    // block and execute it.
                    break switcher;
                  }
                }
                // If we get here, we are either a thread outside
                // of the
                // Execute environment, i.e., no worker thread,
                // or we are a
                // worker thread and the global task queue is
                // empty. In
                // both cases, we have to wait.

                // We will wait for at most 1000ms if we are a
                // worker
                // thread and then try again. Reason: Maybe the
                // task won't
                // have finished yet after 1000ms if we did not
                // get
                // notified, but new, other tasks may have come
                // in.
                // Obviously, for non-worker threads, no such
                // behavior is
                // necessary and we can just wait.
                this.m_synch
                    .wait((threadType == 1) ? 1000L : 0L);
              } catch (@SuppressWarnings("unused") final InterruptedException ie) {
                /** ignore **/
              }
              continue looper;
            }

            case STATE_DONE: {
              // The task has already been execute. This means
              // that its
              // error and result have been set.
              if (this.m_error != null) {
                // First, we check if there was an exception and
                // if there
                // was one, we try to re-throw it.
                if (this.m_error instanceof RuntimeException) {
                  throw ((RuntimeException) (this.m_error));
                }
                if (this.m_error instanceof Error) {
                  throw ((Error) (this.m_error));
                }
                throw new ExecutionException(//
                    "An error has happened while executing a parallel task via Execute.", //$NON-NLS-1$
                    this.m_error);
              }
              return this.m_result;
            }

            default: { // case STATE_CANCELED:
              // The task has been cancelled
              throw new CancellationException(//
                  "The task has been cancelled."); //$NON-NLS-1$
            }
          }
        }

        // OK, we are a worker thread and can execute a task.
        // This is
        // either this task here, which is in STATE_SELECTED, or
        // another
        // task that can be executed while waiting for the
        // current task.
        execute._run();
      }
    }

    /** {@inheritDoc} */
    @Override
    public final Object get(final long timeout,
        final TimeUnit unit) throws InterruptedException,
        ExecutionException, TimeoutException {
      return this.get();
    }

  }

  /** the worker threads */
  private static final class __Worker extends Thread {

    /**
     * create the worker
     *
     * @param id
     *          the worker's id
     */
    __Worker(final int id) {
      super("Executor#" + id); //$NON-NLS-1$
      this.setDaemon(true);
    }

    /** run */
    @Override
    public final void run() {
      __Task task;

      while ((task = Execute._next(true)) != null) {
        task._run();
      }
    }
  }

}
