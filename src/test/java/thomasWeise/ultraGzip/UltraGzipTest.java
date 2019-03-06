package thomasWeise.ultraGzip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/** A class for testing UltraZip */
public class UltraGzipTest {

  /**
   * Test compressing and de-compressing the given bytes
   *
   * @param bytes
   *          the bytes
   * @throws IOException
   *           if it fails
   */
  private final void __test(final byte[] bytes)
      throws IOException {
    final byte[] archive;

    archive = UltraGzip.getInstance().get().setData(bytes)//
        .setName("test").get().call(); //$NON-NLS-1$

    Assert.assertNotNull(archive);
    try (final ByteArrayOutputStream bos =
        new ByteArrayOutputStream(bytes.length)) {

      try (final ByteArrayInputStream bis =
          new ByteArrayInputStream(archive)) {
        try (final java.util.zip.GZIPInputStream gis =
            new java.util.zip.GZIPInputStream(bis)) {
          this.copy(gis, bos);
        }
      }
      Assert.assertArrayEquals(bytes, bos.toByteArray());

      bos.reset();
      try (final ByteArrayInputStream bis =
          new ByteArrayInputStream(archive)) {
        try (final com.jcraft.jzlib.GZIPInputStream gis =
            new com.jcraft.jzlib.GZIPInputStream(bis)) {
          this.copy(gis, bos);
        }
      }
      Assert.assertArrayEquals(bytes, bos.toByteArray());
    }
  }

  /**
   * Test compressing an array of length 1 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero1() throws IOException {
    this.__test(new byte[1]);
  }

  /**
   * Test compressing an array of length 2 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero2() throws IOException {
    this.__test(new byte[2]);
  }

  /**
   * Test compressing an array of length 3 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero3() throws IOException {
    this.__test(new byte[3]);
  }

  /**
   * Test compressing an array of length 4 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero4() throws IOException {
    this.__test(new byte[4]);
  }

  /**
   * Test compressing an array of length 5 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero5() throws IOException {
    this.__test(new byte[5]);
  }

  /**
   * Test compressing an array of length 6 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero6() throws IOException {
    this.__test(new byte[6]);
  }

  /**
   * Test compressing an array of length 7 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero7() throws IOException {
    this.__test(new byte[7]);
  }

  /**
   * Test compressing an array of length 8 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero8() throws IOException {
    this.__test(new byte[8]);
  }

  /**
   * Test compressing an array of length 9 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero9() throws IOException {
    this.__test(new byte[9]);
  }

  /**
   * Test compressing an array of length 10 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero10() throws IOException {
    this.__test(new byte[10]);
  }

  /**
   * Test compressing an array of length 11 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero11() throws IOException {
    this.__test(new byte[11]);
  }

  /**
   * Test compressing an array of length 12 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero12() throws IOException {
    this.__test(new byte[12]);
  }

  /**
   * Test compressing an array of length 46 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero46() throws IOException {
    this.__test(new byte[46]);
  }

  /**
   * Test compressing an array of length 124 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero124() throws IOException {
    this.__test(new byte[124]);
  }

  /**
   * Test compressing an array of length 584 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero584() throws IOException {
    this.__test(new byte[584]);
  }

  /**
   * Test compressing an array of length 1001 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero1001() throws IOException {
    this.__test(new byte[1001]);
  }

  /**
   * Test compressing an array of length 2552584 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero25584() throws IOException {
    this.__test(new byte[25584]);
  }

  /**
   * Test compressing an array of length 11231 and all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero11231() throws IOException {
    this.__test(new byte[11231]);
  }

  /**
   * Test arrays of random length with random data
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testRandomSmall() throws IOException {
    final Random random;
    byte[] data;

    random = new Random();

    data = new byte[1 + (1 << random.nextInt(5))
        + random.nextInt(16)];
    random.nextBytes(data);
    this.__test(data);
  }

  /**
   * Test arrays of random length with random data
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testRandomBig() throws IOException {
    final Random random;
    byte[] data;

    random = new Random();

    data = new byte[1 + (1 << random.nextInt(12))
        + random.nextInt(32)];
    random.nextBytes(data);
    this.__test(data);
  }

  /**
   * Test arrays of random length with semi-random data
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testSemiRandomSmall() throws IOException {
    final Random random;
    int j, k;
    byte[] data;

    random = new Random();

    data = new byte[1 + (1 << random.nextInt(5))
        + random.nextInt(16)];
    random.nextBytes(data);

    do {
      if (random.nextBoolean()) {
        j = random.nextInt(data.length);
        k = j + 1 + Math.min((data.length >>> 5),
            random.nextInt(data.length - j));
      } else {
        j = random.nextInt(data.length + 1);
        k = random.nextInt(data.length + 1);
      }
      Arrays.fill(data, Math.min(j, k), Math.max(j, k),
          ((byte) (random.nextInt())));
    } while (random.nextInt(80) > 0);

    do {
      data[random.nextInt(data.length)] =
          ((byte) (random.nextInt()));
    } while (random.nextInt(20) > 0);

    this.__test(data);
  }

  /**
   * Test arrays of random length with semi-random data
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testSemiRandomBig() throws IOException {
    final Random random;
    int j, k;
    byte[] data;

    random = new Random();

    data = new byte[1 + (1 << random.nextInt(14))
        + random.nextInt(32)];
    random.nextBytes(data);

    do {
      if (random.nextBoolean()) {
        j = random.nextInt(data.length);
        k = j + 1 + Math.min((data.length >>> 5),
            random.nextInt(data.length - j));
      } else {
        j = random.nextInt(data.length + 1);
        k = random.nextInt(data.length + 1);
      }
      Arrays.fill(data, Math.min(j, k), Math.max(j, k),
          ((byte) (random.nextInt())));
    } while (random.nextInt(80) > 0);

    do {
      data[random.nextInt(data.length)] =
          ((byte) (random.nextInt()));
    } while (random.nextInt(20) > 0);

    this.__test(data);
  }

  /**
   * Test test on a reproducible dataset
   *
   * @param n
   *          the first multiplier
   * @param m
   *          the second multiplier
   * @param l
   *          the third multiplier
   * @throws IOException
   *           if i/o fails
   */
  private final void __testReproducible(final int n, final int m,
      final int l) throws IOException {
    final byte[] source = new byte[n * m * l];
    int index = 0;
    for (int i = n; (--i) >= 0;) {
      for (int j = m; (--j) >= 0;) {
        for (int k = l; (--k) >= 0;) {
          source[index++] = (byte) k;
        }
      }
    }

    this.__test(source);
  }

