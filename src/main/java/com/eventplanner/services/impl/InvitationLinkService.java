package com.eventplanner.services.impl;

import com.eventplanner.dtos.EventsResponseDTO;
import com.eventplanner.entities.EventInvitations;
import com.eventplanner.entities.Events;
import com.eventplanner.entities.InvitationLink;
import com.eventplanner.exceptions.NotFoundException;
import com.eventplanner.mappers.EventsMapper;
import com.eventplanner.repositories.EventInvitationsRepository;
import com.eventplanner.repositories.InvitationLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvitationLinkService {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_IDENTIFIER_LENGTH = 8;
    private static final Random RANDOM = new SecureRandom();
    private final InvitationLinkRepository linkRepository;
    private final EventsMapper eventsMapper;


    public static String generateRandomString() {
        return RANDOM.ints(SHORT_IDENTIFIER_LENGTH, 0, CHARACTERS.length())
                .mapToObj(CHARACTERS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    public boolean isShortLinkUnique (String shortIdentifier)
    {
        return !linkRepository.existsByShortIdentifier(shortIdentifier);
    }

    public void generateUniqueShortIdentifier(EventInvitations invitation) {
        String shortIdentifier;
        do {
            shortIdentifier = generateRandomString();
        } while (!isShortLinkUnique(shortIdentifier));

        InvitationLink invitationLink = new InvitationLink();
        invitationLink.setInvitation(invitation);
        invitationLink.setShortIdentifier(shortIdentifier);
        linkRepository.save(invitationLink);
    }

    public EventsResponseDTO findByShortIdentifier (String shortIdentifier)
    {
        InvitationLink link = linkRepository.findByShortIdentifier(shortIdentifier)
                .orElseThrow(() -> new NotFoundException("The invitation is incorrect or expired"));

        Events event = link.getInvitation().getEvent();
        return eventsMapper.toDTO(event);
    }
}
