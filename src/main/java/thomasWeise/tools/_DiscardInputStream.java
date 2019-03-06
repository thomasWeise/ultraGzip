package thomasWeise.tools;

import java.io.InputStream;

/**
 * A thread shoveling data from an {@link java.io.InputStream} to
 * the Nirvana, by {@link java.io.InputStream#skip(long)
 * skipping} over it as long as
 * <code>{@link #m_mode}&le;{@link _WorkerThread#SHUTTING_DOWN}</code>.
 * As soon as
 * <code>{@link #m_mode}&ge;{@link _WorkerThread#KILLED}</code>,
 * it will cease all activity.
 */
final class _DiscardInputStream extends _WorkerThread {

  /** the source */
  private final InputStream m_source;

  /**
   * create
   *
   * @param source
   *          the source
   * @param log
   *          the logger
   */
  _DiscardInputStream(final InputStream source) {
    super("Discard-InputStream"); //$NON-NLS-1$
    this.m_source = source;
  }

  /** {@inheritDoc} */
  @Override
  public final void run() {
    byte[] buffer;

    try {
      buffer = new byte[4096];
      while (this.m_mode <= _WorkerThread.SHUTTING_DOWN) {
        if (this.m_source.read(buffer) <= 0) {
          break;
        }
      }
      buffer = null;
    } catch (final Throwable t) {
      ConsoleIO.stderr(
          "Error during discarding input stream (by skipping).", //$NON-NLS-1$
          t);
      throw new RuntimeException(t);
    }
  }
}
