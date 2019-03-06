package thomasWeise.ultraGzip;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import thomasWeise.tools.ConsoleIO;

/** The job for the ultra gzip I/O tool. */
public final class UltraGzipIOJob implements Runnable {

  /** the input path */
  private final Path m_input;

  /** should we use stdin instead of an input file? */
  private final boolean m_useStdIn;

  /** the output path */
  private final Path m_output;

  /** should we use stdout instead of an output file? */
  private final boolean m_useStdOut;

  /**
   * create
   *
   * @param ugo
   *          the job builder
   */
  UltraGzipIOJob(final UltraGzipIOJobBuilder ugo) {
    super();

    this.m_input = ugo.m_input;
    this.m_useStdIn = ugo.m_useStdIn;
    this.m_output = ugo.m_output;
    this.m_useStdOut = ugo.m_useStdOut;
    UltraGzipIOJobBuilder._validate(this.m_input,
        this.m_useStdIn, this.m_output, this.m_useStdOut);
  }

  /** run! */
  @Override
  public final void run() {
    byte[] data;
    int size;
    String name;

    if (this.m_useStdIn) {
      name = "stdin"; //$NON-NLS-1$
    } else {
      name = this.m_input.getFileName().toString();
    }
    if (this.m_useStdOut) {
      name += "->stdout"; //$NON-NLS-1$
    } else {
      name = (((name + '-') + '>')
          + this.m_output.getFileName().toString());
    }

    ConsoleIO.stdout(name + " is now loading input data."); //$NON-NLS-1$
    try {

      try (final InputStream is = (this.m_useStdIn ? System.in//
          : Files.newInputStream(this.m_input))) {
        data = _Buffers._get()._load(is);
      }

      size = data.length;
      ConsoleIO.stdout(name + " has loaded " + size//$NON-NLS-1$
          + "B of input data - now compressing."); //$NON-NLS-1$

      data = UltraGzip.getInstance().get().setData(data)//
          .setName(name).get().call();

      ConsoleIO.stdout(name + " has compressed the " + size //$NON-NLS-1$
          + "B of input data down to " + data.length + //$NON-NLS-1$
          "B, which will now be written to the output."); //$NON-NLS-1$

      try (final OutputStream os =
          (this.m_useStdOut ? System.out : //
              Files.newOutputStream(this.m_output))) {//
        os.write(data);
        size = data.length;
        data = null;
      }

      ConsoleIO.stdout(name + " has written all " + size //$NON-NLS-1$
          + "B to the output and, hence, completed its task."); //$NON-NLS-1$
    } catch (final Throwable error) {
      ConsoleIO.stderr(name + " has failed.", error);//$NON-NLS-1$
    }
  }
}
