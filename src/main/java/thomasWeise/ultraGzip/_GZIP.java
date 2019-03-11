package thomasWeise.ultraGzip;

import java.io.OutputStream;
import java.nio.file.Path;

import thomasWeise.tools.ByteBuffers;
import thomasWeise.tools.Configuration;
import thomasWeise.tools.EProcessStream;
import thomasWeise.tools.ExternalProcess;
import thomasWeise.tools.ExternalProcessExecutor;
import thomasWeise.tools.TempDir;

/**
 * The internal class for using the operating system's GZIP
 * implementation.
 */
final class _GZIP implements Runnable {
  /** the argument */
  static final String ARG = "gzip"; //$NON-NLS-1$
  /** the source name */
  private static final String FROM = "GZIP installation"; //$NON-NLS-1$

  /** the GZIP executable */
  private static final Path __GZIP_PATH =
      Configuration.getExecutable(_GZIP.ARG);

  /** the quality range */
  private static final int[] QUALITY =
      UltraGzip._qualityRange(1, 9, 7);

  /** the job */
  private final UltraGzipJob m_owner;

  /** the compression quality */
  private final int m_quality;

  /**
   * create the GZIP job
   *
   * @param job
   *          the owning job
   * @param quality
   *          the compression quality
   */
  private _GZIP(final UltraGzipJob job, final int quality) {
    super();
    this.m_owner = job;
    this.m_quality = quality;
  }

  /**
   * enqueue the java gzip jobs.
   *
   * @param job
   *          the owning job
   */
  static final void _enqueue(final UltraGzipJob job) {
    if (_GZIP.__GZIP_PATH != null) {
      for (final int quality : _GZIP.QUALITY) {
        job._execute(new _GZIP(job, quality));
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public final void run() {
    byte[] compressed;
    _ERegistrationResult result;
    int retCode;

    if (_GZIP.__GZIP_PATH == null) {
      return;
    }

    compressed = null;
    result = null;
    try (final TempDir temp = new TempDir()) {
      try (final ExternalProcess ep =
          ExternalProcessExecutor.getInstance().get()//
              .setDirectory(temp.getPath())//
              .setExecutable(_GZIP.__GZIP_PATH)//
              .addStringArgument("-" + this.m_quality) //$NON-NLS-1$
              .addStringArgument("-c") //$NON-NLS-1$
              .setStdErr(EProcessStream.INHERIT)//
              .setStdIn(EProcessStream.AS_STREAM)//
              .setStdOut(EProcessStream.AS_STREAM)//
              .setDirectory(temp.getPath())//
              .get()) {

        try (final OutputStream os = ep.getStdIn()) {
          os.write(this.m_owner.m_data);
        }

        compressed = ByteBuffers.get().load(ep.getStdOut());
        result = this.m_owner._register(compressed, _GZIP.FROM);

        if ((retCode = ep.waitFor()) != 0) {
          this.m_owner._processError(retCode, _GZIP.FROM,
              _GZIP.__GZIP_PATH);
        } else {
          result = null;
          compressed = null;
        }
      }
    } catch (final Throwable ioe) { // ignore!
      this.m_owner._error(ioe, _GZIP.FROM);
      result = null;
      compressed = null;
    }

    if ((result != null) && (compressed != null)
        && (result != _ERegistrationResult.INVALID)) {
      _ADVDEF._postprocess(this.m_owner, compressed, _GZIP.FROM);
    }
  }
}
