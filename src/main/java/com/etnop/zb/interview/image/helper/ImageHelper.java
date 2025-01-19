package com.etnop.zb.interview.image.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Static helper for image operations (size check, converters, ...etc.)
 */
public class ImageHelper {

    private static Logger logger = LoggerFactory.getLogger(ImageHelper.class);

    /**
     * Image oversize check both width and height compare with max parameters
     *
     * @param imageContent
     * @param imageType
     * @param maxWidth
     * @param maxHeight
     * @return {@code true}, if any max parameters less than size of image parameters
     * @throws IOException
     */
    public static boolean isOversize(byte[] imageContent, String imageType, int maxWidth, int maxHeight) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(imageContent);
        Iterator<?> readers = ImageIO.getImageReadersByMIMEType(imageType);

        ImageReader reader = (ImageReader) readers.next();
        Object source = bis;
        ImageInputStream iis = ImageIO.createImageInputStream(source);
        reader.setInput(iis, true);
        ImageReadParam param = reader.getDefaultReadParam();

        Image image = reader.read(0, param);

        return image.getWidth(null) > maxWidth || image.getHeight(null) > maxHeight;
    }

    public static Pair<Integer, Integer> getImageWidthHeight(byte[] originalImageContent, MediaType mediaType) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(originalImageContent);
        Iterator<?> readers = ImageIO.getImageReadersByFormatName(mediaType.getSubtype());

        ImageReader reader = (ImageReader) readers.next();
        Object source = bis;
        ImageInputStream iis = ImageIO.createImageInputStream(source);
        reader.setInput(iis, true);
        ImageReadParam param = reader.getDefaultReadParam();

        Image image = reader.read(0, param);
        logger.info("Image width: {}, height: {}", image.getWidth(null), image.getHeight(null));


        return Pair.of(image.getWidth(null), image.getHeight(null));
    }

    public static BufferedImage convertByteArray2BufferedImage(byte[] originalImageContent) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(originalImageContent));
    }

    public static byte[] convertBufferedImage2ByteArray(BufferedImage image, MediaType mediaType) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, mediaType.getSubtype(), baos);
        byte[] bytes = baos.toByteArray();
        return bytes;
    }
}
