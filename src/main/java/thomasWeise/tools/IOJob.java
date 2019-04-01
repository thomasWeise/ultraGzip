package thomasWeise.tools;

/** The job for an I/O tool. */
public abstract class IOJob extends _IOJobBase
    implements Runnable {

  /**
   * create
   *
   * @param ugo
   *          the job builder
   */
  protected IOJob(final IOJobBuilder ugo) {
    super();

    this.m_input = ugo.m_input;
    this.m_useStdIn = ugo.m_useStdIn;
    this.m_output = ugo.m_output;
    this.m_useStdOut = ugo.m_useStdOut;
    _IOJobBase.validate(this.m_input, this.m_useStdIn,
        this.m_output, this.m_useStdOut);
  }
}
