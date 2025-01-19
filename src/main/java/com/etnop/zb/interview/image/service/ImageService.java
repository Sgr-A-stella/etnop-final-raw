package com.etnop.zb.interview.image.service;

import com.etnop.zb.interview.image.model.EncryptedImage;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Business logic tier - Image service business interface
 */
public interface ImageService {

    /**
     * Get image entity by name
     *
     * @param fileName
     * @return
     */
    EncryptedImage getEncryptedImage(String fileName);

    /**
     * All image entities
     *
     * @return
     */
    List<EncryptedImage> getEncryptedImages();

    /**
     * Decrypt an encrypted image
     *
     * @param imageContent
     * @return
     */
    byte[] decryptImage(byte[] imageContent, IvParameterSpec ivParameterSpec);

    /**
     * Images packed to zip format
     *
     * @param decryptedImageList
     * @return
     * @throws IOException
     */
    ByteArrayOutputStream addFilesToArchive(List<EncryptedImage> decryptedImageList) throws IOException;

    /**
     * Save uploaded image to DB with encrypted
     *
     * @param uploadedFile
     * @return
     * @throws IOException
     */
    String saveImage(MultipartFile uploadedFile) throws IOException;
}
