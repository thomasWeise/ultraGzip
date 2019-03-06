package thomasWeise.ultraGzip;

import java.util.function.Supplier;

/** The ultra gzip tool. */
public final class UltraGzip
    implements Supplier<UltraGzipJobBuilder> {

  /** create */
  UltraGzip() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    return "Ultra Gzip"; //$NON-NLS-1$
  }

  /** {@inheritDoc} */
  @Override
  public final UltraGzipJobBuilder get() {
    return new UltraGzipJobBuilder();
  }

  /**
   * Get the globally shared instance of the Ultra Gzip tool
   *
   * @return the globally shared instance of the Ultra Gzip tool
   */
  public static final UltraGzip getInstance() {
    return __UltraGzipHolder.INSTANCE;
  }

  /** the ultra gzip tool */
  private static final class __UltraGzipHolder {
    /** the globally shared instance */
    static final UltraGzip INSTANCE = new UltraGzip();
  }
}
