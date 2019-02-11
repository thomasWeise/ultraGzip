package thomasWeise.ultraGzip;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.optimizationBenchmarking.utils.io.paths.PathUtils;
import org.optimizationBenchmarking.utils.tools.impl.abstr.ToolJob;

/** The job for the ultra gzip I/O tool. */
public final class UltraGzipIOJob extends ToolJob implements Runnable {

  /** the input path */
  private final Path m_input;

  /** should we use stdin instead of an input file? */
  private final boolean m_useStdIn;

  /** the output path */
  private final Path m_output;

  /** should we use stdout instead of an output file? */
  private final boolean m_useStdOut;

  /**
   * create
   *
   * @param ugo
   *          the job builder
   */
  UltraGzipIOJob(final UltraGzipIOJobBuilder ugo) {
    super(ugo);

    this.m_input = ugo.m_input;
    this.m_useStdIn = ugo.m_useStdIn;
    this.m_output = ugo.m_output;
    this.m_useStdOut = ugo.m_useStdOut;
    UltraGzipIOJobBuilder._validate(this.m_input, this.m_useStdIn,
        this.m_output, this.m_useStdOut);
  }

  /** run! */
  @Override
  public final void run() {
    final Logger logger;
    byte[] data;
    int size;
    String name;

    logger = this.getLogger();

    if (this.m_useStdIn) {
      name = "stdin"; //$NON-NLS-1$
    } else {
      name = this.m_input.getFileName().toString();
    }
    if (this.m_useStdOut) {
      name += "->stdout"; //$NON-NLS-1$
    } else {
      name = (((name + '-') + '>')
          + this.m_output.getFileName().toString());
    }

    if ((logger != null) && (logger.isLoggable(Level.INFO))) {
      logger.info(name + " is now loading input data."); //$NON-NLS-1$
    }
    try {

      try (final InputStream is = (this.m_useStdIn ? System.in//
          : PathUtils.openInputStream(this.m_input))) {
        data = _Buffers._get()._load(is);
      }

      size = data.length;
      if ((logger != null) && (logger.isLoggable(Level.INFO))) {
        logger.info(name + " has loaded " + size//$NON-NLS-1$
            + "B of input data - now compressing."); //$NON-NLS-1$
      }

      data = UltraGzip.getInstance().use().setData(data)//
          .setLogger(logger)//
          .setName(name).create().call();

      if ((logger != null) && (logger.isLoggable(Level.INFO))) {
        logger.info(name + " has compressed the " + size //$NON-NLS-1$
            + "B of input data down to " + data.length + //$NON-NLS-1$
            "B, which will now be written to the output."); //$NON-NLS-1$
      }

      try (final OutputStream os = (this.m_useStdOut ? System.out : //
          PathUtils.openOutputStream(this.m_output))) {//
        os.write(data);
        size = data.length;
        data = null;
      }

      if ((logger != null) && (logger.isLoggable(Level.INFO))) {
        logger.info(name + " has written all " + size //$NON-NLS-1$
            + "B to the output and, hence, completed its task."); //$NON-NLS-1$
      }

    } catch (final Throwable error) {
      if ((logger != null) && (logger.isLoggable(Level.SEVERE))) {
        logger.log(Level.SEVERE, name + " has failed.", error);//$NON-NLS-1$
      }
    }
  }
}
