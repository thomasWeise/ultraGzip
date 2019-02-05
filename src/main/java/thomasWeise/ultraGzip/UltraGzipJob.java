package thomasWeise.ultraGzip;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.optimizationBenchmarking.utils.parallel.Execute;
import org.optimizationBenchmarking.utils.tools.impl.abstr.ToolJob;

/** The ultra gzip job. */
public final class UltraGzipJob extends ToolJob
    implements Callable<byte[]> {

  /** the data */
  final byte[] m_data;

  /** the job's name */
  final String m_name;

  /** the best compression */
  private volatile byte[] m_best;

  /** the jobs */
  private ArrayList<Future<?>> m_jobs;

  /**
   * create the ultra gzip job
   *
   * @param data
   *          the data to compress
   * @param name
   *          the name of the data
   * @param logger
   *          the logger
   */
  UltraGzipJob(final byte[] data, final String name, final Logger logger) {
    super(logger);

    UltraGzipJobBuilder._checkData(data);

    this.m_data = data;
    this.m_name = (((((UltraGzipJobBuilder._checkName(name) + //
        ' ') + '(') + data.length) + 'B') + ')');
    this.m_jobs = new ArrayList<>();
  }

  /**
   * Register a new gzipped data.
   *
   * @param data
   *          the data
   * @param from
   *          the source process creating the data
   * @return the result
   */
  final _ERegistrationResult _register(final byte[] data,
      final String from) {
    final _Buffers buffer;
    final Logger logger;
    byte[] best;

    valid: {

      if ((data == null) || (data.length <= 0)) {
        break valid;
      }

      best = this.m_best;
      if ((best != null) && ((3L * best.length) < (2L * data.length))) {
        // far from improvement, skip checking contents
        // we might still get an improvement after refinement
        return _ERegistrationResult.NO_IMPROVEMENT;
      }

      // ok, it might be that the new data is better, let's check
      buffer = _Buffers._get();

      try (final ByteArrayInputStream bis = new ByteArrayInputStream(
          data)) {
        // check if data is consistent using Java's GZIPInputStream
        try (
            final java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(
                bis)) {
          if (!(buffer._compare(gis, this.m_data))) {
            break valid;
          }
        }

        best = this.m_best;
        if ((best != null) && (best.length < data.length)) {
          // no improvement, skip checking contents a second time
          return _ERegistrationResult.NO_IMPROVEMENT;
        }

        bis.reset();

        // check if data is consistent using jzlib's GZIPInputStream
        try (
            final com.jcraft.jzlib.GZIPInputStream gis = new com.jcraft.jzlib.GZIPInputStream(
                bis)) {
          if (!(buffer._compare(gis, this.m_data))) {
            break valid;
          }
        }
      } catch (final Throwable error) {
        this._error(error, from);
        break valid;
      }

      // if we get here, the compression was successful and likely yielded
      // an improvement
      synchronized (this.m_name) {
        if ((this.m_best != null) && (this.m_best.length <= data.length)) {
          return _ERegistrationResult.NO_IMPROVEMENT;
        }
        this.m_best = data;
      }

      logger = this.getLogger();
      if ((logger != null) && (logger.isLoggable(Level.INFO))) {
        logger.info(
            from + " improved minimal gzip archive size when packing "//$NON-NLS-1$
                + this.m_name + " down to " + //$NON-NLS-1$
                data.length + "B (" + //$NON-NLS-1$
                (((100L * data.length) + (data.length - 1))
                    / this.m_data.length)
                + "%)."); //$NON-NLS-1$
      }
      return _ERegistrationResult.IMPROVEMENT;
    }

    logger = this.getLogger();
    if ((logger != null) && (logger.isLoggable(Level.WARNING))) {
      logger.warning(from + " produced invalid gzip archive for " + //$NON-NLS-1$
          this.m_name + '.');
    }
    return _ERegistrationResult.INVALID;
  }

  /**
   * log an error
   *
   * @param from
   *          the source causing the error
   * @param error
   *          the error
   */
  final void _error(final Throwable error, final String from) {
    final Logger logger;

    logger = this.getLogger();
    if ((logger != null) && (logger.isLoggable(Level.WARNING))) {
      logger.log(Level.WARNING,
          "UltraGzip has encountered the following exception in " + from //$NON-NLS-1$
              + " when packing " + this.m_name + '.', //$NON-NLS-1$
          error);
    }
  }

  /**
   * log a warning
   *
   * @param from
   *          the source causing the error
   * @param warning
   *          the warning message
   */
  final void _warning(final String from, final String warning) {
    final Logger logger;

    logger = this.getLogger();
    if ((logger != null) && (logger.isLoggable(Level.WARNING))) {
      logger.warning("Problem encountered when applying " + from + //$NON-NLS-1$
          " to " + this.m_name + ": " + warning);//$NON-NLS-1$//$NON-NLS-2$
    }
  }

  /**
   * Add another job to wait for.
   *
   * @param job
   *          the job to add
   */
  final void _execute(final Runnable job) {
    final Future<?> future;

    if (job == null) {
      throw new IllegalArgumentException("Job cannot be null."); //$NON-NLS-1$
    }

    future = Execute.parallel(job);

    synchronized (this.m_jobs) {
      this.m_jobs.add(future);
    }
  }

  /** {@inheritDoc} */
  @Override
  public final byte[] call() {
    final byte[] best;
    int size;
    Future<?> job;

    // enqueue all the default jobs
    _JavaGZip._enqueue(this);
    _JZLibGZip._enqueue(this);
    _GZIP._enqueue(this);
    _PIGZ._enqueue(this);
    _7ZIP._enqueue(this);
    _Zopfli._enqueue(this);

    // wait until all jobs have completed
    wait: for (;;) {
      synchronized (this.m_jobs) {
        size = this.m_jobs.size();
        if (size <= 0) {
          this.m_jobs = null;
          break wait;
        }
        job = this.m_jobs.remove(size - 1);
      }
      if (job != null) {
        try {
          job.get();
        } catch (final Throwable error) {
          this._error(error, "the job waiting routine"); //$NON-NLS-1$
        }
      }
    }

    synchronized (this.m_name) {
      best = this.m_best;
    }

    if (best == null) {
      throw new IllegalStateException("Gzipping of " + this.m_name //$NON-NLS-1$
          + " failed."); //$NON-NLS-1$
    }
    return best;
  }

  /**
   * Check whether a given data size might be promising for being a new
   * better result.
   *
   * @param size
   *          the size
   * @return {@code true} if it may make sense to register the result of
   *         that size, {@code false} otherwise
   */
  final boolean _isPromising(final long size) {
    final byte[] res;

    synchronized (this.m_name) {
      res = this.m_best;
    }
    return ((res == null) || (res.length > size));
  }

  /**
   * Get the logger of this job
   *
   * @return the logger of this job
   */
  final Logger _getLogger() {
    return this.getLogger();
  }

  /**
   * Print a warning regarding a non-zero return code
   *
   * @param returnCode
   *          the program's return code (should not be {@code 0} if you
   *          call this method)
   * @param from
   *          the "from" identifier
   * @param path
   *          the path to the program
   */
  final void _processError(final int returnCode, final String from,
      final Path path) {
    this._warning(from, "program " + path + //$NON-NLS-1$
        " terminated with exit code " + returnCode + '.');//$NON-NLS-1$
  }
}
