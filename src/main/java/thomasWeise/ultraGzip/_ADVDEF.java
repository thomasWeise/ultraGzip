package thomasWeise.ultraGzip;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import thomasWeise.tools.Configuration;
import thomasWeise.tools.EProcessStream;
import thomasWeise.tools.ExternalProcess;
import thomasWeise.tools.ExternalProcessExecutor;
import thomasWeise.tools.TempDir;

/** The compressor class used Advanced Computing. */
final class _ADVDEF implements Runnable {

  /** the advdef executable */
  private static final Path __ADVDEF_PATH =
      Configuration.getExecutable("advdef"); //$NON-NLS-1$

  /** the source name */
  private static final String FROM = "AdvanceComp"; //$NON-NLS-1$
  /** the prefix name */
  private static final String FROM_PREFIX =
      _ADVDEF.FROM + " recompressing results of "; //$NON-NLS-1$

  /** the job */
  private final UltraGzipJob m_owner;

  /** the data to compress */
  private final byte[] m_data;

  /** the data source */
  private final String m_source;

  /** the compression quality */
  private final int m_quality;

  /**
   * create the Java advdef job.
   *
   * @param job
   *          the owning job
   * @param quality
   *          the compression quality
   * @param data
   *          the data
   * @param source
   *          the source
   */
  private _ADVDEF(final UltraGzipJob job, final int quality,
      final byte[] data, final String source) {
    super();
    this.m_owner = job;
    this.m_quality = quality;
    this.m_data = data;
    this.m_source = source;
  }

  /**
   * post-process the results of another archiver
   *
   * @param job
   *          the job
   * @param data
   *          the data
   * @param source
   *          the source job
   */
  static final void _postprocess(final UltraGzipJob job,
      final byte[] data, final String source) {
    if (_ADVDEF.__ADVDEF_PATH != null) {
      for (int quality = 3; quality <= 4; quality++) {
        job._execute(new _ADVDEF(job, quality, data,
            _ADVDEF.FROM_PREFIX + source));
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public final void run() {
    final Path tempFile;
    final int retCode;

    if (_ADVDEF.__ADVDEF_PATH == null) {
      return;
    }

    try (final TempDir temp = new TempDir()) {

      tempFile = Files.createTempFile(temp.getPath(), "advdef", //$NON-NLS-1$
          ".gz"); //$NON-NLS-1$
      try (final OutputStream os =
          Files.newOutputStream(tempFile)) {
        os.write(this.m_data);
      }

      try (final ExternalProcess ep =
          ExternalProcessExecutor.getInstance().get()//
              .setDirectory(temp.getPath())//
              .setExecutable(_ADVDEF.__ADVDEF_PATH)
              .addStringArgument("-" + this.m_quality) //$NON-NLS-1$
              .addStringArgument("-i 64") //$NON-NLS-1$
              .addStringArgument("-z")//$NON-NLS-1$
              .addStringArgument("-q") //$NON-NLS-1$
              .addPathArgument(tempFile)//
              .setStdErr(EProcessStream.INHERIT)//
              .setStdIn(EProcessStream.IGNORE)//
              .setStdOut(EProcessStream.INHERIT)//
              .setMergeStdOutAndStdErr(true)//
              .get()) {

        if ((retCode = ep.waitFor()) != 0) {
          this.m_owner._processError(retCode, this.m_source,
              _ADVDEF.__ADVDEF_PATH);
          return;
        }

        if (this.m_owner
            ._isPromising(Files.readAttributes(tempFile, //
                BasicFileAttributes.class).size())) {
          this.m_owner._register(_Buffers._get()._load(tempFile),
              this.m_source);
        }
      }
    } catch (final Throwable error) { // the error
      this.m_owner._error(error, this.m_source);
    }
  }
}
