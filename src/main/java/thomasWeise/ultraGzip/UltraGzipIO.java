package thomasWeise.ultraGzip;

import java.util.function.Supplier;

/** A tool which provdes UltraGzip */
public final class UltraGzipIO
    implements Supplier<UltraGzipIOJobBuilder> {

  /** create */
  UltraGzipIO() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public final UltraGzipIOJobBuilder get() {
    return new UltraGzipIOJobBuilder();
  }

  /**
   * Get the globally shared instance of the Ultra Gzip tool
   *
   * @return the globally shared instance of the Ultra Gzip tool
   */
  public static final UltraGzipIO getInstance() {
    return __UltraGzipHolder.INSTANCE;
  }

  /** the ultra gzip tool */
  private static final class __UltraGzipHolder {
    /** the globally shared instance */
    static final UltraGzipIO INSTANCE = new UltraGzipIO();
  }
}
