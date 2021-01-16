package uk.co.rosehilltimber.rosehilltreatmentapp.utils;

import android.support.annotation.NonNull;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtility
{

    private static final String HASHING_ALGORITHM = "SHA-256";
    private static final String HASHING_ALGORITHM_CHARSET_NAME = "UTF-8";

    @NonNull
    public static String newHash(@NonNull final String string)
            throws RuntimeException, NoSuchAlgorithmException
    {

        final MessageDigest messageDigest = MessageDigest.getInstance(HASHING_ALGORITHM);
        final byte[] bytes = messageDigest.digest(string.getBytes(Charset.forName(
                HASHING_ALGORITHM_CHARSET_NAME
        )));

        final StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            stringBuffer.append(Integer.toHexString((bytes[i] & 0xFF) | 0x100).substring(1, 3));
        }

        return stringBuffer.toString();
    }
}
