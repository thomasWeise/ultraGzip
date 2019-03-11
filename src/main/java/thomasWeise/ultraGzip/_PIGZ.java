package thomasWeise.ultraGzip;

import java.io.OutputStream;
import java.nio.file.Path;

import thomasWeise.tools.ByteBuffers;
import thomasWeise.tools.Configuration;
import thomasWeise.tools.EProcessStream;
import thomasWeise.tools.ExternalProcess;
import thomasWeise.tools.ExternalProcessBuilder;
import thomasWeise.tools.ExternalProcessExecutor;
import thomasWeise.tools.TempDir;

/**
 * The internal class for using the operating system's PIGZ
 * implementation.
 */
final class _PIGZ implements Runnable {

  /** the source name */
  private static final String FROM = "PIGZ installation"; //$NON-NLS-1$
  /** the argument */
  static final String ARG = "pigz"; //$NON-NLS-1$
  /** the PIGZ executable */
  private static final Path __PIGZ_PATH =
      Configuration.getExecutable(_PIGZ.ARG);

  /** the quality range */
  private static final int[] QUALITY =
      UltraGzip._qualityRange(1, 9, 8);

  /** the job */
  private final UltraGzipJob m_owner;

  /** the compression quality */
  private final int m_quality;

  /**
   * create the pigz job
   *
   * @param job
   *          the owning job
   * @param quality
   *          the compression quality
   */
  private _PIGZ(final UltraGzipJob job, final int quality) {
    super();
    this.m_owner = job;
    this.m_quality = quality;
  }

  /**
   * enqueue the java pigz jobs.
   *
   * @param job
   *          the owning job
   */
  static final void _enqueue(final UltraGzipJob job) {
    if (_PIGZ.__PIGZ_PATH != null) {
      for (final int quality : _PIGZ.QUALITY) {
        job._execute(new _PIGZ(job, quality));
      }
      job._execute(new _PIGZ(job, 11));
    }
  }

  /** {@inheritDoc} */
  @Override
  public final void run() {
    byte[] compressed;
    _ERegistrationResult result;
    int retCode;

    if (_PIGZ.__PIGZ_PATH == null) {
      return;
    }

    compressed = null;
    result = null;
    try (final TempDir temp = new TempDir()) {

      final ExternalProcessBuilder epb =
          ExternalProcessExecutor.getInstance().get();
      epb.setDirectory(temp.getPath());
      epb.setExecutable(_PIGZ.__PIGZ_PATH);
      epb.addStringArgument("-" + this.m_quality); //$NON-NLS-1$
      epb.addStringArgument("-c"); //$NON-NLS-1$
      epb.setStdErr(EProcessStream.INHERIT);
      epb.setStdIn(EProcessStream.AS_STREAM);
      epb.setStdOut(EProcessStream.AS_STREAM);
      epb.setDirectory(temp.getPath());

      try (final ExternalProcess ep = epb.get()) {

        try (final OutputStream os = ep.getStdIn()) {
          os.write(this.m_owner.m_data);
        }

        compressed = ByteBuffers.get().load(ep.getStdOut());
        result = this.m_owner._register(compressed, _PIGZ.FROM);

        if ((retCode = ep.waitFor()) != 0) {
          this.m_owner._processError(retCode, _PIGZ.FROM,
              _PIGZ.__PIGZ_PATH);
        } else {
          result = null;
          compressed = null;
        }
      }

    } catch (final Throwable ioe) { // ignore!
      this.m_owner._error(ioe, _PIGZ.FROM);
      result = null;
      compressed = null;
    }

    if ((result != null) && (compressed != null)
        && (result != _ERegistrationResult.INVALID)) {
      _ADVDEF._postprocess(this.m_owner, compressed, _PIGZ.FROM);
    }
  }
}
