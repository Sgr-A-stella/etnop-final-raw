package com.etnop.zb.interview.image.controller;

import com.etnop.zb.interview.image.helper.ImageHelper;
import com.etnop.zb.interview.image.model.EncryptedImage;
import com.etnop.zb.interview.image.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * REST controller for upload and download image(s).
 */
@RestController
@RequestMapping("/api")
// TODO correct error handling
public class ImageController {

    private static Logger logger = LoggerFactory.getLogger(ImageController.class);

    @Value("${image.max.width:5000}")
    private int maxWidth;

    @Value("${image.max.height:5000}")
    private int maxHeight;

    @Autowired
    ImageService imageService;


    /**
     * Download image by name
     *
     * @param fileName
     * @return
     */
    @GetMapping("/file/{fileName}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> getImageByName(@PathVariable("fileName") String fileName) {
        EncryptedImage encryptedImage = imageService.getEncryptedImage(fileName);
        if (encryptedImage != null && encryptedImage.getImage() != null && encryptedImage.getImage().length > 0) {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(encryptedImage.getInitVector());
            byte[] decryptedImage = imageService.decryptImage(encryptedImage.getImage(), ivParameterSpec);

            MediaType contentType = MediaType.valueOf(encryptedImage.getContentType());
            InputStream downloadImageContent = new ByteArrayInputStream(decryptedImage);
            return ResponseEntity.ok()
                    .contentType(contentType)
                    .body(new InputStreamResource(downloadImageContent));
        } else if (encryptedImage == null){
            logger.info("Requested {} image not found...", fileName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(new InputStreamResource(
                            new ByteArrayInputStream((
                                    "Requested image (" +fileName + ") not found").getBytes(StandardCharsets.UTF_8)
                            )
                    ));
        } else {
            logger.info("Requested {} image is empty...", fileName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(new InputStreamResource(
                            new ByteArrayInputStream((
                                    "Requested image (" +fileName + ") is empty").getBytes(StandardCharsets.UTF_8)
                            )
                    ));
        }
    }

    /**
     * Download all images in zip file
     *
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/files", produces = "application/zip")
    @ResponseBody
    public ResponseEntity<byte[]> getAllImages() throws IOException {
        List<EncryptedImage> encryptedImageList = imageService.getEncryptedImages();
        List<EncryptedImage> decryptedImageList = new ArrayList<>();
        encryptedImageList.stream().forEach(encryptedImage -> {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(encryptedImage.getInitVector());
            byte[] decryptedImage = imageService.decryptImage(encryptedImage.getImage(), ivParameterSpec);
            decryptedImageList.add(new EncryptedImage(encryptedImage.getName(), encryptedImage.getContentType(), decryptedImage, encryptedImage.getInitVector()));
        });

        ByteArrayOutputStream byteArrayOutputStream = imageService.addFilesToArchive(decryptedImageList);

        return ResponseEntity
                .ok()
                .header("Content-Disposition", "attachment; filename=\"images.zip\"")
                .body(byteArrayOutputStream.toByteArray());
    }

    /**
     * Upload image(s) and save to database in encrypted format
     *
     * @param files
     * @return
     */
    @PostMapping("/files")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile[] files) throws IOException {
        logger.info("Arrived count of file(s): {}", files.length);

        List<String> doneNames = new ArrayList<>();
        List<String> failNames = new ArrayList<>();
        List<String> overNames = new ArrayList<>();

        Arrays.stream(files).forEach(multipartFile -> {
                try {
                    if (!ImageHelper.isOversize(multipartFile.getBytes(), multipartFile.getContentType(), maxWidth, maxHeight)) {
                        doneNames.add(imageService.saveImage(multipartFile));
                    } else {
                        logger.warn("Uploaded image ({}) too big...", multipartFile.getOriginalFilename());
                        logger.info("Current max width: {}; max height: {}", maxWidth, maxHeight);
                        overNames.add(multipartFile.getOriginalFilename());
                    }
                } catch (Exception e) {
                    logger.error("Uploaded file ({}) saving failed...", multipartFile.getOriginalFilename());
                    failNames.add(multipartFile.getOriginalFilename());
                }
        });

        return ResponseEntity.ok("Image(s) uploaded, downloadable(s) by name: " + doneNames +
                "; too big image(s): " + overNames + "; failed image(s): " + failNames);
    }
}
