package thomasWeise.ultraGzip;

import java.io.PrintStream;
import java.util.function.Supplier;

import thomasWeise.tools.Configuration;
import thomasWeise.tools.ConsoleIO;
import thomasWeise.tools.Execute;

/** The ultra gzip tool. */
public final class UltraGzip
    implements Supplier<UltraGzipJobBuilder> {

  /** the intensity parameter */
  private static final String PARAM_INTENSITY = "gzipIntensity"; //$NON-NLS-1$

  /** the intensity */
  static final int INTENSITY;

  static {
    final int[] d = new int[] { 5 };
    Configuration.synchronizedConfig(() -> {
      final Integer inten =
          Configuration.getInteger(UltraGzip.PARAM_INTENSITY);
      if (inten != null) {
        d[0] = Math.max(0, Math.min(10, inten.intValue()));
      }
      Configuration.putInteger(UltraGzip.PARAM_INTENSITY, d[0]);
    });
    INTENSITY = d[0];

    Execute.parallel(() -> {
      ConsoleIO.stdout(
          UltraGzip.PARAM_INTENSITY + " (0:min...10:max) is " //$NON-NLS-1$
              + UltraGzip.INTENSITY);
    });
  }

  /**
   * Create a quality range starting between min and max and
   * always ending at max. The default value is used as starting
   * point when INTENSITY=5.
   *
   * @param min
   *          the minimum quality
   * @param max
   *          the maximum quality
   * @param defa
   *          the default start value
   * @return the range
   */
  static int[] _qualityRange(final int min, final int max,
      final int defa) {
    int mi, ma;

    ma = max;
    if (UltraGzip.INTENSITY == 5) {
      mi = defa;
    } else {
      if (UltraGzip.INTENSITY < 5) {
        mi = Math.max(defa, (int) (Math.round(max
            - ((UltraGzip.INTENSITY / 5.0) * (max - defa)))));
      } else {
        mi = Math.min(defa,
            (int) (Math.round(min
                + ((1.0 - ((UltraGzip.INTENSITY - 5.0) / 5.0))
                    * (defa - min)))));
      }
    }

    mi = Math.min(ma, Math.max(min, mi));
    final int[] res = new int[(ma - mi) + 1];
    int i = 0;
    for (int k = ma; k >= mi; k--) {
      res[i++] = k;
    }
    return res;
  }

  /** create */
  UltraGzip() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    return "UltraGzip"; //$NON-NLS-1$
  }

  /**
   * print the command line arguments
   *
   * @param out
   *          the print stream to write to
   */
  public static final void printArgs(final PrintStream out) {
    out.println(' ' + UltraGzip.PARAM_INTENSITY
        + "=0(min)..10(max), default 5: intensity - the higher the slower");//$NON-NLS-1$
    out.println(' ' + _GZIP.ARG
        + "=/path/to/gzip, default: autodetect.. path to gzip binary");//$NON-NLS-1$
    out.println(' ' + _7ZIP.ARG
        + "=/path/to/7zip, default: autodetect.. path to 7zip binary");//$NON-NLS-1$
    out.println(' ' + _ADVDEF.ARG
        + "=/path/to/advdef, default: autodetect.. path to AdvanceComp binary");//$NON-NLS-1$
    out.println(' ' + _GZPython.ARG
        + "=/path/to/python3, default: autodetect.. path to python3 binary");//$NON-NLS-1$
    out.println(' ' + _PIGZ.ARG
        + "=/path/to/pigz, default: autodetect.. path to pigz binary");//$NON-NLS-1$
    out.println(' ' + _Zopfli.ARG
        + "=/path/to/zopfli, default: autodetect.. path to zopfli binary");//$NON-NLS-1$
    out.println(
        "The GPL-3 licensed UltraGzip tool makes use of many other tools, including 7zip, AdvanceComp, gzip, zopfli, pigz, JZLib (http://www.jcraft.com/jzlib/), and python, which have their own licensing requirements."); //$NON-NLS-1$

  }

  /** {@inheritDoc} */
  @Override
  public final UltraGzipJobBuilder get() {
    return new UltraGzipJobBuilder();
  }

  /**
   * Get the globally shared instance of the Ultra Gzip tool
   *
   * @return the globally shared instance of the Ultra Gzip tool
   */
  public static final UltraGzip getInstance() {
    return __UltraGzipHolder.INSTANCE;
  }

  /** the ultra gzip tool */
  private static final class __UltraGzipHolder {
    /** the globally shared instance */
    static final UltraGzip INSTANCE = new UltraGzip();
  }
}
