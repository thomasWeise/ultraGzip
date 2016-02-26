package thomasWeise.ultraGzip;

import org.optimizationBenchmarking.utils.tools.impl.abstr.ToolSuite;

/** A tool which provdes UltraGzip */
public final class UltraGzipIO extends ToolSuite {

  /** create */
  UltraGzipIO() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public final boolean canUse() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public final UltraGzipIOJobBuilder use() {
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
