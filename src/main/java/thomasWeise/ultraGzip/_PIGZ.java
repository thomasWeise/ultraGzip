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
 * The internal class for using the operating system's PIGZ implementation.
 */
final class _PIGZ implements Runnable {

  /** the source name */
  private static final String FROM = "PIGZ installation"; //$NON-NLS-1$

  /** the PIGZ executable */
  private static final Path __PIGZ_PATH = PathUtils.findFirstInPath(
      new AndPredicate<>(new FileNamePredicate(true, "pigz"), //$NON-NLS-1$
          CanExecutePredicate.INSTANCE), //
      IsFilePredicate.INSTANCE, null);

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
      for (int quality = 1; quality <= 9; quality++) {
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
    try {
      final ExternalProcessBuilder epb = ExternalProcessExecutor
          .getInstance().use();
      epb.setDirectory(PathUtils.getTempDir());
      epb.setExecutable(_PIGZ.__PIGZ_PATH);
      epb.addStringArgument("-" + this.m_quality); //$NON-NLS-1$
      epb.addStringArgument("-c"); //$NON-NLS-1$
      epb.setLogger(this.m_owner._getLogger());
      epb.setStdErr(EProcessStream.REDIRECT_TO_LOGGER);
      epb.setStdIn(EProcessStream.AS_STREAM);
      epb.setStdOut(EProcessStream.AS_STREAM);
      epb.setDirectory(PathUtils.getTempDir());

      try (final ExternalProcess ep = epb.create()) {

        try (final OutputStream os = ep.getStdIn()) {
          os.write(this.m_owner.m_data);
        }

        compressed = _Buffers._get()._load(ep.getStdOut());
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

    if ((result != null) && (compressed != null)) {
      if ((this.m_quality >= 8)
          || (result == _ERegistrationResult.IMPROVEMENT)) {
        _ADVDEF._postprocess(this.m_owner, compressed, _PIGZ.FROM);
      }
    }
  }
}
