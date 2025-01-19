package com.etnop.zb.interview.image.repository;

import com.etnop.zb.interview.image.model.EncryptedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DAO tier: repository for query image entity / entities
 */
@Repository
public interface EncryptedImageRepository  extends JpaRepository<EncryptedImage, Long> {

    /**
     * Query image entity by name
     *
     * @param name
     * @return image entity with parameter name
     */
    EncryptedImage findByName(String name);

    /**
     * Query all image entities
     *
     * @return all image entities
     */
    List<EncryptedImage> findAll();

    // FIXME unnecessary
    /**
     * Save image entity
     *
     * @param encryptedImage
     * @return
     */
    EncryptedImage saveAndFlush(EncryptedImage encryptedImage);

}