package com.etnop.zb.interview.image;

/**
 * Application business logic exception for unique error handling
 */
public class ImageApplicationException extends RuntimeException {

    public ImageApplicationException() {}

    public ImageApplicationException(Throwable cause) {
        super(cause);
    }

    public ImageApplicationException(String message) {
        super(message);
    }

    public ImageApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
