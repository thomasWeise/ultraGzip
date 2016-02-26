package thomasWeise.ultraGzip;

import java.nio.file.Path;

import org.optimizationBenchmarking.utils.config.Configuration;
import org.optimizationBenchmarking.utils.io.paths.PathUtils;
import org.optimizationBenchmarking.utils.tools.impl.abstr.ConfigurableToolJobBuilder;

/** Build a job for the ultrag gzip I/O tool. */
public final class UltraGzipIOJobBuilder extends
    ConfigurableToolJobBuilder<UltraGzipIOJob, UltraGzipIOJobBuilder> {

  /** use the standard in */
  static final String PARAM_STDIN = "si"; //$NON-NLS-1$

  /** the input path */
  static final String PARAM_IN = "in";//$NON-NLS-1$

  /** use the standard out */
  static final String PARAM_STDOUT = "so"; //$NON-NLS-1$

  /** the output path */
  static final String PARAM_OUT = "out";//$NON-NLS-1$

  /** the input path */
  Path m_input;

  /** should we use stdin instead of an input file? */
  boolean m_useStdIn;

  /** the output path */
  Path m_output;

  /** should we use stdout instead of an output file? */
  boolean m_useStdOut;

  /** create */
  UltraGzipIOJobBuilder() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public final UltraGzipIOJobBuilder configure(
      final Configuration config) {
    Path path;

    super.configure(config);

    if (config.getBoolean(UltraGzipIOJobBuilder.PARAM_STDIN, false)) {
      this.setUseStdIn(true);
    }
    path = config.getPath(UltraGzipIOJobBuilder.PARAM_IN, null);
    if (path != null) {
      this.setInputPath(path);
    }

    if (config.getBoolean(UltraGzipIOJobBuilder.PARAM_STDOUT, false)) {
      this.setUseStdOut(true);
    }
    path = config.getPath(UltraGzipIOJobBuilder.PARAM_OUT, null);
    if (path != null) {
      this.setOutputPath(path);
    }

    return this;
  }

  /**
   * Set the input path
   *
   * @param path
   *          the path
   * @return this builder
   */
  public final UltraGzipIOJobBuilder setInputPath(final Path path) {
    final Path use;
    if (path == null) {
      throw new IllegalArgumentException("Input path cannot be null"); //$NON-NLS-1$
    }
    use = PathUtils.normalize(path);
    if (use == null) {
      throw new IllegalArgumentException(//
          "Input path cannot normalize to null, but '" + //$NON-NLS-1$
              path + "' does."); //$NON-NLS-1$
    }
    this.m_input = use;
    this.m_useStdIn = false;
    return this;
  }

  /**
   * Set whether data should be loaded from stdin instead from a file
   *
   * @param useStdIn
   *          the standard input choice
   * @return this builder
   */
  public final UltraGzipIOJobBuilder setUseStdIn(final boolean useStdIn) {
    if (useStdIn) {
      this.m_input = null;
    }
    this.m_useStdIn = useStdIn;
    return this;
  }

  /**
   * Set the ouput path
   *
   * @param path
   *          the path
   * @return this builder
   */
  public final UltraGzipIOJobBuilder setOutputPath(final Path path) {
    final Path use;
    if (path == null) {
      throw new IllegalArgumentException("Output path cannot be null"); //$NON-NLS-1$
    }
    use = PathUtils.normalize(path);
    if (use == null) {
      throw new IllegalArgumentException(//
          "Output path cannot normalize to null, but '" + //$NON-NLS-1$
              path + "' does."); //$NON-NLS-1$
    }
    this.m_output = use;
    this.m_useStdOut = false;
    return this;
  }

  /**
   * Set whether data should be loaded from stdout instead of a file
   *
   * @param useStdOut
   *          the standard input choice
   * @return this builder
   */
  public final UltraGzipIOJobBuilder setUseStdOut(
      final boolean useStdOut) {
    if (useStdOut) {
      this.m_output = null;
    }
    this.m_useStdOut = useStdOut;
    return this;
  }

  /**
   * Validate
   *
   * @param input
   *          the input path
   * @param stdin
   *          use stdin instead?
   * @param output
   *          the output path
   * @param stdout
   *          use stdout instead?
   */
  static final void _validate(final Path input, final boolean stdin,
      final Path output, final boolean stdout) {
    if (stdin) {
      if (input != null) {
        throw new IllegalArgumentException(
            "Cannot use both, stdin and path '" + input + '\'' + '.'); //$NON-NLS-1$
      }
    } else {
      if (input == null) {
        throw new IllegalArgumentException(
            "Must either specify input file or stdin."); //$NON-NLS-1$
      }
    }
    if (stdout) {
      if (output != null) {
        throw new IllegalArgumentException(
            "Cannot use both, stdout and path '" + output + '\'' + '.'); //$NON-NLS-1$
      }
    } else {
      if (output == null) {
        throw new IllegalArgumentException(
            "Must either specify output file or stdout."); //$NON-NLS-1$
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  protected final void validate() {
    super.validate();
    UltraGzipIOJobBuilder._validate(this.m_input, this.m_useStdIn,
        this.m_output, this.m_useStdOut);
  }

  /** {@inheritDoc} */
  @Override
  public final UltraGzipIOJob create() {
    this.validate();
    return new UltraGzipIOJob(this);
  }
}
