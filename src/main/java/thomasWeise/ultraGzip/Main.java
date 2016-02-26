package thomasWeise.ultraGzip;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.optimizationBenchmarking.utils.config.Configuration;
import org.optimizationBenchmarking.utils.parallel.Execute;

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
    final Configuration config;
    final Logger logger;

    Configuration.setup(args);
    config = Configuration.getRoot();

    if (config.getBoolean(Main.PARAM_HELP, false)) {
      System.out.print("Welcome to version ");//$NON-NLS-1$
      System.out.print(UltraGzipIO.getInstance().getProjectVersion());
      System.out.println(" of the UltraGzip Tool.");//$NON-NLS-1$
      System.out.println("Usage: java -jar ultraGzip.jar [ARGUMENTS]");//$NON-NLS-1$
      System.out.println("Command line arguments are.");//$NON-NLS-1$
      System.out.println(' ' + UltraGzipIOJobBuilder.PARAM_IN + //
          "=path/to/file ... the path to the file to compress");//$NON-NLS-1$
      System.out.println(' ' + UltraGzipIOJobBuilder.PARAM_STDIN + //
          " ... read data to compress from stdin instead of file");//$NON-NLS-1$
      System.out.println(' ' + UltraGzipIOJobBuilder.PARAM_OUT + //
          "=path/to/file ... the path to the file to write compressed output to");//$NON-NLS-1$
      System.out.println(' ' + UltraGzipIOJobBuilder.PARAM_STDOUT + //
          " ... write data to stdout instead of file");//$NON-NLS-1$
      System.out.println(Configuration.PARAM_LOGGER + //
          "=global,LOG_LEVEL ... use the given Java LOG_LEVEL for status info");//$NON-NLS-1$
      System.out.println(//
          "                            (ALL, FINEST, ..., INFO, ..., WARNING, SEVERE)");//$NON-NLS-1$
      System.out.println(' ' + Main.PARAM_HELP + //
          "=path/to/file ... print this help screen");//$NON-NLS-1$

      return;
    }

    try {
      Execute
          .submitToCommonPool( //
              UltraGzipIO.getInstance()//
                  .use()//
                  .configure(config)//
                  .create(), //
              null)//
          .get();
    } catch (final Throwable error) {
      logger = config.getLogger(Configuration.PARAM_LOGGER, null);
      if ((logger != null) && (logger.isLoggable(Level.SEVERE))) {
        logger.log(Level.SEVERE, "Ultra GZIP has failed.", error);//$NON-NLS-1$
      }
    }

  }

}
