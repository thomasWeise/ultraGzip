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
import org.optimizationBenchmarking.utils.tools.impl.process.ExternalProcessExecutor;

/**
 * The internal class for using the operating system's GZIP implementation.
 */
final class _GZIP implements Runnable {

  /** the source name */
  private static final String FROM = "GZIP installation"; //$NON-NLS-1$

  /** the GZIP executable */
  private static final Path __GZIP_PATH = PathUtils.findFirstInPath(
      new AndPredicate<>(new FileNamePredicate(true, "gzip"), //$NON-NLS-1$
          CanExecutePredicate.INSTANCE), //
      IsFilePredicate.INSTANCE, null);

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
      for (int quality = 1; quality <= 9; quality++) {
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
    try {
      try (final ExternalProcess ep = ExternalProcessExecutor.getInstance()
          .use()//
          .setDirectory(PathUtils.getTempDir())//
          .setExecutable(_GZIP.__GZIP_PATH)//
          .addStringArgument("-" + this.m_quality) //$NON-NLS-1$
          .addStringArgument("-c") //$NON-NLS-1$
          .setLogger(this.m_owner._getLogger())//
          .setStdErr(EProcessStream.REDIRECT_TO_LOGGER)//
          .setStdIn(EProcessStream.AS_STREAM)//
          .setStdOut(EProcessStream.AS_STREAM)//
          .setDirectory(PathUtils.getTempDir())//
          .create()) {

        try (final OutputStream os = ep.getStdIn()) {
          os.write(this.m_owner.m_data);
        }

        compressed = _Buffers._get()._load(ep.getStdOut());
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

    if ((result != null) && (compressed != null)) {
      if ((this.m_quality >= 8)
          || (result == _ERegistrationResult.IMPROVEMENT)) {
        _ADVDEF._postprocess(this.m_owner, compressed, _GZIP.FROM);
      }
    }
  }
}
