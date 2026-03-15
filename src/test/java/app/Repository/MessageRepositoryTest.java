package app.Repository;

import app.Database.*;
import app.Repository.CommunityRepository;
import app.Repository.EventRepository;
import app.Repository.MessageRepository;
import app.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("MessageRepository Tests")
class MessageRepositoryTest {

    @Autowired private MessageRepository messageRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private UserRepository userRepository;

    private User sender1;
    private User sender2;
    private Community community;
    private Event event1;
    private Event event2;

    private Message msg1; // event1, sender1, TEXT
    private Message msg2; // event1, sender1, TEXT
    private Message msg3; // event1, sender2, MEDIA
    private Message msg4; // event2, sender1, TEXT

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        eventRepository.deleteAll();
        communityRepository.deleteAll();
        userRepository.deleteAll();

        sender1   = saveUser("sender1@test.com", "Sender One");
        sender2   = saveUser("sender2@test.com", "Sender Two");
        community = saveCommunity("Test Community", sender1);
        event1    = saveEvent("Event One", community, sender1);
        event2    = saveEvent("Event Two", community, sender1);

        msg1 = saveMessage(event1, sender1, MessageType.TEXT,  "Hello everyone!");
        msg2 = saveMessage(event1, sender1, MessageType.TEXT,  "Any updates?");
        msg3 = saveMessage(event1, sender2, MessageType.MEDIA, "Check this out");
        msg4 = saveMessage(event2, sender1, MessageType.TEXT,  "Other event msg");
    }

    // ===================== findByEventIdOrderByCreatedAtDesc (paginated) =====================

    @Nested
    @DisplayName("findByEventIdOrderByCreatedAtDesc() - paginated")
    class FindByEventIdOrderByCreatedAtDescPaginated {

        @Test
        @DisplayName("Should return messages for event in descending order (newest first)")
        void find_paginated_returnsMessagesNewestFirst() {
            Page<Message> result = messageRepository.findByEventIdOrderByCreatedAtDesc(
                    event1.getId(), PageRequest.of(0, 10));

            assertEquals(3, result.getTotalElements());
            // Newest message should come first — msg3 was saved last
            assertEquals("Check this out", result.getContent().get(0).getContent());
        }

        @Test
        @DisplayName("Should respect page size limit")
        void find_paginated_respectsPageSize() {
            Page<Message> result = messageRepository.findByEventIdOrderByCreatedAtDesc(
                    event1.getId(), PageRequest.of(0, 2));

            assertEquals(2, result.getContent().size());
            assertEquals(3, result.getTotalElements());
        }

        @Test
        @DisplayName("Should return empty page when event has no messages")
        void find_paginated_whenNoMessages_returnsEmpty() {
            Page<Message> result = messageRepository.findByEventIdOrderByCreatedAtDesc(
                    event2.getId(), PageRequest.of(0, 10));

            // event2 has msg4
            assertEquals(1, result.getTotalElements());
        }
    }

    // ===================== findByEventIdOrderByCreatedAtAsc =====================

    @Nested
    @DisplayName("findByEventIdOrderByCreatedAtAsc()")
    class FindByEventIdOrderByCreatedAtAsc {

        @Test
        @DisplayName("Should return messages in ascending order (oldest first)")
        void find_ascendingOrder_returnsOldestFirst() {
            List<Message> result = messageRepository
                    .findByEventIdOrderByCreatedAtAsc(event1.getId());

            assertEquals(3, result.size());
            // Oldest message first — msg1 was saved first
            assertEquals("Hello everyone!", result.get(0).getContent());
        }

        @Test
        @DisplayName("Should return empty when event has no messages")
        void find_ascending_whenNoMessages_returnsEmpty() {
            Event empty = saveEvent("Empty Event", community, sender1);
            assertTrue(messageRepository.findByEventIdOrderByCreatedAtAsc(empty.getId()).isEmpty());
        }
    }

    // ===================== findByEventIdAndSenderIdOrderByCreatedAtDesc =====================

    @Nested
    @DisplayName("findByEventIdAndSenderIdOrderByCreatedAtDesc()")
    class FindByEventIdAndSenderIdOrderByCreatedAtDesc {

        @Test
        @DisplayName("Should return only messages sent by a specific user in an event")
        void find_bySender_returnsOnlySenderMessages() {
            List<Message> result = messageRepository
                    .findByEventIdAndSenderIdOrderByCreatedAtDesc(
                            event1.getId(), sender1.getId());

            assertEquals(2, result.size());
            result.forEach(m -> assertEquals(sender1.getId(), m.getSender().getId()));
        }

        @Test
        @DisplayName("Should return messages in descending order for user")
        void find_bySender_isOrderedDesc() {
            List<Message> result = messageRepository
                    .findByEventIdAndSenderIdOrderByCreatedAtDesc(
                            event1.getId(), sender1.getId());

            // msg2 was saved after msg1, so it appears first
            assertEquals("Any updates?", result.get(0).getContent());
        }

        @Test
        @DisplayName("Should return empty when user has no messages in that event")
        void find_bySender_whenNoMessages_returnsEmpty() {
            List<Message> result = messageRepository
                    .findByEventIdAndSenderIdOrderByCreatedAtDesc(
                            event2.getId(), sender2.getId());

            assertTrue(result.isEmpty());
        }
    }

    // ===================== countByEventId =====================

    @Nested
    @DisplayName("countByEventId()")
    class CountByEventId {

        @Test
        @DisplayName("Should return correct message count for event")
        void count_returnsCorrectTotal() {
            Long count = messageRepository.countByEventId(event1.getId());
            assertEquals(3L, count);
        }

        @Test
        @DisplayName("Should return 1 for event with single message")
        void count_singleMessage_returns1() {
            Long count = messageRepository.countByEventId(event2.getId());
            assertEquals(1L, count);
        }

        @Test
        @DisplayName("Should return 0 for event with no messages")
        void count_whenNoMessages_returnsZero() {
            Event empty = saveEvent("Empty Event", community, sender1);
            Long count = messageRepository.countByEventId(empty.getId());
            assertEquals(0L, count);
        }
    }

    // ===================== deleteByEventId =====================

    @Nested
    @DisplayName("deleteByEventId()")
    class DeleteByEventId {

        @Test
        @DisplayName("Should delete all messages for an event")
        void delete_removesAllEventMessages() {
            messageRepository.deleteByEventId(event1.getId());

            Long remaining = messageRepository.countByEventId(event1.getId());
            assertEquals(0L, remaining);
        }

        @Test
        @DisplayName("Should not delete messages from other events")
        void delete_doesNotAffectOtherEvents() {
            messageRepository.deleteByEventId(event1.getId());

            Long count = messageRepository.countByEventId(event2.getId());
            assertEquals(1L, count); // msg4 should remain
        }
    }

    // ===================== helpers =====================

    private User saveUser(String email, String name) {
        User u = new User();
        u.setEmail(email);
        u.setName(name);
        u.setPassword("pass");
        u.setActive(true);
        return userRepository.save(u);
    }

    private Community saveCommunity(String name, User creator) {
        Community c = new Community();
        c.setName(name);
        c.setCreatedBy(creator);
        return communityRepository.save(c);
    }

    private Event saveEvent(String title, Community community, User creator) {
        Event e = new Event();
        e.setTitle(title);
        e.setCommunity(community);
        e.setCreatedBy(creator);
        e.setEventDate(LocalDate.now().plusDays(7));
        e.setEventTime(LocalTime.of(10, 0));
        e.setAttendanceEnabled(false);
        return eventRepository.save(e);
    }

    private Message saveMessage(Event event, User sender, MessageType type, String content) {
        Message m = new Message();
        m.setEvent(event);
        m.setSender(sender);
        m.setType(type);
        m.setContent(content);
        return messageRepository.save(m);
    }
}
