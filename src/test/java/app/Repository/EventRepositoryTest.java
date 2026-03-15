package app.Repository;

import app.Database.Community;
import app.Database.Event;
import app.Database.Group;
import app.Database.User;
import app.Repository.CommunityRepository;
import app.Repository.EventRepository;
import app.Repository.GroupRepository;
import app.Repository.UserRepository;
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
@DisplayName("EventRepository Tests")
class EventRepositoryTest {

    @Autowired private EventRepository eventRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private UserRepository userRepository;

    private User creator;
    private Community community;
    private Group group;

    // Events
    private Event futureCommEvent;      // community-wide, future
    private Event pastCommEvent;        // community-wide, past
    private Event futureGroupEvent;     // group-specific, future
    private Event attendanceEvent;      // attendance enabled, future

    private final LocalDate TODAY = LocalDate.now();
    private final LocalDate FUTURE = TODAY.plusDays(10);
    private final LocalDate PAST   = TODAY.minusDays(10);

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        groupRepository.deleteAll();
        communityRepository.deleteAll();
        userRepository.deleteAll();

        creator   = saveUser("creator@test.com", "Creator");
        community = saveCommunity("Test Community", creator);
        group     = saveGroup("Test Group", community, creator);

        futureCommEvent = saveEvent("Annual Meetup",    "Desc", community, null,  FUTURE, false);
        pastCommEvent   = saveEvent("Old Conference",   "Desc", community, null,  PAST,   false);
        futureGroupEvent= saveEvent("Group Workshop",   "Desc", community, group, FUTURE, false);
        attendanceEvent = saveEvent("Attendance Event", "Desc", community, null,  FUTURE, true);
    }

    // ===================== findByCommunity =====================

    @Nested
    @DisplayName("findByCommunity()")
    class FindByCommunity {

        @Test
        @DisplayName("Should return all events for a community (including group events)")
        void find_byCommunity_returnsAllEvents() {
            List<Event> result = eventRepository.findByCommunity(community);
            assertEquals(4, result.size());
        }
    }

    // ===================== findByCommunityId =====================

    @Nested
    @DisplayName("findByCommunityId()")
    class FindByCommunityId {

        @Test
        @DisplayName("Should return all events by community ID")
        void find_byCommunityId_returnsEvents() {
            List<Event> result = eventRepository.findByCommunityId(community.getId());
            assertEquals(4, result.size());
        }
    }

    // ===================== findByGroup =====================

    @Nested
    @DisplayName("findByGroup()")
    class FindByGroup {

        @Test
        @DisplayName("Should return only group-specific events")
        void find_byGroup_returnsGroupEvents() {
            List<Event> result = eventRepository.findByGroup(group);
            assertEquals(1, result.size());
            assertEquals("Group Workshop", result.get(0).getTitle());
        }

        @Test
        @DisplayName("Should return empty when group has no events")
        void find_byGroup_whenNoEvents_returnsEmpty() {
            Group emptyGroup = saveGroup("Empty Group", community, creator);
            assertTrue(eventRepository.findByGroup(emptyGroup).isEmpty());
        }
    }

    // ===================== findByGroupId =====================

    @Nested
    @DisplayName("findByGroupId()")
    class FindByGroupId {

        @Test
        @DisplayName("Should return group events by group ID")
        void find_byGroupId_returnsEvents() {
            List<Event> result = eventRepository.findByGroupId(group.getId());
            assertEquals(1, result.size());
        }
    }

    // ===================== findByCreatedBy =====================

    @Nested
    @DisplayName("findByCreatedBy()")
    class FindByCreatedBy {

        @Test
        @DisplayName("Should return all events created by a user")
        void find_byCreator_returnsAllCreatorEvents() {
            List<Event> result = eventRepository.findByCreatedBy(creator);
            assertEquals(4, result.size());
        }

        @Test
        @DisplayName("Should return empty when user has created no events")
        void find_byCreator_whenNoEvents_returnsEmpty() {
            User nobody = saveUser("nobody@test.com", "Nobody");
            assertTrue(eventRepository.findByCreatedBy(nobody).isEmpty());
        }
    }

    // ===================== findByEventDateBetween =====================

    @Nested
    @DisplayName("findByEventDateBetween()")
    class FindByEventDateBetween {

        @Test
        @DisplayName("Should return events within date range")
        void find_withinRange_returnsEvents() {
            List<Event> result = eventRepository
                    .findByEventDateBetween(TODAY.minusDays(15), TODAY.minusDays(1));
            assertEquals(1, result.size());
            assertEquals("Old Conference", result.get(0).getTitle());
        }

        @Test
        @DisplayName("Should return empty when no events fall in range")
        void find_outsideRange_returnsEmpty() {
            List<Event> result = eventRepository
                    .findByEventDateBetween(TODAY.plusDays(100), TODAY.plusDays(200));
            assertTrue(result.isEmpty());
        }
    }

    // ===================== findUpcomingEventsByCommunityId =====================

    @Nested
    @DisplayName("findUpcomingEventsByCommunityId()")
    class FindUpcomingEventsByCommunityId {

        @Test
        @DisplayName("Should return only future events ordered by date ascending")
        void find_upcoming_returnsFutureEvents() {
            List<Event> result = eventRepository
                    .findUpcomingEventsByCommunityId(community.getId(), TODAY);

            // 3 future events: futureCommEvent, futureGroupEvent, attendanceEvent
            assertEquals(3, result.size());
            // All should be on or after today
            result.forEach(e -> assertFalse(e.getEventDate().isBefore(TODAY)));
        }

        @Test
        @DisplayName("Should return empty when community has no upcoming events")
        void find_upcoming_whenNone_returnsEmpty() {
            Community other = saveCommunity("Other", creator);
            assertTrue(eventRepository
                    .findUpcomingEventsByCommunityId(other.getId(), TODAY).isEmpty());
        }
    }

    // ===================== findUpcomingEventsByGroupId =====================

    @Nested
    @DisplayName("findUpcomingEventsByGroupId()")
    class FindUpcomingEventsByGroupId {

        @Test
        @DisplayName("Should return upcoming group-specific events")
        void find_upcomingGroup_returnsFutureGroupEvents() {
            List<Event> result = eventRepository
                    .findUpcomingEventsByGroupId(group.getId(), TODAY);

            assertEquals(1, result.size());
            assertEquals("Group Workshop", result.get(0).getTitle());
        }
    }

    // ===================== findPastEventsByCommunityId =====================

    @Nested
    @DisplayName("findPastEventsByCommunityId()")
    class FindPastEventsByCommunityId {

        @Test
        @DisplayName("Should return only past events for a community")
        void find_past_returnsPastEvents() {
            List<Event> result = eventRepository
                    .findPastEventsByCommunityId(community.getId(), TODAY);

            assertEquals(1, result.size());
            assertEquals("Old Conference", result.get(0).getTitle());
        }
    }

    // ===================== findByCommunityAndAttendanceEnabled =====================

    @Nested
    @DisplayName("findByCommunityAndAttendanceEnabled()")
    class FindByCommunityAndAttendanceEnabled {

        @Test
        @DisplayName("Should return only events with attendance enabled")
        void find_attendanceEnabled_returnsEnabledEvents() {
            List<Event> result = eventRepository
                    .findByCommunityAndAttendanceEnabled(community, true);

            assertEquals(1, result.size());
            assertEquals("Attendance Event", result.get(0).getTitle());
            assertTrue(result.get(0).getAttendanceEnabled());
        }

        @Test
        @DisplayName("Should return events with attendance disabled")
        void find_attendanceDisabled_returnsDisabledEvents() {
            List<Event> result = eventRepository
                    .findByCommunityAndAttendanceEnabled(community, false);

            assertEquals(3, result.size());
        }
    }

    // ===================== findByCommunityIdAndAttendanceEnabled =====================

    @Nested
    @DisplayName("findByCommunityIdAndAttendanceEnabled()")
    class FindByCommunityIdAndAttendanceEnabled {

        @Test
        @DisplayName("Should return enabled events by community ID")
        void find_byCommunityIdAndEnabled_returnsEnabledEvents() {
            List<Event> result = eventRepository
                    .findByCommunityIdAndAttendanceEnabled(community.getId(), true);
            assertEquals(1, result.size());
        }
    }

    // ===================== searchByTitle =====================

    @Nested
    @DisplayName("searchByTitle()")
    class SearchByTitle {

        @Test
        @DisplayName("Should return events with title partially matching (case-insensitive)")
        void search_partialMatch_returnsEvents() {
            List<Event> result = eventRepository.searchByTitle("meetup");
            assertEquals(1, result.size());
            assertEquals("Annual Meetup", result.get(0).getTitle());
        }

        @Test
        @DisplayName("Should be case-insensitive")
        void search_caseInsensitive_returnsEvents() {
            List<Event> result = eventRepository.searchByTitle("WORKSHOP");
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return empty when no match")
        void search_noMatch_returnsEmpty() {
            assertTrue(eventRepository.searchByTitle("zzznotatitle").isEmpty());
        }
    }

    // ===================== countByCommunityId / countByGroupId =====================

    @Nested
    @DisplayName("countByCommunityId() and countByGroupId()")
    class Count {

        @Test
        @DisplayName("Should count all events in a community")
        void countByCommunityId_returnsTotal() {
            assertEquals(4L, eventRepository.countByCommunityId(community.getId()));
        }

        @Test
        @DisplayName("Should count events in a group")
        void countByGroupId_returnsGroupEventCount() {
            assertEquals(1L, eventRepository.countByGroupId(group.getId()));
        }
    }

    // ===================== findByCommunityIdOrderByDateDesc =====================

    @Nested
    @DisplayName("findByCommunityIdOrderByDateDesc()")
    class FindByCommunityIdOrderByDateDesc {

        @Test
        @DisplayName("Should return events ordered newest first")
        void find_orderedDesc_returnsNewestFirst() {
            List<Event> result = eventRepository
                    .findByCommunityIdOrderByDateDesc(community.getId());

            assertEquals(4, result.size());
            // Future events come first
            assertFalse(result.get(0).getEventDate().isBefore(result.get(result.size() - 1).getEventDate()));
        }
    }

    // ===================== findByDateRange =====================

    @Nested
    @DisplayName("findByDateRange()")
    class FindByDateRange {

        @Test
        @DisplayName("Should return events within the provided date range")
        void find_byDateRange_returnsEvents() {
            List<Event> result = eventRepository
                    .findByDateRange(TODAY, TODAY.plusDays(20));

            // Future events (3) fall within this range
            assertEquals(3, result.size());
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

    private Group saveGroup(String name, Community community, User creator) {
        Group g = new Group();
        g.setName(name);
        g.setCommunity(community);
        g.setCreatedBy(creator);
        return groupRepository.save(g);
    }

    private Event saveEvent(String title, String desc, Community community,
                            Group group, LocalDate date, boolean attendanceEnabled) {
        Event e = new Event();
        e.setTitle(title);
        e.setDescription(desc);
        e.setCommunity(community);
        e.setGroup(group);
        e.setCreatedBy(creator);
        e.setEventDate(date);
        e.setEventTime(LocalTime.of(10, 0));
        e.setAttendanceEnabled(attendanceEnabled);
        return eventRepository.save(e);
    }
}
