package thomasWeise.ultraGzip;

import java.io.OutputStream;
import java.nio.file.Path;

import org.optimizationBenchmarking.utils.io.paths.PathUtils;
import org.optimizationBenchmarking.utils.io.paths.predicates.CanExecutePredicate;
import org.optimizationBenchmarking.utils.io.paths.predicates.FileNamePredicate;
import org.optimizationBenchmarking.utils.io.paths.predicates.IsFilePredicate;
import org.optimizationBenchmarking.utils.predicates.AndPredicate;
import org.optimizationBenchmarking.utils.tools.impl.process.EProcessStream;
import org.optimizationBenchmarking.utils.tools.impl.process.ExternalProcess;
import org.optimizationBenchmarking.utils.tools.impl.process.ExternalProcessBuilder;
import org.optimizationBenchmarking.utils.tools.impl.process.ExternalProcessExecutor;

/**
 * The internal class for using the operating system's 7-zip
 * implementation.
 */
final class _7ZIP implements Runnable {

  /** the source name */
  private static final String FROM = "7-Zip installation"; //$NON-NLS-1$

  /** the GZIP executable */
  private static final Path __7ZIP_PATH = PathUtils.findFirstInPath(
      new AndPredicate<>(new FileNamePredicate(true, "7z"), //$NON-NLS-1$
          CanExecutePredicate.INSTANCE), //
      IsFilePredicate.INSTANCE, null);

  /** the compression qualities to test */
  private static final int[] QUALITIES = { 8, 9 };
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

    try {
      epb = ExternalProcessExecutor.getInstance().use();
      epb.setDirectory(PathUtils.getTempDir());
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
          PathUtils.getPhysicalPath(PathUtils.getTempDir(), false));

      epb.setDirectory(PathUtils.getTempDir());
      epb.setLogger(this.m_owner._getLogger());
      epb.setStdErr(EProcessStream.REDIRECT_TO_LOGGER);
      epb.setStdIn(EProcessStream.AS_STREAM);
      epb.setStdOut(EProcessStream.AS_STREAM);

      try (final ExternalProcess ep = epb.create()) {
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

    if ((result != null) && (compressed != null)) {
      if ((this.m_quality >= 9)
          || (result == _ERegistrationResult.IMPROVEMENT)) {
        _ADVDEF._postprocess(this.m_owner, compressed, _7ZIP.FROM);
      }
    }
  }
}
