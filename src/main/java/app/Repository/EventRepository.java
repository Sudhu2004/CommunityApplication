package app.Repository;

import app.Database.Community;
import app.Database.Event;
import app.Database.Group;
import app.Database.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    // Find all events in a community (community-wide events)
    List<Event> findByCommunity(Community community);

    // Find community events by ID
    List<Event> findByCommunityId(UUID communityId);

    // Find all events in a group (group-specific events)
    List<Event> findByGroup(Group group);

    // Find group events by ID
    List<Event> findByGroupId(UUID groupId);

    // Find events created by a user
    List<Event> findByCreatedBy(User createdBy);

    // Find events by date range
    List<Event> findByEventDateBetween(LocalDate startDate, LocalDate endDate);

    // Find upcoming events in a community
    @Query("SELECT e FROM Event e WHERE e.community.id = :communityId AND e.eventDate >= :currentDate ORDER BY e.eventDate ASC")
    List<Event> findUpcomingEventsByCommunityId(@Param("communityId") UUID communityId, @Param("currentDate") LocalDate currentDate);

    // Find upcoming events in a group
    @Query("SELECT e FROM Event e WHERE e.group.id = :groupId AND e.eventDate >= :currentDate ORDER BY e.eventDate ASC")
    List<Event> findUpcomingEventsByGroupId(@Param("groupId") UUID groupId, @Param("currentDate") LocalDate currentDate);

    // Find past events in a community
    @Query("SELECT e FROM Event e WHERE e.community.id = :communityId AND e.eventDate < :currentDate ORDER BY e.eventDate DESC")
    List<Event> findPastEventsByCommunityId(@Param("communityId") UUID communityId, @Param("currentDate") LocalDate currentDate);

    // Find events with attendance enabled
    List<Event> findByCommunityAndAttendanceEnabled(Community community, Boolean attendanceEnabled);

    // Find events with attendance enabled by community ID
    List<Event> findByCommunityIdAndAttendanceEnabled(UUID communityId, Boolean attendanceEnabled);

    // Get all events (both community and group) that a user should see based on their group memberships
    @Query("SELECT e FROM Event e WHERE " +
            "(e.community.id = :communityId AND e.group IS NULL) OR " +
            "(e.group.id IN :groupIds)")
    List<Event> findEventsByCommunityCommunityIdAndGroupIds(
            @Param("communityId") UUID communityId,
            @Param("groupIds") List<UUID> groupIds
    );

    // Count events in a community
    long countByCommunityId(UUID communityId);

    // Count events in a group
    long countByGroupId(UUID groupId);

    // Find events by date and community
    List<Event> findByCommunityIdAndEventDate(UUID communityId, LocalDate eventDate);

    // Search events by title
    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Event> searchByTitle(@Param("title") String title);

    @Query("SELECT e FROM Event e WHERE e.community.id = :communityId ORDER BY e.eventDate DESC, e.eventTime DESC")
    List<Event> findByCommunityIdOrderByDateDesc(@Param("communityId") UUID communityId);

    @Query("SELECT e FROM Event e WHERE e.group.id = :groupId ORDER BY e.eventDate DESC, e.eventTime DESC")
    List<Event> findByGroupIdOrderByDateDesc(@Param("groupId") UUID groupId);

    @Query("SELECT e FROM Event e WHERE e.eventDate >= :startDate AND e.eventDate <= :endDate ORDER BY e.eventDate, e.eventTime")
    List<Event> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
