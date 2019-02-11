package thomasWeise.ultraGzip;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.optimizationBenchmarking.utils.io.paths.PathUtils;
import org.optimizationBenchmarking.utils.io.paths.TempDir;
import org.optimizationBenchmarking.utils.io.paths.predicates.CanExecutePredicate;
import org.optimizationBenchmarking.utils.io.paths.predicates.FileNamePredicate;
import org.optimizationBenchmarking.utils.io.paths.predicates.IsFilePredicate;
import org.optimizationBenchmarking.utils.predicates.AndPredicate;
import org.optimizationBenchmarking.utils.tools.impl.process.EProcessStream;
import org.optimizationBenchmarking.utils.tools.impl.process.ExternalProcess;
import org.optimizationBenchmarking.utils.tools.impl.process.ExternalProcessBuilder;
import org.optimizationBenchmarking.utils.tools.impl.process.ExternalProcessExecutor;

/**
 * The internal class for using the operating system's zopfli
 * implementation.
 */
final class _Zopfli implements Runnable {

  /** the source name */
  private static final String FROM = "Zopfli installation"; //$NON-NLS-1$

  /** the GZIP executable */
  private static final Path __ZOPFLI_PATH = PathUtils.findFirstInPath(
      new AndPredicate<>(new FileNamePredicate(true, "zopfli"), //$NON-NLS-1$
          CanExecutePredicate.INSTANCE), //
      IsFilePredicate.INSTANCE, null);

  /** the job */
  private final UltraGzipJob m_owner;

  /**
   * create the GZIP job
   *
   * @param job
   *          the owning job
   */
  private _Zopfli(final UltraGzipJob job) {
    super();
    this.m_owner = job;
  }

  /**
   * enqueue the java zopfli jobs.
   *
   * @param job
   *          the owning job
   */
  static final void _enqueue(final UltraGzipJob job) {
    if (_Zopfli.__ZOPFLI_PATH != null) {
      job._execute(new _Zopfli(job));
    }
  }

  /** {@inheritDoc} */
  @Override
  public final void run() {
    ExternalProcessBuilder epb;
    Path path;
    byte[] compressed;
    _ERegistrationResult result;
    int retCode;

    if (_Zopfli.__ZOPFLI_PATH == null) {
      return;
    }

    compressed = null;
    result = null;

    try (TempDir td = new TempDir()) {
      path = Files.createTempFile(td.getPath(), "zopfli", ".bin");//$NON-NLS-1$//$NON-NLS-2$

      try (final OutputStream os = PathUtils.openOutputStream(path)) {
        os.write(this.m_owner.m_data);
      }

      epb = ExternalProcessExecutor.getInstance().use();
      epb.setDirectory(td.getPath());
      epb.setExecutable(_Zopfli.__ZOPFLI_PATH);
      epb.setDirectory(PathUtils.getTempDir());
      epb.addStringArgument("-c"); //$NON-NLS-1$
      epb.addStringArgument("--gzip"); //$NON-NLS-1$
      epb.addStringArgument("--i2000"); //$NON-NLS-1$
      epb.addPathArgument(path);

      epb.setLogger(this.m_owner._getLogger());
      epb.setStdErr(EProcessStream.REDIRECT_TO_LOGGER);
      epb.setStdIn(EProcessStream.IGNORE);
      epb.setStdOut(EProcessStream.AS_STREAM);

      try (final ExternalProcess ep = epb.create()) {
        epb = null;

        try (final OutputStream os = ep.getStdIn()) {
          os.write(this.m_owner.m_data);
        }

        compressed = _Buffers._get()._load(ep.getStdOut());
        result = this.m_owner._register(compressed, _Zopfli.FROM);

        if ((retCode = ep.waitFor()) != 0) {
          this.m_owner._processError(retCode, _Zopfli.FROM,
              _Zopfli.__ZOPFLI_PATH);
        } else {
          result = null;
          compressed = null;
        }
      }
    } catch (final Throwable ioe) { // ignore!
      this.m_owner._error(ioe, _Zopfli.FROM);
      result = null;
      compressed = null;
    }

    if ((result != null) && (compressed != null)
        && (result != _ERegistrationResult.INVALID)) {
      _ADVDEF._postprocess(this.m_owner, compressed, _Zopfli.FROM);
    }
  }
}
