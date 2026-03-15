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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("EventAttendanceRepository Tests")
class EventAttendanceRepositoryTest {

    @Autowired private EventAttendanceRepository attendanceRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private CommunityRepository communityRepository;
    @Autowired private UserRepository userRepository;

    private User creator;
    private User user1;
    private User user2;
    private Community community;
    private Group group1;
    private Group group2;
    private Event event;

    private EventAttendance attendance1; // user1, group1, PRESENT
    private EventAttendance attendance2; // user2, group1, ABSENT
    private EventAttendance attendance3; // user1, group2, PENDING

    @BeforeEach
    void setUp() {
        attendanceRepository.deleteAll();
        eventRepository.deleteAll();
        groupRepository.deleteAll();
        communityRepository.deleteAll();
        userRepository.deleteAll();

        creator   = saveUser("creator@test.com", "Creator");
        user1     = saveUser("user1@test.com", "User One");
        user2     = saveUser("user2@test.com", "User Two");
        community = saveCommunity("Test Community", creator);
        group1    = saveGroup("Group Alpha", community, creator);
        group2    = saveGroup("Group Beta",  community, creator);
        event     = saveEvent("Tech Talk", community, creator);

        attendance1 = saveAttendance(event, user1, group1, AttendanceStatus.PRESENT, creator);
        attendance2 = saveAttendance(event, user2, group1, AttendanceStatus.ABSENT,  creator);
        attendance3 = saveAttendance(event, user1, group2, AttendanceStatus.PENDING, null);
    }

    // ===================== findByEvent =====================

    @Nested
    @DisplayName("findByEvent()")
    class FindByEvent {

        @Test
        @DisplayName("Should return all attendance records for an event")
        void find_byEvent_returnsAll() {
            List<EventAttendance> result = attendanceRepository.findByEvent(event);
            assertEquals(3, result.size());
        }
    }

    // ===================== findByEventId =====================

    @Nested
    @DisplayName("findByEventId()")
    class FindByEventId {

        @Test
        @DisplayName("Should return all attendance records by event ID")
        void find_byEventId_returnsAll() {
            List<EventAttendance> result = attendanceRepository.findByEventId(event.getId());
            assertEquals(3, result.size());
        }
    }

    // ===================== findByEventAndGroup =====================

    @Nested
    @DisplayName("findByEventAndGroup()")
    class FindByEventAndGroup {

        @Test
        @DisplayName("Should return only attendance records for specific group")
        void find_byEventAndGroup_returnsGroupRecords() {
            List<EventAttendance> result =
                    attendanceRepository.findByEventAndGroup(event, group1);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should return single record for group2")
        void find_byEventAndGroup2_returnsOneRecord() {
            List<EventAttendance> result =
                    attendanceRepository.findByEventAndGroup(event, group2);
            assertEquals(1, result.size());
            assertEquals(AttendanceStatus.PENDING, result.get(0).getStatus());
        }
    }

    // ===================== findByEventIdAndGroupId =====================

    @Nested
    @DisplayName("findByEventIdAndGroupId()")
    class FindByEventIdAndGroupId {

        @Test
        @DisplayName("Should return attendance by event and group IDs")
        void find_byIds_returnsGroupAttendance() {
            List<EventAttendance> result = attendanceRepository
                    .findByEventIdAndGroupId(event.getId(), group1.getId());
            assertEquals(2, result.size());
        }
    }

    // ===================== findByUser =====================

    @Nested
    @DisplayName("findByUser()")
    class FindByUser {

        @Test
        @DisplayName("Should return all attendance records for a user across groups")
        void find_byUser_returnsAllUserRecords() {
            List<EventAttendance> result = attendanceRepository.findByUser(user1);
            assertEquals(2, result.size()); // attendance1 (group1) + attendance3 (group2)
        }

        @Test
        @DisplayName("Should return empty when user has no attendance records")
        void find_byUser_whenNoRecords_returnsEmpty() {
            User nobody = saveUser("nobody@test.com", "Nobody");
            assertTrue(attendanceRepository.findByUser(nobody).isEmpty());
        }
    }

    // ===================== findByUserId =====================

    @Nested
    @DisplayName("findByUserId()")
    class FindByUserId {

