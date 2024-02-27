package com.eventplanner.util;

import java.security.Key;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.eventplanner.entities.Events;
import com.eventplanner.entities.Users;
import org.springframework.stereotype.Component;

@Component
public class HashingUtils {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String secret = "984hg493gh0439rt";
    private static final int SHORT_IDENTIFIER_LENGTH = 8;

    private static final Random RANDOM = new SecureRandom();

    public static String generateInvitationLink(UUID eventId, UUID invitedByUserId)
    {
        return eventId.toString() + ";"  + ";" + invitedByUserId.toString();
    }



}
