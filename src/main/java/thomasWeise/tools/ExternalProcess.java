package thomasWeise.tools;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * An external process with which you can communicate via
 * standard streams that cannot deadlock.
 */
public final class ExternalProcess implements Closeable {

  /** the wrapped process instance */
  Process m_process;

  /** a stream providing the stdout of the process */
  InputStream m_stdout;
  /**
   * the worker thread associated with stdout, or {@code null} if
   * none is needed
   */
  _WorkerThread m_stdoutWorker;
  /**
   * the byte buffer associated with stdout, or {@code null} if
   * none is needed
   */
  ByteProducerConsumerBuffer m_stdoutBuffer;

  /** a stream providing stderr of the process */
  InputStream m_stderr;
  /**
   * the worker thread associated with stderr, or {@code null} if
   * none is needed
   */
  _WorkerThread m_stderrWorker;
  /**
   * the byte buffer associated with stderr, or {@code null} if
   * none is needed
   */
  ByteProducerConsumerBuffer m_stderrBuffer;

  /** a stream providing stdint of the process */
  OutputStream m_stdin;
  /**
   * the worker thread associated with stdin, or {@code null} if
   * none is needed
   */
  _WorkerThread m_stdinWorker;
  /**
   * the byte buffer associated with stdin, or {@code null} if
   * none is needed
   */
  ByteProducerConsumerBuffer m_stdinBuffer;

  /** an error caught somewhere */
  private Throwable m_error;

  /**
   * create
   *
   * @param process
   *          the process
   * @param log
   *          the logger to use
   * @param name
   *          the process' name
   */
  ExternalProcess(final Process process, final String name) {
    super();

    if (process == null) {
      throw new IllegalArgumentException(((//
      "Process cannot be null, but was for name '" //$NON-NLS-1$
          + name) + '\'') + '.');
    }

    this.m_process = process;
  }

  /** start all threads associated with this process */
  final void _start() {
    __UncaughtExceptionHandler ueh;

    ueh = null;

    if (this.m_stdinWorker != null) {
      ueh = new __UncaughtExceptionHandler();
      this.m_stdinWorker.setUncaughtExceptionHandler(ueh);
      this.m_stdinWorker.start();
    }

    if ((this.m_stdoutWorker != null)
        && (this.m_stdoutWorker != this.m_stdinWorker)) {
      if (ueh == null) {
        ueh = new __UncaughtExceptionHandler();
      }
      this.m_stdoutWorker.setUncaughtExceptionHandler(ueh);
      this.m_stdoutWorker.start();
    }

    if ((this.m_stderrWorker != null)
        && (this.m_stderrWorker != this.m_stdinWorker)
        && (this.m_stderrWorker != this.m_stdoutWorker)) {
      if (ueh == null) {
        ueh = new __UncaughtExceptionHandler();
      }
      this.m_stderrWorker.setUncaughtExceptionHandler(ueh);
      this.m_stderrWorker.start();
    }
  }

  /**
   * Wait until the process has finished and obtain its return
   * value.
   *
   * @return the return value
   * @throws IOException
   */
  public final int waitFor() throws IOException {
    return this.__close(false);
  }

