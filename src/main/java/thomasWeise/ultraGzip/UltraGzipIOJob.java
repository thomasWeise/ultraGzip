package thomasWeise.ultraGzip;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import thomasWeise.tools.ByteBuffers;
import thomasWeise.tools.ConsoleIO;
import thomasWeise.tools.IOJob;

/** The job for the ultra gzip I/O tool. */
public final class UltraGzipIOJob extends IOJob {

  /**
   * create
   *
   * @param ugo
   *          the job builder
   */
  UltraGzipIOJob(final UltraGzipIOJobBuilder ugo) {
    super(ugo);
  }

  /** run! */
  @Override
  public final void run() {
    byte[] data;
    int size;
    String name;

    if (this.isUsingStdIn()) {
      name = "stdin"; //$NON-NLS-1$
    } else {
      name = this.getInputPath().getFileName().toString();
    }
    if (this.isUsingStdOut()) {
      name += "->stdout"; //$NON-NLS-1$
    } else {
      name = (((name + '-') + '>')
          + this.getOutputPath().getFileName().toString());
    }

    ConsoleIO.stdout(name + " is now loading input data."); //$NON-NLS-1$
    try {

      try (
          final InputStream is = (this.isUsingStdIn() ? System.in//
              : Files.newInputStream(this.getInputPath()))) {
        data = ByteBuffers.get().load(is);
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
          (this.isUsingStdOut() ? System.out : //
              Files.newOutputStream(this.getOutputPath()))) {//
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
