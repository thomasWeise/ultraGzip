package thomasWeise.tools;

import java.io.Closeable;
import java.io.IOException;

/**
 * A base class for processes.
 */
public abstract class AbstractProcess implements Closeable {

  /** create */
  protected AbstractProcess() {
    super();
  }

  /**
   * Wait until the process has finished and obtain its return
   * value.
   *
   * @return the return value
   * @throws IOException
   *           if i/o fails
   */
  public abstract int waitFor() throws IOException;

  /**
   * Terminate the process if it is still alive
   *
   * @throws IOException
   *           if i/o fails
   */
  @Override
  public abstract void close() throws IOException;
}
