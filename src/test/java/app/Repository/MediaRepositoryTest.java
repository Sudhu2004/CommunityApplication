package app.Repository;

import app.Database.*;
import app.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("MediaRepository Tests")
class MediaRepositoryTest {

    @Autowired private MediaRepository mediaRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private UserRepository userRepository;

    private User sender;
    private Community community;
    private Event event1;
    private Event event2;
    private Message message1; // in event1 — has IMAGE + VIDEO
    private Message message2; // in event1 — has PDF
    private Message message3; // in event2 — has IMAGE

    private Media img1;
    private Media vid1;
    private Media pdf1;
    private Media img2; // in event2

    @BeforeEach
    void setUp() {
        mediaRepository.deleteAll();
        messageRepository.deleteAll();
        eventRepository.deleteAll();
        communityRepository.deleteAll();
        userRepository.deleteAll();

        sender    = saveUser("sender@test.com", "Sender");
        community = saveCommunity("Community", sender);
        event1    = saveEvent("Event One", community, sender);
        event2    = saveEvent("Event Two", community, sender);

        message1 = saveMessage(event1, sender, MessageType.MEDIA, "Media message 1");
        message2 = saveMessage(event1, sender, MessageType.MEDIA, "Media message 2");
        message3 = saveMessage(event2, sender, MessageType.MEDIA, "Media message 3");

        img1 = saveMedia(message1, MediaType.IMAGE, "https://cdn.test.com/img1.jpg", 102400L);
        vid1 = saveMedia(message1, MediaType.VIDEO, "https://cdn.test.com/vid1.mp4", 5120000L);
        pdf1 = saveMedia(message2, MediaType.PDF,   "https://cdn.test.com/doc1.pdf", 204800L);
        img2 = saveMedia(message3, MediaType.IMAGE, "https://cdn.test.com/img2.jpg", 81920L);
    }

    // ===================== findByMessageId =====================

    @Nested
    @DisplayName("findByMessageId()")
    class FindByMessageId {

        @Test
        @DisplayName("Should return all media for a specific message")
        void find_byMessageId_returnsMessageMedia() {
            List<Media> result = mediaRepository.findByMessageId(message1.getId());
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should return single media for message2")
        void find_byMessageId_singleMedia_returnsOne() {
            List<Media> result = mediaRepository.findByMessageId(message2.getId());
            assertEquals(1, result.size());
            assertEquals(MediaType.PDF, result.get(0).getMediaType());
        }

        @Test
        @DisplayName("Should return empty when message has no media")
        void find_byMessageId_whenNoMedia_returnsEmpty() {
            Message textMsg = saveMessage(event1, sender, MessageType.TEXT, "Plain text");
            List<Media> result = mediaRepository.findByMessageId(textMsg.getId());
            assertTrue(result.isEmpty());
        }
    }

    // ===================== findByEventId =====================

    @Nested
    @DisplayName("findByEventId()")
    class FindByEventId {

        @Test
        @DisplayName("Should return all media associated with an event's messages")
        void find_byEventId_returnsAllEventMedia() {
            List<Media> result = mediaRepository.findByEventId(event1.getId());
            assertEquals(3, result.size()); // img1 + vid1 + pdf1
        }

        @Test
        @DisplayName("Should return media only for the specified event")
        void find_byEventId_doesNotReturnOtherEventsMedia() {
            List<Media> result = mediaRepository.findByEventId(event2.getId());
            assertEquals(1, result.size());
            assertEquals(MediaType.IMAGE, result.get(0).getMediaType());
        }

        @Test
        @DisplayName("Should return empty when event has no messages with media")
        void find_byEventId_whenNoMedia_returnsEmpty() {
            Event emptyEvent = saveEvent("Empty Event", community, sender);
            List<Media> result = mediaRepository.findByEventId(emptyEvent.getId());
            assertTrue(result.isEmpty());
        }
    }

    // ===================== findByEventIdAndMediaType =====================

    @Nested
    @DisplayName("findByEventIdAndMediaType()")
    class FindByEventIdAndMediaType {

        @Test
        @DisplayName("Should return only IMAGE media for event1")
        void find_byEventAndImageType_returnsImages() {
            List<Media> result = mediaRepository
                    .findByEventIdAndMediaType(event1.getId(), MediaType.IMAGE);

            assertEquals(1, result.size());
            assertEquals("https://cdn.test.com/img1.jpg", result.get(0).getUrl());
        }

        @Test
        @DisplayName("Should return only VIDEO media for event1")
        void find_byEventAndVideoType_returnsVideos() {
            List<Media> result = mediaRepository
                    .findByEventIdAndMediaType(event1.getId(), MediaType.VIDEO);

            assertEquals(1, result.size());
            assertEquals(MediaType.VIDEO, result.get(0).getMediaType());
        }

        @Test
        @DisplayName("Should return only PDF media for event1")
        void find_byEventAndPdfType_returnsPdfs() {
            List<Media> result = mediaRepository
                    .findByEventIdAndMediaType(event1.getId(), MediaType.PDF);

            assertEquals(1, result.size());
            assertEquals(MediaType.PDF, result.get(0).getMediaType());
        }

        @Test
        @DisplayName("Should return empty when no media of that type exists for event")
        void find_byEventAndType_whenNoMatch_returnsEmpty() {
            List<Media> result = mediaRepository
                    .findByEventIdAndMediaType(event1.getId(), MediaType.DOCUMENT);

            assertTrue(result.isEmpty());
        }
    }

    // ===================== deleteByMessageId =====================

    @Nested
    @DisplayName("deleteByMessageId()")
    class DeleteByMessageId {

        @Test
        @DisplayName("Should delete all media belonging to a message")
        void delete_byMessageId_removesMedia() {
            mediaRepository.deleteByMessageId(message1.getId());

            List<Media> remaining = mediaRepository.findByMessageId(message1.getId());
            assertTrue(remaining.isEmpty());
        }

        @Test
        @DisplayName("Should not delete media from other messages")
        void delete_byMessageId_doesNotAffectOtherMessages() {
            mediaRepository.deleteByMessageId(message1.getId());

            List<Media> remaining = mediaRepository.findByMessageId(message2.getId());
            assertEquals(1, remaining.size()); // pdf1 should remain
        }
    }

    // ===================== basic CRUD =====================

    @Nested
    @DisplayName("Basic CRUD")
    class BasicCrud {

        @Test
        @DisplayName("Should persist media and assign UUID")
        void save_persistsMediaWithId() {
            Media m = saveMedia(message1, MediaType.DOCUMENT, "https://cdn.test.com/doc2.docx", 30720L);
            assertNotNull(m.getId());
        }

        @Test
        @DisplayName("Should find media by ID")
        void findById_returnsMedia() {
            assertTrue(mediaRepository.findById(img1.getId()).isPresent());
        }

        @Test
        @DisplayName("Should delete media by ID")
        void deleteById_removesMedia() {
            mediaRepository.deleteById(img1.getId());
            assertFalse(mediaRepository.findById(img1.getId()).isPresent());
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

    private Media saveMedia(Message message, MediaType type, String url, Long size) {
        Media m = new Media();
        m.setMessage(message);
        m.setMediaType(type);
        m.setUrl(url);
        m.setSizeInBytes(size);
        return mediaRepository.save(m);
    }
}
