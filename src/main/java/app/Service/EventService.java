package app.Service;

import app.DTO.Event.*;
import app.Database.*;
import app.DTO.Event.EventMapper;
import app.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventAttendanceRepository attendanceRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository groupMembershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventMapper eventMapper;

    /**
     * Create a new event
     */
    @Transactional
    public EventDTO createEvent(UUID creatorId, CreateEventRequest request) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + creatorId));

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCreatedBy(creator);
        event.setEventDate(request.getEventDate());
        event.setEventTime(request.getEventTime());
        event.setLocation(request.getLocation());
        event.setAttendanceEnabled(request.getAttendanceEnabled());

        // Set community if provided
        if (request.getCommunityId() != null) {
            Community community = communityRepository.findById(request.getCommunityId())
                    .orElseThrow(() -> new RuntimeException("Community not found"));
            event.setCommunity(community);
        }

        // Set group if provided
        if (request.getGroupId() != null) {
            Group group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new RuntimeException("Group not found"));

            // Verify creator is a member of the group
            if (!groupMembershipRepository.existsByUserIdAndGroupId(creator.getId(), group.getId())) {
                throw new RuntimeException("You must be a member of the group to create events");
            }

            event.setGroup(group);
            // If group is set, also set the community
            if (event.getCommunity() == null) {
                event.setCommunity(group.getCommunity());
            }
        }

        Event savedEvent = eventRepository.save(event);

        // Initialize attendance if enabled
        if (savedEvent.getAttendanceEnabled() && savedEvent.getGroup() != null) {
            initializeAttendanceForEvent(savedEvent);
        }

        return eventMapper.toDTO(savedEvent);
    }

    /**
     * Get event by ID
     */
    public EventDTO getEventById(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        return eventMapper.toDTO(event);
    }

    /**
     * Get events by community
     */
    public List<EventDTO> getEventsByCommunity(UUID communityId) {
        List<Event> events = eventRepository.findByCommunityIdOrderByDateDesc(communityId);
        return events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get events by group
     */
    public List<EventDTO> getEventsByGroup(UUID groupId) {
        List<Event> events = eventRepository.findByGroupIdOrderByDateDesc(groupId);
        return events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming events by community
     */
    public List<EventDTO> getUpcomingEventsByCommunity(UUID communityId) {
        List<Event> events = eventRepository.findUpcomingEventsByCommunityId(communityId, LocalDate.now());
        return events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming events by group
     */
    public List<EventDTO> getUpcomingEventsByGroup(UUID groupId) {
        List<Event> events = eventRepository.findUpcomingEventsByGroupId(groupId, LocalDate.now());
        return events.stream()
                .map(eventMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update event
     */
    @Transactional
    public EventDTO updateEvent(UUID eventId, UUID userId, UpdateEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Check authorization
        if (!canManageEvent(userId, eventId)) {
            throw new RuntimeException("You don't have permission to update this event");
        }

        // Update fields if provided
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getEventTime() != null) {
            event.setEventTime(request.getEventTime());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toDTO(updatedEvent);
    }

    /**
     * Delete event
     */
    @Transactional
    public void deleteEvent(UUID eventId, UUID userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Only creator can delete
        if (!event.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Only the creator can delete this event");
        }

        eventRepository.delete(event);
    }

    /**
     * Toggle attendance for event
     */
    @Transactional
    public EventDTO toggleAttendance(UUID eventId, UUID userId, boolean enabled) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        if (!canManageEvent(userId, eventId)) {
            throw new RuntimeException("You don't have permission to modify attendance");
        }

        event.setAttendanceEnabled(enabled);
        Event updatedEvent = eventRepository.save(event);

        // Initialize attendance records if enabling and event has a group
        if (enabled && event.getGroup() != null && event.getAttendances().isEmpty()) {
            initializeAttendanceForEvent(event);
        }

        return eventMapper.toDTO(updatedEvent);
    }

    /**
     * Mark attendance
     */
    @Transactional
    public EventAttendanceDTO markAttendance(UUID eventId, UUID markerId, MarkAttendanceRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getAttendanceEnabled()) {
            throw new RuntimeException("Attendance is not enabled for this event");
        }

        User marker = userRepository.findById(markerId)
                .orElseThrow(() -> new RuntimeException("Marker user not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Find or create attendance record
        EventAttendance attendance = attendanceRepository
                .findByEventAndUserAndGroup(event, user, group)
                .orElseGet(() -> {
                    EventAttendance newAttendance = new EventAttendance();
                    newAttendance.setEvent(event);
                    newAttendance.setUser(user);
                    newAttendance.setGroup(group);
                    return newAttendance;
                });

        attendance.setStatus(request.getStatus());
        attendance.setMarkedBy(marker);
        attendance.setMarkedAt(LocalDateTime.now());

        EventAttendance savedAttendance = attendanceRepository.save(attendance);
        return eventMapper.toAttendanceDTO(savedAttendance);
    }

    /**
     * Get event attendance
     */
    public List<EventAttendanceDTO> getEventAttendance(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<EventAttendance> attendances = attendanceRepository.findByEvent(event);
        return attendances.stream()
                .map(eventMapper::toAttendanceDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get event attendance by group
     */
    public List<EventAttendanceDTO> getEventAttendanceByGroup(UUID eventId, UUID groupId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        List<EventAttendance> attendances = attendanceRepository.findByEventAndGroup(event, group);
        return attendances.stream()
                .map(eventMapper::toAttendanceDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get attendance statistics
     */
    public AttendanceStatsDTO getAttendanceStats(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        int totalAttendance = attendanceRepository.findByEvent(event).size();
        int presentCount = attendanceRepository.countByEventAndStatus(event, AttendanceStatus.PRESENT);
        int absentCount = attendanceRepository.countByEventAndStatus(event, AttendanceStatus.ABSENT);
        int pendingCount = attendanceRepository.countByEventAndStatus(event, AttendanceStatus.PENDING);

        return new AttendanceStatsDTO(totalAttendance, presentCount, absentCount, pendingCount);
    }

    /**
     * Initialize attendance records for all group members
     */
    @Transactional
    public void initializeAttendanceForEvent(Event event) {
        if (event.getGroup() == null) {
            return;
        }

        List<GroupMembership> memberships = groupMembershipRepository.findByGroup(event.getGroup());

        for (GroupMembership membership : memberships) {
            if (!attendanceRepository.existsByEventIdAndUserIdAndGroupId(
                    event.getId(), membership.getUser().getId(), event.getGroup().getId())) {
                EventAttendance attendance = new EventAttendance();
                attendance.setEvent(event);
                attendance.setUser(membership.getUser());
                attendance.setGroup(event.getGroup());
                attendance.setStatus(AttendanceStatus.PENDING);
                attendanceRepository.save(attendance);
            }
        }
    }

    // Helper methods
    private boolean canManageEvent(UUID userId, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Creator can always manage
        if (event.getCreatedBy().getId().equals(userId)) {
            return true;
        }

        // Group admins/owners can manage group events
        if (event.getGroup() != null) {
            Group group = event.getGroup();
            Set<GroupMembership> memberships = group.getMemberships();
            for(GroupMembership membership: memberships) {
                if(membership.getUser().getId().equals(userId)) {
                    if(membership.getRole().equals(MemberRole.ADMIN) || membership.getRole().equals(MemberRole.OWNER)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
