package thomasWeise.tools;

import java.io.OutputStream;

/**
 * A thread shoveling data from a
 * {@link org.optimizationBenchmarking.utils.parallel.ByteProducerConsumerBuffer
 * buffer} to an {@link java.io.OutputStream} as long as
 * <code>{@link #m_mode}&le;{@link _WorkerThread#SHUTTING_DOWN}</code>
 * and the {@link #m_source buffer} has either not yet been
 * closed or data is available in it. As soon as
 * <code>{@link #m_mode}&ge;{@link _WorkerThread#KILLED}</code>,
 * it will cease all activity.
 */
final class _BufferToOutputStream extends _WorkerThread {

  /** the source */
  private final ByteProducerConsumerBuffer m_source;
  /** the destination */
  private final OutputStream m_dest;

  /**
   * create
   *
   * @param dest
   *          the destination
   * @param source
   *          the source
   */
  _BufferToOutputStream(final OutputStream dest,
      final ByteProducerConsumerBuffer source) {
    super("Buffer-to-OutputStream"); //$NON-NLS-1$
    this.m_dest = dest;
    this.m_source = source;
  }

  /** {@inheritDoc} */
  @Override
  public final void run() {
    byte[] buffer;
    int s;

    buffer = new byte[4096];
    try {
      try {
        while (this.m_mode <= _WorkerThread.SHUTTING_DOWN) {
          s = this.m_source.readFromBuffer(buffer, 0,
              buffer.length);
          if (s <= 0) {
            break;
          }
          this.m_dest.write(buffer, 0, s);
          this.m_dest.flush();
        }
      } finally {
        try {
          this.m_dest.close();
        } finally {
          this.m_source.close();
        }
      }
    } catch (final Throwable t) {
      ConsoleIO.stderr(
          "Error during shoveling bytes from byte buffer to external process.", //$NON-NLS-1$
          t);
      throw new RuntimeException(t);
    }
  }
}
