package thomasWeise.ultraGzip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.jcraft.jzlib.GZIPOutputStream;

import thomasWeise.tools.ByteBuffers;

/** The internal class for JZLib's GZIP jobs. */
final class _JZLibGZip implements Runnable {

  /** the source name */
  private static final String FROM = "JZLib GZIP implementation"; //$NON-NLS-1$

  /** the quality range */
  private static final int[] QUALITY =
      UltraGzip._qualityRange(1, 9, 7);

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
  private _JZLibGZip(final UltraGzipJob job, final int quality) {
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
    for (final int quality : _JZLibGZip.QUALITY) {
      job._execute(new _JZLibGZip(job, quality));
    }
  }

  /** {@inheritDoc} */
  @Override
  public final void run() {
    final ByteBuffers buffers;
    byte[] compressed;
    _ERegistrationResult res;

    buffers = ByteBuffers.get();
    res = _ERegistrationResult.INVALID;
    compressed = null;
    try (final ByteArrayOutputStream bos =
        buffers.getBufferedOutputStream()) {
      try (final GZIPOutputStream gzo =
          new __JZLibGZIPOutputStream(bos,
              this.m_owner.m_data.length, this.m_quality)) {
        gzo.write(_JZLibGZip.this.m_owner.m_data);
      }

      compressed = bos.toByteArray();
      res = _JZLibGZip.this.m_owner._register(compressed,
          _JZLibGZip.FROM);
    } catch (final Throwable error) {
      compressed = null;
      res = _ERegistrationResult.INVALID;
      this.m_owner._error(error, _JZLibGZip.FROM);
    }

    if ((compressed != null) && (res != null)
        && (res != _ERegistrationResult.INVALID)) {
      _ADVDEF._postprocess(this.m_owner, compressed,
          _JZLibGZip.FROM);
    }
  }

  /** the internal gzip class */
  private static final class __JZLibGZIPOutputStream
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
    __JZLibGZIPOutputStream(final ByteArrayOutputStream _out,
        final int size, final int level) throws IOException {
      super(_out, new com.jcraft.jzlib.Deflater(level, 15 + 16),
          size, false);
      this.mydeflater = true;
    }
  }
}