        @Test
        @DisplayName("Should return attendance records by user ID")
        void find_byUserId_returnsRecords() {
            List<EventAttendance> result = attendanceRepository.findByUserId(user2.getId());
            assertEquals(1, result.size());
        }
    }

    // ===================== findByEventAndUserAndGroup =====================

    @Nested
    @DisplayName("findByEventAndUserAndGroup()")
    class FindByEventAndUserAndGroup {

        @Test
        @DisplayName("Should return specific attendance record for event+user+group")
        void find_byEventUserGroup_returnsRecord() {
            Optional<EventAttendance> result =
                    attendanceRepository.findByEventAndUserAndGroup(event, user1, group1);
            assertTrue(result.isPresent());
            assertEquals(AttendanceStatus.PRESENT, result.get().getStatus());
        }

        @Test
        @DisplayName("Should return empty when combination does not exist")
        void find_byEventUserGroup_whenNotExists_returnsEmpty() {
            Optional<EventAttendance> result =
                    attendanceRepository.findByEventAndUserAndGroup(event, user2, group2);
            assertFalse(result.isPresent());
        }
    }

    // ===================== findByEventIdAndUserIdAndGroupId =====================

    @Nested
    @DisplayName("findByEventIdAndUserIdAndGroupId()")
    class FindByEventIdAndUserIdAndGroupId {

        @Test
        @DisplayName("Should find attendance by all three IDs")
        void find_byAllIds_returnsRecord() {
            Optional<EventAttendance> result = attendanceRepository
                    .findByEventIdAndUserIdAndGroupId(
                            event.getId(), user1.getId(), group1.getId());
            assertTrue(result.isPresent());
        }
    }

    // ===================== existsByEventIdAndUserIdAndGroupId =====================

    @Nested
    @DisplayName("existsByEventIdAndUserIdAndGroupId()")
    class ExistsByEventIdAndUserIdAndGroupId {

        @Test
        @DisplayName("Should return true when record exists")
        void exists_whenExists_returnsTrue() {
            assertTrue(attendanceRepository.existsByEventIdAndUserIdAndGroupId(
                    event.getId(), user1.getId(), group1.getId()));
        }

        @Test
        @DisplayName("Should return false when record does not exist")
        void exists_whenNotExists_returnsFalse() {
            assertFalse(attendanceRepository.existsByEventIdAndUserIdAndGroupId(
                    event.getId(), user2.getId(), group2.getId()));
        }
    }

    // ===================== findByEventIdAndStatus =====================

    @Nested
    @DisplayName("findByEventIdAndStatus()")
    class FindByEventIdAndStatus {

        @Test
        @DisplayName("Should return only PRESENT records")
        void find_byStatus_returnsPresent() {
            List<EventAttendance> result = attendanceRepository
                    .findByEventIdAndStatus(event.getId(), AttendanceStatus.PRESENT);
            assertEquals(1, result.size());
            assertEquals(AttendanceStatus.PRESENT, result.get(0).getStatus());
        }

        @Test
        @DisplayName("Should return only PENDING records")
        void find_byPendingStatus_returnsPending() {
            List<EventAttendance> result = attendanceRepository
                    .findByEventIdAndStatus(event.getId(), AttendanceStatus.PENDING);
            assertEquals(1, result.size());
        }
    }

    // ===================== findByEventIdAndGroupIdAndStatus =====================

    @Nested
    @DisplayName("findByEventIdAndGroupIdAndStatus()")
    class FindByEventIdAndGroupIdAndStatus {

        @Test
        @DisplayName("Should return ABSENT records for a specific group")
        void find_byGroupAndStatus_returnsAbsent() {
            List<EventAttendance> result = attendanceRepository
                    .findByEventIdAndGroupIdAndStatus(
                            event.getId(), group1.getId(), AttendanceStatus.ABSENT);
            assertEquals(1, result.size());
            assertEquals(AttendanceStatus.ABSENT, result.get(0).getStatus());
        }
    }

    // ===================== countPresentByEventId =====================

    @Nested
    @DisplayName("countPresentByEventId()")
    class CountPresentByEventId {

