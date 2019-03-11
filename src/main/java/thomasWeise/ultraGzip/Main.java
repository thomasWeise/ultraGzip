package thomasWeise.ultraGzip;

import thomasWeise.tools.Configuration;
import thomasWeise.tools.ConsoleIO;
import thomasWeise.tools.Execute;

/** The main class for the Ultra Gzip tool */
public final class Main {
  /** the print help */
  private static final String PARAM_HELP = "help";//$NON-NLS-1$

  /**
   * The main routine.
   *
   * @param args
   *          the command line arguments
   */
  public static void main(final String[] args) {

    thomasWeise.tools.Configuration.putCommandLine(args);

    final boolean help =
        Configuration.getBoolean(Main.PARAM_HELP);
    ConsoleIO.stdout((out) -> {
      out.println("Welcome to UltraGzip "//$NON-NLS-1$
          + UltraGzip.VERSION);
      if (help) {
        out.println("Usage: java -jar ultraGzip-" + //$NON-NLS-1$
        UltraGzip.VERSION + "-full.jar [ARGUMENTS]");//$NON-NLS-1$
        out.println("Command line arguments are.");//$NON-NLS-1$
        out.println(' ' + UltraGzipIOJobBuilder.PARAM_IN + //
        "=path/to/file ... the path to the file to compress");//$NON-NLS-1$
        out.println(' ' + UltraGzipIOJobBuilder.PARAM_STDIN + //
        " ... read data to compress from stdin instead of file");//$NON-NLS-1$
        out.println(' ' + UltraGzipIOJobBuilder.PARAM_OUT + //
        "=path/to/file ... the path to the file to write compressed output to");//$NON-NLS-1$
        out.println(' ' + UltraGzipIOJobBuilder.PARAM_STDOUT + //
        " ... write data to stdout instead of file");//$NON-NLS-1$
        out.println(' ' + Main.PARAM_HELP + //
        "=path/to/file ... print this help screen");//$NON-NLS-1$
        Execute.printArgs(out);
        UltraGzip.printArgs(out);
      }
    });
    if (help) {
      return;
    }

    try {
      Execute.parallel( //
          UltraGzipIO.getInstance()//
              .get()//
              .configure()//
              .get())//
          .get();
    } catch (final Throwable error) {
      ConsoleIO.stderr("UltraGzip has failed.", error);//$NON-NLS-1$
    }
  }
}
