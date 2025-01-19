package com.etnop.zb.interview.image.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Secret key generator utility for create persistent (filesystem stored) secret key of encryption-decryption operations
 */
public class SecretKeyGenerator {

    private static Logger logger = LoggerFactory.getLogger(SecretKeyGenerator.class);

    private static final String DEFAULT_SECRETKEY_FILENAME = "ImageSecretKey.txt";
    private static final String DEFAULT_BASE_ALGORITHM = "AES";

    // NOTE: Valid values: 256, 192, 128
    private static final int DEFAULT_BASE_KEYSIZE = 256;
    private static final String DEFAULT_PASSWORD_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DEFAULT_ITERATION_COUNT = 65536;
    private static final int DEFAULT_KEY_LENGTH = 256;


    public static void main(String[] args) {
        try {
            SecretKey secretKey;
            if (args.length == 0) {
                secretKey = generateKey(DEFAULT_BASE_ALGORITHM, DEFAULT_BASE_KEYSIZE);
                String base64SecretKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
                File secretKeyFile = new File(DEFAULT_SECRETKEY_FILENAME);
                if (secretKeyFile.createNewFile()) {
                    try (FileOutputStream fileOutputStream = new FileOutputStream(secretKeyFile)) {
                        fileOutputStream.write(base64SecretKey.getBytes());
                    } catch (Exception e) {
                        logger.error("Secret key file write fail: ", e);
                    }
                } else {
                    logger.error("Secret file exist! (Override not supported, because of stored images could be undecryptable!)");
                }
            } else {
                // TODO: arguments based generation (path, filename, keysize / password-iteration-keylength, ...etc.)
                logger.warn("Arguments based generation not supported yet...");
            }
        } catch (Exception e) {
            logger.error("Secret key file generation fail: ", e);
        }
    }

    /**
     * Base secret key generation without password
     *
     * @param baseAlgorithm
     * @param keySize
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey generateKey(String baseAlgorithm, int keySize) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(baseAlgorithm);
        keyGenerator.init(keySize);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    /**
     * Password based secret key generation
     *
     * @param password
     * @param salt
     * @param baseAlgorithm
     * @param pwAlgorithm
     * @param iterationCount
     * @param keyLength
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static SecretKey getKeyFromPassword(String password, String salt, String baseAlgorithm, String pwAlgorithm,
                                               int iterationCount, int keyLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance(pwAlgorithm);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), iterationCount, keyLength);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                .getEncoded(), baseAlgorithm);
        return secret;
    }
}
