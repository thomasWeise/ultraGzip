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

    if (Boolean
        .parseBoolean(Configuration.get(Main.PARAM_HELP))) {
      System.out.print("Welcome to the UltraGzip Tool.");//$NON-NLS-1$
      System.out
          .println("Usage: java -jar ultraGzip.jar [ARGUMENTS]");//$NON-NLS-1$
      System.out.println("Command line arguments are.");//$NON-NLS-1$
      System.out.println(' ' + UltraGzipIOJobBuilder.PARAM_IN + //
          "=path/to/file ... the path to the file to compress");//$NON-NLS-1$
      System.out
          .println(' ' + UltraGzipIOJobBuilder.PARAM_STDIN + //
              " ... read data to compress from stdin instead of file");//$NON-NLS-1$
      System.out.println(' ' + UltraGzipIOJobBuilder.PARAM_OUT + //
          "=path/to/file ... the path to the file to write compressed output to");//$NON-NLS-1$
      System.out
          .println(' ' + UltraGzipIOJobBuilder.PARAM_STDOUT + //
              " ... write data to stdout instead of file");//$NON-NLS-1$
      System.out.println(' ' + Main.PARAM_HELP + //
          "=path/to/file ... print this help screen");//$NON-NLS-1$

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
      ConsoleIO.stderr("Ultra GZIP has failed.", error);//$NON-NLS-1$
    }
  }
}
