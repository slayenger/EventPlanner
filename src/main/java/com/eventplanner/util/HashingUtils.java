package com.eventplanner.util;

import java.security.Key;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.eventplanner.entities.Events;
import com.eventplanner.entities.Users;
import org.springframework.stereotype.Component;

@Component
public class HashingUtils {

    private static final String secret = "984hg493gh0439rt";

    public static String encryptData(String data) {
        try {
            Key secretKey = new SecretKeySpec(secret.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));
            return Base64.getUrlEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decryptData(String encryptedData) {
        try {
            Key secretKey = new SecretKeySpec(secret.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedData = cipher.doFinal(Base64.getUrlDecoder().decode(encryptedData));
            return new String(decryptedData, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("error decrypting");
        }
    }

    public static String generateInvitationLink(UUID eventId, UUID invitedUserId, UUID invitedByUserId)
    {
        String data = eventId.toString() + ";" + invitedUserId.toString() + ";" + invitedByUserId.toString();
        String hash = encryptData(data);
        return hash;
    }

}
