package thomasWeise.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** A buffer to be used to store re-useable stuff */
public final class ByteBuffers {

  /** the getter for the buffer */
  private static final ThreadLocal<ByteBuffers> GET =
      new __ThreadLocal();

  /** the output buffer */
  private final __BOS m_outputBuffer;

  /** a byte buffer */
  private final byte[] m_byteBuffer;

  /** create the byte array output stream */
  ByteBuffers() {
    super();
    this.m_outputBuffer = new __BOS();
    this.m_byteBuffer = new byte[8192];
  }

  /**
   * get the buffered output stream
   *
   * @return the buffered output stream
   */
  public final ByteArrayOutputStream getBufferedOutputStream() {
    this.m_outputBuffer.reset();
    return this.m_outputBuffer;
  }

  /**
   * Load a given file into memory. This method will invalidate
   * the contents of {@link #getBufferedOutputStream()}.
   *
   * @param path
   *          the path to load
   * @return the bytes read
   * @throws IOException
   *           if i/o fails
   */
  public final byte[] load(final Path path) throws IOException {
    try (final InputStream is = Files.newInputStream(path)) {
      return this.load(is);
    }
  }

  /**
   * Load a given stream into memory. This method will invalidate
   * the contents of {@link #getBufferedOutputStream()} .
   *
   * @param is
   *          the stream to load
   * @return the bytes read
   * @throws IOException
   *           if i/o fails
   */
  public final byte[] load(final InputStream is)
      throws IOException {
    return this.__load(is).toByteArray();
  }

  /**
   * load the given stream
   *
   * @param is
   *          the stream
   * @return the filled buffer
   * @throws IOException
   *           if i/o fails
   */
  private final __BOS __load(final InputStream is)
      throws IOException {
    final byte[] buffer;
    final __BOS os;
    int read;

    buffer = this.m_byteBuffer;
    os = this.m_outputBuffer;
    os.reset();
    while ((read = is.read(buffer)) > 0) {
      os.write(buffer, 0, read);
    }
    return os;
  }

  /**
   * Compare the given input stream to the given data array
   *
   * @param is
   *          the input stream
   * @param data
   *          the data array
   * @return {@code true} if the contents of the stream are the
   *         same as the contents of the data array,
   *         {@code false} otherwise
   * @throws IOException
   *           if i/o fails
   */
  public final boolean compare(final InputStream is,
      final byte[] data) throws IOException {
    return this.__load(is)._compare(data);
  }

  // /**
  // * get the byte array of at least the given size
  // *
  // * @param minSize
  // * the minimum required size
  // * @return the byte array
  // */
  // final byte[] _getByteArray(final int minSize) {
  // byte[] b;
  // int size;
  //
  // b = this.m_byteBuffer;
  // if ((b == null) || (b.length < minSize)) {
  // if (minSize < 8192) {
  // size = 8192;
  // } else {
  // if (minSize < (Integer.MAX_VALUE >>> 3)) {
  // size = (minSize << 1);
  // } else {
  // size = minSize;
  // }
  // }
  // this.m_byteBuffer = b = new byte[size];
  // }
  //
  // return b;
  // }
  /**
   * Get the thread local buffer.
   *
   * @return the data.
   */
  public static final ByteBuffers get() {
    return ByteBuffers.GET.get();
  }

  /** create */
  private static final class __ThreadLocal
      extends ThreadLocal<ByteBuffers> {
    /** create the thread local buffer local */
    __ThreadLocal() {
      super();
    }

    /** {@inheritDoc} */
    @Override
    protected final ByteBuffers initialValue() {
      return new ByteBuffers();
    }
  }

  /** the internal byte array output stream */
  static final class __BOS extends ByteArrayOutputStream {

    /** create */
    __BOS() {
      super(8192);
    }

    /**
     * comare the data
     *
     * @param data
     *          the data
     * @return {@code true} if the contents of this buffer are
     *         the same as the given data, {@code false}
     *         otherwise
     */
    final boolean _compare(final byte[] data) {
      final byte[] cmp;
      int i;

      i = data.length;
      if (i != this.count) {
        return false;
      }

      i = -1;
      cmp = this.buf;
      for (final byte b : data) {
        if (b != cmp[++i]) {
          return false;
        }
      }

      return true;
    }
  }
}
