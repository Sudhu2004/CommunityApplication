package app.Repository;

import app.Database.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventAttendanceRepository extends JpaRepository<EventAttendance, UUID> {

    // Find all attendance records for an event
    List<EventAttendance> findByEvent(Event event);

    // Find all attendance records by event ID
    List<EventAttendance> findByEventId(UUID eventId);

    // Find attendance for a specific group at an event
    List<EventAttendance> findByEventAndGroup(Event event, Group group);

    // Find attendance by event ID and group ID
    List<EventAttendance> findByEventIdAndGroupId(UUID eventId, UUID groupId);

    // Find attendance for a specific user
    List<EventAttendance> findByUser(User user);

    // Find attendance by user ID
    List<EventAttendance> findByUserId(UUID userId);

    // Find specific attendance record (event + user + group)
    Optional<EventAttendance> findByEventAndUserAndGroup(Event event, User user, Group group);

    // Find specific attendance by IDs
    Optional<EventAttendance> findByEventIdAndUserIdAndGroupId(UUID eventId, UUID userId, UUID groupId);

    // Check if attendance exists
    boolean existsByEventIdAndUserIdAndGroupId(UUID eventId, UUID userId, UUID groupId);

    // Find attendance by status
    List<EventAttendance> findByEventIdAndStatus(UUID eventId, AttendanceStatus status);

    // Find attendance by group and status
    List<EventAttendance> findByEventIdAndGroupIdAndStatus(UUID eventId, UUID groupId, AttendanceStatus status);

    // Count present attendees for an event
    @Query("SELECT COUNT(ea) FROM EventAttendance ea WHERE ea.event.id = :eventId AND ea.status = 'PRESENT'")
    Long countPresentByEventId(@Param("eventId") UUID eventId);

    // Count present attendees for an event in a specific group
    @Query("SELECT COUNT(ea) FROM EventAttendance ea WHERE ea.event.id = :eventId AND ea.group.id = :groupId AND ea.status = 'PRESENT'")
    Long countPresentByEventIdAndGroupId(@Param("eventId") UUID eventId, @Param("groupId") UUID groupId);

    // Get attendance summary for an event
    @Query("SELECT ea.status, COUNT(ea) FROM EventAttendance ea WHERE ea.event.id = :eventId GROUP BY ea.status")
    List<Object[]> getAttendanceSummaryByEventId(@Param("eventId") UUID eventId);

    // Get attendance summary for a group at an event
    @Query("SELECT ea.status, COUNT(ea) FROM EventAttendance ea WHERE ea.event.id = :eventId AND ea.group.id = :groupId GROUP BY ea.status")
    List<Object[]> getAttendanceSummaryByEventIdAndGroupId(@Param("eventId") UUID eventId, @Param("groupId") UUID groupId);

    // Find attendance marked by a specific user (group owner)
    List<EventAttendance> findByMarkedBy(User markedBy);

    // Find pending attendance records for a group
    @Query("SELECT ea FROM EventAttendance ea WHERE ea.group.id = :groupId AND ea.status = 'PENDING' ORDER BY ea.event.eventDate DESC")
    List<EventAttendance> findPendingAttendanceByGroupId(@Param("groupId") UUID groupId);

    // Get user's attendance history
    @Query("SELECT ea FROM EventAttendance ea WHERE ea.user.id = :userId ORDER BY ea.event.eventDate DESC")
    List<EventAttendance> findAttendanceHistoryByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(ea) FROM EventAttendance ea WHERE ea.event = :event AND ea.status = :status")
    int countByEventAndStatus(@Param("event") Event event, @Param("status") AttendanceStatus status);
}
