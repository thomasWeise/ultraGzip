package thomasWeise.ultraGzip;

import org.optimizationBenchmarking.utils.text.TextUtils;
import org.optimizationBenchmarking.utils.tools.impl.abstr.ToolJobBuilder;

/** Build a job for the ultrag gzip. */
public final class UltraGzipJobBuilder
    extends ToolJobBuilder<UltraGzipJob, UltraGzipJobBuilder> {

  /** the data */
  private byte[] m_data;

  /** the job's name */
  private String m_name;

  /** create */
  UltraGzipJobBuilder() {
    super();
  }

  /**
   * Check a name string
   *
   * @param name
   *          the name string
   * @return the prepared name
   */
  static final String _checkName(final String name) {
    final String str;

    str = TextUtils.prepare(name);
    if (str == null) {
      throw new IllegalArgumentException(//
          "Name cannot be empty string, null, or just composed white space, but '" //$NON-NLS-1$
              + name + "' falls into this category."); //$NON-NLS-1$
    }
    return str;
  }

  /**
   * Set the name of the data element to be packed
   *
   * @param name
   *          the name of the data element to be packed
   * @return this builder
   */
  public final UltraGzipJobBuilder setName(final String name) {
    this.m_name = UltraGzipJobBuilder._checkName(name);
    return this;
  }

  /**
   * Check the data to be compressed.
   *
   * @param data
   *          the data
   */
  static final void _checkData(final byte[] data) {
    if (data == null) {
      throw new IllegalArgumentException(
          "Data to be gzip-compressed cannot be null."); //$NON-NLS-1$
    }
    if (data.length <= 0) {
      throw new IllegalArgumentException(
          "The data to be gzip-compressed cannot be zero-lengthed.");//$NON-NLS-1$
    }
  }

  /**
   * Set the data to be compressed
   *
   * @param data
   *          the data to be compressed
   * @return this builder
   */
  public final UltraGzipJobBuilder setData(final byte[] data) {
    UltraGzipJobBuilder._checkData(data);
    this.m_data = data;
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public final UltraGzipJob create() {
    return new UltraGzipJob(this.m_data, this.m_name, this.getLogger());
  }
}
