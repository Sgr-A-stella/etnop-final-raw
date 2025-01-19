package com.etnop.zb.interview.image.service;

import com.etnop.zb.interview.image.ImageApplicationException;
import com.etnop.zb.interview.image.helper.CryptoHelper;
import com.etnop.zb.interview.image.model.EncryptedImage;
import com.etnop.zb.interview.image.repository.EncryptedImageRepository;
import jakarta.annotation.PostConstruct;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Business logic tier - Image service business logic implementation
 */
@Service
public class ImageServiceImpl implements ImageService {

    private static Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);


    private static final String DEFAULT_SECRETKEY_FILENAME = "ImageSecretKey.txt";

    @Value("${image.resize.width: 0}")
    private int resizeWidth;

    @Value("${image.resize.height: 0}")
    private int resizeHeight;

    @Value("${image.accept.contentTypes:image/jpeg,image/png}")
    private String ACCEPT_FILE_CONTENT_TYPES;

    @Value("${image.accept.formats:png,jpg,jpeg,jfif,pjp}")
    private String ACCEPT_FILE_FORMATS_PARAMETER;

    @Value("${image.fileextension.check:true}")
    private Boolean IMAGE_FILEEXTENSION_CHECK_PARAMETER;

    @Value("classpath:" + DEFAULT_SECRETKEY_FILENAME)
    private static Resource SECRETKEY_RESOURCE_FILE;

    private SecretKey secretKey;


    @Autowired
    private EncryptedImageRepository encryptedImageRepository;

    @Autowired
    @Qualifier("imageEditService3rdpartyImgscalrImpl")
    //@Qualifier("imageEditServiceJDKGraphics2DImpl")
    private ImageEditService imageEditService;


    @PostConstruct
    public void init() {
        try {
            this.secretKey = getSecretKey();
        } catch (IOException ioe) {
            logger.error("Secret key file ({}) not found...", DEFAULT_SECRETKEY_FILENAME);
        }
    }

    /**
     * @inherited
     */
    @Override
    public EncryptedImage getEncryptedImage(String fileName) {
        EncryptedImage encryptedImage = encryptedImageRepository.findByName(fileName);
        return encryptedImage;
    }

    /**
     * @inherited
     */
    @Override
    public List<EncryptedImage> getEncryptedImages() {
        List<EncryptedImage> encryptedImageList = encryptedImageRepository.findAll();
        return encryptedImageList;
    }

    /**
     * @inherited
     */
    @Override
    public byte[] decryptImage(byte[] imageContent, IvParameterSpec ivParameterSpec) {
        try {
            SecretKey key = getSecretKey();
            String algorithm = "AES/CBC/PKCS5Padding";

            byte[] decryptedImageContent = CryptoHelper.decrypt(algorithm, imageContent, key, ivParameterSpec);

            return decryptedImageContent;
        } catch (Exception e) {
            throw new ImageApplicationException("Image content encryption error.", e);
        }
    }

    /**
     * @inherited
     */
    @Override
    public ByteArrayOutputStream addFilesToArchive(List<EncryptedImage> decryptedImageList) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);

        decryptedImageList.forEach(decryptedImage -> {
            if (decryptedImage != null && decryptedImage.getImage() != null && decryptedImage.getImage().length > 0) {
                InputStream downloadImageContent = new ByteArrayInputStream(decryptedImage.getImage());
                try {
                    zipOutputStream.putNextEntry(new ZipEntry(decryptedImage.getName()));
                    IOUtils.copy(downloadImageContent, zipOutputStream);
                    zipOutputStream.closeEntry();
                } catch (IOException e) {
                    // FIXME correct exception handling (errorlist?)
                    // NOTE: maybe partially unsuccessful in case of multi file download,
                    // but incorrect exception swallowing from user view
                    logger.error("Failed decrypted image packing: {}", decryptedImage.getName());
                }
            }
        });

        zipOutputStream.finish();
        zipOutputStream.flush();
        IOUtils.closeQuietly(zipOutputStream);
        IOUtils.closeQuietly(bufferedOutputStream);
        IOUtils.closeQuietly(byteArrayOutputStream);

        return byteArrayOutputStream;
    }

    /**
     * @inherited
     */
    @Override
    public String saveImage(MultipartFile uploadedFile) throws IOException {
        String fileName = uploadedFile.getOriginalFilename();

        String contentType = uploadedFile.getContentType();
        logger.info("Upload file content type: {}", contentType);
        logger.info("Accept content types: {}", ACCEPT_FILE_CONTENT_TYPES);
        if (!Arrays.stream(ACCEPT_FILE_CONTENT_TYPES.split(",")).anyMatch(acceptContentType -> acceptContentType.equals(contentType))) {
            throw new IllegalArgumentException("Only JPG, JPEG or PNG images are allowed");
        }

        if (IMAGE_FILEEXTENSION_CHECK_PARAMETER) {
            String fileNameExtension = getFileExtension(fileName);
            if (!Arrays.stream(ACCEPT_FILE_FORMATS_PARAMETER.split(",")).anyMatch(acceptExtension -> acceptExtension.equals(fileNameExtension))) {
                throw new IllegalArgumentException("Only JPG, JPEG or PNG images are allowed");
            }
        }

        EncryptedImage encryptedImage = createEncrpytedImage(uploadedFile);
        encryptedImageRepository.saveAndFlush(encryptedImage);

        return fileName;
    }


    private EncryptedImage createEncrpytedImage(MultipartFile uploadedFile) throws ImageApplicationException {
        try {
            IvParameterSpec ivParameterSpec = CryptoHelper.generateIv();
            byte[] resizedImageContent = resizeImage(uploadedFile);
            byte[] encrytedImageContent = encryptImage(resizedImageContent, ivParameterSpec);
            EncryptedImage encryptedImage = new EncryptedImage(
                    uploadedFile.getOriginalFilename(), uploadedFile.getContentType(), encrytedImageContent, ivParameterSpec.getIV());

            return encryptedImage;
        } catch (Exception e) {
            throw new ImageApplicationException("Image content encryption error.", e);
        }
    }

    private byte[] encryptImage(byte[] imageContent, IvParameterSpec ivParameterSpec) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {
        SecretKey key = getSecretKey();
        String algorithm = "AES/CBC/PKCS5Padding";

        byte[] encrytedImageContent = CryptoHelper.encrypt(algorithm, imageContent, key, ivParameterSpec);

        return encrytedImageContent;
    }

    private byte[] resizeImage(MultipartFile uploadedFile) throws IOException {
        logger.info("Resize max width: {}, height: {}", resizeWidth, resizeHeight);

        byte[] uploadedImageContent = uploadedFile.getBytes();
        byte[] resizedImageContent = (resizeWidth == 0 && resizeHeight == 0) ? uploadedImageContent :
                imageEditService.resizeImage(uploadedImageContent, MediaType.parseMediaType(uploadedFile.getContentType()),
                        resizeWidth, resizeHeight);

        return resizedImageContent;
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

    private SecretKey getSecretKey() throws IOException {
        return this.secretKey != null ? this.secretKey : (this.secretKey = getSecretKeyFromFile());
    }

    private SecretKey getSecretKeyFromFile() throws IOException {
        if (SECRETKEY_RESOURCE_FILE != null && SECRETKEY_RESOURCE_FILE.isFile()) {
            File resource = SECRETKEY_RESOURCE_FILE.getFile();
            return extractSecretKeyFromFile(resource.toPath());
        } else {
            File secretKeyFile = new File(DEFAULT_SECRETKEY_FILENAME);
            if (secretKeyFile.exists()) {
                return extractSecretKeyFromFile(secretKeyFile.toPath());
            } else {
                logger.info("Secret key absolut path: {}", secretKeyFile.toPath().toAbsolutePath());
                String errorText = "Secret key file ("+DEFAULT_SECRETKEY_FILENAME+") not found...";
                logger.error(errorText);
                throw new ImageApplicationException(errorText);
            }
        }
    }

    private SecretKey extractSecretKeyFromFile(Path secretKeyFilePath) throws IOException {
        String base64SecretKey = new String(Files.readAllBytes(secretKeyFilePath));
        byte[] decodedKey = Base64.getDecoder().decode(base64SecretKey);
        logger.info("Extracted secret key: {}", decodedKey);
        // FIXME algorithm string extraction
        SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        return secretKey;
    }
}
