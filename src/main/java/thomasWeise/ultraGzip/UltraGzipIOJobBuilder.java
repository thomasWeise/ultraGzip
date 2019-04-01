package thomasWeise.ultraGzip;

import thomasWeise.tools.IOJobBuilder;

/** Build a job for the ultrag gzip I/O tool. */
public final class UltraGzipIOJobBuilder extends IOJobBuilder {

  /** create */
  public UltraGzipIOJobBuilder() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public final UltraGzipIOJob get() {
    return new UltraGzipIOJob(this);
  }
}
