package thomasWeise.tools;

/**
 * The base class for worker {@link java.lang.Thread threads} for
 * shoveling data to and from an external process. By default,
 * they will be {@link java.lang.Thread#isDaemon() deamon}
 * threads and run at the {@link java.lang.Thread#MIN_PRIORITY
 * lowest priority}.
 */
abstract class _WorkerThread extends Thread {

  /** the worker thread is alive and works */
  static final int ALIVE = 0;
  /** the worker thread is hutting down */
  static final int SHUTTING_DOWN = (_WorkerThread.ALIVE + 1);
  /**
   * the worker thread has been killed and should quit
   * immediately
   */
  static final int KILLED = (_WorkerThread.SHUTTING_DOWN + 1);

  /**
   * are we alive: {@code 0}=alive, {@code 1}=shutting down, data
   * can be ignored, {@code 2}=dead, just quit
   */
  volatile int m_mode;

  /**
   * create
   *
   * @param name
   *          the thread's name
   * @param log
   *          the logger
   */
  @SuppressWarnings("unused")
  _WorkerThread(final String name) {
    super(name);

    this.m_mode = _WorkerThread.ALIVE;

    try {
      // Worker threads can have a low priority. They are only
      // _required_ to do stuff when the main threads are
      // blocked.
      // Otherwise, they _may_ do stuff.
      this.setPriority(Thread.MIN_PRIORITY);
    } catch (final Throwable t) {
      // if we cannot set the priority, it is also OK
    }
    this.setDaemon(true);
  }
}
