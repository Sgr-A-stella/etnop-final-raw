package com.etnop.zb.interview.image.service;

import com.etnop.zb.interview.image.ImageApplicationException;
import com.etnop.zb.interview.image.helper.ImageHelper;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Business logic tier - Image editing service business logic 3rd-party Imgscalr library based implementation
 */
@Service
public class ImageEditService3rdpartyImgscalrImpl implements ImageEditService {

    private static Logger logger = LoggerFactory.getLogger(ImageEditService3rdpartyImgscalrImpl.class);

    /**
     * {@inherited}
     * <br />
     * J3rd-party Imgscalr library based implementation...
     *
     * @param originalImage
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    @Override
    public byte[] resizeImage(byte[] originalImage, MediaType mediaType, int maxWidth, int maxHeight) {
        logger.info("Called Image editing service business logic 3rd-party Imgscalr library based implementation...");

        if (maxWidth != 0 || maxHeight != 0) {
            try {
                Pair<Integer, Integer> originalWidthHeightSize = ImageHelper.getImageWidthHeight(originalImage, mediaType);
                logger.info("Resize max width: {}, height: {}", maxWidth, maxHeight);
                logger.info("Image width: {}, height: {}", originalWidthHeightSize.getFirst(), originalWidthHeightSize.getSecond());

                if (originalWidthHeightSize.getFirst() > maxWidth || originalWidthHeightSize.getSecond() > maxHeight) {
                    int targetWidth = maxWidth;
                    if (originalWidthHeightSize.getFirst() < maxWidth && originalWidthHeightSize.getSecond() > maxHeight) {
                        targetWidth = (int) ((double) (maxHeight / originalWidthHeightSize.getSecond())) * originalWidthHeightSize.getFirst();
                    } else if (originalWidthHeightSize.getFirst() > maxWidth && originalWidthHeightSize.getSecond() > maxHeight) {
                        double widthRatio = (double) maxWidth / originalWidthHeightSize.getFirst();
                        double heightRatio = (double) maxHeight / originalWidthHeightSize.getSecond();
                        double resizeRatio = Math.min(widthRatio, heightRatio);
                        logger.info("Resize ratio: {}", resizeRatio);
                        targetWidth = (int) (resizeRatio * originalWidthHeightSize.getFirst());
                    } else {
                        // NOTE: width of image bigger than maxWidth, so default maxWidth OK
                    }

                    logger.info("Required image resize, target new width: {}", targetWidth);
                    return ImageHelper.convertBufferedImage2ByteArray(
                            simpleResizeImage(ImageHelper.convertByteArray2BufferedImage(originalImage), targetWidth),
                            mediaType
                    );
                }
            } catch (Exception e) {
                throw new ImageApplicationException("Image resize error", e);
            }
        }

        logger.info("No required image resize");
        return originalImage;
    }

    private BufferedImage simpleResizeImage(BufferedImage originalImage, int targetWidth) throws Exception {
        return Scalr.resize(originalImage, targetWidth);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws Exception {
        return Scalr.resize(originalImage, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, targetWidth, targetHeight, Scalr.OP_ANTIALIAS);
    }
}
