package thomasWeise.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Supplier;

/** A configuration map */
public final class Configuration {

  /** the internal configuration map */
  private static final HashMap<String, Object> CONFIGURATION =
      new HashMap<>();

  /**
   * Run the given {@link java.lang.Runnable} in a synchronized
   * configuration environment.
   *
   * @param run
   *          the runnable
   */
  public static final void
      synchronizedConfig(final Runnable run) {
    synchronized (Configuration.CONFIGURATION) {
      run.run();
    }
  }

  /**
   * Put a value
   *
   * @param key
   *          the key
   * @param value
   *          the value
   */
  public static final void putString(final String key,
      final String value) {
    final String k = Objects.requireNonNull(key);
    final String v = Objects.requireNonNull(value);
    synchronized (Configuration.CONFIGURATION) {
      Configuration.CONFIGURATION.put(k, v);
    }
  }

  /**
   * Put a value
   *
   * @param key
   *          the key
   * @param value
   *          the value
   */
  public static final void putBoolean(final String key,
      final boolean value) {
    final String k = Objects.requireNonNull(key);
    final Boolean b = Boolean.valueOf(value);
    synchronized (Configuration.CONFIGURATION) {
      Configuration.CONFIGURATION.put(k, b);
    }
  }

  /**
   * Put a value
   *
   * @param key
   *          the key
   * @param value
   *          the value
   */
  public static final void putInteger(final String key,
      final Integer value) {
    final String k = Objects.requireNonNull(key);
    final Integer b = Objects.requireNonNull(value);
    synchronized (Configuration.CONFIGURATION) {
      Configuration.CONFIGURATION.put(k, b);
    }
  }

  /**
   * Put a value
   *
   * @param key
   *          the key
   * @param value
   *          the value
   */
  public static final void putInteger(final String key,
      final int value) {
    Configuration.putInteger(key, Integer.valueOf(value));
  }

  /**
   * Put a string to the map. The string is considered to be in
   * the form {@code key=value} or {@code key:value} and may be
   * preceded by any number of {@code -} or {@code /}-es. If the
   * value part is missing {@code "true"} is used as value.
   *
   * @param s
   *          the string
   */
  public static final void putCommandLine(final String s) {
    String t;
    int i, j;
    final int len;
    char ch;
    boolean canUseSlash;

    if (s == null) {
      return;
    }

    t = s.trim();
    len = t.length();
    if (len <= 0) {
      return;
    }

    canUseSlash = (File.separatorChar != '/');

    for (i = 0; i < len; i++) {
      ch = t.charAt(i);
      if ((ch == '-') || (canUseSlash && (ch == '/'))
          || (ch <= 32)) {
        continue;
      }

      for (j = i + 1; j < len; j++) {
        ch = t.charAt(j);
        if ((ch == ':') || (ch == '=')) {
          Configuration.putString(t.substring(i, j),
              t.substring(j + 1).trim());
          return;
        }
      }

      Configuration.putBoolean(t.substring(i), Boolean.TRUE);

      return;
    }
  }

  /**
   * Load command line arguments into a map
   *
   * @param args
   *          the arguments
   */
  public static final void putCommandLine(final String... args) {
    if (args != null) {
      for (final String s : args) {
        Configuration.putCommandLine(s);
      }
    }
  }

  /**
   * Delete a key from the configuration
   *
   * @param key
   *          the key to delete
   */
  public static final void delete(final String key) {
    final String k = Objects.requireNonNull(key);
    synchronized (Configuration.CONFIGURATION) {
      Configuration.CONFIGURATION.remove(k);
    }
  }

  /**
   * Get a value from the configuration
   *
   * @param key
   *          the key
   * @return the value
   */
  public static final String getString(final String key) {
    final String k = Objects.requireNonNull(key);
    final Object res;
    synchronized (Configuration.CONFIGURATION) {
      res = Configuration.CONFIGURATION.get(k);
    }
    return ((res != null) ? res.toString() : null);
  }

  /**
   * Get a value from the configuration
   *
   * @param key
   *          the key
   * @return the value
   */
  public static final boolean getBoolean(final String key) {
    final String k = Objects.requireNonNull(key);
    final Object res;
    synchronized (Configuration.CONFIGURATION) {
      res = Configuration.CONFIGURATION.get(k);
      if (res instanceof Boolean) {
        return ((Boolean) res).booleanValue();
      }
      if (res == null) {
        Configuration.CONFIGURATION.put(key, Boolean.FALSE);
        return false;
      }
      if (res instanceof String) {
        final boolean resb = Boolean.parseBoolean((String) res);
        Configuration.CONFIGURATION.put(key,
            Boolean.valueOf(resb));
        return resb;
      }
    }
    throw new IllegalStateException("config key '"//$NON-NLS-1$
        + k + "' is not a boolean but a " + //$NON-NLS-1$
        res.getClass());
  }

  /**
   * Get a path value from the configuration
   *
   * @param key
   *          the key
   * @return the value
   */
  public static final Path getPath(final String key) {
    return Configuration.getPath(key, null);
  }

