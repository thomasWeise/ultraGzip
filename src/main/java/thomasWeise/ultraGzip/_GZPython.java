package thomasWeise.ultraGzip;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import thomasWeise.tools.Configuration;
import thomasWeise.tools.EProcessStream;
import thomasWeise.tools.ExternalProcess;
import thomasWeise.tools.ExternalProcessExecutor;
import thomasWeise.tools.TempDir;

/**
 * The internal class for using the Python's GZIP implementation.
 */
final class _GZPython implements Runnable {

  /** the source name */
  private static final String FROM =
      "Python GZip Implementation"; //$NON-NLS-1$

  /** the GZIP executable */
  private static final Path __PYTHON_PATH =
      Configuration.getExecutable("python3"); //$NON-NLS-1$

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
    try (final TempDir temp = new TempDir()) {

      final Path tempFile =
          Files.createTempFile(temp.getPath(), "gzipper", //$NON-NLS-1$
              "py");//$NON-NLS-1$

      Files.copy(
          _GZPython.class.getResourceAsStream("gzipper.py"), //$NON-NLS-1$
          tempFile, StandardCopyOption.REPLACE_EXISTING);

      try (final ExternalProcess ep =
          ExternalProcessExecutor.getInstance().get()//
              .setDirectory(temp.getPath())//
              .setExecutable(_GZPython.__PYTHON_PATH)//
              .addPathArgument(tempFile) //
              .setStdErr(EProcessStream.INHERIT)//
              .setStdIn(EProcessStream.AS_STREAM)//
              .setStdOut(EProcessStream.AS_STREAM)//
              .setDirectory(temp.getPath())//
              .get()) {

        try (final OutputStream os = ep.getStdIn()) {
          os.write(this.m_owner.m_data);
        }

        compressed = _Buffers._get()._load(ep.getStdOut());
        result =
            this.m_owner._register(compressed, _GZPython.FROM);

        if ((retCode = ep.waitFor()) != 0) {
          this.m_owner._processError(retCode, _GZPython.FROM,
              _GZPython.__PYTHON_PATH);
        } else {
          result = null;
          compressed = null;
        }
      }

    } catch (final Throwable ioe) { // ignore!
      this.m_owner._error(ioe, _GZPython.FROM);
      result = null;
      compressed = null;
    }

    if ((result != null) && (compressed != null)
        && (result != _ERegistrationResult.INVALID)) {
      _ADVDEF._postprocess(this.m_owner, compressed,
          _GZPython.FROM);
    }
  }
}
