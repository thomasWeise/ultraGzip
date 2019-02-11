package thomasWeise.ultraGzip;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.optimizationBenchmarking.utils.io.paths.PathUtils;
import org.optimizationBenchmarking.utils.io.paths.predicates.CanExecutePredicate;
import org.optimizationBenchmarking.utils.io.paths.predicates.FileNamePredicate;
import org.optimizationBenchmarking.utils.io.paths.predicates.IsFilePredicate;
import org.optimizationBenchmarking.utils.predicates.AndPredicate;
import org.optimizationBenchmarking.utils.tools.impl.process.EProcessStream;
import org.optimizationBenchmarking.utils.tools.impl.process.ExternalProcess;
import org.optimizationBenchmarking.utils.tools.impl.process.ExternalProcessExecutor;

/**
 * The internal class for using the Python's GZIP implementation.
 */
final class _GZPython implements Runnable {

  /** the source name */
  private static final String FROM = "Python GZip Implementation"; //$NON-NLS-1$

  /** the GZIP executable */
  private static final Path __PYTHON_PATH = PathUtils.findFirstInPath(
      new AndPredicate<>(new FileNamePredicate(true, "python3"), //$NON-NLS-1$
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
  private _GZPython(final UltraGzipJob job) {
    super();
    this.m_owner = job;
  }

  /**
   * enqueue the java gzip jobs.
   *
   * @param job
   *          the owning job
   */
  static final void _enqueue(final UltraGzipJob job) {
    if (_GZPython.__PYTHON_PATH != null) {
      job._execute(new _GZPython(job));
    }
  }

  /** {@inheritDoc} */
  @Override
  public final void run() {
    byte[] compressed;
    _ERegistrationResult result;
    int retCode;

    if (_GZPython.__PYTHON_PATH == null) {
      return;
    }

    compressed = null;
    result = null;
    try {

      final Path tempFile = Files.createTempFile("gzipper", //$NON-NLS-1$
          "py");//$NON-NLS-1$
      try {
        Files.copy(_GZPython.class.getResourceAsStream("gzipper.py"), //$NON-NLS-1$
            tempFile, StandardCopyOption.REPLACE_EXISTING);

        try (final ExternalProcess ep = ExternalProcessExecutor
            .getInstance().use()//
            .setDirectory(PathUtils.getTempDir())//
            .setExecutable(_GZPython.__PYTHON_PATH)//
            .addPathArgument(tempFile) //
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
          result = this.m_owner._register(compressed, _GZPython.FROM);

          if ((retCode = ep.waitFor()) != 0) {
            this.m_owner._processError(retCode, _GZPython.FROM,
                _GZPython.__PYTHON_PATH);
          } else {
            result = null;
            compressed = null;
          }
        }

      } finally {
        Files.delete(tempFile);
      }
    } catch (final Throwable ioe) { // ignore!
      this.m_owner._error(ioe, _GZPython.FROM);
      result = null;
      compressed = null;
    }

    if ((result != null) && (compressed != null)
        && (result != _ERegistrationResult.INVALID)) {
      _ADVDEF._postprocess(this.m_owner, compressed, _GZPython.FROM);
    }
  }
}
