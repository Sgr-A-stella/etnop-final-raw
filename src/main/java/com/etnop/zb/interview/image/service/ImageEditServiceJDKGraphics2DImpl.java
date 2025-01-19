package com.etnop.zb.interview.image.service;

import com.etnop.zb.interview.image.ImageApplicationException;
import com.etnop.zb.interview.image.helper.ImageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.awt.Graphics2D;
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
 * Business logic tier - Image editing service business logic JDK Graphics2D API based implementation
 */
@Service
public class ImageEditServiceJDKGraphics2DImpl implements ImageEditService {

    private static Logger logger = LoggerFactory.getLogger(ImageEditServiceJDKGraphics2DImpl.class);

    /**
     * {@inherited}
     * <br />
     * JDK Graphics2D API based implementation...
     *
     * @param originalImage
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    @Override
    public byte[] resizeImage(byte[] originalImage, MediaType mediaType, int maxWidth, int maxHeight) {
        logger.info("Called image editing service business logic JDK Graphics2D API based implementation...");
        return resizeImageWithKeepAspectRatio(originalImage, mediaType, maxWidth, maxHeight);
    }

    private byte[] resizeImageWithoutKeepAspectRatio(byte[] originalImage, MediaType mediaType, int width, int height) {
        try {
            return ImageHelper.convertBufferedImage2ByteArray(
                    resizeImageBufferedImage(
                            ImageHelper.convertByteArray2BufferedImage(originalImage),
                            width, height
                    ),
                    mediaType
            );
        } catch (IOException e) {
            throw new ImageApplicationException("Image resize fails", e);
        }
    }

    private byte[] resizeImageWithKeepAspectRatio(byte[] originalImage, final MediaType mediaType, int maxWidth, int maxHeight) {
        try {
            return resizeImage(originalImage, maxWidth, maxHeight,
                    (byteArrayImage) -> {
                        try {
                            return ImageHelper.getImageWidthHeight(originalImage, mediaType);
                        } catch (IOException e) {
                            throw new ImageApplicationException("Get image size error", e);
                        }
                    },
                    (byteArrayImage, imageWidthHeight) ->
                            resizeImageWithoutKeepAspectRatio(originalImage, mediaType, imageWidthHeight.getFirst(), imageWidthHeight.getSecond())
            );
        } catch (Exception e) {
            throw new ImageApplicationException("Image resize error", e);
        }
    }

    BufferedImage resizeImageBufferedImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }
}
