package thomasWeise.tools;

import java.nio.file.Path;

/** the base class for I/O jobs and their builders */
class _IOJobBase {

  /** the input path */
  Path m_input;

  /** should we use stdin instead of an input file? */
  boolean m_useStdIn;

  /** the output path */
  Path m_output;

  /** should we use stdout instead of an output file? */
  boolean m_useStdOut;

  /** create */
  _IOJobBase() {
    super();
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
  protected static final void validate(final Path input,
      final boolean stdin, final Path output,
      final boolean stdout) {
    if (stdin) {
      if (input != null) {
        throw new IllegalArgumentException(
            "Cannot use both, stdin and path '" + input + '\'' //$NON-NLS-1$
                + '.');
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
            "Cannot use both, stdout and path '" + output + '\'' //$NON-NLS-1$
                + '.');
      }
    } else {
      if (output == null) {
        throw new IllegalArgumentException(
            "Must either specify output file or stdout."); //$NON-NLS-1$
      }
    }
  }

  /**
   * get the input path
   *
   * @return the input path
   */
  public final Path getInputPath() {
    return this.m_input;
  }

  /**
   * Get whether data should be loaded from stdin instead from a
   * file
   *
   * @return the standard input choice
   */
  public final boolean isUsingStdIn() {
    return this.m_useStdIn;
  }

  /**
   * Set the ouput path
   *
   * @return the path
   */
  public final Path getOutputPath() {
    return this.m_output;
  }

  /**
   * Get whether data should be loaded from stdout instead from a
   * file
   *
   * @return the standard output choice
   */
  public final boolean isUsingStdOut() {
    return this.m_useStdIn;
  }
}