  /**
   * Terminate the process if it is still alive
   *
   * @param kill
   *          should we use force?
   * @return the process' return value
   * @throws IOException
   *           if i/o fails
   */
  @SuppressWarnings("unused")
  private final int __close(final boolean kill)
      throws IOException {
    Throwable error;
    boolean shouldKill;
    int returnValue;

    error = null;
    returnValue = (-1);
    // <kill the main process>
    if (this.m_process != null) {

      try {
        shouldKill = kill;
        if (!kill) {
          waiter: for (;;) {
            try {
              returnValue = this.m_process.waitFor();
              break waiter;
            } catch (final InterruptedException ie) {
              // ingore
            } catch (final Throwable tt) {
              shouldKill = true;
              if (error == null) {
                error = tt;
              }
              break waiter;
            }
          }
        }

        if (shouldKill) {
          try {
            // wait a bit in a last-ditch effort to let the
            // process
            // gracefully terminate
            Thread.sleep(20L);
          } catch (final InterruptedException ie) {
            // ingore
          }
          this.m_process.destroy();
          try {
            // wait a bit in an effort to let destroy() work
            Thread.sleep(20L);
          } catch (final InterruptedException ie) {
            // ingore
          }
        }
      } catch (final Throwable t) {
        if (error == null) {
          error = t;
        }
      } finally {
        this.m_process = null;
      }
    }
    // </kill the main process>

    // <kill stdout>
    if (this.m_stdout != null) {
      try {
        this.m_stdout.close();
      } catch (final Throwable t) {
        if (error == null) {
          error = t;
        }
      } finally {
        this.m_stdout = null;
      }
    }

    if (this.m_stdoutBuffer != null) {
      try {
        this.m_stdoutBuffer.close();
      } catch (final Throwable t) {
        if (error == null) {
          error = t;
        }
      } finally {
        this.m_stdoutBuffer = null;
      }
    }

    if (this.m_stdoutWorker != null) {
      try {
        shouldKill = kill;
        if (!kill) {
          waiter: for (;;) {
            try {
              this.m_stdoutWorker.m_mode =
                  _WorkerThread.SHUTTING_DOWN;
              this.m_stdoutWorker.join();
              break waiter;
            } catch (final InterruptedException ie) {
              // ingore
            } catch (final Throwable tt) {
              shouldKill = true;
              if (error == null) {
                error = tt;
              }
              break waiter;
            }
          }
        }

        if (shouldKill) {
          this.m_stdoutWorker.m_mode = _WorkerThread.KILLED;
        }
      } catch (final Throwable t) {
        if (error == null) {
          error = t;
        }
      } finally {
        this.m_stdoutWorker = null;
      }
    }
    // </kill stdout>

    // <kill stderr>
    if (this.m_stderr != null) {
      try {
        this.m_stderr.close();
      } catch (final Throwable t) {
        if (error == null) {
          error = t;
        }
      } finally {
        this.m_stderr = null;
      }
    }

    if (this.m_stderrBuffer != null) {
      try {
        this.m_stderrBuffer.close();
      } catch (final Throwable t) {
        if (error == null) {
          error = t;
        }
      } finally {
        this.m_stderrBuffer = null;
      }
    }

    if (this.m_stderrWorker != null) {
      try {
        shouldKill = kill;
        if (!kill) {
          waiter: for (;;) {
            try {
              this.m_stderrWorker.m_mode =
                  _WorkerThread.SHUTTING_DOWN;
              this.m_stderrWorker.join();
              break waiter;
            } catch (final InterruptedException ie) {
              // ingore
            } catch (final Throwable tt) {
              shouldKill = true;
              if (error == null) {
                error = tt;
              }
              break waiter;
            }
          }
        }

        if (shouldKill) {
          this.m_stderrWorker.m_mode = _WorkerThread.KILLED;
        }
      } catch (final Throwable t) {
        if (error == null) {
          error = t;
        }
      } finally {
        this.m_stderrWorker = null;
      }
    }
    // </kill stderr>

    // <kill stdin>
    if (this.m_stdin != null) {
      try {
        this.m_stdin.close();
      } catch (final Throwable t) {
        if (error == null) {
          error = t;
        }
      } finally {
        this.m_stdin = null;
      }
    }

    if (this.m_stdinBuffer != null) {
      try {
        this.m_stdinBuffer.close();
      } catch (final Throwable t) {
        if (error == null) {
          error = t;
        }
      } finally {
        this.m_stdinBuffer = null;
      }
    }

    if (this.m_stdinWorker != null) {
      try {
        shouldKill = kill;
        if (!kill) {
          waiter: for (;;) {
            try {
              this.m_stdinWorker.m_mode =
                  _WorkerThread.SHUTTING_DOWN;
              this.m_stdinWorker.join();
              break waiter;
            } catch (final InterruptedException ie) {
              // ingore
            } catch (final Throwable tt) {
              shouldKill = true;
              if (error == null) {
                error = tt;
              }
              break waiter;
            }
          }
        }

        if (shouldKill) {
          this.m_stdinWorker.m_mode = _WorkerThread.KILLED;
        }
      } catch (final Throwable t) {
        if (error == null) {
          error = t;
        }
      } finally {
        this.m_stdinWorker = null;
      }
    }

    // </kill stdin>

    if (error == null) {
      error = this.m_error;
    }
    if (error != null) {
      if (kill) {
        ConsoleIO.stderr("Error while forcefully killing ", //$NON-NLS-1$
            error);
        throw new IOException(error);
      }

      ConsoleIO.stderr("Error while gracefully shutting down ", //$NON-NLS-1$
          error);

      this.m_error = error;
    }

    return returnValue;
  }

  /**
   * Terminate the process if it is still alive
   *
   * @throws IOException
   *           if i/o fails
   */
  @Override
  public final void close() throws IOException {
    this.__close(true);
  }

  /**
   * Get the standard-output stream of the process
   *
   * @return the standard-output stream of the process
   */
  public final InputStream getStdOut() {
    return this.m_stdout;
  }

  /**
   * Get the standard-error stream of the process
   *
   * @return the standard-error stream of the process
   */
  public final InputStream getStdError() {
    return this.m_stderr;
  }

  /**
   * Get the standard-input stream of the process
   *
   * @return the standard-input stream of the process
   */
  public final OutputStream getStdIn() {
    return this.m_stdin;
  }

  /**
   * Add an error
   *
   * @param t
   *          the error
   */
  synchronized final void _addError(final Throwable t) {
    if (t != null) {
      if (this.m_error == null) {
        this.m_error = t;
      }
    }
  }

  /** the internal exception handler for worker threads */
  private final class __UncaughtExceptionHandler
      implements UncaughtExceptionHandler {

    /** create */
    __UncaughtExceptionHandler() {
      super();
    }

    /** {@inheritDoc} */
    @Override
    public final void uncaughtException(final Thread t,
        final Throwable e) {
      ExternalProcess.this._addError(e);
    }
  }
}
