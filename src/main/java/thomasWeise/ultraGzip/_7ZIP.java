package thomasWeise.ultraGzip;

import java.io.OutputStream;
import java.nio.file.Path;

import thomasWeise.tools.Configuration;
import thomasWeise.tools.EProcessStream;
import thomasWeise.tools.ExternalProcess;
import thomasWeise.tools.ExternalProcessBuilder;
import thomasWeise.tools.ExternalProcessExecutor;
import thomasWeise.tools.TempDir;

/**
 * The internal class for using the operating system's 7-zip
 * implementation.
 */
final class _7ZIP implements Runnable {

  /** the source name */
  private static final String FROM = "7-Zip installation"; //$NON-NLS-1$

  /** the GZIP executable */
  private static final Path __7ZIP_PATH =
      Configuration.getExecutable("7z"); //$NON-NLS-1$

  /** the compression qualities to test */
  private static final int[] QUALITIES = { 7, 8, 9 };
  /** the fast bytes to test */
  private static final int[] FAST_BYTES = { -1, 200, 258 };
  /** the passes to test */
  private static final int[] PASSES = { -1, 15 };

  /** the job */
  private final UltraGzipJob m_owner;

  /** the compression quality */
  private final int m_quality;

  /** the fast bytes */
  private final int m_fastBytes;

  /** the passes */
  private final int m_passes;

  /**
   * create the 7-zip job
   *
   * @param job
   *          the owning job
   * @param quality
   *          the compression quality
   * @param fastBytes
   *          the fast bytes
   * @param passes
   *          the passes
   */
  private _7ZIP(final UltraGzipJob job, final int quality,
      final int fastBytes, final int passes) {
    super();
    this.m_owner = job;
    this.m_quality = quality;
    this.m_fastBytes = fastBytes;
    this.m_passes = passes;
  }

  /**
   * enqueue the java 7-zip jobs.
   *
   * @param job
   *          the owning job
   */
  static final void _enqueue(final UltraGzipJob job) {
    if (_7ZIP.__7ZIP_PATH != null) {
      for (final int quality : _7ZIP.QUALITIES) {
        for (final int fb : _7ZIP.FAST_BYTES) {
          for (final int passes : _7ZIP.PASSES) {
            job._execute(new _7ZIP(job, quality, fb, passes));
          }
        }
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public final void run() {
    ExternalProcessBuilder epb;
    byte[] compressed;

    _ERegistrationResult result;
    int retCode;

    if (_7ZIP.__7ZIP_PATH == null) {
      return;
    }

    compressed = null;
    result = null;

    try (final TempDir temp = new TempDir()) {
      epb = ExternalProcessExecutor.getInstance().get();
      epb.setDirectory(temp.getPath());
      epb.setExecutable(_7ZIP.__7ZIP_PATH);
      epb.addStringArgument("a"); //$NON-NLS-1$
      epb.addStringArgument("invalid"); //$NON-NLS-1$
      epb.addStringArgument("-tgzip"); //$NON-NLS-1$
      epb.addStringArgument("-si"); //$NON-NLS-1$
      epb.addStringArgument("-so"); //$NON-NLS-1$
      epb.addStringArgument("-mx=" + this.m_quality); //$NON-NLS-1$
      if (this.m_fastBytes > 0) {
        epb.addStringArgument("-mfb=" + this.m_fastBytes); //$NON-NLS-1$
      }
      if (this.m_passes > 0) {
        epb.addStringArgument("-mpass=" + this.m_passes); //$NON-NLS-1$
      }
      epb.addStringArgument("-w" + //$NON-NLS-1$
          temp.getPath());

      epb.setDirectory(temp.getPath());
      epb.setStdErr(EProcessStream.INHERIT);
      epb.setStdIn(EProcessStream.AS_STREAM);
      epb.setStdOut(EProcessStream.AS_STREAM);

      try (final ExternalProcess ep = epb.get()) {
        epb = null;

        try (final OutputStream os = ep.getStdIn()) {
          os.write(this.m_owner.m_data);
        }

        compressed = _Buffers._get()._load(ep.getStdOut());
        result = this.m_owner._register(compressed, _7ZIP.FROM);

        if ((retCode = ep.waitFor()) != 0) {
          this.m_owner._processError(retCode, _7ZIP.FROM,
              _7ZIP.__7ZIP_PATH);
        } else {
          result = null;
          compressed = null;
        }
      }
    } catch (final Throwable ioe) { // ignore!
      this.m_owner._error(ioe, _7ZIP.FROM);
      result = null;
      compressed = null;
    }

    if ((result != null) && (compressed != null)
        && (result != _ERegistrationResult.INVALID)) {
      _ADVDEF._postprocess(this.m_owner, compressed, _7ZIP.FROM);
    }
  }
}
