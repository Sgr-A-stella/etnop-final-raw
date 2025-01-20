package com.etnop.zb.interview.image.controller;

import com.etnop.zb.interview.image.model.EncryptedImage;
import com.etnop.zb.interview.image.repository.EncryptedImageRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = WebConfig.class
)
@AutoConfigureMockMvc
public class ImageControllerIntegrationTests {
    private static Logger logger = LoggerFactory.getLogger(ImageControllerIntegrationTests.class);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ImageController imageController;

    @Autowired
    private EncryptedImageRepository encryptedImageRepository;

    @Test
    public void testGetImageByNameNotFound() throws Exception {
        String fileName = "nincs_ilyen.png";

        mvc.perform(get("/api/file/" + fileName).contentType(MediaType.IMAGE_PNG))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetImageByNameFound() throws Exception {
        String name3x3_whitePNG = "3x3_white.png";
        byte[] imageContent = new byte[] {};
        File imageFile = new File(name3x3_whitePNG);
        logger.info("Test image file: {}, {}", imageFile.getAbsolutePath(), imageFile.getPath());
        if (imageFile.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(imageFile)) {
                imageContent = fileInputStream.readAllBytes();
            } catch (Exception e) {
                logger.error("Test image file {} read fail: ", name3x3_whitePNG, e);
            }
        } else {
            logger.error("Test image file {} not exists: , name3x3_whitePNG");
        }

        MockMultipartFile mockMultipartFile = new MockMultipartFile(name3x3_whitePNG, name3x3_whitePNG, MediaType.IMAGE_PNG.toString(), imageContent);
        imageController.uploadImage(new MultipartFile[]{ mockMultipartFile });
        EncryptedImage encryptedImage = encryptedImageRepository.findByName(name3x3_whitePNG);

        mvc.perform(get("/api/file/" + name3x3_whitePNG)
                        .contentType(MediaType.IMAGE_PNG))
                .andExpect(status().isOk());

        encryptedImageRepository.delete(encryptedImage);
    }

    @Test
    public void testGetAllImages() throws Exception {
        String name3x3_whitePNG = "3x3_white.png";
        byte[] imageContent = new byte[] {};
        File imageFile = new File(name3x3_whitePNG);
        logger.info("Test image file: {}, {}", imageFile.getAbsolutePath(), imageFile.getPath());
        if (imageFile.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(imageFile)) {
                imageContent = fileInputStream.readAllBytes();
            } catch (Exception e) {
                logger.error("Test image file {} read fail: ", name3x3_whitePNG, e);
            }
        } else {
            logger.error("Test image file {} not exists: , name3x3_whitePNG");
        }

        MockMultipartFile mockMultipartFile = new MockMultipartFile(name3x3_whitePNG, name3x3_whitePNG, MediaType.IMAGE_PNG.toString(), imageContent);
        imageController.uploadImage(new MultipartFile[]{ mockMultipartFile });
        EncryptedImage encryptedImage = encryptedImageRepository.findByName(name3x3_whitePNG);

        mvc.perform(get("/api/files"))
                .andExpect(status().isOk());

        encryptedImageRepository.delete(encryptedImage);
    }

    @Test
    public void testUploadImage() throws Exception {
        String name3x3_whitePNG = "3x3_white.png";
        byte[] imageContent = new byte[] {};
        File imageFile = new File(name3x3_whitePNG);
        logger.info("Test image file: {}, {}", imageFile.getAbsolutePath(), imageFile.getPath());
        if (imageFile.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(imageFile)) {
                imageContent = fileInputStream.readAllBytes();
            } catch (Exception e) {
                logger.error("Test image file {} read fail: ", name3x3_whitePNG, e);
            }
        } else {
            logger.error("Test image file {} not exists: , name3x3_whitePNG");
        }

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", name3x3_whitePNG, MediaType.IMAGE_PNG.toString(), imageContent);
        mvc.perform(multipart("/api/files")
                        .file(mockMultipartFile)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Image(s) uploaded, downloadable(s) by name: [" + name3x3_whitePNG +
                        "]; too big image(s): []; failed image(s): []"));
        EncryptedImage encryptedImage = encryptedImageRepository.findByName(name3x3_whitePNG);

        Assertions.assertTrue(encryptedImage.getName().equals(name3x3_whitePNG));

        encryptedImageRepository.delete(encryptedImage);
    }

    @Test
    public void testUploadImages() throws Exception {
        String name3x3_whitePNG = "3x3_white.png";
        String name3x3_whitePNG1 = "3x3_white_1.png";
        String name3x3_whitePNG2 = "3x3_white_2.png";
        byte[] imageContent = new byte[] {};
        File imageFile = new File(name3x3_whitePNG);
        logger.info("Test image file: {}, {}", imageFile.getAbsolutePath(), imageFile.getPath());
        if (imageFile.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(imageFile)) {
                imageContent = fileInputStream.readAllBytes();
            } catch (Exception e) {
                logger.error("Test image file {} read fail: ", name3x3_whitePNG, e);
            }
        } else {
            logger.error("Test image file {} not exists: , name3x3_whitePNG");
        }

        MockMultipartFile mockMultipartFile1 = new MockMultipartFile("file", name3x3_whitePNG1, MediaType.IMAGE_PNG.toString(), imageContent);
        MockMultipartFile mockMultipartFile2 = new MockMultipartFile("file", name3x3_whitePNG2, MediaType.IMAGE_PNG.toString(), imageContent);
        mvc.perform(multipart("/api/files")
                        //.file(mockMultipartFile)
                        .file(mockMultipartFile1)
                        .file(mockMultipartFile2)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Image(s) uploaded, downloadable(s) by name: [" + name3x3_whitePNG1 + ", " + name3x3_whitePNG2 +
                        "]; too big image(s): []; failed image(s): []"));
        EncryptedImage encryptedImage1 = encryptedImageRepository.findByName(name3x3_whitePNG1);
        EncryptedImage encryptedImage2 = encryptedImageRepository.findByName(name3x3_whitePNG2);

        Assertions.assertTrue(encryptedImage1.getName().equals(name3x3_whitePNG1));
        Assertions.assertTrue(encryptedImage2.getName().equals(name3x3_whitePNG2));

        encryptedImageRepository.delete(encryptedImage1);
        encryptedImageRepository.delete(encryptedImage2);
    }
}