        @Test
        @DisplayName("Should count only PRESENT records across all groups")
        void countPresent_returnsCorrectCount() {
            Long count = attendanceRepository.countPresentByEventId(event.getId());
            assertEquals(1L, count); // only attendance1 is PRESENT
        }
    }

    // ===================== countPresentByEventIdAndGroupId =====================

    @Nested
    @DisplayName("countPresentByEventIdAndGroupId()")
    class CountPresentByEventIdAndGroupId {

        @Test
        @DisplayName("Should count PRESENT records for a specific group")
        void countPresent_forGroup_returnsCorrectCount() {
            Long count = attendanceRepository
                    .countPresentByEventIdAndGroupId(event.getId(), group1.getId());
            assertEquals(1L, count);
        }

        @Test
        @DisplayName("Should return 0 when no PRESENT records for group")
        void countPresent_whenNone_returnsZero() {
            Long count = attendanceRepository
                    .countPresentByEventIdAndGroupId(event.getId(), group2.getId());
            assertEquals(0L, count);
        }
    }

    // ===================== getAttendanceSummaryByEventId =====================

    @Nested
    @DisplayName("getAttendanceSummaryByEventId()")
    class GetAttendanceSummaryByEventId {

        @Test
        @DisplayName("Should return grouped summary with status counts")
        void getSummary_returnsSummaryRows() {
            List<Object[]> result = attendanceRepository
                    .getAttendanceSummaryByEventId(event.getId());

            // Should have 3 groups: PRESENT(1), ABSENT(1), PENDING(1)
            assertEquals(3, result.size());
        }
    }

    // ===================== countByEventAndStatus =====================

    @Nested
    @DisplayName("countByEventAndStatus()")
    class CountByEventAndStatus {

        @Test
        @DisplayName("Should count by event entity and status")
        void count_byEventAndStatus_returnsCorrect() {
            int count = attendanceRepository.countByEventAndStatus(event, AttendanceStatus.PRESENT);
            assertEquals(1, count);
        }

        @Test
        @DisplayName("Should return 0 when no records for that status")
        void count_whenNone_returnsZero() {
            int count = attendanceRepository.countByEventAndStatus(event, AttendanceStatus.ABSENT);
            // attendance2 is ABSENT
            assertEquals(1, count);
        }
    }

    // ===================== findByMarkedBy =====================

    @Nested
    @DisplayName("findByMarkedBy()")
    class FindByMarkedBy {

        @Test
        @DisplayName("Should return records marked by specific user")
        void find_byMarker_returnsRecordsMarkedByUser() {
            List<EventAttendance> result = attendanceRepository.findByMarkedBy(creator);
            // attendance1 and attendance2 were marked by creator
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should return empty when no records marked by user")
        void find_byMarker_whenNone_returnsEmpty() {
            User nobody = saveUser("nobody@test.com", "Nobody");
            assertTrue(attendanceRepository.findByMarkedBy(nobody).isEmpty());
        }
    }

    // ===================== findAttendanceHistoryByUserId =====================

    @Nested
    @DisplayName("findAttendanceHistoryByUserId()")
    class FindAttendanceHistoryByUserId {

        @Test
        @DisplayName("Should return full attendance history for a user")
        void find_history_returnsAllUserAttendance() {
            List<EventAttendance> result = attendanceRepository
                    .findAttendanceHistoryByUserId(user1.getId());
            assertEquals(2, result.size()); // user1 in group1 and group2
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

    private Event saveEvent(String title, Community community, User creator) {
        Event e = new Event();
        e.setTitle(title);
        e.setCommunity(community);
        e.setCreatedBy(creator);
        e.setEventDate(LocalDate.now().plusDays(7));
        e.setEventTime(LocalTime.of(10, 0));
        e.setAttendanceEnabled(true);
        return eventRepository.save(e);
    }

    private EventAttendance saveAttendance(Event event, User user, Group group,
                                           AttendanceStatus status, User markedBy) {
        EventAttendance a = new EventAttendance();
        a.setEvent(event);
        a.setUser(user);
        a.setGroup(group);
        a.setStatus(status);
        a.setMarkedBy(markedBy);
        if (markedBy != null) {
            a.setMarkedAt(LocalDateTime.now());
        }
        return attendanceRepository.save(a);
    }
}
