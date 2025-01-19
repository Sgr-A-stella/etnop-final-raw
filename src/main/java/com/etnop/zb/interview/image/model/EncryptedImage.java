package com.etnop.zb.interview.image.model;

import jakarta.persistence.*;

/**
 * Encrypted image entity for ORM operations
 */
@Entity
@Table(name = "encypted_image", schema = "public", uniqueConstraints={@UniqueConstraint(columnNames={"name"})})
public class EncryptedImage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "content_type")
    private String contentType;

    @Lob
    @Column(name = "image")
    private byte[] image;

    @Lob
    @Column(name = "init_vector")
    private byte[] initVector;

    public EncryptedImage() {}

    /**
     * Essential constructor for entity creation from uploaded and encrypted image file
     * @param name  image file name
     * @param contentType image file format/type
     * @param image encypted image file
     */
    public EncryptedImage(String name, String contentType, byte[] image, byte[] initVector) {
        this.name = name;
        this.contentType = contentType;
        this.image = image;
        this.initVector = initVector;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public byte[] getInitVector() {
        return initVector;
    }

    public void setInitVector(byte[] initVector) {
        this.initVector = initVector;
    }
}
