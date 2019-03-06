package thomasWeise.ultraGzip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/** The internal class for Java's GZIP jobs. */
final class _JavaGZip implements Runnable {

  /** the source name */
  private static final String FROM =
      "Java's GZIP implementation"; //$NON-NLS-1$

  /** the job */
  private final UltraGzipJob m_owner;

  /** the compression quality */
  private final int m_quality;

  /**
   * create the Java Gzip job.
   *
   * @param job
   *          the owning job
   * @param quality
   *          the compression quality
   */
  _JavaGZip(final UltraGzipJob job, final int quality) {
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
    for (int quality = 7; quality <= 9; quality++) {
      job._execute(new _JavaGZip(job, quality));
    }
  }

  /** {@inheritDoc} */
  @Override
  public final void run() {
    final _Buffers buffers;
    byte[] compressed;
    _ERegistrationResult res;

    buffers = _Buffers._get();
    res = _ERegistrationResult.INVALID;
    compressed = null;
    try (final ByteArrayOutputStream bos =
        buffers._getBufferedOutputStream()) {
      try (final GZIPOutputStream gzo =
          new __JavaGZIPOutputStream(bos,
              this.m_owner.m_data.length, this.m_quality)) {
        gzo.write(_JavaGZip.this.m_owner.m_data);
      }

      compressed = bos.toByteArray();
      res = _JavaGZip.this.m_owner._register(compressed,
          _JavaGZip.FROM);
    } catch (final Throwable error) {
      compressed = null;
      res = _ERegistrationResult.INVALID;
      this.m_owner._error(error, _JavaGZip.FROM);
    }

    if ((compressed != null) && (res != null)
        && (res != _ERegistrationResult.INVALID)) {
      _ADVDEF._postprocess(this.m_owner, compressed,
          _JavaGZip.FROM);
    }
  }

  /** the internal gzip class */
  private static final class __JavaGZIPOutputStream
      extends GZIPOutputStream {

    /**
     * Create the stream
     *
     * @param _out
     *          the output stream to write to
     * @param size
     *          the size
     * @param level
     *          the level to use
     * @throws IOException
     *           if something goes wrong
     */
    __JavaGZIPOutputStream(final ByteArrayOutputStream _out,
        final int size, final int level) throws IOException {
      super(_out, size, false);
      this.def.setLevel(level);
    }
  }
}
