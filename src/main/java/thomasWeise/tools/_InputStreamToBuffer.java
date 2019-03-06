package thomasWeise.tools;

import java.io.InputStream;

/**
 * A thread shoveling data from an {@link java.io.InputStream} to
 * a
 * {@link org.optimizationBenchmarking.utils.parallel.ByteProducerConsumerBuffer
 * buffer} as long as
 * <code>{@link #m_mode}=={@link _WorkerThread#ALIVE}</code>. As
 * soon as
 * <code>{@link #m_mode}&ge;{@link _WorkerThread#SHUTTING_DOWN}</code>,
 * the data from the input stream will be
 * {@link java.io.InputStream#skip(long) skipped} over and
 * discarted. If
 * <code>{@link #m_mode}&ge;{@link _WorkerThread#KILLED}</code>,
 * all activity is ceased.
 */
final class _InputStreamToBuffer extends _WorkerThread {

  /** the destination */
  private final ByteProducerConsumerBuffer m_dest;
  /** the source */
  private final InputStream m_source;

  /**
   * create
   *
   * @param dest
   *          the destination
   * @param source
   *          the source
   */
  _InputStreamToBuffer(final ByteProducerConsumerBuffer dest,
      final InputStream source) {
    super("InputStream-to-Buffer"); //$NON-NLS-1$
    this.m_dest = dest;
    this.m_source = source;
  }

  /** {@inheritDoc} */
  @Override
  public final void run() {
    byte[] buffer;
    int s;

    try {
      try {
        try {
          buffer = new byte[4096];
          while (this.m_mode <= _WorkerThread.SHUTTING_DOWN) {
            s = this.m_source.read(buffer);
            if (s < 0) {
              break;
            }
            if (s > 0) {
              if (this.m_mode <= _WorkerThread.ALIVE) {
                this.m_dest.writeToBuffer(buffer, 0, s);
              }
            }
          }
          buffer = null;
        } finally {
          this.m_dest.close();
        }
      } finally {
        this.m_source.close();
      }
    } catch (final Throwable t) {
      ConsoleIO.stderr(
          "Error during shoveling bytes from external process to byte buffer.", //$NON-NLS-1$
          t);
      throw new RuntimeException(t);
    }
  }
}