  /**
   * Test on a reproducible setup
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testReproducible_2_2_2() throws IOException {
    this.__testReproducible(2, 2, 2);
  }

  /**
   * Test on a reproducible setup
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testReproducible_2_2_3() throws IOException {
    this.__testReproducible(2, 2, 3);
  }

  /**
   * Test on a reproducible setup
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testReproducible_3_3_3() throws IOException {
    this.__testReproducible(3, 3, 3);
  }

  /**
   * Test on a reproducible setup
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testReproducible_9_9_92()
      throws IOException {
    this.__testReproducible(9, 9, 9);
  }

  /**
   * Test on a reproducible setup
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testReproducible_22_22_2()
      throws IOException {
    this.__testReproducible(22, 22, 2);
  }

  /**
   * Test on a reproducible setup
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testReproducible_2_22_22()
      throws IOException {
    this.__testReproducible(2, 22, 22);
  }

  /**
   * Test on a reproducible setup
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testReproducible_7_12_5()
      throws IOException {
    this.__testReproducible(7, 12, 5);
  }

  /**
   * the internal stream copy method
   *
   * @param in
   *          the input stream
   * @param out
   *          the output stream
   * @throws IOException
   *           if io fails
   */
  private void copy(final InputStream in, final OutputStream out)
      throws IOException {
    final byte[] b = new byte[8192];
    for (int r; (r = in.read(b)) != -1;) {
      out.write(b, 0, r);
    }
  }
}
