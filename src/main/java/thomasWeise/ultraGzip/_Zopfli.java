package thomasWeise.ultraGzip;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import thomasWeise.tools.ByteBuffers;
import thomasWeise.tools.Configuration;
import thomasWeise.tools.EProcessStream;
import thomasWeise.tools.ExternalProcess;
import thomasWeise.tools.ExternalProcessBuilder;
import thomasWeise.tools.ExternalProcessExecutor;
import thomasWeise.tools.TempDir;

/**
 * The internal class for using the operating system's zopfli
 * implementation.
 */
final class _Zopfli implements Runnable {

  /** the source name */
  private static final String FROM = "Zopfli installation"; //$NON-NLS-1$
  /** the argument */
  static final String ARG = "zopfli"; //$NON-NLS-1$
  /** the GZIP executable */
  private static final Path __ZOPFLI_PATH =
      Configuration.getExecutable(_Zopfli.ARG);

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
      path =
          Files.createTempFile(td.getPath(), "zopfli", ".bin");//$NON-NLS-1$//$NON-NLS-2$

      try (final OutputStream os = Files.newOutputStream(path)) {
        os.write(this.m_owner.m_data);
      }

      epb = ExternalProcessExecutor.getInstance().get();
      epb.setDirectory(td.getPath());
      epb.setExecutable(_Zopfli.__ZOPFLI_PATH);
      epb.setDirectory(td.getPath());
      epb.addStringArgument("-c"); //$NON-NLS-1$
      epb.addStringArgument("--gzip"); //$NON-NLS-1$
      epb.addStringArgument(
          "--i" + ((UltraGzip._getIntensity() + 1) * 250)); //$NON-NLS-1$
      epb.addPathArgument(path);

      epb.setStdErr(EProcessStream.INHERIT);
      epb.setStdIn(EProcessStream.IGNORE);
      epb.setStdOut(EProcessStream.AS_STREAM);

      try (final ExternalProcess ep = epb.get()) {
        epb = null;

        try (final OutputStream os = ep.getStdIn()) {
          os.write(this.m_owner.m_data);
        }

        compressed = ByteBuffers.get().load(ep.getStdOut());
        result =
            this.m_owner._register(compressed, _Zopfli.FROM);

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
      _ADVDEF._postprocess(this.m_owner, compressed,
          _Zopfli.FROM);
    }
  }
}