  /**
   * Get a path value from the configuration
   *
   * @param key
   *          the key
   * @param ifNotSet
   *          the supplier if no path was found
   * @return the value
   */
  public static final Path getPath(final String key,
      final Supplier<Path> ifNotSet) {
    final String k = Objects.requireNonNull(key);
    final Object res;

    synchronized (Configuration.CONFIGURATION) {
      res = Configuration.CONFIGURATION.get(k);
      if (res == null) {
        if (ifNotSet != null) {
          final Path p =
              IOUtils.canonicalizePath(ifNotSet.get());
          if (p != null) {
            Configuration.CONFIGURATION.put(k, p);
          }
          return p;
        }
        return null;
      }
      if (res instanceof Path) {
        return ((Path) res);
      }
      if (res instanceof String) {
        final Path p = IOUtils.canonicalizePath((String) res);
        Configuration.CONFIGURATION.put(k, p);
        return p;
      }
    }
    throw new IllegalStateException("config key '"//$NON-NLS-1$
        + key + "' is not a path but an instance of "//$NON-NLS-1$
        + res.getClass());
  }

  /**
   * Get a value from the configuration
   *
   * @param key
   *          the key
   * @return the value
   */
  public static final Integer getInteger(final String key) {
    final String k = Objects.requireNonNull(key);
    final Object res;
    synchronized (Configuration.CONFIGURATION) {
      res = Configuration.CONFIGURATION.get(k);
      if (res == null) {
        return null;
      }
      if (res instanceof Number) {
        if (res instanceof Integer) {
          return ((Integer) res);
        }
        final Number n = ((Number) res);
        final int v = n.intValue();
        if (n.doubleValue() == v) {
          final Integer i = Integer.valueOf(v);
          Configuration.CONFIGURATION.put(k, i);
          return i;
        }
      } else {
        if (res instanceof String) {
          final int p = Integer.parseInt((String) res);
          Configuration.CONFIGURATION.put(k, Integer.valueOf(p));
          return p;
        }
      }
    }
    throw new IllegalStateException("config key '"//$NON-NLS-1$
        + key
        + "' is not an integer but an incompatible instance of "//$NON-NLS-1$
        + res.getClass());
  }

  /**
   * Put a path value from the configuration
   *
   * @param key
   *          the key
   * @param value
   *          the path
   */
  public static final void putPath(final String key,
      final Path value) {
    final String k = Objects.requireNonNull(key);
    final Path p = value.normalize();
    synchronized (Configuration.CONFIGURATION) {
      Configuration.CONFIGURATION.put(k, p);
    }
  }

  /** Print the whole configuration to stdout */
  public static final void print() {
    ConsoleIO.stdout((stdout) -> {
      stdout.println("The current full configuration is:"); //$NON-NLS-1$
      synchronized (Configuration.CONFIGURATION) {
        for (final Entry<String,
            Object> e : Configuration.CONFIGURATION.entrySet()) {
          stdout.print('\t');
          stdout.print(e.getKey());
          stdout.print("\t->\t"); //$NON-NLS-1$
          stdout.println(e.getValue());
        }
      }
    });
  }

  /**
   * Get the path to the specified executable.
   *
   * @param name
   *          the executable name
   * @return the path
   */
  public static Path getExecutable(final String name) {
    String stdout = null;
    String stderr = null;
    Path path = null;
    Object res;

    synch: synchronized (Configuration.CONFIGURATION) {
      res = Configuration.CONFIGURATION.get(name);
      if (res != null) {
        if (res instanceof Path) {
          path = ((Path) res);
          if (Files.isExecutable(path)) {
            break synch;
          }
          throw new IllegalStateException("config key '"//$NON-NLS-1$
              + name + "' is path '" + path + //$NON-NLS-1$
              "', but it is not executable");//$NON-NLS-1$
        }
        if (res instanceof String) {
          path = IOUtils.canonicalizePath((String) res);
          if (Files.isExecutable(path)) {
            stdout = (name + " executable configured as "//$NON-NLS-1$
                + path);
            Configuration.CONFIGURATION.put(name, path);
            break synch;
          }
          path = null;
          stderr = ("Configured file '" + //$NON-NLS-1$
              res + "' is not executable.");//$NON-NLS-1$
        }
        throw new IllegalStateException("config key '"//$NON-NLS-1$
            + name
            + "' does not reference an executable path, but is an instance of"//$NON-NLS-1$
            + res.getClass());
      }

      for (final String dirname : System.getenv("PATH")
          .split(File.pathSeparator)) {
        for (final String ext : new String[] { "", ".exe" }) {
          path = IOUtils.canonicalizePath(dirname, name + ext);
          if (Files.isExecutable(path)) {
            stdout = (name + " executable detected in PATH as "
                + path);
            Configuration.CONFIGURATION.put(name, path);
            break synch;
          }
        }
      }
      path = null;

      try {
        final Process process =
            Runtime.getRuntime().exec("which " + name);
        try (BufferedReader in = new BufferedReader(
            new InputStreamReader(process.getInputStream()))) {
          path =
              IOUtils.canonicalizePath(Paths.get(in.readLine()));
          if (Files.isExecutable(path)) {
            stdout = (name + " executable found via which as "
                + path);
            Configuration.CONFIGURATION.put(name, path);
            break synch;
          }
        }
      } catch (final Throwable ignore) {
        // ignored
      }
      path = null;
    }

    if (stderr != null) {
      ConsoleIO.stderr(stderr, null);
    }
    if (stdout != null) {
      ConsoleIO.stdout(stdout);
    }
    if (path == null) {
      ConsoleIO.stdout("no " + name + " executable detected.");
    }
    return path;
  }
}
