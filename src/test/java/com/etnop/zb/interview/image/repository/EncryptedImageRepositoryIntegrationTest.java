package com.etnop.zb.interview.image.repository;

import com.etnop.zb.interview.image.model.EncryptedImage;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.http.MediaType;

import java.util.List;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureEmbeddedDatabase(provider = ZONKY)
public class EncryptedImageRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EncryptedImageRepository encryptedImageRepository;


    private EncryptedImage testPngImage;
    private EncryptedImage testJpgImage;


    @Test
    public void whenFindByName_thenReturnEncryptedImage() {
        // given in beforeEach

        // when
        EncryptedImage found = encryptedImageRepository.findByName(testPngImage.getName());

        // then
        assertThat(found.getName())
                .isEqualTo(testPngImage.getName());
    }

    @Test
    public void whenFindAll_thenReturnAllEncryptedImages() {
        // given in beforeEach

        // when
        List<EncryptedImage> foundList = encryptedImageRepository.findAll();

        // then
        assertThat(foundList != null && foundList.size() == 2).isTrue();
    }

    @Test
    public void whenSaveAndFlush_thenQueryEncryptedImage() {
        // given
        EncryptedImage newTestPngImage = new EncryptedImage("NewTestPng.png", MediaType.IMAGE_PNG.toString(), new byte[] {}, new byte[] {});

        // when
        encryptedImageRepository.saveAndFlush(newTestPngImage);
        EncryptedImage found = encryptedImageRepository.findByName(newTestPngImage.getName());

        // then
        assertThat(found.getName())
                .isEqualTo(newTestPngImage.getName());
    }

    @BeforeEach
    public void setup() {
        testPngImage = new EncryptedImage("TestPng.png", MediaType.IMAGE_PNG.toString(), new byte[] {}, new byte[] {});
        entityManager.persist(testPngImage);
        entityManager.flush();

        testJpgImage = new EncryptedImage("TestJpg.jpg", MediaType.IMAGE_JPEG.toString(), new byte[] {}, new byte[] {});
        entityManager.persist(testJpgImage);
        entityManager.flush();
    }

    @AfterEach
    public void clear() {
        entityManager.remove(testPngImage);
        entityManager.remove(testJpgImage);
    }

    // TODO more tests, example: negative tests
}