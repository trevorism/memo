package com.trevorism.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.imageio.ImageIO
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage

/**
 * Downsizes image bytes into a small JPEG thumbnail using only the JDK (ImageIO + Graphics2D).
 * Returns null when the source cannot be decoded (e.g. WebP has no ImageIO reader by default),
 * so callers can fall back to serving the original bytes.
 *
 * ImageIO.read drops EXIF metadata, so the camera's orientation tag (common on phone photos) is
 * read separately and baked into the pixels here — otherwise portrait shots come out sideways.
 */
class ThumbnailGenerator {

    private static final Logger log = LoggerFactory.getLogger(ThumbnailGenerator)

    static final int DEFAULT_MAX_EDGE = 600

    static {
        // AWT image scaling must run headless on App Engine.
        System.setProperty("java.awt.headless", "true")
    }

    byte[] generateJpeg(byte[] original, int maxEdge = DEFAULT_MAX_EDGE) {
        if (!original || original.length == 0) {
            return null
        }
        try {
            BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(original))
            if (decoded == null || decoded.width <= 0 || decoded.height <= 0) {
                // Unreadable or unsupported format.
                return null
            }

            // Apply the EXIF orientation before scaling so aspect ratio is computed from the upright image.
            BufferedImage src = applyOrientation(decoded, readExifOrientation(original))

            double scale = Math.min(1.0d, (double) maxEdge / (double) Math.max(src.width, src.height))
            int targetWidth = Math.max(1, (int) Math.round(src.width * scale))
            int targetHeight = Math.max(1, (int) Math.round(src.height * scale))

            // JPEG has no alpha channel; paint onto an RGB canvas.
            BufferedImage dst = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
            Graphics2D graphics = dst.createGraphics()
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                graphics.drawImage(src, 0, 0, targetWidth, targetHeight, null)
            } finally {
                graphics.dispose()
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream()
            if (!ImageIO.write(dst, "jpg", out)) {
                return null
            }
            return out.toByteArray()
        } catch (Exception e) {
            log.warn("Thumbnail generation failed; caller will fall back to the original", e)
            return null
        }
    }

    /**
     * Returns an upright copy of the image for EXIF orientations 2-8, or the original for 1/unknown.
     * Orientation values follow the EXIF spec (1=normal, 3=180°, 6=90°CW, 8=270°CW, 2/4/5/7 mirrored).
     */
    static BufferedImage applyOrientation(BufferedImage image, int orientation) {
        if (orientation <= 1 || orientation > 8) {
            return image
        }

        int w = image.width
        int h = image.height
        boolean swapDimensions = orientation >= 5
        AffineTransform t = new AffineTransform()
        switch (orientation) {
            case 2: // mirror horizontal
                t.scale(-1.0d, 1.0d); t.translate(-w, 0); break
            case 3: // rotate 180
                t.translate(w, h); t.rotate(Math.PI); break
            case 4: // mirror vertical
                t.scale(1.0d, -1.0d); t.translate(0, -h); break
            case 5: // mirror horizontal + rotate 270 CW
                t.rotate(-Math.PI / 2); t.scale(-1.0d, 1.0d); break
            case 6: // rotate 90 CW
                t.translate(h, 0); t.rotate(Math.PI / 2); break
            case 7: // mirror horizontal + rotate 90 CW
                t.scale(-1.0d, 1.0d); t.translate(-h, 0); t.translate(0, w); t.rotate(3 * Math.PI / 2); break
            case 8: // rotate 270 CW
                t.translate(0, w); t.rotate(3 * Math.PI / 2); break
        }

        BufferedImage dst = new BufferedImage(swapDimensions ? h : w, swapDimensions ? w : h, BufferedImage.TYPE_INT_RGB)
        Graphics2D g = dst.createGraphics()
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            g.drawImage(image, t, null)
        } finally {
            g.dispose()
        }
        return dst
    }

    /** Reads the EXIF Orientation tag (0x0112) from a JPEG, defaulting to 1 (normal) when absent. */
    static int readExifOrientation(byte[] bytes) {
        try {
            // Must be a JPEG (starts with SOI marker 0xFFD8).
            if (bytes.length < 4 || (bytes[0] & 0xFF) != 0xFF || (bytes[1] & 0xFF) != 0xD8) {
                return 1
            }
            int offset = 2
            while (offset + 4 <= bytes.length) {
                int marker = ((bytes[offset] & 0xFF) << 8) | (bytes[offset + 1] & 0xFF)
                offset += 2
                if (marker == 0xFFDA || marker == 0xFFD9) {
                    break // start of scan / end of image — no EXIF beyond here
                }
                int segmentLength = ((bytes[offset] & 0xFF) << 8) | (bytes[offset + 1] & 0xFF)
                if (segmentLength < 2) {
                    break
                }
                // APP1 (0xFFE1) carries EXIF; verify the "Exif\0\0" identifier.
                if (marker == 0xFFE1) {
                    int base = offset + 2
                    if (base + 6 <= bytes.length &&
                            (bytes[base] & 0xFF) == 0x45 && (bytes[base + 1] & 0xFF) == 0x78 &&
                            (bytes[base + 2] & 0xFF) == 0x69 && (bytes[base + 3] & 0xFF) == 0x66 &&
                            (bytes[base + 4] & 0xFF) == 0x00 && (bytes[base + 5] & 0xFF) == 0x00) {
                        return parseTiffOrientation(bytes, base + 6)
                    }
                }
                offset += segmentLength
            }
        } catch (Exception ignored) {
            // Any malformed metadata just means "assume normal".
        }
        return 1
    }

    private static int parseTiffOrientation(byte[] b, int tiffStart) {
        if (tiffStart + 8 > b.length) {
            return 1
        }
        boolean little
        int b0 = b[tiffStart] & 0xFF
        int b1 = b[tiffStart + 1] & 0xFF
        if (b0 == 0x49 && b1 == 0x49) {
            little = true // "II" little-endian
        } else if (b0 == 0x4D && b1 == 0x4D) {
            little = false // "MM" big-endian
        } else {
            return 1
        }

        int ifdOffset = readInt(b, tiffStart + 4, little)
        int ifd = tiffStart + ifdOffset
        if (ifd + 2 > b.length) {
            return 1
        }
        int entries = readShort(b, ifd, little)
        int p = ifd + 2
        for (int i = 0; i < entries; i++) {
            if (p + 12 > b.length) {
                break
            }
            int tag = readShort(b, p, little)
            if (tag == 0x0112) {
                int value = readShort(b, p + 8, little) // SHORT value is stored inline in the value field
                return (value >= 1 && value <= 8) ? value : 1
            }
            p += 12
        }
        return 1
    }

    private static int readShort(byte[] b, int offset, boolean little) {
        int b0 = b[offset] & 0xFF
        int b1 = b[offset + 1] & 0xFF
        return little ? (b1 << 8) | b0 : (b0 << 8) | b1
    }

    private static int readInt(byte[] b, int offset, boolean little) {
        int b0 = b[offset] & 0xFF
        int b1 = b[offset + 1] & 0xFF
        int b2 = b[offset + 2] & 0xFF
        int b3 = b[offset + 3] & 0xFF
        return little ? (b3 << 24) | (b2 << 16) | (b1 << 8) | b0 : (b0 << 24) | (b1 << 16) | (b2 << 8) | b3
    }
}
