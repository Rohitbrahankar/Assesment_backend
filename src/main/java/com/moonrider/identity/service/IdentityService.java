package com.moonrider.identity.service;

import com.moonrider.identity.dto.IdentifyRequest;
import com.moonrider.identity.dto.IdentifyResponse;
import com.moonrider.identity.model.Contact;
import com.moonrider.identity.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IdentityService {

    @Autowired
    private ContactRepository contactRepository;

    @Transactional
    public IdentifyResponse processRequest(IdentifyRequest request) {
        String email = request.getEmail();
        String phone = request.getPhoneNumber();

        List<Contact> matchedContacts = contactRepository.findByEmailOrPhoneNumber(email, phone);

        if (matchedContacts.isEmpty()) {
            Contact newContact = new Contact();
            newContact.setEmail(email);
            newContact.setPhoneNumber(phone);
            newContact.setLinkPrecedence("primary");
            newContact.setCreatedAt(LocalDateTime.now());
            newContact.setUpdatedAt(LocalDateTime.now());
            contactRepository.save(newContact);

            return new IdentifyResponse(
                newContact.getId(),
                List.of(email),
                List.of(phone),
                List.of()
            );
        }

        Set<Contact> allLinked = new HashSet<>(matchedContacts);
        for (Contact c : matchedContacts) {
            if (c.getLinkedId() != null) {
                allLinked.addAll(contactRepository.findByEmailOrPhoneNumber(null, null).stream()
                        .filter(e -> Objects.equals(e.getLinkedId(), c.getLinkedId()))
                        .collect(Collectors.toSet()));
            }
        }

        Contact primary = allLinked.stream()
            .filter(c -> "primary".equals(c.getLinkPrecedence()))
            .min(Comparator.comparing(Contact::getCreatedAt))
            .orElseThrow();

        if (allLinked.stream().noneMatch(c -> Objects.equals(c.getEmail(), email) && Objects.equals(c.getPhoneNumber(), phone))) {
            Contact secondary = new Contact();
            secondary.setEmail(email);
            secondary.setPhoneNumber(phone);
            secondary.setLinkPrecedence("secondary");
            secondary.setLinkedId(primary.getId());
            secondary.setCreatedAt(LocalDateTime.now());
            secondary.setUpdatedAt(LocalDateTime.now());
            contactRepository.save(secondary);
            allLinked.add(secondary);
        }

        Set<String> emails = allLinked.stream().map(Contact::getEmail).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> phones = allLinked.stream().map(Contact::getPhoneNumber).filter(Objects::nonNull).collect(Collectors.toSet());
        List<Integer> secondaries = allLinked.stream()
                .filter(c -> "secondary".equals(c.getLinkPrecedence()))
                .map(Contact::getId)
                .toList();

        return new IdentifyResponse(primary.getId(), new ArrayList<>(emails), new ArrayList<>(phones), secondaries);
    }
}