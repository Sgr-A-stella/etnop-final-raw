package com.etnop.zb.interview.image.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CryptoHelperTest {

    private static Logger logger = LoggerFactory.getLogger(CryptoHelperTest.class);
    @Test
    void testEncryptDecrypt()
            throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException,
            BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {

        String original = "Mint tudjuk, Isten paradoxonokban szól hozzánk...";

        SecretKey key = SecretKeyGenerator.generateKey("AES", 128);
        IvParameterSpec ivParameterSpec = CryptoHelper.generateIv();
        String algorithm = "AES/CBC/PKCS5Padding";

        byte[] encrypted = CryptoHelper.encrypt(algorithm, original.getBytes(), key, ivParameterSpec);
        byte[] decrypted = CryptoHelper.decrypt(algorithm, encrypted, key, ivParameterSpec);

        logger.info("original : {}", original);
        logger.info("encrypted: {}", new String(encrypted));
        logger.info("decrypted: {}", new String(decrypted));

        Assertions.assertEquals(original, new String(decrypted));
    }
}
