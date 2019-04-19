package thomasWeise.tools;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/** Utils for I/O */
public final class IOUtils {

  /**
   * Obtain a canonical path
   *
   * @param first
   *          the first string
   * @param more
   *          the more strings
   * @return the canonical version
   */
  public static final Path canonicalizePath(final String first,
      final String... more) {
    if (first == null) {
      throw new IllegalArgumentException(
          "path string cannot be null.");//$NON-NLS-1$
    }
    return IOUtils.canonicalizePath(Paths.get(first, more));
  }

  /**
   * Obtain a canonical path
   *
   * @param p
   *          the path
   * @return the canonical version
   */
  public static final Path canonicalizePath(final Path p) {
    if (p == null) {
      return null;
    }

    Path r = p.normalize();
    if (r == null) {
      r = p;
    } else {
      if (Objects.equals(p, r)) {
        r = p;
      }
    }

    Path z = r.toAbsolutePath();
    if (z != null) {
      if (Objects.equals(p, z)) {
        r = p;
      } else {
        r = z;
      }
    }

    z = z.normalize();
    if (z != null) {
      if (Objects.equals(p, z)) {
        r = p;
      } else {
        r = z;
      }
    }

    try {
      z = z.toRealPath();
      if (z != null) {
        if (Objects.equals(p, z)) {
          r = p;
        } else {
          r = z;
        }
      }
    } catch (final IOException ioe) {
      // ignore
    }
    return r;
  }
}
