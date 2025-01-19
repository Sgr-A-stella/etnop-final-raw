package com.etnop.zb.interview.image.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Business logic tier - Image editing service business interface
 */
public interface ImageEditService {

    Logger logger = LoggerFactory.getLogger(ImageEditService.class);

    /**
     * Resize image to width or height (both size of resized image have not bigger than proper max parameter) with aspect ratio keeping
     *
     * @param originalImage
     * @param mediaType
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    byte[] resizeImage(byte[] originalImage, MediaType mediaType, int maxWidth, int maxHeight);

    /**
     * Resize image through 3rd-party utility specific lambdas with default new size computing logic.
     * This useful, if 3rd-party utility require concrete new size values and / or do not keep aspect ratio automatically.
     * (Idempotent: if original image is oversize, then resize only.)
     *
     * @param originalImage
     * @param maxWidth
     * @param maxHeight
     * @param sizeExtractor 3rd-party utility specific lambda for extract image sizes
     * @param resizer 3rd-party utility specific lambda for image resize
     * @return
     * @param <T> 3rd-party utility specific image type
     */
    default <T> T resizeImage(T originalImage, int maxWidth, int maxHeight,
                              Function<T, Pair<Integer, Integer>> sizeExtractor,
                              BiFunction<T, Pair<Integer, Integer>, T> resizer) {
        Pair<Integer, Integer> originalWidthHeightSize = sizeExtractor.apply(originalImage);
        logger.info("Resize max width: {}, height: {}", maxWidth, maxHeight);
        logger.info("Image width: {}, height: {}", originalWidthHeightSize.getFirst(), originalWidthHeightSize.getSecond());

        if (originalWidthHeightSize.getFirst() > maxWidth || originalWidthHeightSize.getSecond() > maxHeight) {
            double resizeRatio;

            // NOTE: convention, that if any resize max value is zero, then no limit on it...
            if (maxWidth != 0 && maxHeight != 0) {
                resizeRatio = Math.min(
                        (double) maxWidth / originalWidthHeightSize.getFirst(),
                        (double) maxHeight / originalWidthHeightSize.getSecond()
                );
                logger.info("Required image resize, resize ratio: {}, max width: {}, originalWidthHeightSize.getFirst(): {}",
                        resizeRatio, maxWidth, originalWidthHeightSize.getFirst());
                logger.info("Required image resize, resize ratio: {}, max height: {}, originalWidthHeightSize.getSecond(): {}",
                        resizeRatio, maxWidth, originalWidthHeightSize.getSecond());
            } else if (maxWidth == 0) {
                resizeRatio = maxHeight / originalWidthHeightSize.getSecond();
                logger.info("Required image resize, resize ratio: {}, max width: {}, originalWidthHeightSize.getFirst(): {}",
                        resizeRatio, maxWidth, originalWidthHeightSize.getFirst());
            } else if (maxHeight == 0) {
                resizeRatio = maxWidth / originalWidthHeightSize.getFirst();
                logger.info("Required image resize, resize ratio: {}, max height: {}, originalWidthHeightSize.getSecond(): {}",
                        resizeRatio, maxWidth, originalWidthHeightSize.getSecond());
            } else {
                // NOTE: if both resize max value is zero, then no any limit, no resize, resize ratio is default 1.0 value
                resizeRatio = 1.0;
            }

            if (resizeRatio != 1.0) {
                Pair<Integer, Integer> newWidthHeightSize = Pair.of(
                        Double.valueOf(originalWidthHeightSize.getFirst() * resizeRatio).intValue(),
                        Double.valueOf(originalWidthHeightSize.getSecond() * resizeRatio).intValue()
                );

                logger.info("Required image resize, resize ratio: {}, target new width: {}, target new height: {}",
                        resizeRatio, newWidthHeightSize.getFirst(), newWidthHeightSize.getSecond());
                return resizer.apply(originalImage, newWidthHeightSize);
            }
        }

        logger.info("No required image resize");
        return originalImage;
    }
}
