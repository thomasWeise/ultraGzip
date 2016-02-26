package test.junit.thomasWeise.ultraGzip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.optimizationBenchmarking.utils.io.IOUtils;

import shared.junit.org.optimizationBenchmarking.utils.tools.ToolTest;
import thomasWeise.ultraGzip.UltraGzip;

/** A class for testing UltraZip */
public class UltraGzipTest extends ToolTest<UltraGzip> {

  /** create */
  public UltraGzipTest() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  protected final UltraGzip getInstance() {
    return UltraGzip.getInstance();
  }

  /**
   * Test compressing and de-compressing the given bytes
   *
   * @param bytes
   *          the bytes
   * @throws IOException
   *           if it fails
   */
  private final void __test(final byte[] bytes) throws IOException {
    final byte[] archive;

    archive = this.getInstance().use().setData(bytes)//
        .setName("test").create().call(); //$NON-NLS-1$

    Assert.assertNotNull(archive);
    try (final ByteArrayOutputStream bos = new ByteArrayOutputStream(
        bytes.length)) {

      try (final ByteArrayInputStream bis = new ByteArrayInputStream(
          archive)) {
        try (
            final java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(
                bis)) {
          IOUtils.copy(gis, bos);
        }
      }
      Assert.assertArrayEquals(bytes, bos.toByteArray());

      bos.reset();
      try (final ByteArrayInputStream bis = new ByteArrayInputStream(
          archive)) {
        try (
            final com.jcraft.jzlib.GZIPInputStream gis = new com.jcraft.jzlib.GZIPInputStream(
                bis)) {
          IOUtils.copy(gis, bos);
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
   * Test compressing arrays of length 5 to 100 with all 0
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testAllZero5to100() throws IOException {
    for (int i = 5; i <= 100; i++) {
      this.__test(new byte[i]);
    }
  }

  /**
   * Test arrays of random length with random data
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testRandom() throws IOException {
    final Random random;
    int i;
    byte[] data;

    random = new Random();

    for (i = 20; (--i) >= 0;) {
      data = new byte[1 + (1 << random.nextInt(18)) + random.nextInt(32)];
      random.nextBytes(data);
      this.__test(data);
    }
  }

  /**
   * Test arrays of random length with semi-random data
   *
   * @throws IOException
   *           if i/o fails
   */
  @Test(timeout = 3600000)
  public final void testSemiRandom() throws IOException {
    final Random random;
    int i, j, k;
    byte[] data;

    random = new Random();

    for (i = 20; (--i) >= 0;) {
      data = new byte[1 + (1 << random.nextInt(18)) + random.nextInt(32)];
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
        data[random.nextInt(data.length)] = ((byte) (random.nextInt()));
      } while (random.nextInt(20) > 0);

      this.__test(data);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void validateInstance() {
    super.validateInstance();
    try {
      this.testAllZero1();
      this.testAllZero2();
      this.testAllZero3();
      this.testAllZero4();
      this.testAllZero5to100();
      this.testRandom();
      this.testSemiRandom();
    } catch (final IOException ioe) {
      throw new AssertionError(ioe);
    }
  }
}
