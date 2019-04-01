package thomasWeise.tools;

import java.nio.file.Path;
import java.util.function.Supplier;

/** Build a job for the an I/O tool. */
public abstract class IOJobBuilder extends _IOJobBase
    implements Supplier<IOJob> {

  /** use the standard in */
  public static final String PARAM_STDIN = "si"; //$NON-NLS-1$

  /** the input path */
  public static final String PARAM_IN = "in";//$NON-NLS-1$

  /** use the standard out */
  public static final String PARAM_STDOUT = "so"; //$NON-NLS-1$

  /** the output path */
  public static final String PARAM_OUT = "out";//$NON-NLS-1$

  /** create */
  protected IOJobBuilder() {
    super();
  }

  /**
   * configure the job from a configuration
   *
   * @return this instance
   */
  public IOJobBuilder configure() {
    Configuration.synchronizedConfig(() -> {
      if (Configuration.getBoolean(//
          IOJobBuilder.PARAM_STDIN)) {
        this.setUseStdIn(true);
      }

      Path path = Configuration.getPath(//
          IOJobBuilder.PARAM_IN);
      if (path != null) {
        this.setInputPath(path);
      }

      if (Configuration.getBoolean(//
          IOJobBuilder.PARAM_STDOUT)) {
        this.setUseStdOut(true);
      }
      path = Configuration.getPath(//
          IOJobBuilder.PARAM_OUT);
      if (path != null) {
        this.setOutputPath(path);
      }
    });

    return this;
  }

  /**
   * Set the input path
   *
   * @param path
   *          the path
   * @return this builder
   */
  public final IOJobBuilder setInputPath(final Path path) {
    final Path use;
    if (path == null) {
      throw new IllegalArgumentException(
          "Input path cannot be null"); //$NON-NLS-1$
    }
    use = path.normalize().toAbsolutePath().normalize();
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
   * Set whether data should be loaded from stdin instead from a
   * file
   *
   * @param useStdIn
   *          the standard input choice
   * @return this builder
   */
  public IOJobBuilder setUseStdIn(final boolean useStdIn) {
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
  public IOJobBuilder setOutputPath(final Path path) {
    final Path use;
    if (path == null) {
      throw new IllegalArgumentException(
          "Output path cannot be null"); //$NON-NLS-1$
    }
    use = path.normalize().toAbsolutePath().normalize();
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
   * Set whether data should be loaded from stdout instead of a
   * file
   *
   * @param useStdOut
   *          the standard input choice
   * @return this builder
   */
  public IOJobBuilder setUseStdOut(final boolean useStdOut) {
    if (useStdOut) {
      this.m_output = null;
    }
    this.m_useStdOut = useStdOut;
    return this;
  }
}
