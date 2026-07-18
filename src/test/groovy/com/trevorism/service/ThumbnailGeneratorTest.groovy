package com.trevorism.service

import org.junit.jupiter.api.Test

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class ThumbnailGeneratorTest {

    private final ThumbnailGenerator generator = new ThumbnailGenerator()

    @Test
    void testGeneratesDownscaledJpegPreservingAspectRatio() {
        byte[] source = pngBytes(1000, 500)

        byte[] thumb = generator.generateJpeg(source, 600)

        assert thumb != null && thumb.length > 0
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(thumb))
        assert decoded.width == 600
        assert decoded.height == 300
    }

    @Test
    void testDoesNotUpscaleSmallImages() {
        byte[] source = pngBytes(120, 80)

        byte[] thumb = generator.generateJpeg(source, 600)

        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(thumb))
        assert decoded.width == 120
        assert decoded.height == 80
    }

    @Test
    void testReturnsNullForUnreadableBytes() {
        assert generator.generateJpeg("not an image".bytes) == null
    }

    @Test
    void testReturnsNullForEmptyOrMissingInput() {
        assert generator.generateJpeg(null) == null
        assert generator.generateJpeg(new byte[0]) == null
    }

    @Test
    void testApplyOrientationSwapsDimensionsForRotatedOrientations() {
        BufferedImage landscape = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB)

        assert ThumbnailGenerator.applyOrientation(landscape, 1).is(landscape)
        assert dims(ThumbnailGenerator.applyOrientation(landscape, 3)) == [100, 50]
        assert dims(ThumbnailGenerator.applyOrientation(landscape, 6)) == [50, 100]
        assert dims(ThumbnailGenerator.applyOrientation(landscape, 8)) == [50, 100]
    }

    @Test
    void testApplyOrientationRotates90ClockwiseForOrientation6() {
        // Left half red, right half blue. A 90 CW rotation moves the left edge to the top.
        BufferedImage src = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB)
        def g = src.createGraphics()
        g.setColor(java.awt.Color.RED); g.fillRect(0, 0, 50, 50)
        g.setColor(java.awt.Color.BLUE); g.fillRect(50, 0, 50, 50)
        g.dispose()

        BufferedImage rotated = ThumbnailGenerator.applyOrientation(src, 6)

        assert dims(rotated) == [50, 100]
        assert dominant(rotated.getRGB(25, 10)) == 'red'   // former left half is now on top
        assert dominant(rotated.getRGB(25, 90)) == 'blue'  // former right half is now on the bottom
    }

    @Test
    void testReadExifOrientationDefaultsToOneWithoutExif() {
        assert ThumbnailGenerator.readExifOrientation(pngBytes(10, 10)) == 1
    }

    @Test
    void testReadExifOrientationParsesTag() {
        // Minimal JPEG with an APP1/EXIF segment carrying Orientation = 6 (little-endian TIFF).
        byte[] exif = bytesOf(
                0xFF, 0xD8,                                  // SOI
                0xFF, 0xE1, 0x00, 0x22,                      // APP1, length 34
                0x45, 0x78, 0x69, 0x66, 0x00, 0x00,          // "Exif\0\0"
                0x49, 0x49, 0x2A, 0x00, 0x08, 0x00, 0x00, 0x00, // TIFF header (II), IFD at offset 8
                0x01, 0x00,                                  // 1 directory entry
                0x12, 0x01, 0x03, 0x00, 0x01, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00, // tag 0x0112 SHORT = 6
                0x00, 0x00, 0x00, 0x00,                      // next IFD offset
                0xFF, 0xD9)                                  // EOI

        assert ThumbnailGenerator.readExifOrientation(exif) == 6
    }

    private static List<Integer> dims(BufferedImage image) {
        [image.width, image.height]
    }

    private static String dominant(int rgb) {
        int r = (rgb >> 16) & 0xFF
        int g = (rgb >> 8) & 0xFF
        int b = rgb & 0xFF
        if (r > g && r > b) return 'red'
        if (b > r && b > g) return 'blue'
        return 'other'
    }

    private static byte[] bytesOf(int... values) {
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        values.each { out.write(it) }
        return out.toByteArray()
    }

    private static byte[] pngBytes(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        def graphics = image.createGraphics()
        graphics.fillRect(0, 0, width, height)
        graphics.dispose()
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        ImageIO.write(image, "png", out)
        return out.toByteArray()
    }
}
